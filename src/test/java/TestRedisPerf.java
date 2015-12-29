import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;

import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-27.
 */
public class TestRedisPerf {
    static JedisPool jedisPool;
    public static void main(String[] args) {
        int N=500000;
        int Nsmall=1000;
        jedisPool = new JedisPool(new JedisPoolConfig(), StormUtils.REDIS.REDIS_SERVER_IP);
        String testkey=mkKey("test","redis");

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(StormUtils.REDIS.PASS);
            jedis.flushAll();
            List<String> keyvals=new ArrayList<>();
            List<String> keys=new ArrayList<>();

            for (int i = 0; i < N; i++) {
                keyvals.add(mkKey(testkey,i+""));
                keyvals.add(i+"");
                keys.add(mkKey(testkey,i+""));
            }
            String[] keyvalsarr= keyvals.toArray(new String[keyvals.size()]);
            long start=System.currentTimeMillis();
            jedis.mset(keyvalsarr);
            System.out.println("MSET: "+(System.currentTimeMillis()-start));

            String[] keysarr= keys.toArray(new String[keys.size()]);
            start=System.currentTimeMillis();
            List<String> mgetres=jedis.mget(keysarr);
            System.out.println("MGET: "+(System.currentTimeMillis()-start));

            start=System.currentTimeMillis();
            String getOnBig=jedis.get(mkKey(testkey,N/2+""));
            System.out.println("GET ON BIG: "+(System.currentTimeMillis()-start));

            jedis.flushAll();
            keyvals.clear();
            keys.clear();

            for (int i = 0; i < Nsmall; i++) {
                keyvals.add(mkKey(testkey,i+""));
                keyvals.add(i+"");
                keys.add(mkKey(testkey,i+""));
            }
            keyvalsarr= keyvals.toArray(new String[keyvals.size()]);
            start=System.currentTimeMillis();
            jedis.mset(keyvalsarr);
            System.out.println("MSET SMALL: "+(System.currentTimeMillis()-start));

            start=System.currentTimeMillis();
            String getOnSmall=jedis.get(mkKey(testkey,Nsmall/2+""));
            System.out.println("GET ON SMALL: "+(System.currentTimeMillis()-start));


        }

        jedisPool.destroy();
    }
}
