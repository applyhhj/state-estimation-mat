package thu.instcloud.app.se.storm.estimator;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.storm.common.JedisRichSpout;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-29.
 */
public class DispatcherRSpout extends JedisRichSpout {
    private String caseid;
    private boolean resetVEst;
    private boolean debug;

    public DispatcherRSpout(String redisIp, String pass, boolean debug) {
        super(redisIp, pass);
        this.debug = debug;
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
        if (debug) {
            caseid = "case2869pegase";
            resetVEst = true;
        }
        emitZones(caseid, resetVEst);
        Utils.sleep(10000000);
    }

    private void emitZones(String caseid, boolean resetVEst) {
        int nz;
        try (Jedis jedis=jedisPool.getResource()){
            auth(jedis);

//            initialize state variables for estimation

//            whether the case is being estimated
            String estimatingKey=mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATING_BIT);
            if (debug) {
                jedis.setbit(estimatingKey, 0, false);
            }
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
//                TODO: consider use one key to record iit
                String zoneItKey=mkKey(caseid, i+"", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT);
//                TODO: consider use one key to record ibad
                String zoneIbadtKey=mkKey(caseid,i+"", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG);
                String vvKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
                String delzKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ);

                //            keys for some state generated later
                String HKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_H);
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
                        HKey,
                        HHkey,
                        WWKey,
                        WWInvKey,
                        ddelzKey);
            }

            p.sync();

//          we set estimated voltage of reference bus to the value of the computed power flow and in the estimation
//          process we do not change this value, means we do not estimate zone 0

//        reset initial values of estimated state of non reference buses
            String vaEstKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH);
            String vmEstKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH);
            if (resetVEst || !jedis.exists(vaEstKey)) {
                p.del(vaEstKey);
                p.del(vmEstKey);
                p.sync();

                List<String> busNumsOutStrs = new ArrayList<>();
//                ignore reference bus
                for (int i = 1; i < nz; i++) {
                    Response<List<String>> busNumsResp = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, i + "", StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
                    p.sync();
                    busNumsOutStrs.addAll(busNumsResp.get());
                }

                Map<String, String> vaEstInit = getVEstInit(busNumsOutStrs, true);
                Map<String, String> vmEstInit = getVEstInit(busNumsOutStrs, false);
                p.hmset(vaEstKey, vaEstInit);
                p.hmset(vmEstKey, vmEstInit);
                p.sync();
            }

//        always reset reference bus state to the value from power flow
            String vaRefKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_REF);
            String vmRefKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_REF);
            Response<String> vaRef = p.get(vaRefKey);
            Response<String> vmRef = p.get(vmRefKey);
            Response<List<String>> refNumResp = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, "0", StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
            p.sync();

            p.hset(vmEstKey, refNumResp.get().get(0), vmRef.get());
            p.hset(vaEstKey, refNumResp.get().get(0), vaRef.get());
            p.sync();

        }

//        we ignore the zone with only reference bus
        for (int i = 1; i < nz; i++) {
            collector.emit(new Values(caseid, i + ""));
        }
    }

    private Map<String, String> getVEstInit(List<String> nums, boolean va) {
        String val;
        if (va) {
            val = "0";
        } else {
            val = "1";
        }

        Map<String, String> res = new HashMap<>();

        for (int i = 0; i < nums.size(); i++) {
            res.put(nums.get(i) + "", val);
        }
        return res;
    }

}
