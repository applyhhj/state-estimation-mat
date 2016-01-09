package thu.instcloud.app.se.storm.initializer;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.esotericsoftware.kryo.Kryo;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.*;
import static thu.instcloud.app.se.storm.common.StormUtils.MW.getArrayElement;

/**
 * Created by hjh on 15-12-27.
 */
public class DistributeZoneRBolt extends JedisRichBolt {
    private MWStructArray zones;
    private String caseid;

    public DistributeZoneRBolt(String reidsIp, String pass) {
        super(reidsIp,pass);
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
        caseid=tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        getZones();
        emitZone();
        collector.ack(tuple);
    }

    private void getZones(){
        try (Jedis jedis=jedisPool.getResource()){
            jedis.auth(StormUtils.REDIS.PASS);
            byte[] zonesKey=mkByteKey(
                    caseid,
                    StormUtils.REDIS.KEYS.RAW_DATA,
                    StormUtils.REDIS.KEYS.ZONES
            );

            if (jedis.exists(zonesKey)){
                zones=(MWStructArray) MWStructArray.deserialize(jedis.get(zonesKey));
            }
        }
    }

    private void emitZone(){
        if (zones!=null) {
            int nz = zones.getDimensions()[1];
            MWStructArray zonei;
            for (int i = 1; i <= nz; i++) {
                zonei=getArrayElement(zones,i);
                if (zonei!=null) {
                    collector.emit(new Values(caseid,zonei.serialize()));
                    zonei.dispose();
                }
            }
        }

        if (zones!=null){
            zones.dispose();
        }
    }


}
