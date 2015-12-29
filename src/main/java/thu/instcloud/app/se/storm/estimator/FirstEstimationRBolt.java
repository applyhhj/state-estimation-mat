package thu.instcloud.app.se.storm.estimator;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.Map;

/**
 * Created by hjh on 15-12-29.
 */
public class FirstEstimationRBolt extends JedisRichBolt{
    String caseid;
    String zoneid;

    public FirstEstimationRBolt(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        super.declareOutputFields(outputFieldsDeclarer);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
    }

    @Override
    public void execute(Tuple tuple) {
        caseid=tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        zoneid=tuple.getStringByField(StormUtils.STORM.FIELDS.ZONE_ID);
        firstEstimate(caseid,zoneid);
        collector.ack(tuple);
    }

    private void firstEstimate(String caseid,String zoneid){

    }

}
