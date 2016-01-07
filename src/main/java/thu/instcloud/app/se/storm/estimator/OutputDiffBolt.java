package thu.instcloud.app.se.storm.estimator;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-30.
 */
public class OutputDiffBolt extends JedisRichBolt {
    public OutputDiffBolt(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        super.declareOutputFields(outputFieldsDeclarer);
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        super.prepare(map, topologyContext, outputCollector);
    }

    @Override
    public void execute(Tuple tuple) {
        String caseid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        boolean converged = tuple.getBooleanByField(StormUtils.STORM.FIELDS.EST_CONVERGED);

        showEstimatedVoltages(caseid);
        if (converged) {
            System.out.println(caseid + " converged!+++++++");
        } else {
            System.out.println(caseid + " not converged!-------");
        }
    }

    private void showEstimatedVoltages(String caseid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();
            String estVaKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH);
            String estVmKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH);
            String valfKey = mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA);
            String vmlfKey = mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM);

            Response<Map<String, String>> vaMapResp = p.hgetAll(estVaKey);
            Response<Map<String, String>> vmMapResp = p.hgetAll(estVmKey);
            Response<Map<String, String>> valfMapResp = p.hgetAll(valfKey);
            Response<Map<String, String>> vmlfMapResp = p.hgetAll(vmlfKey);

            p.sync();
            System.out.printf("\n\nTotal number of buses: %8d", vaMapResp.get().size());
            System.out.print("\nBus\tVa\tVm\t");
            for (Map.Entry<String, String> e : vaMapResp.get().entrySet()) {
                System.out.printf("\n%8s\t%15s\t%15s", e.getKey(), e.getValue(), vmMapResp.get().get(e.getKey()));
            }
        }
    }
}
