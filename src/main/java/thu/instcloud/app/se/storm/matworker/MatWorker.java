package thu.instcloud.app.se.storm.matworker;

import Estimator.Estimator;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import org.apache.commons.cli.*;
import redis.clients.jedis.*;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.List;

import static thu.instcloud.app.se.storm.common.StormUtils.*;
import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 16-1-8.
 */
public class MatWorker {

    private static final String HOST_ARG_NAME = "h";
    private static final String AUTH_ARG_NAME = "a";
    private static final String TASK_ARG_NAME = "t";
    private static final String CASEID_ARG_NAME = "c";
    private static final String ZONEID_ARG_NAME = "z";
    private static String redisIp;
    private static String pass;
    private static String task;
    private static String caseid;
    private static String zoneid;
    private static Estimator estimator;

    public static void main(String[] args) {
        int exitcode = 0;
        parseArgs(args);
        if (pass == null || redisIp == null || task == null) {
            exitcode = -1;
        } else {
            JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisIp);
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.auth(pass);
                if (!jedis.exists(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES, zoneid))) {
                    exitcode = -2;
                    return;
                } else {
                    try {
                        estimator = new Estimator();
                        exitcode = runTask(jedis.pipelined(), task);
                    } catch (MWException e) {
                        e.printStackTrace();
                    } finally {
                        estimator.dispose();
                    }

                }
            }
        }

        System.exit(exitcode);
    }

    private static int runTask(Pipeline p, String taskName) {
        if (taskName.equals(StormUtils.MW.TASK.ESTIMATE_TASK)) {
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

            p.sync();
//            get related ids
            List<String> busIdsLst = busIds.get();

            MWNumericArray zMatSArrRow = getMatZ(p, caseid, zoneid);

            List<MWNumericArray> matVamEstExt = getMatVamEstExt(p, caseid, zoneid);
            MWNumericArray vaEstMatSArrRow = matVamEstExt.get(0);
            MWNumericArray vmEstMatSArrRow = matVamEstExt.get(1);
            MWNumericArray vaExtMatSArrRow = matVamEstExt.get(2);
            MWNumericArray vmExtMatSArrRow = matVamEstExt.get(3);

//            get zone data
            MWStructArray zoneDataMatSArr = (MWStructArray) MWStructArray.deserialize(zoneDataByte.get());

//            state
            MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
            MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
            MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
            MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());

            Object[] res = null;
            MWNumericArray delz = null, normF = null, ddelz = null, VVa = null, VVm = null, step = null, success = null;

            try {
                res = estimator.api_estimateOnce(7, HHMat, WWInvMat, ddelzMat, vvMat,
                        vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow, zMatSArrRow, zoneDataMatSArr);
            } catch (MWException e) {
                e.printStackTrace();
            }

            if (res != null) {
                VVa = (MWNumericArray) res[0];
                VVm = (MWNumericArray) res[1];
                delz = (MWNumericArray) res[2];
                ddelz = (MWNumericArray) res[3];
                normF = (MWNumericArray) res[4];
                step = (MWNumericArray) res[5];
                success = (MWNumericArray) res[6];

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
//                debug
//                if (step.getDouble(1)>toldbl) {
                System.out.println("zone: " + zoneid + ";  step:" + step);
//                }

            }
            disposeMatArrays(VVa, VVm, delz, ddelz, normF, success, step);
            disposeMatArrays(zoneDataMatSArr, zMatSArrRow, vaEstMatSArrRow, vmEstMatSArrRow, vaExtMatSArrRow, vmExtMatSArrRow,
                    HHMat, WWInvMat, ddelzMat, vvMat);

            return 0;
        } else if (taskName.equals(StormUtils.MW.TASK.BADRECOG_TASK)) {
            byte[] HHkey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_HH);
            byte[] WWKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WW);
            byte[] WWInvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_WWINV);
            byte[] ddelzKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE_DDELZ);
            byte[] vvKey = mkByteKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_VV);

            Response<byte[]> HHByte = p.get(HHkey);
            Response<byte[]> WWByte = p.get(WWKey);
            Response<byte[]> WWInvByte = p.get(WWInvKey);
            Response<byte[]> ddelzByte = p.get(ddelzKey);
            Response<byte[]> vvByte = p.get(vvKey);

            String thrshldKey = mkKey(caseid,
                    StormUtils.REDIS.KEYS.ZONES,
                    zoneid,
                    StormUtils.REDIS.KEYS.BAD_RECOG_THRESHOLD);
            Response<String> badthrshld = p.get(thrshldKey);
            p.sync();

            MWNumericArray badthrshldMat = new MWNumericArray(badthrshld.get(), MWClassID.DOUBLE);
            MWNumericArray HHMat = (MWNumericArray) MWNumericArray.deserialize(HHByte.get());
            MWNumericArray WWMat = (MWNumericArray) MWNumericArray.deserialize(WWByte.get());
            MWNumericArray WWInvMat = (MWNumericArray) MWNumericArray.deserialize(WWInvByte.get());
            MWNumericArray ddelzMat = (MWNumericArray) MWNumericArray.deserialize(ddelzByte.get());
            MWNumericArray vvMat = (MWNumericArray) MWNumericArray.deserialize(vvByte.get());

            MWNumericArray vvNewMat = null, convergedMat = null;
            Object[] res = null;
            try {
                res = estimator.api_badDataRecognition(2, HHMat, WWMat, WWInvMat,
                        vvMat, ddelzMat, badthrshldMat);
            } catch (MWException e) {
                e.printStackTrace();
            }

