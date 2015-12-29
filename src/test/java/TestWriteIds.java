import redis.clients.jedis.*;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.List;

/**
 * Created by hjh on 15-12-28.
 */
public class TestWriteIds {

    public static void main(String[] args) {
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), StormUtils.REDIS.REDIS_SERVER_IP);
        String testkey="case2869pegase.keys";
        Response<List<String>> res;
        Response<List<String>> res1;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(StormUtils.REDIS.PASS);
            Pipeline p=jedis.pipelined();

//            byte[] val=jedis.get(testkey.getBytes());
//            MWStructArray zones= (MWStructArray) MWStructArray.deserialize(val);
//            double[][] iie2out=((double[][]) zones.get(StormUtils.MW.FIELDS.OUT_BUS_NUM_OUT,1));
//            String[] ii2eoutStrArr=toStringArray(iie2out);
//
//            String lii2eoutkey=testkey+".ii2eout";
//            jedis.del(lii2eoutkey);
//            System.out.printf("%d\n",ii2eoutStrArr.length);
//            jedis.lpush(lii2eoutkey,ii2eoutStrArr);
//            List<String> ret=jedis.lrange(lii2eoutkey,0,-1);
//            System.out.printf("%d\n",ret.size());
//            jedis.del(lii2eoutkey);

//            Set<String> keys=jedis.smembers(testkey);
//            Set<String> allkeys=jedis.keys("*");
//            System.out.printf("%d   %d\n",keys.size(),allkeys.size());
//            for (String str:allkeys){
//                if (keys.contains(str)){
//                    keys.remove(str);
//                }
//            }
//            for (String str:keys){
//                System.out.println(str);
//            }
            res=p.lrange("test",0,-1);
            res1=p.lrange("case2869pegase.zones.1.ii2eList",0,-1);
            p.sync();
        }

        List<String> resStr=res.get();
        List<String> resStr1=res1.get();
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
