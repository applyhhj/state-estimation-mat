package thu.instcloud.app.se.storm.initializer;

import Estimator.Estimator;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.HashMap;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-27.
 */
public class PrepareRBolt extends JedisRichBolt {
    //    private String caseid;
    private MWStructArray zone;
    private MWStructArray zoneNew;
    private Estimator estimator;

    public PrepareRBolt(String reidsIp,String pass) {
        super(reidsIp,pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        super.declareOutputFields(outputFieldsDeclarer);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
        try {
            estimator = new Estimator();
        } catch (MWException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(Tuple tuple) {
        String caseid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        initializeZoneNew(tuple);
        storeNewZoneData(caseid);
        collector.ack(tuple);
    }

    private void initializeZoneNew(Tuple tuple){
        try {
            zone=(MWStructArray)MWStructArray.deserialize(tuple.getBinaryByField(StormUtils.STORM.FIELDS.ZONE_DATA));
            if (zone!=null) {
                zoneNew = (MWStructArray) estimator.api_prepareEstimation(1, zone)[0];
            }
        } catch (MWException e) {
            e.printStackTrace();
        }finally {
            if (zone!=null){
                zone.dispose();
            }
        }

    }

    private void storeNewZoneData(String caseid) {
        if (zoneNew==null){
            return;
        }
//            keys for flush all data related to this case
        String keysRec = mkKey(caseid, StormUtils.REDIS.KEYS.KEYS);
        try (Jedis jedis=jedisPool.getResource()){
            auth(jedis);
            Pipeline p=jedis.pipelined();

//            store data of each zone
            int zoneNum=(int)((double[][]) zoneNew.get(StormUtils.MW.FIELDS.ZONE_NUM,1))[0][0];
            byte[] zoneKey = mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, String.valueOf(zoneNum));
            p.set(zoneKey, zoneNew.serialize());

//            store bad data recognition threshold separately
            double thrshld = ((double[][]) zoneNew.get(StormUtils.MW.FIELDS.BAD_THRESHOLD, 1))[0][0];
            String thrshldKey = mkKey(caseid,
                    StormUtils.REDIS.KEYS.ZONES,
                    zoneNum + "",
                    StormUtils.REDIS.KEYS.BAD_RECOG_THRESHOLD);
            p.set(thrshldKey, thrshld + "");
            p.sadd(keysRec, thrshldKey);

//            store ids for getting estimated state for each zone
            Map<String, String[]> idsKeyVals = getIdsMap(zoneNew, new String(zoneKey));
            for (Map.Entry<String,String[]> e:idsKeyVals.entrySet()){
                p.del( e.getKey());
                p.lpush(e.getKey(),e.getValue());
                p.sadd(keysRec, e.getKey());
            }

//          store reference bus voltage
            if (zoneNum==0){
                double vaRef=((double[][]) zoneNew.get(StormUtils.MW.FIELDS.VA_REF,1))[0][0];
                double vmRef=((double[][]) zoneNew.get(StormUtils.MW.FIELDS.VM_REF,1))[0][0];
                String vaRefKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_REF);
                String vmRefKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_REF);
                p.set(vaRefKey, vaRef + "");
                p.set(vmRefKey, vmRef + "");
                p.sadd(keysRec,
                        vaRefKey,
                        vmRefKey);
            }

//            case data is ready
            String caseReadyKey = mkKey(caseid, StormUtils.REDIS.KEYS.READY);
            p.setbit(caseReadyKey, 0, true);

//            add measure keys to keys that need to flush when exit estimation
            p.sadd(keysRec,
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PF),
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PBUS),
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PT),
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QBUS),
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QT),
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QF),
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA),
                    mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM),
                    new String(zoneKey)
                    );

            p.sync();
        }finally {
            zoneNew.dispose();
        }

    }

    private Map<String,String[]> getIdsMap(MWStructArray zoneNew,String zonesKey){
        Map<String,String[]> res=new HashMap<>();
        double[][] ii2eout=(double[][]) zoneNew.get(StormUtils.MW.FIELDS.OUT_BUS_NUM_OUT,1);
        double[][] brids=(double[][]) zoneNew.get(StormUtils.MW.FIELDS.BRANCH_IDS,1);
        double[][] ii2e=(double[][]) zoneNew.get(StormUtils.MW.FIELDS.BUS_NUM_OUT,1);

/*        if any of this list is empty then no entry will be added into redis.
        When adding element to a list reids uses push method which makes the
        first element the last one in redis so we need to inverse the sequence
        of the input list*/
        String[] ii2eoutArr=toStringArrayInv(ii2eout);
        String[] bridsArr=toStringArrayInv(brids);
        String[] ii2eArr=toStringArrayInv(ii2e);

        String ii2eoutKey=mkKey(zonesKey, StormUtils.REDIS.KEYS.OUT_BUS_NUM_OUT);
        String ii2eKey=mkKey(zonesKey, StormUtils.REDIS.KEYS.BUS_NUM_OUT);
        String bridsKey=mkKey(zonesKey, StormUtils.REDIS.KEYS.BRANCH_IDS);

        res.put(ii2eKey,ii2eArr);
        res.put(ii2eoutKey,ii2eoutArr);
        res.put(bridsKey,bridsArr);

        return res;

    }


    private String[] toStringArrayInv(double[][] data){
        String[] res=new String[data.length];
        for (int i = 0; i < data.length; i++) {
//            reverse sequence
            res[data.length-i-1]=String.valueOf((int)data[i][0]);
        }

        return res;
    }
}
