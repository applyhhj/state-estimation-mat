package thu.instcloud.app.se.storm.splitter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.common.StormUtils;
import thu.instcloud.app.se.storm.initializer.InitializerRBolt;
import thu.instcloud.app.se.storm.initializer.PrepareRBolt;

/**
 * Created by hjh on 15-12-26.
 */
public class RunSplitter {

    public static void main(String[] args) throws Exception {
        String redisIp= StormUtils.REDIS.REDIS_SERVER_IP;
        String pass= StormUtils.REDIS.PASS;
        String debugcase = "case9241pegase";

        Config conf = new Config();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("caseSource", new CaseDataSpout(true, debugcase), 1);
        builder.setBolt("splitter", new SplitSystemRBolt(redisIp, pass), 1).shuffleGrouping("caseSource");
        builder.setBolt("showCase", new ShowCaseRBolt(redisIp, pass), 1).shuffleGrouping("splitter");

        builder.setBolt("distributer", new InitializerRBolt(redisIp, pass), 1).shuffleGrouping("splitter");
        builder.setBolt("prepare",new PrepareRBolt(redisIp,pass),2).shuffleGrouping("distributer");

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
