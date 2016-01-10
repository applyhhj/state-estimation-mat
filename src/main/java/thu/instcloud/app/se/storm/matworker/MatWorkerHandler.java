package thu.instcloud.app.se.storm.matworker;

import Estimator.Estimator;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import org.apache.thrift.TException;
import redis.clients.jedis.*;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;
import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 16-1-9.
 */
public class MatWorkerHandler implements MatWorkerService.Iface {
    //    for debug
    private static final String testTask = "test";
    private JedisPool jedisPool;
    private String pass;
    private Estimator estimator;
    private Long lastHeartBeat;

    public MatWorkerHandler(String redisIp, String pass) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), redisIp);
        this.pass = pass;
        this.lastHeartBeat = System.currentTimeMillis();
        try {
            this.estimator = new Estimator();
        } catch (MWException e) {
            e.printStackTrace();
        }
    }

    public Long getLastHeartBeat() {
        return lastHeartBeat;
    }

    @Override
    public int runTask(String caseid, List<String> zoneids, String taskName) throws TException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(pass);
            for (String zoneid : zoneids) {
                if (!jedis.exists(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid))) {
                    return -2;
                }
            }

            Pipeline p = jedis.pipelined();
            Response<String> tol = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_EST_TOL);
            p.sync();
            double toldbl = Double.parseDouble(tol.get());
            int nz = zoneids.size();
            if (taskName.equals(StormUtils.MW.WORKER.ESTIMATE_TASK)) {
                List<Response<byte[]>> zoneDataByteList = new ArrayList<>();
                List<Response<byte[]>> HHByteList = new ArrayList<>();
                List<Response<byte[]>> WWInvByteList = new ArrayList<>();
                List<Response<byte[]>> ddelzByteList = new ArrayList<>();
                List<Response<byte[]>> vvByteList = new ArrayList<>();

                List<Response<List<String>>> busIdsList = new ArrayList<>();
                List<Response<List<String>>> outBusIdsList = new ArrayList<>();
                List<Response<List<String>>> brIdsList = new ArrayList<>();

                for (int i = 0; i < nz; i++) {
                    String zoneid = zoneids.get(i);

                    byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
                    byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
                    byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
                    byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
                    byte[] zoneDataKey = mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid);

                    zoneDataByteList.add(p.get(zoneDataKey));
                    HHByteList.add(p.get(HHkey));
                    WWInvByteList.add(p.get(WWInvKey));
                    ddelzByteList.add(p.get(ddelzKey));
                    vvByteList.add(p.get(vvKey));

                    String busIdsKey = mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT);
                    String outBusIdsKey = mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.OUT_BUS_NUM_OUT);
                    String brIdsKey = mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BRANCH_IDS);

                    busIdsList.add(p.lrange(busIdsKey, 0, -1));
                    outBusIdsList.add(p.lrange(outBusIdsKey, 0, -1));
                    brIdsList.add(p.lrange(brIdsKey, 0, -1));
                }

                p.sync();

                String field = "value";
                String[] fields = {field};
                MWStructArray zonesData = new MWStructArray(nz, 1, fields);
                MWStructArray HHsData = new MWStructArray(nz, 1, fields);
                MWStructArray WWInvsData = new MWStructArray(nz, 1, fields);
                MWStructArray ddelzsData = new MWStructArray(nz, 1, fields);
                MWStructArray vvsData = new MWStructArray(nz, 1, fields);
                for (int i = 0; i < nz; i++) {
                    zonesData.set(field, i + 1, MWStructArray.deserialize(zoneDataByteList.get(i).get()));
                    HHsData.set(field, i + 1, MWNumericArray.deserialize(HHByteList.get(i).get()));
                    WWInvsData.set(field, i + 1, MWNumericArray.deserialize(WWInvByteList.get(i).get()));
                    ddelzsData.set(field, i + 1, MWNumericArray.deserialize(ddelzByteList.get(i).get()));
                    vvsData.set(field, i + 1, MWNumericArray.deserialize(vvByteList.get(i).get()));
                }

