package thu.instcloud.app.se.storm.common;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hjh on 15-12-26.
 */
public class StormUtils {

    public static String mkKey(String ...keys){
        String res=keys[0];

        if (keys.length>1) {
            for (int i = 1; i < keys.length; i++) {
                res=res+"."+keys[i];
            }
        }
        return res;
    }

    public static byte[] mkByteKey(String ...keys){
        return mkKey(keys).getBytes();
    }

    public static String getCaseFromFileName(String caseFile){
        return caseFile.replace(CONSTANTS.CASE_FILE_EXT,"");
    }

    public static void updateEstimatedVoltagesToBuffer(String caseid, String zoneid, Pipeline p, MWNumericArray va, MWNumericArray vm) {
        //            get related ids
        Response<List<String>> busIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
        p.sync();
        List<String> busids = busIds.get();
        double[][] vaArr = (double[][]) va.toArray();
        double[][] vmArr = (double[][]) vm.toArray();
        Map<String, String> vaMap = new HashMap<>();
        Map<String, String> vmMap = new HashMap<>();

        for (int i = 0; i < busids.size(); i++) {
            vaMap.put(busids.get(i), String.valueOf(vaArr[i][0]));
            vmMap.put(busids.get(i), String.valueOf(vmArr[i][0]));
        }

//        only when all zones have finished one round estimation can we update the voltages of the system, before
//        that all estimated values are stored in buffer.
        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_BUFFER_HASH), vaMap);
        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_BUFFER_HASH), vmMap);
        p.sync();
    }

    public static void setRefBusEstState(Pipeline p, String caseid) {
        String vaEstKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH);
        String vmEstKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH);
        //        always reset reference bus state to the value from power flow
        String vaRefKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_REF);
        String vmRefKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_REF);
        Response<String> vaRef = p.get(vaRefKey);
        Response<String> vmRef = p.get(vmRefKey);
        Response<List<String>> refNumResp = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, "0", StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
        p.sync();

        p.hset(vmEstKey, refNumResp.get().get(0), vmRef.get());
        p.hset(vaEstKey, refNumResp.get().get(0), vaRef.get());
        p.sync();
    }

    public static MWNumericArray getMatZ(Pipeline p, String caseid, String zoneid) {
        Response<List<String>> busIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
        Response<List<String>> brids = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BRANCH_IDS), 0, -1);

        p.sync();

        //            get related ids
        List<String> busIdsLst = busIds.get();
        List<String> bridsLst = brids.get();

        //            get measurement
        Response<List<String>> zpf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PF), bridsLst.toArray(new String[bridsLst.size()]));
        Response<List<String>> zpt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PT), bridsLst.toArray(new String[bridsLst.size()]));
        Response<List<String>> zqf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QF), bridsLst.toArray(new String[bridsLst.size()]));
        Response<List<String>> zqt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QT), bridsLst.toArray(new String[bridsLst.size()]));
        Response<List<String>> pbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PBUS), busIdsLst.toArray(new String[busIdsLst.size()]));
        Response<List<String>> qbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QBUS), busIdsLst.toArray(new String[busIdsLst.size()]));
        Response<List<String>> Vam = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA), busIdsLst.toArray(new String[busIdsLst.size()]));
        Response<List<String>> Vmm = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM), busIdsLst.toArray(new String[busIdsLst.size()]));

        p.sync();

        List<String> z = new ArrayList<>();
        z.addAll(zpf.get());
        z.addAll(zpt.get());
        z.addAll(pbus.get());
        z.addAll(Vam.get());
        z.addAll(zqf.get());
        z.addAll(zqt.get());
        z.addAll(qbus.get());
        z.addAll(Vmm.get());

        return new MWNumericArray(z.toArray(), MWClassID.DOUBLE);

    }

    public static List<MWNumericArray> getMatVamEstExt(Pipeline p, String caseid, String zoneid) {
        List<MWNumericArray> res = new ArrayList<>();

        Response<List<String>> busIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
        Response<List<String>> outBusIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.OUT_BUS_NUM_OUT), 0, -1);

        p.sync();

        List<String> busIdsLst = busIds.get();
        List<String> outBusIdsLst = outBusIds.get();

        Response<List<String>> VaEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()]));
        Response<List<String>> VmEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()]));
        Response<List<String>> VaExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()]));
        Response<List<String>> VmExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()]));

        p.sync();

        MWNumericArray vaEstMatSArrRow = new MWNumericArray(VaEst.get().toArray(), MWClassID.DOUBLE);
        MWNumericArray vmEstMatSArrRow = new MWNumericArray(VmEst.get().toArray(), MWClassID.DOUBLE);
        MWNumericArray vaExtMatSArrRow = new MWNumericArray(VaExt.get().toArray(), MWClassID.DOUBLE);
        MWNumericArray vmExtMatSArrRow = new MWNumericArray(VmExt.get().toArray(), MWClassID.DOUBLE);

        res.add(vaEstMatSArrRow);
        res.add(vmEstMatSArrRow);
        res.add(vaExtMatSArrRow);
        res.add(vmExtMatSArrRow);

        return res;
    }

    public static MWStructArray getMatZoneData(Pipeline p, String caseid, String zoneid) {
        Response<byte[]> zoneDataByte = p.get(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid));
        p.sync();
        return (MWStructArray) MWStructArray.deserialize(zoneDataByte.get());
    }

    public static boolean checkMeasureReady(Jedis jedis, String caseid) {
        Pipeline p = jedis.pipelined();
        return checkMeasureReady(p, caseid);
    }

    public static boolean checkMeasureReady(Pipeline p, String caseid) {

        String nbKey = mkKey(caseid, StormUtils.REDIS.KEYS.N_BUS);
        String nbrKey = mkKey(caseid, StormUtils.REDIS.KEYS.N_BRANCH);

        Response<String> nbStr = p.get(nbKey);
        Response<String> nbrStr = p.get(nbrKey);
        Response<Long> npfStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.PF));
        Response<Long> nptStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.PT));
        Response<Long> npbusStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.PBUS));
        Response<Long> nvaStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.VA));
        Response<Long> nqfStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.QF));
        Response<Long> nqtStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.QT));
        Response<Long> nqbusStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.QBUS));
        Response<Long> nvmStr = p.hlen(mkKey(caseid, StormUtils.MEASURE.TYPE.VM));

        p.sync();

        Long nb = Long.parseLong(nbStr.get());
        Long nbr = Long.parseLong(nbrStr.get());

        return (npfStr.get() == nbr && nptStr.get() == nbr && nqfStr.get() == nbr && nqtStr.get() == nbr
                && npbusStr.get() == nb && nqbusStr.get() == nb && nvaStr.get() == nb && nvmStr.get() == nb);

    }

    public static void resetNonRefBustEstState(Jedis jedis, String caseid) {
        Pipeline p = jedis.pipelined();
        resetNonRefBustEstState(p, caseid);
    }

    public static void resetNonRefBustEstState(Pipeline p, String caseid) {
        Response<String> nzStr = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES));
        int nz = Integer.parseInt(nzStr.get());
        resetNonRefBustEstState(p, caseid, nz);
    }

    public static void resetNonRefBustEstState(Pipeline p, String caseid, int nz) {
        //        reset initial values of estimated state of non reference buses
        String vaEstKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH);
        String vmEstKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH);
        p.del(vaEstKey);
        p.del(vmEstKey);
        p.sync();

        List<String> busNumsOutStrs = new ArrayList<>();
        List<Response<List<String>>> busNumsRespList = new ArrayList<>();
