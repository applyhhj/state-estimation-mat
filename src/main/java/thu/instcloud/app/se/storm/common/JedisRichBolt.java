package thu.instcloud.app.se.storm.common;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Created by hjh on 15-12-26.
 */
public class JedisRichBolt extends BaseRichBolt {
    protected static JedisPool jedisPool;
    protected String pass;
    protected OutputCollector collector;

    public JedisRichBolt(String redisIp,String pass){
        if (jedisPool==null){
            jedisPool = new JedisPool(new JedisPoolConfig(), redisIp);
        }
        this.pass=pass;
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
    protected void finalize() throws Throwable {
        super.finalize();
        jedisPool.destroy();
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return super.getComponentConfiguration();
    }

    protected void auth(Jedis jedis){
        if (pass!=null){
            jedis.auth(pass);
        }
    }
}