//                get measurement and voltages
                List<Response<List<String>>> vaEstList = new ArrayList<>();
                List<Response<List<String>>> vmEstList = new ArrayList<>();
                List<Response<List<String>>> vaExtList = new ArrayList<>();
                List<Response<List<String>>> vmExtList = new ArrayList<>();

                List<Response<List<String>>> zpfList = new ArrayList<>();
                List<Response<List<String>>> zptList = new ArrayList<>();
                List<Response<List<String>>> zqfList = new ArrayList<>();
                List<Response<List<String>>> zqtList = new ArrayList<>();
                List<Response<List<String>>> pbusList = new ArrayList<>();
                List<Response<List<String>>> qbusList = new ArrayList<>();
                List<Response<List<String>>> vamList = new ArrayList<>();
                List<Response<List<String>>> vmmList = new ArrayList<>();

                for (int i = 0; i < nz; i++) {
                    List<String> busIdsLst = busIdsList.get(i).get();
                    List<String> outBusIdsLst = outBusIdsList.get(i).get();
                    List<String> bridsLst = brIdsList.get(i).get();

                    vaEstList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()])));
                    vmEstList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()])));
                    vaExtList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()])));
                    vmExtList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()])));

                    zpfList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PF), bridsLst.toArray(new String[bridsLst.size()])));
                    zptList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PT), bridsLst.toArray(new String[bridsLst.size()])));
                    zqfList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QF), bridsLst.toArray(new String[bridsLst.size()])));
                    zqtList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QT), bridsLst.toArray(new String[bridsLst.size()])));
                    pbusList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PBUS), busIdsLst.toArray(new String[busIdsLst.size()])));
                    qbusList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QBUS), busIdsLst.toArray(new String[busIdsLst.size()])));
                    vamList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA), busIdsLst.toArray(new String[busIdsLst.size()])));
                    vmmList.add(p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM), busIdsLst.toArray(new String[busIdsLst.size()])));
                }
                p.sync();

                MWStructArray vasEstData = new MWStructArray(nz, 1, fields);
                MWStructArray vmsEstData = new MWStructArray(nz, 1, fields);
                MWStructArray vasExtData = new MWStructArray(nz, 1, fields);
                MWStructArray vmsExtData = new MWStructArray(nz, 1, fields);
                MWStructArray zsData = new MWStructArray(nz, 1, fields);
                for (int i = 0; i < nz; i++) {
                    vasEstData.set(field, i + 1, new MWNumericArray(vaEstList.get(i).get().toArray(), MWClassID.DOUBLE));
                    vmsEstData.set(field, i + 1, new MWNumericArray(vmEstList.get(i).get().toArray(), MWClassID.DOUBLE));
                    vasExtData.set(field, i + 1, new MWNumericArray(vaExtList.get(i).get().toArray(), MWClassID.DOUBLE));
                    vmsExtData.set(field, i + 1, new MWNumericArray(vmExtList.get(i).get().toArray(), MWClassID.DOUBLE));

                    List<String> z = new ArrayList<>();
                    z.addAll(zpfList.get(i).get());
                    z.addAll(zptList.get(i).get());
                    z.addAll(pbusList.get(i).get());
                    z.addAll(vamList.get(i).get());
                    z.addAll(zqfList.get(i).get());
                    z.addAll(zqtList.get(i).get());
                    z.addAll(qbusList.get(i).get());
                    z.addAll(vmmList.get(i).get());
                    zsData.set(field, i + 1, new MWNumericArray(z.toArray(), MWClassID.DOUBLE));
                }

                Object[] res = null;
                MWStructArray delz = null, normF = null, ddelz = null, VVa = null, VVm = null, step = null, success = null;

                try {
                    res = estimator.api_estimateOnce_batch(7, HHsData, WWInvsData, ddelzsData, vvsData,
                            vasEstData, vmsEstData, vasExtData, vmsExtData, zsData, zonesData);
                    disposeMatArrays(zonesData, zsData, vasEstData, vmsEstData, vasExtData, vmsExtData,
                            HHsData, WWInvsData, ddelzsData, vvsData);
                } catch (MWException e) {
                    e.printStackTrace();
                }

                if (res != null) {
                    VVa = (MWStructArray) res[0];
                    VVm = (MWStructArray) res[1];
                    delz = (MWStructArray) res[2];
                    ddelz = (MWStructArray) res[3];
                    normF = (MWStructArray) res[4];
                    step = (MWStructArray) res[5];
                    success = (MWStructArray) res[6];

                    for (int i = 0; i < nz; i++) {
                        List<String> busids = busIdsList.get(i).get();
//                      update state
                        MWNumericArray va = (MWNumericArray) VVa.getField(field, i + 1);
                        MWNumericArray vm = (MWNumericArray) VVm.getField(field, i + 1);
                        double[][] vaArr = (double[][]) va.toArray();
                        double[][] vmArr = (double[][]) vm.toArray();
                        Map<String, String> vaMap = new HashMap<>();
                        Map<String, String> vmMap = new HashMap<>();
                        for (int j = 0; j < busids.size(); j++) {
                            vaMap.put(busids.get(j), String.valueOf(vaArr[j][0]));
                            vmMap.put(busids.get(j), String.valueOf(vmArr[j][0]));
                        }
                        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_BUFFER_HASH), vaMap);
                        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_BUFFER_HASH), vmMap);

                        String zoneid = zoneids.get(i);
                        MWNumericArray ddelzi = (MWNumericArray) ddelz.getField(field, i + 1);
                        MWNumericArray delzi = (MWNumericArray) delz.getField(field, i + 1);
                        p.set(mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ), ddelzi.serialize());
                        p.set(mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ), delzi.serialize());
