package thu.instcloud.app.se.storm.matworker;

import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hjh on 16-1-8.
 */
public class TestMatWorker {

    public static void main(String[] args) throws Exception {
        List<MatWorker> matWorkers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            matWorkers.add(new MatWorker(
                    StormUtils.REDIS.REDIS_SERVER_IP,
                    StormUtils.REDIS.PASS,
                    "localhost",
                    9091 + i
            ));
        }

        String caseid = "case9241pegase";
        List<String> zoneid = new ArrayList<>();
        String task = "test";
        zoneid.add("1");
        int i = 0;
        MatWorker matWorker = matWorkers.get(0);
        while (i++ < 100) {
            System.out.println(matWorker.perform(caseid, zoneid, task));
            Thread.sleep(2000);
        }

        for (int j = 0; j < 8; j++) {
            matWorkers.get(j).stop();
        }

    }


}
