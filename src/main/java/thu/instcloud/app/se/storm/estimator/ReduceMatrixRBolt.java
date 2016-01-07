package thu.instcloud.app.se.storm.estimator;

import Estimator.Estimator;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;
import static thu.instcloud.app.se.storm.common.StormUtils.getMatVamEstExt;
import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-29.
 */
public class ReduceMatrixRBolt extends JedisRichBolt {
    private Estimator estimator;

    public ReduceMatrixRBolt(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
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
        String caseid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        String zoneid = tuple.getStringByField(StormUtils.STORM.FIELDS.ZONE_ID);

        reduceMatrix(caseid, zoneid);
        collector.emit(new Values(caseid, zoneid));
        collector.ack(tuple);
    }


    private void reduceMatrix(String caseid, String zoneid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();
            Response<byte[]> zoneDataByte = p.get(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid));
            Response<byte[]> vv = p.get(mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV));
            Response<byte[]> delz = p.get(mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ));

            p.sync();

            List<MWNumericArray> matVamEstExt = getMatVamEstExt(p, caseid, zoneid);
            MWNumericArray vaEstMatSArrRow = matVamEstExt.get(0);
            MWNumericArray vmEstMatSArrRow = matVamEstExt.get(1);
            MWNumericArray vaExtMatSArrRow = matVamEstExt.get(2);
            MWNumericArray vmExtMatSArrRow = matVamEstExt.get(3);

            MWStructArray zoneDataMat = (MWStructArray) MWStructArray.deserialize(zoneDataByte.get());
            MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vv.get());
            MWNumericArray delzMat = (MWNumericArray) MWNumericArray.deserialize(delz.get());
            Object[] reducedMat = null;
            try {
                reducedMat = estimator.api_reducedMatrix(4, zoneDataMat, vaEstMatSArrRow, vmEstMatSArrRow,
                        vaExtMatSArrRow, vmExtMatSArrRow, delzMat, vvMat);
            } catch (MWException e) {
                e.printStackTrace();
            }

            if (reducedMat != null) {
                MWNumericArray HH = (MWNumericArray) reducedMat[0];
                MWNumericArray WW = (MWNumericArray) reducedMat[1];
                MWNumericArray WWInv = (MWNumericArray) reducedMat[2];
                MWNumericArray ddelz = (MWNumericArray) reducedMat[3];

                byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
                byte[] WWKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WW);
                byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
                byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);

                p.set(HHkey, HH.serialize());
                p.set(WWKey, WW.serialize());
                p.set(WWInvKey, WWInv.serialize());
                p.set(ddelzKey, ddelz.serialize());

                p.sync();

                disposeMatArrays(HH, WW, WWInv, ddelz);

            }

            disposeMatArrays(zoneDataMat, vvMat, delzMat, vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow);
        }
    }
}
