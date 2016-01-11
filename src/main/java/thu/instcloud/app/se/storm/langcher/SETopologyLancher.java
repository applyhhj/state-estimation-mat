package thu.instcloud.app.se.storm.langcher;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.common.StormUtils.STORM.COMPONENT;
import thu.instcloud.app.se.storm.common.StormUtils.STORM.STREAM;
import thu.instcloud.app.se.storm.estimator.*;
import thu.instcloud.app.se.storm.initializer.InitializerRBolt;
import thu.instcloud.app.se.storm.initializer.PrepareRBolt;
import thu.instcloud.app.se.storm.measure.MeasureRSpout;
import thu.instcloud.app.se.storm.measure.StoreMeasureRBolt;
import thu.instcloud.app.se.storm.splitter.CaseDataSpout;
import thu.instcloud.app.se.storm.splitter.ShowCaseRBolt;
import thu.instcloud.app.se.storm.splitter.SplitSystemRBolt;

/**
 * Created by hjh on 16-1-11.
 */
public class SETopologyLancher {
    private Config config;
    private TopologyBuilder builder;
    private LocalCluster cluster;
    private String topologyName;
    private long runningTime;

    private String redisip;
    private String pass;
    private String caseid;

    private int paraEst;
    private int paraBad;

    public SETopologyLancher(String redisIp, String pass, String caseid) {
        this.redisip = redisIp;
        this.pass = pass;
        this.caseid = caseid;

        this.paraEst = 4;
        this.paraBad = 4;

        this.builder = new TopologyBuilder();
        this.config = new Config();
        this.runningTime = 1800;
    }

    public void composeTopology(String topName) {
        this.topologyName = topName;
//        system split part
        builder.setSpout(COMPONENT.COMP_SPLIT_DATA_SOURCE_SPOUT, new CaseDataSpout(true, caseid), 1);
        builder.setBolt(COMPONENT.COMP_SPLIT_SPLITTER_BOLT, new SplitSystemRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_SPLIT_DATA_SOURCE_SPOUT);
        builder.setBolt(COMPONENT.COMP_SPLIT_INITIALIZER_BOLT, new InitializerRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_SPLIT_SPLITTER_BOLT);
        builder.setBolt(COMPONENT.COMP_SPLIT_PREPARE_BOLT, new PrepareRBolt(redisip, pass), 1);
        builder.setBolt(COMPONENT.COMP_SPLIT_SHOW_CASE_BOLT, new ShowCaseRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_SPLIT_SPLITTER_BOLT);

//        measurement system
        builder.setSpout(COMPONENT.COMP_MEAS_DATA_SOURCE_SPOUT, new MeasureRSpout(redisip, pass, caseid), 1);
        builder.setBolt(COMPONENT.COMP_MEAS_STORE_BOLT, new StoreMeasureRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_MEAS_DATA_SOURCE_SPOUT);

//        estimator part
        builder.setSpout(COMPONENT.COMP_EST_TRIGGER_SPOUT, new TriggerRSpout(redisip, pass, caseid, true), 1);
        builder.setSpout(COMPONENT.COMP_EST_TRIGGER_CANDI_SPOUT, new TriggerForCandidateRSpout(redisip, pass), 1);
        builder.setBolt(COMPONENT.COMP_EST_FIRSTEST, new FirstEstimationRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_EST_TRIGGER_SPOUT)
                .shuffleGrouping(COMPONENT.COMP_EST_TRIGGER_CANDI_SPOUT);
        builder.setBolt(COMPONENT.COMP_EST_REDUCEMAT, new ReduceMatrixRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_EST_FIRSTEST, STREAM.STREAM_ESTIMATE)
                .shuffleGrouping(COMPONENT.COMP_EST_CHECKAFTERBADRECOG, STREAM.STREAM_ESTIMATE);
        builder.setBolt(COMPONENT.COMP_EST_ESTONCE, new EstimateOnceRBolt(redisip, pass), paraEst)
                .shuffleGrouping(COMPONENT.COMP_EST_REDUCEMAT)
                .shuffleGrouping(COMPONENT.COMP_EST_CHECKCONV, STREAM.STREAM_ESTIMATE);
        builder.setBolt(COMPONENT.COMP_EST_CHECKCONV, new CheckConvergeRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_EST_ESTONCE);
        builder.setBolt(COMPONENT.COMP_EST_BADRECOG, new BadDataRecognitionRBolt(redisip, pass), paraBad)
                .shuffleGrouping(COMPONENT.COMP_EST_CHECKCONV, STREAM.STREAM_BAD_RECOG);
        builder.setBolt(COMPONENT.COMP_EST_CHECKAFTERBADRECOG, new CheckAfterBadRecogRBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_EST_BADRECOG);
        builder.setBolt(COMPONENT.COMP_EST_OUTPUTDIFF, new OutputDiffBolt(redisip, pass), 1)
                .shuffleGrouping(COMPONENT.COMP_EST_CHECKAFTERBADRECOG, STREAM.STREAM_OUTPUT)
                .shuffleGrouping(COMPONENT.COMP_EST_FIRSTEST, STREAM.STREAM_OUTPUT);
    }

    public void runLocal() {
        if (cluster == null) {
            cluster = new LocalCluster();
        }

        if (topologyName == null) {
            topologyName = "default";
        }
        cluster.submitTopology(topologyName, config, builder.createTopology());
        Utils.sleep(runningTime * 1000);
        cluster.killTopology(topologyName);
        cluster.shutdown();
    }

    public void runInCluster(int nWorkers, String[] args) throws Exception {
        if (args != null && args.length > 0) {
            config.setNumWorkers(nWorkers);
            StormSubmitter.submitTopologyWithProgressBar(args[0], config, builder.createTopology());
        }
    }
}
