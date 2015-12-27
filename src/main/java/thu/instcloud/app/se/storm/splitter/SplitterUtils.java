package thu.instcloud.app.se.storm.splitter;

import com.mathworks.toolbox.javabuilder.MWStructArray;

import java.util.List;

/**
 * Created by hjh on 15-12-26.
 */
public class SplitterUtils {

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

    public abstract class REDIS{

        public abstract class KEYS{
            public static final String RAW_DATA="rawData";

            public static final String BUS="bus";
            public static final String GEN="gen";
            public static final String BRANCH="branch";
            public static final String SBASE="sbase";

            public static final String ZONES="zones";
        }

        public static final String REDIS_SERVER_IP="10.0.0.1";
        public static final String PASS="redis";
    }

    public abstract class FIELDS{
        public static final String CASE_ID="caseID";
        public static final String CASE_DATA="caseData";
        public static final String CASE_ZONE_BN="zoneBusNum";
        public static final String ZONE_DATA="zoneData";

        public static final String OVERWRITE="overwrite";
        public static final String DATA_CHANGED="changed";

    }

    public abstract class CONSTANTS{
        public static final String CASE_FILE_EXT=".txt";
    }

    public static class MW{
        public static MWStructArray getArrayElement(MWStructArray array,int idx){
            MWStructArray res=new MWStructArray(1,1,array.fieldNames());
            for (String key:array.fieldNames()){
                res.set(key,1,array.get(key,idx));
            }
            return res;
        }

        public abstract class FIELDS{
            public static final String ZONE_NUM="num";
        }

    }

}
