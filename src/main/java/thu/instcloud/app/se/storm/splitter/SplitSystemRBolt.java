package thu.instcloud.app.se.storm.splitter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import thu.instcloud.app.se.mpdata.MPData;
import thu.instcloud.app.se.splitter.SplitMPData;
import thu.instcloud.app.se.storm.common.JedisRichBolt;

import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkByteKey;
import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkKey;

/**
 * Created by hjh on 15-12-26.
 */
public class SplitSystemRBolt extends JedisRichBolt {
    OutputCollector collector;
    boolean changed;

    public SplitSystemRBolt(String reidsIp,String pass) {
        super(reidsIp,pass);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        changed = false;
        String caseID = tuple.getStringByField(SplitterUtils.STORM.FIELDS.CASE_ID);
        splitAndStore(caseID, tuple);
        collector.emit(new Values(caseID, changed));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                SplitterUtils.STORM.FIELDS.CASE_ID,
                SplitterUtils.STORM.FIELDS.DATA_CHANGED
        ));

    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    private void splitAndStore(String caseid, Tuple tuple) {
        boolean overwrite = tuple.getBooleanByField(SplitterUtils.STORM.FIELDS.OVERWRITE);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(SplitterUtils.REDIS.PASS);
            Pipeline p = jedis.pipelined();
            String rawdatakey = mkKey(caseid, SplitterUtils.REDIS.KEYS.RAW_DATA);

            if (!(!overwrite && jedis.exists(mkKey(rawdatakey, SplitterUtils.REDIS.KEYS.ZONES)))) {
                List<String> caseDataStrs = (List<String>) tuple.getValueByField(SplitterUtils.STORM.FIELDS.CASE_DATA);
                int zbn = tuple.getIntegerByField(SplitterUtils.STORM.FIELDS.CASE_ZONE_BN);
                SplitMPData data = splitSystem(caseid, caseDataStrs, zbn);

//                TODO: use kryo to serialize
                p.set(mkByteKey(rawdatakey, SplitterUtils.REDIS.KEYS.BUS), data.getBus().serialize());
                p.set(mkByteKey(rawdatakey, SplitterUtils.REDIS.KEYS.BRANCH), data.getBranch().serialize());
                p.set(mkByteKey(rawdatakey, SplitterUtils.REDIS.KEYS.GEN), data.getGen().serialize());
                p.set(mkByteKey(rawdatakey, SplitterUtils.REDIS.KEYS.SBASE), data.getBaseMVA().serialize());

//              TODO: later, try to pass this data to the next bolt instead of store to redis then fetch it
                p.set(mkByteKey(rawdatakey, SplitterUtils.REDIS.KEYS.ZONES), data.getZones().serialize());
                p.set(mkKey(caseid, SplitterUtils.REDIS.KEYS.ZONES, SplitterUtils.REDIS.KEYS.NUM),
                        data.getZones().getDimensions()[1]+"");

//                well this is clumsy but we have no choice
                String keysRec = mkKey(caseid, SplitterUtils.REDIS.KEYS.KEYS);
                p.del(keysRec);
                p.sadd(keysRec,
                        keysRec,
                        mkKey(rawdatakey, SplitterUtils.REDIS.KEYS.BUS),
                        mkKey(rawdatakey, SplitterUtils.REDIS.KEYS.BRANCH),
                        mkKey(rawdatakey, SplitterUtils.REDIS.KEYS.GEN),
                        mkKey(rawdatakey, SplitterUtils.REDIS.KEYS.SBASE),
                        mkKey(rawdatakey, SplitterUtils.REDIS.KEYS.ZONES),
                        mkKey(caseid, SplitterUtils.REDIS.KEYS.ZONES, SplitterUtils.REDIS.KEYS.NUM)
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

    private SplitMPData splitSystem(String caseid, List<String> data, int zoneBn) {
        MPData mpData = new MPData(data);
        SplitMPData splitMPData = new SplitMPData(mpData, zoneBn);
        splitMPData.setCaseID(caseid);

        return splitMPData;
    }

}
