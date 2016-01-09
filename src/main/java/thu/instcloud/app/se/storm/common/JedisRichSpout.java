package thu.instcloud.app.se.storm.common;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import com.esotericsoftware.kryo.Kryo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Created by hjh on 15-12-28.
 */
public class JedisRichSpout extends BaseRichSpout {
    protected static JedisPool jedisPool;
    protected String pass;
    protected SpoutOutputCollector collector;

    public JedisRichSpout(String redisIp, String pass) {
        if (jedisPool == null) {
            jedisPool = new JedisPool(new JedisPoolConfig(), redisIp);
        }
        this.pass = pass;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        collector=spoutOutputCollector;
    }

    @Override
    public void nextTuple() {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return super.getComponentConfiguration();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        jedisPool.destroy();
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @Override
    public void ack(Object msgId) {
        super.ack(msgId);
    }

    @Override
    public void fail(Object msgId) {
        super.fail(msgId);
    }

    protected void auth(Jedis jedis){
        if (pass!=null){
            jedis.auth(pass);
        }
    }
}
