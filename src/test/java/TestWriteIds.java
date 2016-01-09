import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
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
        String caseid = "case9241pegase";
        Response<List<String>> res;
        Response<List<String>> res1;
        List<Response<byte[]>> itsList = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(StormUtils.REDIS.PASS);
            Pipeline p=jedis.pipelined();

            List<Response<byte[]>> vvMatBytes = new ArrayList<>();
            for (int i = 1; i < 30; i++) {
                String zoneid = i + "";
                byte[] zoneDataKey = mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid);
                vvMatBytes.add(p.get(zoneDataKey));
            }
            p.sync();

            String[] fields = {"value"};
            MWStructArray vvs = new MWStructArray(29, 1, fields);
            for (int i = 1; i < 30; i++) {
//                MWStructArray e=(MWStructArray)MWStructArray.deserialize(vvMatBytes.get(i-1).get());
                vvs.set("value", i, (MWStructArray) MWStructArray.deserialize(vvMatBytes.get(i - 1).get()));
            }
            for (int i = 1; i < 30; i++) {
                MWStructArray e = (MWStructArray) vvs.getField("value", i);
                System.out.println(e);
            }

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
