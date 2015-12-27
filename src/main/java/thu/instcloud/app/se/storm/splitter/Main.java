package thu.instcloud.app.se.storm.splitter;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.initializer.DistrbuteZoneRBolt;
import thu.instcloud.app.se.storm.initializer.PrepareRBolt;

/**
 * Created by hjh on 15-12-26.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String redisIp= SplitterUtils.REDIS.REDIS_SERVER_IP;
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("caseSource", new CaseDataSpout(), 1);
        builder.setBolt("splitter", new SplitSystemRBolt(redisIp), 3).shuffleGrouping("caseSource");
        builder.setBolt("distributer",new DistrbuteZoneRBolt(redisIp),1).shuffleGrouping("splitter");
        builder.setBolt("prepare",new PrepareRBolt(redisIp),2).shuffleGrouping("distributer");
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