//                estimated zones
                        p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES));
//                estimate times
//                TODO: record only one iteration number
                        p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT));
                        MWNumericArray stepimat = (MWNumericArray) step.getField(field, i + 1);
                        double stepi = stepimat.getDouble();
                        if (stepi < toldbl) {
                            p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), Long.parseLong(zoneid), true);
                        }

                        disposeMatArrays(va, vm, ddelzi, delzi, stepimat);
//                debug
//                        System.out.println("zone: " + zoneid + ";  step:" + stepi);
                    }
                    p.sync();
                    disposeMatArrays(VVa, VVm, delz, ddelz, normF, success, step);
                }

                return 0;
            } else if (taskName.equals(StormUtils.MW.WORKER.BADRECOG_TASK)) {
                List<Response<byte[]>> WWByteList = new ArrayList<>();
                List<Response<byte[]>> HHByteList = new ArrayList<>();
                List<Response<byte[]>> WWInvByteList = new ArrayList<>();
                List<Response<byte[]>> ddelzByteList = new ArrayList<>();
                List<Response<byte[]>> vvByteList = new ArrayList<>();

                List<Response<String>> badthdList = new ArrayList<>();

                for (int i = 0; i < nz; i++) {
                    String zoneid = zoneids.get(i);

                    byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
                    byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
                    byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
                    byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
                    byte[] WWKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WW);

                    WWByteList.add(p.get(WWKey));
                    HHByteList.add(p.get(HHkey));
                    WWInvByteList.add(p.get(WWInvKey));
                    ddelzByteList.add(p.get(ddelzKey));
                    vvByteList.add(p.get(vvKey));

                    String thdKey = mkKey(caseid,
                            StormUtils.REDIS.KEYS.ZONES,
                            zoneid,
                            StormUtils.REDIS.KEYS.BAD_RECOG_THRESHOLD);
                    badthdList.add(p.get(thdKey));

                }

                p.sync();

                String field = "value";
                String[] fields = {field};
                MWStructArray WWsData = new MWStructArray(nz, 1, fields);
                MWStructArray HHsData = new MWStructArray(nz, 1, fields);
                MWStructArray WWInvsData = new MWStructArray(nz, 1, fields);
                MWStructArray ddelzsData = new MWStructArray(nz, 1, fields);
                MWStructArray vvsData = new MWStructArray(nz, 1, fields);
                MWStructArray badthdsData = new MWStructArray(nz, 1, fields);

                for (int i = 0; i < nz; i++) {
                    WWsData.set(field, i + 1, MWNumericArray.deserialize(WWByteList.get(i).get()));
                    HHsData.set(field, i + 1, MWNumericArray.deserialize(HHByteList.get(i).get()));
                    WWInvsData.set(field, i + 1, MWNumericArray.deserialize(WWInvByteList.get(i).get()));
                    ddelzsData.set(field, i + 1, MWNumericArray.deserialize(ddelzByteList.get(i).get()));
                    vvsData.set(field, i + 1, MWNumericArray.deserialize(vvByteList.get(i).get()));
                    badthdsData.set(field, i + 1, new MWNumericArray(badthdList.get(i).get(), MWClassID.DOUBLE));
                }

                MWStructArray vvNewMat = null, convergedMat = null;
                Object[] res = null;
                try {
                    res = estimator.api_badDataRecognition_batch(2, HHsData, WWsData, WWInvsData,
                            vvsData, ddelzsData, badthdsData);
                    disposeMatArrays(badthdsData, HHsData, WWInvsData, WWsData, ddelzsData);
                } catch (MWException e) {
                    e.printStackTrace();
                }

