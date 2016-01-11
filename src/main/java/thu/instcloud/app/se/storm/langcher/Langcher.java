package thu.instcloud.app.se.storm.langcher;

import thu.instcloud.app.se.storm.common.StormUtils;

/**
 * Created by hjh on 16-1-11.
 */
public class Langcher {
    public static void main(String[] args) throws Exception {
        String redisIp = StormUtils.REDIS.REDIS_SERVER_IP;
        String pass = StormUtils.REDIS.PASS;
        String debugcase = "case9241pegase";

        SETopologyLancher lancher = new SETopologyLancher(redisIp, pass, debugcase);

        lancher.composeTopology("estimator");
        lancher.runLocal();
    }
}
