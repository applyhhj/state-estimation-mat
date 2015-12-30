package thu.instcloud.app.se.storm.estimator;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-30.
 */
public class CheckConvergeRBolt extends JedisRichBolt {
    public CheckConvergeRBolt(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(StormUtils.STORM.STREAM.STREAM_BAD_RECOG, new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.ZONE_ID
        ));

        outputFieldsDeclarer.declareStream(StormUtils.STORM.STREAM.STREAM_ESTIMATE, new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.ZONE_ID
        ));
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
    }

    @Override
    public void execute(Tuple tuple) {
        String caseid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        checkConverge(caseid);
        collector.ack(tuple);
    }

    private void checkConverge(String caseid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();

            Response<String> nz = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES));
            Response<String> estimatedZones = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES));
            p.sync();

            long nzLong = Long.parseLong(nz.get());

//            all zones are estimated
            if (nzLong == Long.parseLong(estimatedZones.get())) {
                updateEstimation(caseid, p);
                p.set(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES), "1");

//                check convergence
                String converKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
                Response<Long> nConver = p.bitcount(converKey);
                Response<String> maxItResp = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_MAX_EST_IT);
                Response<String> currItResp = p.get(mkKey(caseid, "1", StormUtils.REDIS.KEYS.STATE_IT));
                p.sync();

//                all converged or reach max iteration number prepare for bad data recognition
                if (nConver.get() == nzLong || Long.parseLong(currItResp.get()) >= Long.parseLong(maxItResp.get())) {
                    for (int i = 1; i < nzLong; i++) {
                        collector.emit(StormUtils.STORM.STREAM.STREAM_BAD_RECOG, new Values(caseid, i + ""));
                    }
                    return;
                } else {
//                    next iteration
                    for (int i = 1; i < nzLong; i++) {
                        collector.emit(StormUtils.STORM.STREAM.STREAM_ESTIMATE, new Values(caseid, i + ""));
                    }
                    return;
                }

            }

        }
    }

    //    can we do it just on the redis server side??
    private void updateEstimation(String caseid, Pipeline p) {
        Response<Map<String, String>> vmbuff = p.hgetAll(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_BUFFER_HASH));
        Response<Map<String, String>> vabuff = p.hgetAll(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_BUFFER_HASH));
//        clear buffer
        p.del(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_BUFFER_HASH));
        p.del(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_BUFFER_HASH));
        p.sync();

        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), vmbuff.get());
        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), vabuff.get());
        p.sync();
    }

}