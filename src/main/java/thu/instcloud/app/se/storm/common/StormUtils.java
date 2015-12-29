package thu.instcloud.app.se.storm.common;

import com.mathworks.toolbox.javabuilder.*;

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
        }

    }

    public static class OPTIONS {
        public static Map<String, String> getDefualtOptions() {
            Map<String, String> res = new HashMap<>();
            res.put(KEYS.OPT_NBUS_ZONE, "300");
            res.put(KEYS.OPT_EST_TOL, "1e-8");
            res.put(KEYS.OPT_MAX_BAD_REG_IT, "50");
            res.put(KEYS.OPT_MAX_EST_IT, "10");
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

            public static final String MEASURE="measure";

            public static final String OUT_BUS_NUM_OUT="ii2eoutList";
            public static final String BUS_NUM_OUT="ii2eList";
            public static final String BRANCH_IDS="bridsList";

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
            public static final String STATE_HH = "HH";
            public static final String STATE_WW = "WW";
            public static final String STATE_WWINV = "WWInv";
            public static final String STATE_DDELZ = "ddelz";

            public static final String ESTIMATING_BIT ="estimatingBit";

//            use this one to remember all data related to a case
            public static final String KEYS="keySet";
        }
    }

    public abstract class STORM {
        public abstract class FIELDS {
            public static final String CASE_ID = "caseID";
            public static final String CASE_DATA = "caseData";
            public static final String OPTIONS_EST = "zoneBusNum";
            public static final String ZONE_DATA = "zoneData";
            public static final String ZONE_ID ="zoneId";

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
