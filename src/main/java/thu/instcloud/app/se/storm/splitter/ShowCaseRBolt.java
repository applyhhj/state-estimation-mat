package thu.instcloud.app.se.storm.splitter;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.mathworks.toolbox.javabuilder.MWArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import thu.instcloud.app.se.storm.common.JedisRichBolt;

import java.util.Map;

import static thu.instcloud.app.se.storm.splitter.SplitterUtils.MW.getArrayElement;
import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkByteKey;

/**
 * Created by hjh on 15-12-26.
 */
public class ShowCaseRBolt extends JedisRichBolt {

    public ShowCaseRBolt(String reidsIp) {
        super(reidsIp);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map,topologyContext,outputCollector);
    }

    @Override
    public void execute(Tuple tuple) {
        String caseID=tuple.getStringByField(SplitterUtils.FIELDS.CASE_ID);
        printDataStrs(caseID);
        collector.ack(tuple);
    }


    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    private void printDataStrs(String caseid){
        byte[] zones=null;

        try (Jedis jedis = jedisPool.getResource()){
            jedis.auth(SplitterUtils.REDIS.PASS);
            byte[] zonesKey=mkByteKey(
                    caseid,
                    SplitterUtils.REDIS.KEYS.RAW_DATA,
                    SplitterUtils.REDIS.KEYS.ZONES
            );
            if (jedis.exists(zonesKey)){
                zones=jedis.get(zonesKey);
            }
        }

        if (zones!=null) {
            MWStructArray zonesStruct=(MWStructArray) MWStructArray.deserialize(zones);
            System.out.printf("\n%s",zonesStruct);
            zonesStruct.dispose();
        }else {
            System.out.printf("\n%s","Data not found!!");
        }
        System.out.printf("\nReceived case %20s\n", caseid);
    }
}
