package thu.instcloud.app.se.storm.estimator;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.common.StormUtils;

/**
 * Created by hjh on 15-12-30.
 */
public class RunEstimator {

    public static void main(String[] args) throws Exception {
        String redisIp = StormUtils.REDIS.REDIS_SERVER_IP;
        String pass = StormUtils.REDIS.PASS;
        boolean debug = true;
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout(StormUtils.STORM.COMPONENT.COMP_EST_DISPATCHER_SPOUT, new DispatcherRSpout(redisIp, pass, debug), 1);
        builder.setBolt(StormUtils.STORM.COMPONENT.COMP_EST_FIRSTEST, new FirstEstimationRBolt(redisIp, pass), 1)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_DISPATCHER_SPOUT);
        builder.setBolt(StormUtils.STORM.COMPONENT.COMP_EST_REDUCEMAT, new ReduceMatrixRBolt(redisIp, pass), 1)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_FIRSTEST, StormUtils.STORM.STREAM.STREAM_ESTIMATE)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_BADRECOG, StormUtils.STORM.STREAM.STREAM_ESTIMATE);
        builder.setBolt(StormUtils.STORM.COMPONENT.COMP_EST_ESTONCE, new EstimateOnceRBolt(redisIp, pass), 1)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_REDUCEMAT)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_CHECKCONV, StormUtils.STORM.STREAM.STREAM_ESTIMATE);
        builder.setBolt(StormUtils.STORM.COMPONENT.COMP_EST_CHECKCONV, new CheckConvergeRBolt(redisIp, pass), 1)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_ESTONCE);
        builder.setBolt(StormUtils.STORM.COMPONENT.COMP_EST_BADRECOG, new BadDataRecognitionRBolt(redisIp, pass), 1)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_CHECKCONV, StormUtils.STORM.STREAM.STREAM_BAD_RECOG);
        builder.setBolt(StormUtils.STORM.COMPONENT.COMP_EST_OUTPUTDIFF, new OutputDiffBolt(redisIp, pass), 1)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_BADRECOG, StormUtils.STORM.STREAM.STREAM_OUTPUT)
                .shuffleGrouping(StormUtils.STORM.COMPONENT.COMP_EST_FIRSTEST, StormUtils.STORM.STREAM.STREAM_OUTPUT);

        Config conf = new Config();

        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("estimator", conf, builder.createTopology());
            Utils.sleep(500000000);
            cluster.killTopology("estimator");
            cluster.shutdown();
        }
    }

}
