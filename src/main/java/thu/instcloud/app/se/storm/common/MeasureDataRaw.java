package thu.instcloud.app.se.storm.common;

import backtype.storm.tuple.Values;

import java.util.Random;

/**
 * Created by hjh on 15-12-28.
 */
public class MeasureDataRaw extends MeasureData {
    private static Random random;
    private double msigma;

    public MeasureDataRaw(){
        random=new Random();
    }

    public MeasureDataRaw(String caseid,int mid,String mtype,double mvalue,double msigma){
        this();
        this.caseid=caseid;
        this.mid=mid;
        this.mtype=mtype;
        this.mvalue=mvalue;
        this.msigma = msigma;
    }

    public Values toTrueMeasureValues(){
        return new Values(caseid,mtype,mid, mvalue);
    }

    public Values toMeasureValues(){
        return new Values(caseid,mtype,mid,random.nextGaussian() * msigma + mvalue);
    }

    public double getMsigma() {
        return msigma;
    }

    public void setMsigma(double msigma) {
        this.msigma = msigma;
    }
}