//            update state
                if (res != null) {
                    vvNewMat = (MWStructArray) res[0];
                    convergedMat = (MWStructArray) res[1];

                    for (int i = 0; i < nz; i++) {
                        byte[] vvKey = mkByteKey(caseid, zoneids.get(i), StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
                        String convKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
                        MWNumericArray vviMat = (MWNumericArray) vvsData.getField(field, i + 1);
                        MWNumericArray vvNewiMat = (MWNumericArray) vvNewMat.getField(field, i + 1);
                        int nvvi = vviMat.getDimensions()[0];
                        int nvvNewi = vvNewiMat.getDimensions()[0];

                        if (nvvi != nvvNewi) {
                            p.set(vvKey, vvNewiMat.serialize());
                        }

                        MWNumericArray convergediMat = (MWNumericArray) convergedMat.getField(field, i + 1);
                        double converged = convergediMat.getDouble();
                        if (!(converged < 0)) {
                            p.setbit(convKey, Long.parseLong(zoneids.get(i)), false);
                        }
                        p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
                        p.incr(mkKey(caseid, zoneids.get(i), StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));

                        disposeMatArrays(vviMat, vvNewiMat, convergediMat);
                    }

                    p.sync();
                    disposeMatArrays(vvsData, vvNewMat, convergedMat);
                }

                return 0;
            } else if (taskName.equals(testTask)) {
                p.set(testTask, System.currentTimeMillis() + "");
                p.sync();
                return 10;
            }
        }

        return -3;
    }

    @Override
    public void heartbeat() throws TException {
        lastHeartBeat = System.currentTimeMillis();
    }