//            update state
            if (res != null) {
                vvNewMat = (MWNumericArray) res[0];
                if (vvMat.getDimensions()[0] != vvNewMat.getDimensions()[0]) {
                    p.set(vvKey, vvNewMat.serialize());
                }
                convergedMat = (MWNumericArray) res[1];
                boolean converbool;
                if (!(convergedMat.getDouble() < 0)) {
                    converbool = false;
                    p.setbit(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_CONVERGED), Long.parseLong(zoneid), converbool);
                }
                p.incr(mkKey(caseid, StormUtils.REDIS.KEYS.STATE_BADRECOG_ZONES));
//                TODO: record only one iteration number
                p.incr(mkKey(caseid, zoneid, StormUtils.REDIS.KEYS.STATE, StormUtils.REDIS.KEYS.STATE_IBADREG));
                p.sync();
            }

            disposeMatArrays(badthrshldMat, HHMat, WWInvMat, WWMat, ddelzMat, vvMat, vvNewMat, convergedMat);

            return 0;
        }


        return -3;
    }

    private static void parseArgs(String[] args) {
        Options options = new Options();

        options.addOption(HOST_ARG_NAME, "host", true, "host or ip of redis server");
        options.addOption(AUTH_ARG_NAME, "auth", true, "password for redis server");
        options.addOption(TASK_ARG_NAME, "task", true, "task for this worker");
        options.addOption(CASEID_ARG_NAME, "case", true, "case id");
        options.addOption(ZONEID_ARG_NAME, "zoneid", true, "zone id");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(HOST_ARG_NAME)) {
                redisIp = line.getOptionValue(HOST_ARG_NAME);
            }
            if (line.hasOption(AUTH_ARG_NAME)) {
                pass = line.getOptionValue(AUTH_ARG_NAME);
            }
            if (line.hasOption(TASK_ARG_NAME)) {
                task = line.getOptionValue(TASK_ARG_NAME);
            }
            if (line.hasOption(CASEID_ARG_NAME)) {
                caseid = line.getOptionValue(CASEID_ARG_NAME);
            }
            if (line.hasOption(ZONEID_ARG_NAME)) {
                zoneid = line.getOptionValue(ZONEID_ARG_NAME);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
