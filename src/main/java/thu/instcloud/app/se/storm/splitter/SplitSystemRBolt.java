package thu.instcloud.app.se.storm.splitter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
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

    public SplitSystemRBolt(String reidsIp){
        super(reidsIp);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector=outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        changed=false;
        String caseID=tuple.getStringByField(SplitterUtils.FIELDS.CASE_ID);
        splitAndStore(caseID,tuple);
        collector.emit(new Values(caseID,changed));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                SplitterUtils.FIELDS.CASE_ID,
                SplitterUtils.FIELDS.DATA_CHANGED
        ));

    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    private void splitAndStore(String caseid,Tuple tuple){
        boolean overwrite=tuple.getBooleanByField(SplitterUtils.FIELDS.OVERWRITE);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(SplitterUtils.REDIS.PASS);
            String rawdatakey=mkKey(caseid, SplitterUtils.REDIS.KEYS.RAW_DATA);

            if(!(!overwrite&&jedis.exists(mkKey(rawdatakey,SplitterUtils.REDIS.KEYS.ZONES)))){
                List<String> caseDataStrs=(List<String>) tuple.getValueByField(SplitterUtils.FIELDS.CASE_DATA);
                int zbn=tuple.getIntegerByField(SplitterUtils.FIELDS.CASE_ZONE_BN);
                SplitMPData data=splitSystem(caseid,caseDataStrs,zbn);

//                TODO: use kryo to serialize
                jedis.set(mkByteKey(rawdatakey,SplitterUtils.REDIS.KEYS.BUS),data.getBus().serialize());
                jedis.set(mkByteKey(rawdatakey,SplitterUtils.REDIS.KEYS.BRANCH),data.getBranch().serialize());
                jedis.set(mkByteKey(rawdatakey,SplitterUtils.REDIS.KEYS.GEN),data.getGen().serialize());
                jedis.set(mkByteKey(rawdatakey,SplitterUtils.REDIS.KEYS.SBASE),data.getBaseMVA().serialize());

//              TODO: later, try to pass this data to the next bolt instead of store to redis then fetch it
                jedis.set(mkByteKey(rawdatakey, SplitterUtils.REDIS.KEYS.ZONES),data.getZones().serialize());

//              do remember to release mem used by matlab
                data.clear();
                changed=true;
            }else {
                System.out.printf("\nIgnore case %s",caseid);
            }
        }
    }

    private SplitMPData splitSystem(String caseid, List<String> data, int zoneBn){
        MPData mpData=new MPData(data);
        SplitMPData splitMPData=new SplitMPData(mpData,zoneBn);
        splitMPData.setCaseID(caseid);

        return splitMPData;
    }

}