//
//    @Override
//    public int runTask(String caseid, String zoneid, String taskName) throws TException {
//        try (Jedis jedis = jedisPool.getResource()) {
//            jedis.auth(pass);
//
//            if (!jedis.exists(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid))) {
//                return -2;
//            }
//
//
//            Pipeline p = jedis.pipelined();
//
//            if (taskName.equals(StormUtils.MW.WORKER.ESTIMATE_TASK)) {
//                byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
//                byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
//                byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
//                byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
//                byte[] zoneDataKey = mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid);
//
//                Response<byte[]> zoneDataByte = p.get(zoneDataKey);
//                Response<byte[]> HHByte = p.get(HHkey);
//                Response<byte[]> WWInvByte = p.get(WWInvKey);
//                Response<byte[]> ddelzByte = p.get(ddelzKey);
//                Response<byte[]> vvByte = p.get(vvKey);
//                Response<List<String>> busIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
//
//                p.sync();
////            get related ids
//                List<String> busIdsLst = busIds.get();
//
//                MWNumericArray zMatSArrRow = getMatZ(p, caseid, zoneid);
//
//                List<MWNumericArray> matVamEstExt = getMatVamEstExt(p, caseid, zoneid);
//                MWNumericArray vaEstMatSArrRow = matVamEstExt.get(0);
//                MWNumericArray vmEstMatSArrRow = matVamEstExt.get(1);
//                MWNumericArray vaExtMatSArrRow = matVamEstExt.get(2);
//                MWNumericArray vmExtMatSArrRow = matVamEstExt.get(3);
//
////            get zone data
//                MWStructArray zoneDataMatSArr = (MWStructArray) MWStructArray.deserialize(zoneDataByte.get());
//
////            state
//                MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
//                MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
//                MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
//                MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());
//
//                Object[] res = null;
//                MWNumericArray delz = null, normF = null, ddelz = null, VVa = null, VVm = null, step = null, success = null;
//
//                try {
//                    res = estimator.api_estimateOnce(7, HHMat, WWInvMat, ddelzMat, vvMat,
//                            vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow, zMatSArrRow, zoneDataMatSArr);
//                } catch (MWException e) {
//                    e.printStackTrace();
//                }
//
//                if (res != null) {
//                    VVa = (MWNumericArray) res[0];
//                    VVm = (MWNumericArray) res[1];
//                    delz = (MWNumericArray) res[2];
//                    ddelz = (MWNumericArray) res[3];
//                    normF = (MWNumericArray) res[4];
//                    step = (MWNumericArray) res[5];
//                    success = (MWNumericArray) res[6];
//
////                update state
//                    updateEstimatedVoltagesToBuffer(caseid, p, busIdsLst, VVa, VVm);
//                    p.set(ddelzKey, ddelz.serialize());
//                    p.set(mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ), delz.serialize());
////                estimated zones
//                    p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES));
////                estimate times
////                TODO: record only one iteration number
//                    p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT));
//                    Response<String> tol = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_EST_TOL);
//                    p.sync();
//
//                    double toldbl = Double.parseDouble(tol.get());
//                    if (step.getDouble() < toldbl) {
//                        String converKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
//                        p.setbit(converKey, Long.parseLong(zoneid), true);
//                        p.sync();
//                    }
////                debug
////                if (step.getDouble(1)>toldbl) {
//                    System.out.println("zone: " + zoneid + ";  step:" + step);
////                }
//
//                }
//                disposeMatArrays(VVa, VVm, delz, ddelz, normF, success, step);
//                disposeMatArrays(zoneDataMatSArr, zMatSArrRow, vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow,
//                        HHMat, WWInvMat, ddelzMat, vvMat);
//
//                return 0;
//            } else if (taskName.equals(StormUtils.MW.WORKER.BADRECOG_TASK)) {
//                byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
//                byte[] WWKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WW);
//                byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
//                byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
//                byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
//
//                Response<byte[]> HHByte = p.get(HHkey);
//                Response<byte[]> WWByte = p.get(WWKey);
//                Response<byte[]> WWInvByte = p.get(WWInvKey);
//                Response<byte[]> ddelzByte = p.get(ddelzKey);
//                Response<byte[]> vvByte = p.get(vvKey);
//
//                String thrshldKey = mkKey(caseid,
//                        StormUtils.REDIS.KEYS.ZONES,
//                        zoneid,
//                        StormUtils.REDIS.KEYS.BAD_RECOG_THRESHOLD);
//                Response<String> badthrshld = p.get(thrshldKey);
//                p.sync();
//
//                MWNumericArray badthrshldMat = new MWNumericArray(badthrshld.get(), MWClassID.DOUBLE);
//                MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
//                MWNumericArray WWMat = (MWNumericArray) MWNumericArray.deserialize(WWByte.get());
//                MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
//                MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
//                MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());
//
//                MWNumericArray vvNewMat = null, convergedMat = null;
//                Object[] res = null;
//                try {
//                    res = estimator.api_badDataRecognition(2, HHMat, WWMat, WWInvMat,
//                            vvMat, ddelzMat, badthrshldMat);
//                } catch (MWException e) {
//                    e.printStackTrace();
//                }
//
////            update state
//                if (res != null) {
//                    vvNewMat = (MWNumericArray) res[0];
//                    if (vvMat.getDimensions()[0] != vvNewMat.getDimensions()[0]) {
//                        p.set(vvKey, vvNewMat.serialize());
//                    }
//                    convergedMat = (MWNumericArray) res[1];
//                    boolean converbool;
//                    if (!(convergedMat.getDouble() < 0)) {
//                        converbool = false;
//                        p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), Long.parseLong(zoneid), converbool);
//                    }
//                    p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
////                TODO: record only one iteration number
//                    p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));
//                    p.sync();
//                }
//
//                disposeMatArrays(badthrshldMat, HHMat, WWInvMat, WWMat, ddelzMat, vvMat, vvNewMat, convergedMat);
//
//                return 0;
//            } else if (taskName.equals(testTask)) {
//                p.set(testTask, "test");
//                p.sync();
//                return 10;
//            }
//        }
//
//        return -3;
//    }
}
