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
        outputFieldsDeclarer.declare(new Fields(
                StormUtils.STORM.FIELDS.CASE_ID
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
        collector.emit(new Values(caesid));
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
                if (vvMat.getDimensions()[0] != vvNewMat.getDimensions()[0]) {
                    p.set(vvKey, vvNewMat.serialize());
                }
                convergedMat = (MWNumericArray) res[1];
                boolean converbool;
                if (!(convergedMat.getDouble() < 0)) {
                    converbool = false;
                    p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), Long.parseLong(zoneid), converbool);
                }
                p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
//                TODO: record only one iteration number
                p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));
                p.sync();
            }

            disposeMatArrays(badthrshldMat, HHMat, WWInvMat, WWMat, ddelzMat, vvMat, vvNewMat, convergedMat);

        }
    }

}
