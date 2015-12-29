import redis.clients.jedis.*;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;

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
            Response<String> nz = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES));
            p.sync();
            long nzLong = Long.parseLong(nz.get());
            for (int i = 1; i < nzLong; i++) {
                itsList.add(p.get(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, i + "")));
            }
            p.sync();
        }

        for (Response<byte[]> resp :
                itsList) {
            System.out.println(resp.get().length);
        }

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