//                ignore reference bus
        for (int i = 1; i < nz; i++) {
            busNumsRespList.add(p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, i + "",
                    StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1));
        }
        p.sync();
        for (int i = 0; i < busNumsRespList.size(); i++) {
            busNumsOutStrs.addAll(busNumsRespList.get(i).get());
        }

        Map<String, String> vaEstInit = getVEstInit(busNumsOutStrs, true);
        Map<String, String> vmEstInit = getVEstInit(busNumsOutStrs, false);
        p.hmset(vaEstKey, vaEstInit);
        p.hmset(vmEstKey, vmEstInit);
        p.sync();
    }

    private static Map<String, String> getVEstInit(List<String> nums, boolean va) {
        String val;
        if (va) {
            val = "0";
        } else {
            val = "1";
        }

        Map<String, String> res = new HashMap<>();

        for (int i = 0; i < nums.size(); i++) {
            res.put(nums.get(i) + "", val);
        }
        return res;
    }


    public static boolean checkCaseState(Jedis jedis, String caseid) {
        boolean res = false;

        String caseReadyKey = mkKey(caseid, StormUtils.REDIS.KEYS.READY);
        String estimatingKey = mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATING_BIT);

        if (jedis.getbit(caseReadyKey, 0) && (checkMeasureReady(jedis, caseid)) && !jedis.getbit(estimatingKey, 0)) {
            res = true;
        }

        return res;
    }

    public static class MW {
        public static MWStructArray getArrayElement(MWStructArray array, int idx) {
            MWStructArray res = new MWStructArray(1, 1, array.fieldNames());
            MWArray e;
            for (String key : array.fieldNames()) {
                e = array.getField(key, idx);
                res.set(key, 1, e);
                e.dispose();
            }
            return res;
        }

        public static void disposeMatArrays(MWArray... arrays) {
            for (MWArray a :
                    arrays) {
                if (a != null) {
                    a.dispose();
                }
            }
        }

        public abstract class FIELDS {
            public static final String ZONE_NUM = "num";
            public static final String OUT_BUS_NUM_OUT = "ii2eout";
            public static final String BUS_NUM_OUT = "ii2e";
            public static final String BRANCH_IDS = "brids";
            public static final String Z_TRUE = "zTrue";
            public static final String SIGMA = "sigma";
            public static final String REF_NUM = "refout";
            public static final String VA_REF = "VaRef";
            public static final String VM_REF = "VmRef";
            public static final String BAD_THRESHOLD = "bad_threshold";
        }

        public abstract class WORKER {
            public static final String HOST_ARG_NAME = "rh";
            public static final String AUTH_ARG_NAME = "ra";
            public static final String WORKER_PORT_ARG_NAME = "p";

            public static final String ESTIMATE_TASK = "estimate";
            public static final String BADRECOG_TASK = "badRecog";

            public static final String STRUCT_ARRAY_VALUE_FIELD = "value";
        }

    }

    public static class OPTIONS {
        public static Map<String, String> getDefualtOptions() {
            Map<String, String> res = new HashMap<>();
            res.put(KEYS.OPT_NBUS_ZONE, "300");
            res.put(KEYS.OPT_EST_TOL, "1e-8");
            res.put(KEYS.OPT_MAX_BAD_REG_IT, "50");
            res.put(KEYS.OPT_MAX_EST_IT, "20");
            res.put(KEYS.OPT_OVERWRITE_CASEDATA, "true");
            return res;
        }

        public abstract class KEYS {
            public static final String OPT_NBUS_ZONE = "nbusZone";
            public static final String OPT_EST_TOL = "estTol";
            public static final String OPT_MAX_EST_IT = "maxEstIt";
            public static final String OPT_MAX_BAD_REG_IT = "maxBadRegIt";
            public static final String OPT_OVERWRITE_CASEDATA = "overWriteCase";
        }
    }

    public static class MEASURE {

        public abstract class TYPE {
            public static final String PF = "pf";
            public static final String PT = "pt";
            public static final String PBUS = "pbus";
            public static final String VA = "va";
            public static final String QF = "qf";
            public static final String QT = "qt";
            public static final String QBUS = "qbus";
            public static final String VM = "vm";
        }

    }

    public abstract class REDIS{

        public static final String REDIS_SERVER_IP = "10.0.0.1";
        public static final String PASS = "redis";

        public abstract class KEYS{
            public static final String CASES_WAITING_FOR_EST = "SE.waitingQueue";

            public static final String RAW_DATA="rawData";

            public static final String BUS="bus";
            public static final String GEN="gen";
            public static final String BRANCH="branch";
            public static final String N_BUS = "nb";
            public static final String N_BRANCH = "nbr";
            public static final String SBASE="sbase";
            public static final String ZONES="zones";
            public static final String NUM_OF_ZONES ="NStr";
            public static final String VA_REF = "vaRefStr";
            public static final String VM_REF = "vmRefStr";

            public static final String MEASURE="measure";

            public static final String OUT_BUS_NUM_OUT="ii2eoutList";
            public static final String BUS_NUM_OUT="ii2eList";
            public static final String BRANCH_IDS="bridsList";
            public static final String BAD_RECOG_THRESHOLD = "badRecogThresholdStr";

            public static final String OPTIONS_EST="optionsHash";
            public static final String VA_EST_HASH="vaEstHash";
            public static final String VM_EST_HASH="vmEstHash";
            public static final String VA_EST_BUFFER_HASH = "vaBufEstHash";
            public static final String VM_EST_BUFFER_HASH = "vmBufEstHash";

            public static final String STATE="state";
            public static final String STATE_CONVERGED="convergedBit";
            public static final String STATE_IT="stateItStr";
            public static final String STATE_IBADREG="stateIbadRegStr";
            public static final String STATE_VV = "stateVv";
            public static final String STATE_DELZ = "delz";
            public static final String STATE_ESTIMATED_ZONES = "estimatedNStr";
            public static final String STATE_BADRECOG_ZONES = "badRecogNStr";
            public static final String STATE_HH = "HH";
            public static final String STATE_WW = "WW";
            public static final String STATE_WWINV = "WWInv";
            public static final String STATE_DDELZ = "ddelz";

            public static final String ESTIMATING_BIT ="estimatingBit";
            public static final String LAST_EST_TIME = "estTime";
            public static final String EST_START_TIME = "estStart";

//            use this one to remember all data related to a case
            public static final String KEYS="keySet";

            public static final String READY = "readyBit";
        }
    }

    public abstract class STORM {
        public static final double factor = 2;

        public abstract class FIELDS {
            public static final String CASE_ID = "caseID";
            public static final String CASE_DATA = "caseData";
            public static final String OPTIONS_EST = "zoneBusNum";
            public static final String ZONE_DATA = "zoneData";
            public static final String ZONE_ID ="zoneId";
            public static final String ZONE_ID_LIST = "zoneList";

            public static final String DATA_CHANGED = "changed";

//            for measurement system
            public static final String MEASURE_TYPE="measureType";
            public static final String MEASURE_ID="measureId";
            public static final String MEASURE_VALUE ="measureData";

            //            for estimator
            public static final String EST_CONVERGED = "converged";

        }

        public abstract class STREAM {
            public static final String STREAM_OUTPUT = "output";
            public static final String STREAM_ESTIMATE = "estimate";
            public static final String STREAM_BAD_RECOG = "badRecog";
        }

        public abstract class COMPONENT {
            //            deprecated
            public static final String COMP_EST_DISPATCHER_SPOUT = "dispatcher";

            public static final String COMP_EST_TRIGGER_SPOUT = "trigger";
            public static final String COMP_EST_TRIGGER_CANDI_SPOUT = "triggerCandi";
            public static final String COMP_EST_FIRSTEST = "firstEstimation";
            public static final String COMP_EST_REDUCEMAT = "reduceMat";
            public static final String COMP_EST_ESTONCE = "estOnce";
            public static final String COMP_EST_CHECKCONV = "checkConv";
            public static final String COMP_EST_BADRECOG = "badRecog";
            public static final String COMP_EST_OUTPUTDIFF = "outputDiff";
            public static final String COMP_EST_CHECKAFTERBADRECOG = "checkingAfterBad";

            public static final String COMP_SPLIT_DATA_SOURCE_SPOUT = "caseDataSource";
            public static final String COMP_SPLIT_SPLITTER_BOLT = "splitter";
            public static final String COMP_SPLIT_SHOW_CASE_BOLT = "showCase";
            public static final String COMP_SPLIT_INITIALIZER_BOLT = "initializer";
            public static final String COMP_SPLIT_PREPARE_BOLT = "prepare";

            public static final String COMP_MEAS_DATA_SOURCE_SPOUT = "measureDataSource";
            public static final String COMP_MEAS_STORE_BOLT = "measureStore";

        }
    }

    public abstract class CONSTANTS{
        public static final String CASE_FILE_EXT=".txt";
    }

}
