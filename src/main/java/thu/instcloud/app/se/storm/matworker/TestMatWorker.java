package thu.instcloud.app.se.storm.matworker;

import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjh on 16-1-8.
 */
public class TestMatWorker {

    public static void main(String[] args) throws Exception {
        MatWorker matWorker = new MatWorker(
                StormUtils.REDIS.REDIS_SERVER_IP,
                StormUtils.REDIS.PASS,
                "localhost",
                9091
        );
        String caseid = "case9241pegase";
        List<String> zoneid = new ArrayList<>();
        String task = "test";
        zoneid.add("1");
        System.out.println(matWorker.perform(caseid, zoneid, task));

        matWorker.stop();
    }


}
