package thu.instcloud.app.se.storm.estimator;

import Estimator.Estimator;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
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

import static thu.instcloud.app.se.storm.common.StormUtils.*;
import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;

/**
 * Created by hjh on 15-12-29.
 */
public class FirstEstimationRBolt extends JedisRichBolt {
    Estimator estimator;

    public FirstEstimationRBolt(String redisIp, String pass) {
        super(redisIp, pass);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(StormUtils.STORM.STREAM.STREAM_OUTPUT, new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.EST_CONVERGED
        ));

        outputFieldsDeclarer.declareStream(StormUtils.STORM.STREAM.STREAM_ESTIMATE,
                new Fields(
                        StormUtils.STORM.FIELDS.CASE_ID,
                        StormUtils.STORM.FIELDS.ZONE_ID
                ));
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
        firstEstimate(caseid, zoneid);
        collector.ack(tuple);
    }

    private void firstEstimate(String caseid, String zoneid) {

        try (Jedis jedis = jedisPool.getResource()) {
            auth(jedis);
            Pipeline p = jedis.pipelined();
//
//            Response<byte[]> zoneDataByte = p.get(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid));
//            Response<List<String>> busIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BUS_NUM_OUT), 0, -1);
//            Response<List<String>> outBusIds = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.OUT_BUS_NUM_OUT), 0, -1);
//            Response<List<String>> brids = p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid, StormUtils.REDIS.KEYS.BRANCH_IDS), 0, -1);
            Response<String> tol = p.hget(mkKey(caseid, StormUtils.REDIS.KEYS.OPTIONS_EST), StormUtils.OPTIONS.KEYS.OPT_EST_TOL);
//
            p.sync();
//
////            get related ids
//            List<String> busIdsLst = busIds.get();
//            List<String> outBusIdsLst = outBusIds.get();
//            List<String> bridsLst = brids.get();
//
////            get estimated voltage and external bus voltage
//            Response<List<String>> VaEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()]));
//            Response<List<String>> VmEst = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), busIdsLst.toArray(new String[busIdsLst.size()]));
//            Response<List<String>> VaExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VA_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()]));
//            Response<List<String>> VmExt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.VM_EST_HASH), outBusIdsLst.toArray(new String[outBusIdsLst.size()]));
//
////            get measurement
//            Response<List<String>> zpf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PF), bridsLst.toArray(new String[bridsLst.size()]));
//            Response<List<String>> zpt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PT), bridsLst.toArray(new String[bridsLst.size()]));
//            Response<List<String>> zqf = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QF), bridsLst.toArray(new String[bridsLst.size()]));
//            Response<List<String>> zqt = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QT), bridsLst.toArray(new String[bridsLst.size()]));
//            Response<List<String>> pbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.PBUS), busIdsLst.toArray(new String[busIdsLst.size()]));
//            Response<List<String>> qbus = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.QBUS), busIdsLst.toArray(new String[busIdsLst.size()]));
//            Response<List<String>> Vam = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA), busIdsLst.toArray(new String[busIdsLst.size()]));
//            Response<List<String>> Vmm = p.hmget(mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VM), busIdsLst.toArray(new String[busIdsLst.size()]));
//
//            p.sync();
//
//            List<String> z = new ArrayList<>();
//            z.addAll(zpf.get());
//            z.addAll(zpt.get());
//            z.addAll(pbus.get());
//            z.addAll(Vam.get());
//            z.addAll(zqf.get());
//            z.addAll(zqt.get());
//            z.addAll(qbus.get());
//            z.addAll(Vmm.get());

//            get zone data
            MWStructArray zoneDataMatSArr = getMatZoneData(p, caseid, zoneid);
            MWNumericArray zMatSArrRow = getMatZ(p, caseid, zoneid);

            List<MWNumericArray> matVamEstExt = getMatVamEstExt(p, caseid, zoneid);
            MWNumericArray vaEstMatSArrRow = matVamEstExt.get(0);
            MWNumericArray vmEstMatSArrRow = matVamEstExt.get(1);
            MWNumericArray vaExtMatSArrRow = matVamEstExt.get(2);
            MWNumericArray vmExtMatSArrRow = matVamEstExt.get(3);

            MWNumericArray delz = null, normF = null, vv = null;
            byte[] delzByte = null, vvByte = null;
            double normFDbl = Double.MAX_VALUE;

//            first estimation
            try {
                Object[] res = estimator.api_firstEstimation(3, vaEstMatSArrRow, vmEstMatSArrRow,
                        vaExtMatSArrRow, vmExtMatSArrRow, zMatSArrRow, zoneDataMatSArr);
                delz = (MWNumericArray) res[0];
                normF = (MWNumericArray) res[1];
                vv = (MWNumericArray) res[2];

                delzByte = delz.serialize();
                vvByte = vv.serialize();
                normFDbl = normF.getDouble();

            } catch (MWException e) {
                e.printStackTrace();
            } finally {
                disposeMatArrays(vaEstMatSArrRow, vmEstMatSArrRow,
                        vaExtMatSArrRow, vmExtMatSArrRow, zMatSArrRow, zoneDataMatSArr, delz, vv, normF);
            }

            String estimatedKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_ESTIMATED_ZONES);
            p.incr(estimatedKey);
            Response<String> estimatedZones = p.get(estimatedKey);
            Response<String> nz = p.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES));

//            check convergence
            double toldbl = Double.parseDouble(tol.get());
            String converKey = mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED);
            if (normFDbl < toldbl) {
//                converged
                p.setbit(converKey, Long.parseLong(zoneid), true);
            }

//             update states for this zone
            if (vvByte != null) {
                byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);
                p.set(vvKey, vvByte);
            }

            if (delzByte != null) {
                byte[] delzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_DELZ);
                p.set(delzKey, delzByte);
            }

            p.sync();

            long nzInt = Long.parseLong(nz.get());
//                all zones are estimated, check if all estimations are converged
            if (Long.parseLong(estimatedZones.get()) == nzInt) {
                Long nConver = jedis.bitcount(converKey);
                if (nConver == nzInt) {
//                    all converged
//                    reset converge states
                    p.del(converKey);
                    p.setbit(converKey, 0, true);
                    p.set(estimatedKey, "1");
//                    finished estimation
                    p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.ESTIMATING_BIT), 0, false);

//                    output
                    collector.emit(StormUtils.STORM.STREAM.STREAM_OUTPUT, new Values(caseid, true));
                    return;
                } else {

//                    reset converge states for further estimation, whenever their is an unconverged estimation we
//                    estimate the whole system again.
                    p.del(converKey);
                    p.setbit(converKey, 0, true);
                    p.set(estimatedKey, "1");
                    p.sync();

//                    redispatch zone for further estimation
                    for (int i = 1; i < nzInt; i++) {
                        collector.emit(StormUtils.STORM.STREAM.STREAM_ESTIMATE,
                                new Values(caseid, i + ""));
                    }


                }
            }

        }

    }


}
