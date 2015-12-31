import redis.clients.jedis.*;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-28.
 */
public class TestWriteIds {

    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), StormUtils.REDIS.REDIS_SERVER_IP);
        String caseid = "case2869pegase";
        Response<List<String>> res;
        Response<List<String>> res1;
        List<Response<byte[]>> itsList = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(StormUtils.REDIS.PASS);
            Pipeline p=jedis.pipelined();
//            Response<String> nz = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES));
//            p.sync();
//            long nzLong = Long.parseLong(nz.get());
//            for (int i = 1; i < nzLong; i++) {
//                itsList.add(p.get(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, i + "")));
//            }
//            p.sync();

//            String converKey = mkKey(caseid, "1", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT);
//            Response<Map<String,String>> VaEst = p.hgetAll(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH));
//            Response<Map<String,String>> VmEst = p.hgetAll(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH));
////            p.del(testKey);
////            p.setbit(testKey,3,true);
////            p.setbit(testKey,1,true);
//            p.sync();
//            for (Map.Entry<String,String> e:VaEst.get().entrySet()) {
//                System.out.printf("\n%8s\t%15s\t%15s",e.getKey(),e.getValue(),VmEst.get().get(e.getKey()));
//            }
            boolean hasit = jedis.exists(mkKey(caseid, "1", StormUtils.REDIS.KEYS.STATE_IBADREG));
            Response<String> currBadItResp = p.get(mkKey(caseid, "1", StormUtils.REDIS.KEYS.STATE_IBADREG));
            p.sync();
            System.out.print(currBadItResp.get());

        }

//        for (Response<byte[]> resp :
//                itsList) {
//            System.out.println(resp.get().length);
//        }

        jedisPool.destroy();
    }

    private static String[] toStringArray(double[][] data){
        String[] res=new String[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i]=String.valueOf((int)data[i][0]);
        }

        return res;
    }
}
