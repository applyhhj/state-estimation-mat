package thu.instcloud.app.se.storm.splitter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;

/**
 * Created by hjh on 15-12-26.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String redisIp= SplitterUtils.REDIS.REDIS_SERVER_IP;
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("caseSource", new CaseDataSpout(), 1);
        builder.setBolt("splitter", new SplitSystemBolt(redisIp), 3).shuffleGrouping("caseSource");
        builder.setBolt("showCase", new ShowCaseBolt(redisIp), 3).shuffleGrouping("splitter");

        Config conf = new Config();

        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        }
        else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("processCaseFile", conf, builder.createTopology());
            Utils.sleep(100000000);
            cluster.killTopology("processCaseFile");
            cluster.shutdown();
        }
    }
}
