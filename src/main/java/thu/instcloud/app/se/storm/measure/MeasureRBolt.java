package thu.instcloud.app.se.storm.measure;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.splitter.SplitterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkKey;

/**
 * Created by hjh on 15-12-28.
 */
public class MeasureRBolt extends JedisRichBolt {

    private CopyOnWriteArrayList<MeasureData> measures;
    private int cacheNum = 50;
    private long cacheDurationMil = 500;
    private ScheduledExecutorService flushDataService;
    private Jedis jedis;
    private Pipeline p;

    private class MeasureData {
        private String caseid;
        private String mtype;
        private int mid;
        private double mdata;

        //        TODO: check data validation???
        public MeasureData(Tuple tuple) {
            caseid = tuple.getStringByField(SplitterUtils.STORM.FIELDS.CASE_ID);
            mtype = tuple.getStringByField(SplitterUtils.STORM.FIELDS.MEASURE_TYPE);
            mid = tuple.getIntegerByField(SplitterUtils.STORM.FIELDS.MEASURE_ID);
            mdata = tuple.getDoubleByField(SplitterUtils.STORM.FIELDS.MEASURE_DATA);
        }

        public String getKey() {
            return mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, mtype);
        }

        public String getHashKey() {
            return String.valueOf(mid);
        }

        public String getHashValue() {
            return String.valueOf(mdata);
        }
    }

    public MeasureRBolt(String reidsIp) {
        super(reidsIp);
    }

    public MeasureRBolt(String reidsIp, int cacheNum, long cacheDurationMil) {
        super(reidsIp);
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
        if (jedis != null) {
            jedis.close();
        }
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
        measures = new CopyOnWriteArrayList<>();

//        jedis is called frequently, so we create a long term instance
        jedis = jedisPool.getResource();
        jedis.auth(SplitterUtils.REDIS.PASS);
        p = jedis.pipelined();

        flushDataService = Executors.newScheduledThreadPool(1);
        flushDataService.schedule(new Runnable() {
            public void run() {
                refreshMeasurements();
            }
        }, cacheDurationMil, TimeUnit.MILLISECONDS);
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

    private synchronized void refreshMeasurements() {
        if (measures.size() > 0) {
            for (MeasureData measureData : measures) {
                p.hset(measureData.getKey(),
                        measureData.getHashKey(),
                        measureData.getHashValue());
            }
            p.sync();
            measures.clear();
        }

    }
}
