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

import java.util.ArrayList;
import java.util.List;
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
        collector.ack(tuple);
    }

    private void showEstimatedVoltages(String caseid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();

            String estTimeKey = mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATE_TIME);
            long esttime = System.currentTimeMillis() - Long.parseLong(jedis.get(estTimeKey));
            jedis.set(estTimeKey, esttime + "");

            String estVaKey = mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH);
            String estVmKey = mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH);
            String valfKey = mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA);
            String vmlfKey = mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM);

            Response<Map<String, String>> vaMapResp = p.hgetAll(estVaKey);
            Response<Map<String, String>> vmMapResp = p.hgetAll(estVmKey);
            Response<Map<String, String>> valfMapResp = p.hgetAll(valfKey);
            Response<Map<String, String>> vmlfMapResp = p.hgetAll(vmlfKey);

            p.sync();
            System.out.printf("\n\nTotal number of buses: %8d with estimation time %10.4fs", vaMapResp.get().size(), esttime / 1000.0);
            System.out.print("\nMaxdiff with power flow:\nVaMaxDiff(degree)\t\tVmMaxDiff(pu)\t");

            List<Double> maxdiff = findMaxDiff(vaMapResp.get(), vmMapResp.get(), valfMapResp.get(), vmlfMapResp.get());
            System.out.printf("\n%10.5f\t%10.5f\n", maxdiff.get(0), maxdiff.get(1));
        }
    }

    private List<Double> findMaxDiff(Map<String, String> vaEst, Map<String, String> vmEst, Map<String, String> valf, Map<String, String> vmlf) {

        int n = vaEst.size();
        double vaMaxDiff = Double.MIN_VALUE;
        double vmMaxDiff = Double.MIN_VALUE;
        List<Double> vadiff = new ArrayList<>();
        List<Double> vmdiff = new ArrayList<>();
        List<Double> res = new ArrayList<>();

        for (Map.Entry<String, String> e : vaEst.entrySet()) {
            String busNum = e.getKey();
            Double vaEsti = Double.parseDouble(vaEst.get(busNum));
            Double vmEsti = Double.parseDouble(vmEst.get(busNum));
            Double valfi = Double.parseDouble(valf.get(busNum));
            Double vmlfi = Double.parseDouble(vmlf.get(busNum));

            vadiff.add(Math.abs(vaEsti - valfi));
            vmdiff.add(Math.abs(vmEsti - vmlfi));
        }

        for (int i = 0; i < n; i++) {
            if (vaMaxDiff < vadiff.get(i)) {
                vaMaxDiff = vadiff.get(i);
            }
            if (vmMaxDiff < vmdiff.get(i)) {
                vmMaxDiff = vmdiff.get(i);
            }
        }

//        to degree
        res.add(vaMaxDiff * 180 / Math.PI);
        res.add(vmMaxDiff);

        return res;

    }
}
