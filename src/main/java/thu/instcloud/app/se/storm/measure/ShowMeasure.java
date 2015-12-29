package thu.instcloud.app.se.storm.measure;

import com.mathworks.toolbox.javabuilder.MWStructArray;
import redis.clients.jedis.*;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-28.
 */
public class ShowMeasure {
    static JedisPool jedisPool;
    static String redisIp= StormUtils.REDIS.REDIS_SERVER_IP;
    static String pass= StormUtils.REDIS.PASS;

    public static void main(String[] args) {
        jedisPool=new JedisPool(new JedisPoolConfig(),redisIp);
        String caseid="case2869pegase";

        String mPbusKey=mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, StormUtils.MEASURE.TYPE.VA);
        Map<String,String> ret;

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(pass);
            ret=jedis.hgetAll(mPbusKey);
        }

        for (Map.Entry<String,String> e:ret.entrySet()){
            System.out.printf("%10s %30.6f\n",e.getKey(),Double.parseDouble(e.getValue())*180/Math.PI);
        }

        System.out.println(ret.size());
    }

    public static void printMeasures(String caseid){
        List<MeasureDataRaw> meas=importTrueMeasurement(caseid);
        for (MeasureDataRaw data :
                meas) {
            if (data.getMtype().equals(StormUtils.MEASURE.TYPE.VM)){
                System.out.printf("%10d %10.6f\n",data.getMid(),data.getMvalue());
            }
        }
    }


    public static List<MeasureDataRaw> importTrueMeasurement(String caseid){
        List<MeasureDataRaw> res=new ArrayList<>();

        List<Response<byte[]>> zonesRes=new ArrayList<>();
        List<Response<List<String>>> ii2eListRes=new ArrayList<>();
        List<Response<List<String>>> bridsListRes=new ArrayList<>();
        int nz;

        try (Jedis jedis=jedisPool.getResource()){
            jedis.auth(pass);

            nz=Integer.parseInt(jedis.get(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES, StormUtils.REDIS.KEYS.NUM_OF_ZONES)));
            Pipeline p=jedis.pipelined();
            for (int i = 0; i < nz; i++) {
                zonesRes.add(p.get(mkByteKey(caseid, StormUtils.REDIS.KEYS.ZONES,i+"")));
                ii2eListRes.add(p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES,i+"", StormUtils.REDIS.KEYS.BUS_NUM_OUT),0,-1));
                bridsListRes.add(p.lrange(mkKey(caseid, StormUtils.REDIS.KEYS.ZONES,i+"", StormUtils.REDIS.KEYS.BRANCH_IDS),0,-1));
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

    private static List<MeasureDataRaw> retrieveData(String caseid,MWStructArray zone,List<String> ii2e,List<String> brids){
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
