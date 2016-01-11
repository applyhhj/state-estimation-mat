package thu.instcloud.app.se.storm.measure;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import thu.instcloud.app.se.storm.common.JedisRichSpout;
import thu.instcloud.app.se.storm.common.MeasureDataRaw;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-28.
 */
public class MeasureRSpout extends JedisRichSpout {
    private String caseid;
    private List<MeasureDataRaw> rawMeasures;
    private int midx;
    private boolean ready;
    private int maxtry;
    private Random random;

    public MeasureRSpout(String redisIp, String pass, String caseid) {
        super(redisIp, pass);
        this.caseid = caseid;
        this.maxtry = 50;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.MEASURE_TYPE,
                StormUtils.STORM.FIELDS.MEASURE_ID,
                StormUtils.STORM.FIELDS.MEASURE_VALUE
        ));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        super.open(map, topologyContext, spoutOutputCollector);
        rawMeasures=importTrueMeasurement(caseid);
        midx=0;
        ready = false;
        random = new Random();
    }

    @Override
    public void nextTuple() {
//        try to get data again
        if (rawMeasures == null) {
            rawMeasures = importTrueMeasurement(caseid);
        } else {
            if (!ready) {
                collector.emit(rawMeasures.get(midx).toTrueMeasureValues());
                midx++;
                if (midx == rawMeasures.size()) {
                    midx = 0;
                    ready = true;
                }
            } else {
                collector.emit(rawMeasures.get(random.nextInt(rawMeasures.size())).toTrueMeasureValues());
                Utils.sleep(500);
            }

        }
    }

    public List<MeasureDataRaw> importTrueMeasurement(String caseid){
        List<MeasureDataRaw> res=new ArrayList<>();

        List<Response<byte[]>> zonesRes=new ArrayList<>();
        List<Response<List<String>>> ii2eListRes=new ArrayList<>();
        List<Response<List<String>>> bridsListRes=new ArrayList<>();
        int nz;

        try (Jedis jedis=jedisPool.getResource()){
            auth(jedis);
//            waiting for case data to be prepared
            int cnt = 0;
            while (!jedis.getbit(mkKey(caseid, StormUtils.REDIS.KEYS.READY), 0) && cnt++ < maxtry) {
                Utils.sleep(1000);
            }
            if (cnt == maxtry) {
                System.out.println("Get measurement data timeout!");
                return null;
            }

            nz=Integer.parseInt(jedis.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES)));
            Pipeline p=jedis.pipelined();
//            including refrence zone
            for (int i = 0; i < nz; i++) {
                zonesRes.add(p.get(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES,i+"")));
                ii2eListRes.add(p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES,i+"", StormUtils.REDIS.KEYS.BUS_NUM_OUT),0,-1));
                bridsListRes.add(p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES,i+"", StormUtils.REDIS.KEYS.BRANCH_IDS),0,-1));
            }
            p.sync();
        }

        for (int i = 0; i < nz; i++) {
            MWStructArray zone = (MWStructArray) MWStructArray.deserialize(zonesRes.get(i).get());
            List<String> ii2eList=ii2eListRes.get(i).get();
            List<String> bridsList=bridsListRes.get(i).get();
            res.addAll(retrieveData(caseid,zone,ii2eList,bridsList));
            zone.dispose();
        }

        return res;
    }

    private List<MeasureDataRaw> retrieveData(String caseid,MWStructArray zone,List<String> ii2e,List<String> brids){
        List<MeasureDataRaw> res=new ArrayList<>();
        double[][] ztrue=(double[][])zone.get(StormUtils.MW.FIELDS.Z_TRUE,1);
        double[][] sigma=(double[][])zone.get(StormUtils.MW.FIELDS.SIGMA,1);
        int nb=ii2e.size();
        int nbr=brids.size();

        int mid,dataidx;
        String mtype;
        double mvalue,msigma;
        for (int i = 0; i < nb; i++) {
            mid=Integer.parseInt(ii2e.get(i));
            mtype= StormUtils.MEASURE.TYPE.PBUS;
            dataidx=i+2*nbr;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype= StormUtils.MEASURE.TYPE.QBUS;
            dataidx=i+4*nbr+2*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype= StormUtils.MEASURE.TYPE.VA;
            dataidx=i+2*nbr+nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype= StormUtils.MEASURE.TYPE.VM;
            dataidx=i+4*nbr+3*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));
        }

        for (int i = 0; i < nbr; i++) {
            mid=Integer.parseInt(brids.get(i));
            mtype= StormUtils.MEASURE.TYPE.PF;
            dataidx=i;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype= StormUtils.MEASURE.TYPE.QF;
            dataidx=i+2*nbr+2*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype= StormUtils.MEASURE.TYPE.PT;
            dataidx=i+nbr;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype= StormUtils.MEASURE.TYPE.QT;
            dataidx=i+3*nbr+2*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));
        }

        return res;

    }

}
