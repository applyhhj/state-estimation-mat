package thu.instcloud.app.se.storm.measure;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import thu.instcloud.app.se.storm.common.JedisRichBolt;

import java.util.Map;

/**
 * Created by hjh on 15-12-28.
 */
public class MeasureRBolt extends JedisRichBolt {
    public MeasureRBolt(String reidsIp) {
        super(reidsIp);
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

    }
}
