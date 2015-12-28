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
import thu.instcloud.app.se.storm.splitter.SplitterUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkByteKey;
import static thu.instcloud.app.se.storm.splitter.SplitterUtils.mkKey;

/**
 * Created by hjh on 15-12-28.
 */
public class MeasurementRSpout extends JedisRichSpout {
    private String caseid ="case2869pegase";
    private List<MeasureDataRaw> rawMeasures;
    private int midx;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                SplitterUtils.STORM.FIELDS.CASE_ID,
                SplitterUtils.STORM.FIELDS.MEASURE_TYPE,
                SplitterUtils.STORM.FIELDS.MEASURE_ID,
                SplitterUtils.STORM.FIELDS.MEASURE_VALUE
        ));
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        super.open(map, topologyContext, spoutOutputCollector);
        rawMeasures=importTrueMeasurement(caseid);
        midx=0;
    }

    @Override
    public void nextTuple() {
        if (rawMeasures.size()>0){
            collector.emit(rawMeasures.get((midx++)%rawMeasures.size()).toTrueMeasureValues());
        }
        Utils.sleep(5);
    }

    public MeasurementRSpout(String redisIp,String pass) {
        super(redisIp,pass);
    }

    public List<MeasureDataRaw> importTrueMeasurement(String caseid){
        List<MeasureDataRaw> res=new ArrayList<>();

        List<Response<byte[]>> zonesRes=new ArrayList<>();
        List<Response<List<String>>> ii2eListRes=new ArrayList<>();
        List<Response<List<String>>> bridsListRes=new ArrayList<>();
        int nz;

        try (Jedis jedis=jedisPool.getResource()){
            auth(jedis);

            nz=Integer.parseInt(jedis.get(mkKey(caseid, SplitterUtils.REDIS.KEYS.ZONES, SplitterUtils.REDIS.KEYS.NUM)));
            Pipeline p=jedis.pipelined();
            for (int i = 0; i < nz; i++) {
                zonesRes.add(p.get(mkByteKey(caseid, SplitterUtils.REDIS.KEYS.ZONES,i+"")));
                ii2eListRes.add(p.lrange(mkKey(caseid, SplitterUtils.REDIS.KEYS.ZONES,i+"", SplitterUtils.REDIS.KEYS.BUS_NUM_OUT),0,-1));
                bridsListRes.add(p.lrange(mkKey(caseid, SplitterUtils.REDIS.KEYS.ZONES,i+"", SplitterUtils.REDIS.KEYS.BRANCH_IDS),0,-1));
            }
            p.sync();
        }

        for (int i = 0; i < nz; i++) {
            MWStructArray zone=(MWStructArray) MWStructArray.deserialize(zonesRes.get(i).get());
            List<String> ii2eList=ii2eListRes.get(i).get();
            List<String> bridsList=bridsListRes.get(i).get();
            res.addAll(retrieveData(caseid,zone,ii2eList,bridsList));
            zone.dispose();
        }

        return res;
    }

    private List<MeasureDataRaw> retrieveData(String caseid,MWStructArray zone,List<String> ii2e,List<String> brids){
        List<MeasureDataRaw> res=new ArrayList<>();
        double[][] ztrue=(double[][])zone.get(SplitterUtils.MW.FIELDS.Z_TRUE,1);
        double[][] sigma=(double[][])zone.get(SplitterUtils.MW.FIELDS.SIGMA,1);
        int nb=ii2e.size();
        int nbr=brids.size();

        int mid,dataidx;
        String mtype;
        double mvalue,msigma;
        for (int i = 0; i < nb; i++) {
            mid=Integer.parseInt(ii2e.get(i));
            mtype=SplitterUtils.MEASURE.TYPE.PBUS;
            dataidx=i+2*nbr;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype=SplitterUtils.MEASURE.TYPE.QBUS;
            dataidx=i+4*nbr+2*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype=SplitterUtils.MEASURE.TYPE.VA;
            dataidx=i+2*nbr+nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype=SplitterUtils.MEASURE.TYPE.VM;
            dataidx=i+4*nbr+3*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));
        }

        for (int i = 0; i < nbr; i++) {
            mid=Integer.parseInt(brids.get(i));
            mtype=SplitterUtils.MEASURE.TYPE.PF;
            dataidx=i;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype=SplitterUtils.MEASURE.TYPE.QF;
            dataidx=i+2*nbr+2*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype=SplitterUtils.MEASURE.TYPE.PT;
            dataidx=i+nbr;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));

            mtype=SplitterUtils.MEASURE.TYPE.QT;
            dataidx=i+3*nbr+2*nb;
            mvalue=ztrue[dataidx][0];
            msigma=sigma[dataidx][0];
            res.add(new MeasureDataRaw(caseid,mid,mtype,mvalue,msigma));
        }

        return res;

    }
}
