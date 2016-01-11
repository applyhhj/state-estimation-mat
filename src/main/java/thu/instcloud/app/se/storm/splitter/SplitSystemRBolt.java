package thu.instcloud.app.se.storm.splitter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.mpdata.MPData;
import thu.instcloud.app.se.splitter.SplitMPData;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-26.
 */
public class SplitSystemRBolt extends JedisRichBolt {
    boolean changed;

    public SplitSystemRBolt(String reidsIp,String pass) {
        super(reidsIp,pass);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
    }

    @Override
    public void execute(Tuple tuple) {
        changed = false;
        String caseID = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        splitAndStore(caseID, tuple);
        collector.emit(new Values(caseID, changed));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.DATA_CHANGED
        ));

    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    private void splitAndStore(String caseid, Tuple tuple) {
        Map<String,String> options=(Map<String,String>)tuple.getValueByField(StormUtils.STORM.FIELDS.OPTIONS_EST);
        boolean overwrite = Boolean.parseBoolean(options.get(StormUtils.OPTIONS.KEYS.OPT_OVERWRITE_CASEDATA));
        int zbn = Integer.parseInt(options.get(StormUtils.OPTIONS.KEYS.OPT_NBUS_ZONE));

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(StormUtils.REDIS.PASS);
            Pipeline p = jedis.pipelined();

//          need to store all keys for cleaning manually, well this is clumsy but we have no choice
            String keysRec = mkKey(caseid, StormUtils.REDIS.KEYS.KEYS);
            String rawdatakey = mkKey(caseid, StormUtils.REDIS.KEYS.RAW_DATA);

            if (!(!overwrite && jedis.exists(mkKey(rawdatakey, StormUtils.REDIS.KEYS.ZONES)))) {
//                we clear all data first
                clearAllData(p, keysRec);

//              if case data is ready
                String caseReadyKey = mkKey(caseid, StormUtils.REDIS.KEYS.READY);
                p.setbit(caseReadyKey, 0, false);

                List<String> caseDataStrs = (List<String>) tuple.getValueByField(StormUtils.STORM.FIELDS.CASE_DATA);
                SplitMPData data = splitSystem(caseid, caseDataStrs, zbn);

                p.set(mkByteKey(rawdatakey, StormUtils.REDIS.KEYS.BUS), data.getBus().serialize());
                p.set(mkByteKey(rawdatakey, StormUtils.REDIS.KEYS.BRANCH), data.getBranch().serialize());
                p.set(mkByteKey(rawdatakey, StormUtils.REDIS.KEYS.GEN), data.getGen().serialize());
                p.set(mkByteKey(rawdatakey, StormUtils.REDIS.KEYS.SBASE), data.getBaseMVA().serialize());

//                used to check if measurement is ready
                String nbKey = mkKey(caseid, StormUtils.REDIS.KEYS.N_BUS);
                String nbrKey = mkKey(caseid, StormUtils.REDIS.KEYS.N_BRANCH);
                p.set(nbKey, data.getNb() + "");
                p.set(nbrKey, data.getNbr() + "");

//              TODO: later, try to pass this data to the next bolt instead of store to redis then fetch it, that means discard data
//                store piecewised zone struct array
                p.set(mkByteKey(rawdatakey, StormUtils.REDIS.KEYS.ZONES), data.getZones().serialize());

//                store number of zones
                p.set(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES),
                        data.getZones().getDimensions()[1]+"");

//                add options
                String optKey=mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST);
                p.del(optKey);
                p.hmset(optKey,options);

//                keys of estimated state
                String vaEstKey=mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH);
                String vmEstKey=mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH);

//                clean original recorded keys, this should perform before any other recordings
                p.del(keysRec);
                p.sadd(keysRec,
                        keysRec,
                        mkKey(rawdatakey, StormUtils.REDIS.KEYS.BUS),
                        mkKey(rawdatakey, StormUtils.REDIS.KEYS.BRANCH),
                        mkKey(rawdatakey, StormUtils.REDIS.KEYS.GEN),
                        mkKey(rawdatakey, StormUtils.REDIS.KEYS.SBASE),
                        mkKey(rawdatakey, StormUtils.REDIS.KEYS.ZONES),
                        mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES),
                        optKey,
                        vaEstKey,
                        vmEstKey,
                        caseReadyKey,
                        nbKey,
                        nbrKey
                );

                p.sync();

//              do remember to release mem used by matlab
                data.clear();
                changed = true;

            } else {
                System.out.printf("\nIgnore case %s", caseid);
            }
        }
    }

    private void clearAllData(Pipeline p, String keysRec) {
        Response<Set<String>> keys = p.smembers(keysRec);
        p.sync();

        for (String key : keys.get()) {
            p.del(key);
        }
        p.sync();
    }

    private SplitMPData splitSystem(String caseid, List<String> data, int zoneBn) {
        SplitMPData splitMPData = new SplitMPData(caseid,new MPData(data), zoneBn);

        return splitMPData;
    }

}
