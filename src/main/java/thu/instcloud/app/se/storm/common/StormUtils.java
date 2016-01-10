package thu.instcloud.app.se.storm.common;

import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
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

    public static class MW {
        public static MWStructArray getArrayElement(MWStructArray array, int idx) {
            MWStructArray res = new MWStructArray(1, 1, array.fieldNames());
            for (String key : array.fieldNames()) {
                res.set(key, 1, array.get(key, idx));
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

    public abstract class REDIS{

        public static final String REDIS_SERVER_IP = "10.0.0.1";
        public static final String PASS = "redis";

        public abstract class KEYS{

            public static final String RAW_DATA="rawData";

            public static final String BUS="bus";
            public static final String GEN="gen";
            public static final String BRANCH="branch";
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
            public static final String ESTIMATE_TIME = "estTime";

//            use this one to remember all data related to a case
            public static final String KEYS="keySet";
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
            public static final String COMP_EST_DISPATCHER_SPOUT = "dispatcher";
            public static final String COMP_EST_FIRSTEST = "firstEstimation";
            public static final String COMP_EST_REDUCEMAT = "reduceMat";
            public static final String COMP_EST_ESTONCE = "estOnce";
            public static final String COMP_EST_CHECKCONV = "checkConv";
            public static final String COMP_EST_BADRECOG = "badRecog";
            public static final String COMP_EST_OUTPUTDIFF = "outputDiff";
            public static final String COMP_EST_CHECKAFTERBADRECOG = "checkingAfterBad";
        }
    }

    public abstract class CONSTANTS{
        public static final String CASE_FILE_EXT=".txt";
    }

    public abstract class MEASURE{

        public abstract class TYPE{
            public static final String PF="pf";
            public static final String PT="pt";
            public static final String PBUS="pbus";
            public static final String VA="va";
            public static final String VM="vm";
            public static final String QF="qf";
            public static final String QT="qt";
            public static final String QBUS="qbus";
        }

    }

}
