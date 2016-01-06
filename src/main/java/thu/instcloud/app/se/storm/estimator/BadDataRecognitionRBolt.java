package thu.instcloud.app.se.storm.estimator;

import Estimator.Estimator;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;
import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-30.
 */
public class BadDataRecognitionRBolt extends JedisRichBolt {
    private Estimator estimator;

    public BadDataRecognitionRBolt(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(StormUtils.STORM.STREAM.STREAM_OUTPUT, new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.EST_CONVERGED
        ));

        outputFieldsDeclarer.declareStream(StormUtils.STORM.STREAM.STREAM_ESTIMATE, new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.ZONE_ID
        ));
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
        try {
            estimator = new Estimator();
        } catch (MWException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {
        String caesid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        String zoneid = tuple.getStringByField(StormUtils.STORM.FIELDS.ZONE_ID);
        recogBadData(caesid, zoneid);

        collector.ack(tuple);
    }

    private void recogBadData(String caseid, String zoneid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();

            byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
            byte[] WWKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WW);
            byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
            byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
            byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);

            Response<byte[]> HHByte = p.get(HHkey);
            Response<byte[]> WWByte = p.get(WWKey);
            Response<byte[]> WWInvByte = p.get(WWInvKey);
            Response<byte[]> ddelzByte = p.get(ddelzKey);
            Response<byte[]> vvByte = p.get(vvKey);

            String thrshldKey = mkKey(caseid,
                    StormUtils.REDIS.KEYS.ZONES,
                    zoneid,
                    StormUtils.REDIS.KEYS.BAD_RECOG_THRESHOLD);
            Response<String> badthrshld = p.get(thrshldKey);
            p.sync();

            MWNumericArray badthrshldMat = new MWNumericArray(badthrshld.get(), MWClassID.DOUBLE);
            MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
            MWNumericArray WWMat = (MWNumericArray) MWNumericArray.deserialize(WWByte.get());
            MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
            MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
            MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());

            MWNumericArray vvNewMat = null, convergedMat = null;
            Object[] res = null;
            try {
                res = estimator.api_badDataRecognition(2, HHMat, WWMat, WWInvMat,
                        vvMat, ddelzMat, badthrshldMat);
            } catch (MWException e) {
                e.printStackTrace();
            }

//            update state
            if (res != null) {
                vvNewMat = (MWNumericArray) res[0];
                p.set(vvKey, vvNewMat.serialize());

                convergedMat = (MWNumericArray) res[1];
                boolean converbool;
                if (convergedMat.getDouble() > 0) {
                    converbool = true;
                } else {
                    converbool = false;
                }
                p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), Long.parseLong(zoneid), converbool);
                p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
//                TODO: record only one iteration number
                p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));
                p.sync();
            }

            disposeMatArrays(badthrshldMat, HHMat, WWInvMat, WWMat, ddelzMat, vvMat, vvNewMat, convergedMat);

            checkCondition(caseid, p);
        }
    }

    private void checkCondition(String caseid, Pipeline p) {
        Response<String> nbadRecog = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
        Response<String> nz = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES));
        Response<String> currBadItResp = p.get(mkKey(caseid, "1", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));
        Response<String> maxBadItResp = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_MAX_BAD_REG_IT);
        Response<Long> nConver = p.bitcount(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED));
        p.sync();

        long nzLong = Long.parseLong(nz.get());
//        all zones have checked bad data
        if (nzLong == Long.parseLong(nbadRecog.get())) {
//            reset checked zone number, sync later
            p.set(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES), "1");
//                all converged
            if (nConver.get() == nzLong) {
                resetState(caseid, p);
//                output
                collector.emit(StormUtils.STORM.STREAM.STREAM_OUTPUT, new Values(caseid, true));
                return;
            } else if (Long.parseLong(currBadItResp.get()) >= Long.parseLong(maxBadItResp.get())) {
                resetState(caseid, p);
//              not converged and reached max bad it number that means estimation failed
                collector.emit(StormUtils.STORM.STREAM.STREAM_OUTPUT, new Values(caseid, false));
                return;
            } else {
                p.del(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED));
                p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), 0, true);
                p.sync();

//                    redispatch zone for further estimation
                for (int i = 1; i < nzLong; i++) {
                    collector.emit(StormUtils.STORM.STREAM.STREAM_ESTIMATE,
                            new Values(caseid, i + ""));
                }
            }
        }
    }

    private void resetState(String caseid, Pipeline p) {
        String converKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
//                reset converge states
        p.del(converKey);
        p.setbit(converKey, 0, true);
//                finished estimation
        p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATING_BIT), 0, false);
        p.sync();
    }
}
