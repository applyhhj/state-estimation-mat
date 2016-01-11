package thu.instcloud.app.se.storm.estimator;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import redis.clients.jedis.Jedis;
import thu.instcloud.app.se.storm.common.JedisRichSpout;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.*;

/**
 * Created by hjh on 16-1-11.
 */
public class TriggerForCandidateRSpout extends JedisRichSpout {
    public TriggerForCandidateRSpout(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.ZONE_ID
        ));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        super.open(map, topologyContext, spoutOutputCollector);
    }

    @Override
    public void nextTuple() {
        emitCandidate();
        Utils.sleep(1000);
    }

    private void emitCandidate() {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);

            Map<String, String> candiCases = jedis.hgetAll(StormUtils.REDIS.KEYS.CASES_WAITING_FOR_EST);

            for (Map.Entry<String, String> e : candiCases.entrySet()) {
                String caseid = e.getKey();
                boolean resetVEst = Boolean.parseBoolean(e.getValue());

                if (checkCaseState(jedis, caseid)) {
                    if (resetVEst) {
                        resetNonRefBustEstState(jedis, caseid);
                    }
//            evaluate estimation elapse time
                    String estStartTimeKey = mkKey(caseid, StormUtils.REDIS.KEYS.EST_START_TIME);
                    jedis.set(estStartTimeKey, System.currentTimeMillis() + "");

                    String nzKey = mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES);
                    int nz = Integer.parseInt(jedis.get(nzKey));

//        start estimation for each zone, we ignore the zone(zone 0) with only reference bus
                    for (int i = 1; i < nz; i++) {
                        collector.emit(new Values(caseid, i + ""));
                    }

                    jedis.hdel(StormUtils.REDIS.KEYS.CASES_WAITING_FOR_EST, caseid);
                }

            }

        }
    }
}
