package thu.instcloud.app.se.storm.measure;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.common.StormUtils;

/**
 * Created by hjh on 15-12-28.
 */
public class RunMeasure {

    public static void main(String[] args) throws Exception {
        String redisIp= StormUtils.REDIS.REDIS_SERVER_IP;
        String pass= StormUtils.REDIS.PASS;
        String caseid = "case9241pegase";
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("measureSource", new MeasureRSpout(redisIp, pass, caseid), 1);
        builder.setBolt("measure", new StoreMeasureRBolt(redisIp, pass), 3).shuffleGrouping("measureSource");

        Config conf = new Config();

        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        }
        else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("measureSystem", conf, builder.createTopology());
            Utils.sleep(500000000);
            cluster.killTopology("measureSystem");
            cluster.shutdown();
        }
    }
}
