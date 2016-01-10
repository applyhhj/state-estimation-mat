package thu.instcloud.app.se.storm.estimator;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.common.StormUtils;
import thu.instcloud.app.se.storm.matworker.MatWorker;

import java.util.List;
import java.util.Map;

/**
 * Created by hjh on 15-12-30.
 */
public class EstimateOnceRBolt extends BaseRichBolt {
    //    private Estimator estimator;
    private String redisIp;
    private String pass;
    private int workerPort;
    private MatWorker matWorker;
    private OutputCollector collector;

    public EstimateOnceRBolt(String redisIp, String pass) {
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
        workerPort = 9190 + topologyContext.getThisTaskIndex();
        try {
            matWorker = new MatWorker(redisIp, pass, "localhost", workerPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        try {
//            estimator = new Estimator();
//        } catch (MWException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void execute(Tuple tuple) {
        String caseid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        List<String> zoneids = (List<String>) Utils.deserialize(tuple.getBinaryByField(StormUtils.STORM.FIELDS.ZONE_ID_LIST));

        estimate(caseid, zoneids);
        collector.emit(new Values(caseid));
        collector.ack(tuple);
    }

    private void estimate(String caseid, List<String> zoneids) {
        int code = matWorker.perform(caseid, zoneids, StormUtils.MW.WORKER.ESTIMATE_TASK);
        if (code != 0) {
            System.out.println("Task failed with erro code: " + code + "!");
        }
//        try {
//            int status= MatWorkerServerProcess.exec(redisIp,pass, MW.TASK.ESTIMATE_TASK,caseid,zoneid);
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
//            byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
//            byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
//            byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
//            byte[] zoneDataKey = mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid);
//
//            Response<byte[]> zoneDataByte = p.get(zoneDataKey);
//            Response<byte[]> HHByte = p.get(HHkey);
//            Response<byte[]> WWInvByte = p.get(WWInvKey);
//            Response<byte[]> ddelzByte = p.get(ddelzKey);
//            Response<byte[]> vvByte = p.get(vvKey);
//            Response<List<String>> busIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
////            Response<List<String>> outBusIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.OUT_BUS_NUM_OUT), 0, -1);
////            Response<List<String>> brids = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BRANCH_IDS), 0, -1);
//
//            p.sync();
////            get related ids
//            List<String> busIdsLst = busIds.get();
////            List<String> outBusIdsLst = outBusIds.get();
////            List<String> bridsLst = brids.get();
//
////            get estimated voltage and external bus voltage
////            Response<List<String>> VaEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()]));
////            Response<List<String>> VmEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()]));
////            Response<List<String>> VaExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()]));
////            Response<List<String>> VmExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()]));
//
////            get measurement
////            Response<List<String>> zpf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PF), bridsLst.toArray(new String[bridsLst.size()]));
////            Response<List<String>> zpt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PT), bridsLst.toArray(new String[bridsLst.size()]));
////            Response<List<String>> zqf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QF), bridsLst.toArray(new String[bridsLst.size()]));
////            Response<List<String>> zqt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QT), bridsLst.toArray(new String[bridsLst.size()]));
////            Response<List<String>> pbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PBUS), busIdsLst.toArray(new String[busIdsLst.size()]));
////            Response<List<String>> qbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QBUS), busIdsLst.toArray(new String[busIdsLst.size()]));
////            Response<List<String>> Vam = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA), busIdsLst.toArray(new String[busIdsLst.size()]));
////            Response<List<String>> Vmm = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM), busIdsLst.toArray(new String[busIdsLst.size()]));
//
////            p.sync();
//
////            List<String> z = new ArrayList<>();
////            z.addAll(zpf.get());
////            z.addAll(zpt.get());
////            z.addAll(pbus.get());
////            z.addAll(Vam.get());
////            z.addAll(zqf.get());
////            z.addAll(zqt.get());
////            z.addAll(qbus.get());
////            z.addAll(Vmm.get());
//
//            MWNumericArray zMatSArrRow = getMatZ(p, caseid, zoneid);
//
//            List<MWNumericArray> matVamEstExt = getMatVamEstExt(p, caseid, zoneid);
//            MWNumericArray vaEstMatSArrRow = matVamEstExt.get(0);
//            MWNumericArray vmEstMatSArrRow = matVamEstExt.get(1);
//            MWNumericArray vaExtMatSArrRow = matVamEstExt.get(2);
//            MWNumericArray vmExtMatSArrRow = matVamEstExt.get(3);
//
////            get zone data
//            MWStructArray zoneDataMatSArr = (MWStructArray) MWStructArray.deserialize(zoneDataByte.get());
////            MWNumericArray zMatSArrRow = new MWNumericArray(z.toArray(), MWClassID.DOUBLE);
////            get estimated voltages of this zone
////            MWNumericArray vaEstMatSArrRow = new MWNumericArray(VaEst.get().toArray(), MWClassID.DOUBLE);
////            MWNumericArray vmEstMatSArrRow = new MWNumericArray(VmEst.get().toArray(), MWClassID.DOUBLE);
////            get estimated voltages of external buses
////            MWNumericArray vaExtMatSArrRow = new MWNumericArray(VaExt.get().toArray(), MWClassID.DOUBLE);
////            MWNumericArray vmExtMatSArrRow = new MWNumericArray(VmExt.get().toArray(), MWClassID.DOUBLE);
//
////            state
//            MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
//            MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
//            MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
//            MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());
//
//            Object[] res = null;
//            MWNumericArray delz = null, normF = null, ddelz = null, VVa = null, VVm = null, step = null, success = null;
//
//            try {
//                res = estimator.api_estimateOnce(7, HHMat, WWInvMat, ddelzMat, vvMat,
//                        vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow, zMatSArrRow, zoneDataMatSArr);
//            } catch (MWException e) {
//                e.printStackTrace();
//            }
//
//            if (res != null) {
//                VVa = (MWNumericArray) res[0];
//                VVm = (MWNumericArray) res[1];
//                delz = (MWNumericArray) res[2];
//                ddelz = (MWNumericArray) res[3];
//                normF = (MWNumericArray) res[4];
//                step = (MWNumericArray) res[5];
//                success = (MWNumericArray) res[6];
//
////                update state
//                updateEstimatedVoltagesToBuffer(caseid, p, busIdsLst, VVa, VVm);
//                p.set(ddelzKey, ddelz.serialize());
//                p.set(mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ), delz.serialize());
////                estimated zones
//                p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES));
////                estimate times
////                TODO: record only one iteration number
//                p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT));
//                Response<String> tol = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_EST_TOL);
//                p.sync();
//
//                double toldbl = Double.parseDouble(tol.get());
//                if (step.getDouble() < toldbl) {
//                    String converKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
//                    p.setbit(converKey, Long.parseLong(zoneid), true);
//                    p.sync();
//                }
////                debug
////                if (step.getDouble(1)>toldbl) {
//                System.out.println("zone: " + zoneid + ";  step:" + step);
////                }
//
//            }
//            disposeMatArrays(VVa, VVm, delz, ddelz, normF, success, step);
//            disposeMatArrays(zoneDataMatSArr, zMatSArrRow, vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow,
//                    HHMat, WWInvMat, ddelzMat, vvMat);
//        }
    }

    @Override
    public void cleanup() {
        matWorker.stop();
    }

}
