package thu.instcloud.app.se.storm.splitter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.common.StormUtils;
import thu.instcloud.app.se.storm.initializer.DistributeZoneRBolt;
import thu.instcloud.app.se.storm.initializer.PrepareRBolt;

/**
 * Created by hjh on 15-12-26.
 */
public class RunSplitter {

    public static void main(String[] args) throws Exception {
        String redisIp= StormUtils.REDIS.REDIS_SERVER_IP;
        String pass= StormUtils.REDIS.PASS;
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("caseSource", new CaseDataSpout(true), 1);
        builder.setBolt("splitter", new SplitSystemRBolt(redisIp,pass), 3).shuffleGrouping("caseSource");

        builder.setBolt("distributer",new DistributeZoneRBolt(redisIp,pass),1).shuffleGrouping("splitter");
        builder.setBolt("prepare",new PrepareRBolt(redisIp,pass),2).shuffleGrouping("distributer");
//        builder.setBolt("showCase", new ShowCaseRBolt(redisIp), 3).shuffleGrouping("splitter");

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
