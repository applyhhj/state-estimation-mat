package thu.instcloud.app.se.storm.initializer;

import Initializer.Initializer;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.splitter.SplitterUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkByteKey;
import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkKey;

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
        caseid=tuple.getStringByField(SplitterUtils.STORM.FIELDS.CASE_ID);
        initializeZoneNew(tuple);
        storeNewZoneData();
        collector.ack(tuple);
    }

    private void initializeZoneNew(Tuple tuple){
        try {
            zone=(MWStructArray)MWStructArray.deserialize(tuple.getBinaryByField(SplitterUtils.STORM.FIELDS.ZONE_DATA));
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
            Pipeline p=jedis.pipelined();
            jedis.auth(SplitterUtils.REDIS.PASS);

            String keysRec = mkKey(caseid, SplitterUtils.REDIS.KEYS.KEYS);

            int zoneNum=(int)((double[][]) zoneNew.get(SplitterUtils.MW.FIELDS.ZONE_NUM,1))[0][0];
            byte[] key=mkByteKey(caseid, SplitterUtils.REDIS.KEYS.ZONES,String.valueOf(zoneNum));
            p.set(key,zoneNew.serialize());
            p.sadd(keysRec,new String(key));

            Map<String,String[]> idsKeyVals=getIdsMap(zoneNew,new String(key));
            for (Map.Entry<String,String[]> e:idsKeyVals.entrySet()){
                p.del( e.getKey());
                p.lpush(e.getKey(),e.getValue());
                p.sadd(keysRec,e.getKey());
            }

//            add measure keys to keys that need to flush when exit estimation
            p.sadd(keysRec,
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.PF),
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.PBUS),
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.PT),
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.QBUS),
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.QT),
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.QF),
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.VA),
                    mkKey(caseid, SplitterUtils.REDIS.KEYS.MEASURE, SplitterUtils.MEASURE.TYPE.VM)
                    );

            p.sync();
        }finally {
            zoneNew.dispose();
        }

    }

    private Map<String,String[]> getIdsMap(MWStructArray zoneNew,String zonesKey){
        Map<String,String[]> res=new HashMap<>();
        double[][] ii2eout=(double[][]) zoneNew.get(SplitterUtils.MW.FIELDS.OUT_BUS_NUM_OUT,1);
        double[][] brids=(double[][]) zoneNew.get(SplitterUtils.MW.FIELDS.BRANCH_IDS,1);
        double[][] ii2e=(double[][]) zoneNew.get(SplitterUtils.MW.FIELDS.BUS_NUM_OUT,1);

//        if any of this list is empty then no entry will be add in redis
        String[] ii2eoutArr=toStringArray(ii2eout);
        String[] bridsArr=toStringArray(brids);
        String[] ii2eArr=toStringArray(ii2e);

        String ii2eoutKey=mkKey(zonesKey, SplitterUtils.MW.FIELDS.OUT_BUS_NUM_OUT);
        String ii2eKey=mkKey(zonesKey, SplitterUtils.MW.FIELDS.BUS_NUM_OUT);
        String bridsKey=mkKey(zonesKey, SplitterUtils.MW.FIELDS.BRANCH_IDS);

        res.put(ii2eKey,ii2eArr);
        res.put(ii2eoutKey,ii2eoutArr);
        res.put(bridsKey,bridsArr);

        return res;

    }


    private String[] toStringArray(double[][] data){
        String[] res=new String[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i]=String.valueOf((int)data[i][0]);
        }

        return res;
    }
}
