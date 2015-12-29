package thu.instcloud.app.se.storm.estimator;

import Estimator.Estimator;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.storm.common.JedisRichBolt;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;
import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-30.
 */
public class EstimateOnceRBolt extends JedisRichBolt {
    private Estimator estimator;

    public EstimateOnceRBolt(String redisIp, String pass) {
        super(redisIp, pass);
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
        String zoneid = tuple.getStringByField(StormUtils.STORM.FIELDS.ZONE_ID);

        estimate(caseid, zoneid);
        collector.emit(new Values(caseid, zoneid));
        collector.ack(tuple);
    }

    private void estimate(String caseid, String zoneid) {
        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();

            byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
            byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
            byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
            byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
            byte[] zoneDataKey = mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid);

            Response<byte[]> zoneDataByte = p.get(zoneDataKey);
            Response<byte[]> HHByte = p.get(HHkey);
            Response<byte[]> WWInvByte = p.get(WWInvKey);
            Response<byte[]> ddelzByte = p.get(ddelzKey);
            Response<byte[]> vvByte = p.get(vvKey);
            Response<List<String>> busIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
            Response<List<String>> outBusIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.OUT_BUS_NUM_OUT), 0, -1);
            Response<List<String>> brids = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BRANCH_IDS), 0, -1);

            p.sync();
//            get related ids
            List<String> busIdsLst = busIds.get();
            List<String> outBusIdsLst = outBusIds.get();
            List<String> bridsLst = brids.get();

//            get estimated voltage and external bus voltage
            Response<List<String>> VaEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), (String[]) busIdsLst.toArray());
            Response<List<String>> VmEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), (String[]) busIdsLst.toArray());
            Response<List<String>> VaExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), (String[]) outBusIdsLst.toArray());
            Response<List<String>> VmExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), (String[]) outBusIdsLst.toArray());

//            get measurement
            Response<List<String>> zpf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PF), (String[]) bridsLst.toArray());
            Response<List<String>> zpt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PT), (String[]) bridsLst.toArray());
            Response<List<String>> zqf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QF), (String[]) bridsLst.toArray());
            Response<List<String>> zqt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QT), (String[]) bridsLst.toArray());
            Response<List<String>> pbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PBUS), (String[]) busIdsLst.toArray());
            Response<List<String>> qbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QBUS), (String[]) busIdsLst.toArray());
            Response<List<String>> Vam = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA), (String[]) busIdsLst.toArray());
            Response<List<String>> Vmm = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM), (String[]) busIdsLst.toArray());

            p.sync();

            List<String> z = new ArrayList<>();
            z.addAll(zpf.get());
            z.addAll(zpt.get());
            z.addAll(pbus.get());
            z.addAll(Vam.get());
            z.addAll(zqf.get());
            z.addAll(zqt.get());
            z.addAll(qbus.get());
            z.addAll(Vmm.get());

//            get zone data
            MWStructArray zoneDataMatSArr = (MWStructArray) MWStructArray.deserialize(zoneDataByte.get());
            MWNumericArray zMatSArrRow = new MWNumericArray(z.toArray(), MWClassID.DOUBLE);
            MWNumericArray vaEstMatSArrRow = new MWNumericArray(VaEst.get().toArray(), MWClassID.DOUBLE);
            MWNumericArray vmEstMatSArrRow = new MWNumericArray(VmEst.get().toArray(), MWClassID.DOUBLE);
            MWNumericArray vaExtMatSArrRow = new MWNumericArray(VaExt.get().toArray(), MWClassID.DOUBLE);
            MWNumericArray vmExtMatSArrRow = new MWNumericArray(VmExt.get().toArray(), MWClassID.DOUBLE);

//            state
            MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
            MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
            MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
            MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());

            MWNumericArray[] res = null;
            MWNumericArray delz = null, normF = null, ddelz = null, VVa = null, VVm = null, step = null, success = null;

            try {
                res = (MWNumericArray[]) estimator.Api_V2_EstimateOnce(1, HHMat, WWInvMat, ddelzMat, vvMat,
                        vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow, zMatSArrRow, zoneDataMatSArr);
            } catch (MWException e) {
                e.printStackTrace();
            }

            if (res != null) {
                VVa = res[0];
                VVm = res[1];
                delz = res[2];
                ddelz = res[3];
                normF = res[4];
                step = res[5];
                success = res[6];

//                update state
                updateEstimatedVoltagesToBuffer(caseid, p, busIdsLst, VVa, VVm);
                p.set(ddelzKey, ddelz.serialize());
                p.set(mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ), delz.serialize());
//                estimated zones
                p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES));
//                estimate times
//                TODO: record only one iteration number
                p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IT));
                Response<String> tol = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_EST_TOL);
                p.sync();

                double toldbl = Double.parseDouble(tol.get());
                if (step.getDouble() < toldbl) {
                    String converKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
                    p.setbit(converKey, Long.parseLong(zoneid), true);
                    p.sync();
                }


            }
            disposeMatArrays(VVa, VVm, delz, ddelz, normF, success, step);
            disposeMatArrays(zoneDataMatSArr, zMatSArrRow, vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow,
                    HHMat, WWInvMat, ddelzMat, vvMat);
        }
    }

    private void updateEstimatedVoltagesToBuffer(String caseid, Pipeline p, List<String> busids, MWNumericArray va, MWNumericArray vm) {
        double[][] vaArr = (double[][]) va.toArray();
        double[][] vmArr = (double[][]) vm.toArray();
        Map<String, String> vaMap = new HashMap<>();
        Map<String, String> vmMap = new HashMap<>();

        for (int i = 0; i < busids.size(); i++) {
            vaMap.put(busids.get(i), String.valueOf(vaArr[i][0]));
            vmMap.put(busids.get(i), String.valueOf(vmArr[i][0]));
        }

//        only when all zones have finished one round estimation can we update the voltages of the system, before
//        that all estimated values are stored in buffer.
        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_BUFFER_HASH), vaMap);
        p.hmset(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_BUFFER_HASH), vmMap);
        p.sync();
    }
}
