package thu.instcloud.app.se.storm.initializer;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;
import static thu.instcloud.app.se.storm.common.StormUtils.MW.getArrayElement;
import static thu.instcloud.app.se.storm.common.StormUtils.*;

/**
 * Created by hjh on 15-12-27.
 */
public class InitializerRBolt extends JedisRichBolt {
    private MWStructArray zones;

    public InitializerRBolt(String reidsIp, String pass) {
        super(reidsIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.ZONE_DATA
        ));
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
    }

    @Override
    public void execute(Tuple tuple) {
        String caseid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        initialState(caseid);
        getZones(caseid);
        emitZone(caseid);
        collector.ack(tuple);
    }

    private void getZones(String caseid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            byte[] zonesKey = mkByteKey(
                    caseid,
                    StormUtils.REDIS.KEYS.RAW_DATA,
                    StormUtils.REDIS.KEYS.ZONES
            );

            if (jedis.exists(zonesKey)) {
                zones = (MWStructArray) MWStructArray.deserialize(jedis.get(zonesKey));
            }
        }
    }

    private void emitZone(String caseid) {
        if (zones != null) {
            int nz = zones.getDimensions()[1];
            MWStructArray zonei;
            for (int i = 1; i <= nz; i++) {
                zonei = getArrayElement(zones, i);
                if (zonei != null) {
                    collector.emit(new Values(caseid, zonei.serialize()));
                    zonei.dispose();
                }
            }
        }

        disposeMatArrays(zones);
    }


    private void initialState(String caseid) {
        int nz;
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);

//            evaluate estimation interval time
            String estTimeKey = mkKey(caseid, StormUtils.REDIS.KEYS.LAST_EST_TIME);
            jedis.set(estTimeKey, System.currentTimeMillis() + "");

//            evaluate estimation elapse time
            String estStartTimeKey = mkKey(caseid, StormUtils.REDIS.KEYS.EST_START_TIME);
            jedis.set(estStartTimeKey, System.currentTimeMillis() + "");

//            initialize state variables for estimation

//            whether the case is being estimated
            String estimatingKey = mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATING_BIT);
            jedis.setbit(estimatingKey, 0, true);

            nz = Integer.parseInt(jedis.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES)));
            Pipeline p = jedis.pipelined();

//            converge state of each zone
            String convergedKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
            p.del(convergedKey);
//            reference zone is always converged
            p.setbit(convergedKey, 0, true);

//            how many zones have already been estimated
            String estimatedKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES);
//            always remember to exclude reference zone
            p.set(estimatedKey, "1");

//            how many zones have already checked bad data
            String badrecogZnsKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES);
//            always remember to exclude reference zone
            p.set(badrecogZnsKey, "1");

//            record keys for flushing
            String keysRec = mkKey(caseid, StormUtils.REDIS.KEYS.KEYS);
            p.sadd(keysRec,
                    badrecogZnsKey,
                    convergedKey,
                    estimatingKey,
                    estimatedKey,
                    estTimeKey,
                    estStartTimeKey
            );
//            ignore reference zone
            for (int i = 1; i < nz; i++) {
//                TODO: consider use one key to record iit
                String zoneItKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT);
//                TODO: consider use one key to record ibad
                String zoneIbadtKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG);
                String vvKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
                String delzKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ);

                //            keys for some state generated later
                String HHkey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_HH);
                String WWKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_WW);
                String WWInvKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_WWINV);
                String ddelzKey = mkKey(caseid, i + "", StormUtils.REDIS.KEYS.STATE_DDELZ);

                p.set(zoneItKey, "0");
                p.set(zoneIbadtKey, "0");
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

/*          we set estimated voltage of reference bus to the value of the computed power flow and during the estimation
          process we do not change this value, means we do not estimate zone 0*/
            resetNonRefBustEstState(p, caseid, nz);


//        always reset reference bus state to the value from power flow
            setRefBusEstState(p, caseid);

        }

//        start estimation for each zone, we ignore the zone with only reference bus
//        for (int i = 1; i < nz; i++) {
//            collector.emit(new Values(caseid, i + ""));
//        }
    }


}
