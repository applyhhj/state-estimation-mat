package thu.instcloud.app.se.storm.initializer;

import Initializer.Initializer;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.splitter.SplitterUtils;

import java.util.Map;

import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkByteKey;

/**
 * Created by hjh on 15-12-27.
 */
public class PrepareRBolt extends JedisRichBolt {
    private String caseid;
    private MWStructArray zone;
    private MWStructArray zoneNew;
    private Initializer initializer;

    public PrepareRBolt(String reidsIp) {
        super(reidsIp);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        super.declareOutputFields(outputFieldsDeclarer);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
        try {
            initializer=new Initializer();
        } catch (MWException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {
        caseid=tuple.getStringByField(SplitterUtils.FIELDS.CASE_ID);
        initializeZoneNew(tuple);
        storeNewZoneData();
        collector.ack(tuple);
    }

    private void initializeZoneNew(Tuple tuple){
        try {
            zone=(MWStructArray)MWStructArray.deserialize(tuple.getBinaryByField(SplitterUtils.FIELDS.ZONE_DATA));
            if (zone!=null) {
                zoneNew = (MWStructArray) initializer.Api_PrepareEstimation(1, zone)[0];
            }
        } catch (MWException e) {
            e.printStackTrace();
        }finally {
            if (zone!=null){
                zone.dispose();
            }
        }

    }

    private void storeNewZoneData(){
        if (zoneNew==null){
            return;
        }

        try (Jedis jedis=jedisPool.getResource()){
            int zoneNum=(int)((double[][]) zoneNew.get(SplitterUtils.MW.FIELDS.ZONE_NUM,1))[0][0];
            byte[] key=mkByteKey(caseid, SplitterUtils.REDIS.KEYS.ZONES,String.valueOf(zoneNum));
            jedis.auth(SplitterUtils.REDIS.PASS);
            jedis.set(key,zoneNew.serialize());
        }finally {
            zoneNew.dispose();
        }

    }
}
