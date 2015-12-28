package thu.instcloud.app.se.storm.measure;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.initializer.DistrbuteZoneRBolt;
import thu.instcloud.app.se.storm.initializer.PrepareRBolt;
import thu.instcloud.app.se.storm.splitter.CaseDataSpout;
import thu.instcloud.app.se.storm.splitter.SplitSystemRBolt;
import thu.instcloud.app.se.storm.splitter.SplitterUtils;

/**
 * Created by hjh on 15-12-28.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String redisIp= SplitterUtils.REDIS.REDIS_SERVER_IP;
        String pass= SplitterUtils.REDIS.PASS;
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("measureSource", new MeasurementRSpout(redisIp,pass), 1);
        builder.setBolt("measure", new MeasureRBolt(redisIp,pass), 3).shuffleGrouping("measureSource");

        Config conf = new Config();

        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        }
        else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("measureSystem", conf, builder.createTopology());
            Utils.sleep(100000000);
            cluster.killTopology("measureSystem");
            cluster.shutdown();
        }
    }
}
