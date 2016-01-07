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

import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 16-1-8.
 * should be unique
 */
public class CheckAfterBadRecogRBolt extends JedisRichBolt {
    public CheckAfterBadRecogRBolt(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(StormUtils.STORM.STREAM.STREAM_OUTPUT, new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.EST_CONVERGED
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
        String caesid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        checkCondition(caesid);
        collector.ack(tuple);

    }


    private void checkCondition(String caseid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();

            Response<String> nbadRecog = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
            Response<String> nz = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES));
            Response<String> currBadItResp = p.get(mkKey(caseid, "1", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));
            Response<String> maxBadItResp = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_MAX_BAD_REG_IT);
            Response<Long> nConver = p.bitcount(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED));
            p.sync();

//        debug
            System.out.println("currIbad: " + currBadItResp.get());

            long nzLong = Long.parseLong(nz.get());
//        all zones have checked bad data
            if (nzLong == Long.parseLong(nbadRecog.get())) {
//            reset checked zone number, sync later
                p.set(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES), "1");
//                all converged
                if (nConver.get() == nzLong) {
                    resetState(caseid, p, true);
                } else if (Long.parseLong(currBadItResp.get()) >= Long.parseLong(maxBadItResp.get())) {
                    resetState(caseid, p, false);
                } else {
//                further estimation
                    p.del(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED));
                    p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), 0, true);

                    p.set(mkKey(caseid, "1", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT), "0");
                    p.sync();

//                    redispatch zone for further estimation
                    for (int i = 1; i < nzLong; i++) {
                        collector.emit(StormUtils.STORM.STREAM.STREAM_ESTIMATE,
                                new Values(caseid, i + ""));
                    }
                }
            }
        }
    }

    private void resetState(String caseid, Pipeline p, boolean converged) {
        String converKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
//                reset converge states
        p.del(converKey);
        p.setbit(converKey, 0, true);
//                finished estimation
        p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATING_BIT), 0, false);
        p.sync();

//              not converged and reached max bad it number that means estimation failed
        collector.emit(StormUtils.STORM.STREAM.STREAM_OUTPUT, new Values(caseid, converged));
    }
}
