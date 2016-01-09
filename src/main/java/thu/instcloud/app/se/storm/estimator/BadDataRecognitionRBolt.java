package thu.instcloud.app.se.storm.estimator;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import thu.instcloud.app.se.storm.common.StormUtils;
import thu.instcloud.app.se.storm.matworker.MatWorker;

import java.util.List;
import java.util.Map;

/**
 * Created by hjh on 15-12-30.
 */
public class BadDataRecognitionRBolt extends BaseRichBolt {
    //    private Estimator estimator;
    private String redisIp;
    private String pass;
    private int workerPort;
    private OutputCollector collector;
    private MatWorker matWorker;

    public BadDataRecognitionRBolt(String redisIp, String pass) {
        this.redisIp = redisIp;
        this.pass = pass;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                StormUtils.STORM.FIELDS.CASE_ID
        ));
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        workerPort = 9090 + topologyContext.getThisTaskIndex();
        try {
            matWorker = new MatWorker(redisIp, pass, "localhost", workerPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        super.prepare(map, topologyContext, outputCollector);
//        try {
//            estimator = new Estimator();
//        } catch (MWException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void execute(Tuple tuple) {
        String caesid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        List<String> zoneids = (List<String>) tuple.getValueByField(StormUtils.STORM.FIELDS.ZONE_ID_LIST);
        recogBadData(caesid, zoneids);
        collector.emit(new Values(caesid));
        collector.ack(tuple);
    }

    private void recogBadData(String caseid, List<String> zoneids) {
        int code = matWorker.perform(caseid, zoneids, StormUtils.MW.WORKER.BADRECOG_TASK);
        if (code != 0) {
            System.out.println("Task failed with erro code: " + code + "!");
        }
//        try {
//            int status= MatWorkerServerProcess.exec(redisIp,pass, StormUtils.MW.WORKER.BADRECOG_TASK,caseid,zoneid);
//            if (status!=0){
//                System.out.println("Error executing estimation task!");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


//        try (Jedis jedis = jedisPool.getResource()) {
//            auth(jedis);
//            Pipeline p = jedis.pipelined();
//
//            byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
//            byte[] WWKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WW);
//            byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
//            byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
//            byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
//
//            Response<byte[]> HHByte = p.get(HHkey);
//            Response<byte[]> WWByte = p.get(WWKey);
//            Response<byte[]> WWInvByte = p.get(WWInvKey);
//            Response<byte[]> ddelzByte = p.get(ddelzKey);
//            Response<byte[]> vvByte = p.get(vvKey);
//
//            String thrshldKey = mkKey(caseid,
//                    StormUtils.REDIS.KEYS.ZONES,
//                    zoneid,
//                    StormUtils.REDIS.KEYS.BAD_RECOG_THRESHOLD);
//            Response<String> badthrshld = p.get(thrshldKey);
//            p.sync();
//
//            MWNumericArray badthrshldMat = new MWNumericArray(badthrshld.get(), MWClassID.DOUBLE);
//            MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
//            MWNumericArray WWMat = (MWNumericArray) MWNumericArray.deserialize(WWByte.get());
//            MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
//            MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
//            MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());
//
//            MWNumericArray vvNewMat = null, convergedMat = null;
//            Object[] res = null;
//            try {
//                res = estimator.api_badDataRecognition(2, HHMat, WWMat, WWInvMat,
//                        vvMat, ddelzMat, badthrshldMat);
//            } catch (MWException e) {
//                e.printStackTrace();
//            }
//
////            update state
//            if (res != null) {
//                vvNewMat = (MWNumericArray) res[0];
//                if (vvMat.getDimensions()[0] != vvNewMat.getDimensions()[0]) {
//                    p.set(vvKey, vvNewMat.serialize());
//                }
//                convergedMat = (MWNumericArray) res[1];
//                boolean converbool;
//                if (!(convergedMat.getDouble() < 0)) {
//                    converbool = false;
//                    p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), Long.parseLong(zoneid), converbool);
//                }
//                p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
////                TODO: record only one iteration number
//                p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));
//                p.sync();
//            }
//
//            disposeMatArrays(badthrshldMat, HHMat, WWInvMat, WWMat, ddelzMat, vvMat, vvNewMat, convergedMat);
//
//        }
    }

    @Override
    public void cleanup() {
        matWorker.stop();
    }


}
