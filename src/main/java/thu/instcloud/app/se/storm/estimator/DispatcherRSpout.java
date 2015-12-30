package thu.instcloud.app.se.storm.estimator;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import thu.instcloud.app.se.storm.common.JedisRichSpout;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-29.
 */
public class DispatcherRSpout extends JedisRichSpout {
    private String caseid;

    public DispatcherRSpout(String redisIp, String pass) {
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
        caseid="case2869pegase";
    }

    @Override
    public void nextTuple() {
        emitZones(caseid);
        Utils.sleep(10000000);
    }

    private void emitZones(String caseid){
        int nz;
        try (Jedis jedis=jedisPool.getResource()){
            auth(jedis);

//            initialize state variables for estimation
//            whether the case is being estimated
            String estimatingKey=mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATING_BIT);
            if (jedis.getbit(estimatingKey,0)){
//                TODO: stop current estimate launch new estimation, and reset
//                we are estimating this case, refuse another estimation on the same case
                return;
            }else {
                jedis.setbit(estimatingKey,0,true);
            }

            nz=Integer.parseInt(jedis.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES)));
            Pipeline p=jedis.pipelined();

//            converge state of each zone
            String convergedKey=mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
//            reference zone is always converged
            p.setbit(convergedKey,0,true);

//            how many zones have already been estimated
            String estimatedKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES);
//            always remember to exclude reference zone
            p.set(estimatedKey, "1");

//            how many zones have already checked bad data
            String badrecogZnsKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES);
//            always remember to exclude reference zone
            p.set(badrecogZnsKey, "1");

            String keysRec = mkKey(caseid, StormUtils.REDIS.KEYS.KEYS);
            p.sadd(keysRec,
                    convergedKey,
                    estimatingKey,
                    estimatedKey
            );
//            ignore reference zone
            for (int i = 1; i < nz; i++) {
                String zoneItKey=mkKey(caseid, i+"", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT);
//                TODO: consider use one key to record ibad
                String zoneIbadtKey=mkKey(caseid,i+"", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG);
                String vvKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
                String delzKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ);

                //            keys for some state generated later
                String HHkey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_HH);
                String WWKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_WW);
                String WWInvKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_WWINV);
                String ddelzKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_DDELZ);

                p.set(zoneItKey,"0");
                p.set(zoneIbadtKey,"0");
                p.sadd(keysRec,
                        zoneItKey,
                        zoneIbadtKey,
                        vvKey,
                        delzKey,
                        HHkey,
                        WWKey,
                        WWInvKey,
                        ddelzKey);
            }

            p.sync();

        }


//        we ignore the zone with only reference bus
        for (int i = 1; i < nz; i++) {
            collector.emit(new Values(caseid, i + ""));
        }
    }

}