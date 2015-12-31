package thu.instcloud.app.se.storm.measure;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.MeasureData;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hjh on 15-12-28.
 */
public class StoreMeasureRBolt extends JedisRichBolt {

    private CopyOnWriteArrayList<MeasureData> measures;
    private int cacheNum = 50;
    private long cacheDurationMil = 500;
    private ScheduledExecutorService flushDataService;
//    private Jedis jedis;
//    private Pipeline p;

    public StoreMeasureRBolt(String reidsIp, String pass) {
        super(reidsIp,pass);
    }

    public StoreMeasureRBolt(String reidsIp, String pass, int cacheNum, long cacheDurationMil) {
        super(reidsIp,pass);
        this.cacheNum = cacheNum;
        this.cacheDurationMil = cacheDurationMil;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        super.declareOutputFields(outputFieldsDeclarer);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
        measures = new CopyOnWriteArrayList<>();

//        jedis is called frequently, so we create a long term instance
//        jedis = jedisPool.getResource();
//        jedis.auth(StormUtils.REDIS.PASS);
//        p = jedis.pipelined();

        flushDataService = Executors.newScheduledThreadPool(1);
        flushDataService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                refreshMeasurements();
            }
        }, 0,cacheDurationMil, TimeUnit.MILLISECONDS);
    }

    @Override
    public void execute(Tuple tuple) {
        addMeasure(tuple);
        collector.ack(tuple);
    }

    private void addMeasure(Tuple tuple) {
        measures.add(new MeasureData(tuple));
        if (measures.size() >= cacheNum) {
            refreshMeasurements();
        }
    }

    private void refreshMeasurements() {
        if (measures.size() > 0) {
            try (Jedis jedis = jedisPool.getResource()) {
                auth(jedis);
                Pipeline p = jedis.pipelined();

                for (MeasureData measureData : measures) {
                    p.hset(measureData.getKey(),
                            measureData.getHashKey(),
                            measureData.getHashValue());
                }
                p.sync();
            }
            measures.clear();
        }

    }
}
