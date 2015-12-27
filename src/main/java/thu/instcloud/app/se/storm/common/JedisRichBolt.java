package thu.instcloud.app.se.storm.common;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Created by hjh on 15-12-26.
 */
public class JedisRichBolt extends BaseRichBolt {
    protected static JedisPool jedisPool;
    protected OutputCollector collector;

    public JedisRichBolt(String reidsIp){
        if (jedisPool==null){
            jedisPool = new JedisPool(new JedisPoolConfig(), reidsIp);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector=outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {

    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return super.getComponentConfiguration();
    }
}
