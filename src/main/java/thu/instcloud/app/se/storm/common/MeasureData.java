package thu.instcloud.app.se.storm.common;

import backtype.storm.tuple.Tuple;

import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 15-12-28.
 */
public class MeasureData {
    protected String caseid;
    protected String mtype;
    protected int mid;
    protected double mvalue;

    public MeasureData(){

    }

    // use factory to create
    public MeasureData(Tuple tuple) {
        mtype = tuple.getStringByField(StormUtils.STORM.FIELDS.MEASURE_TYPE);
        caseid = tuple.getStringByField(StormUtils.STORM.FIELDS.CASE_ID);
        mid = tuple.getIntegerByField(StormUtils.STORM.FIELDS.MEASURE_ID);
        mvalue = tuple.getDoubleByField(StormUtils.STORM.FIELDS.MEASURE_VALUE);
    }

    public String getKey() {
        return mkKey(caseid, StormUtils.REDIS.KEYS.MEASURE, mtype);
    }

    public String getHashKey() {
        return String.valueOf(mid);
    }

    public String getHashValue() {
        return String.valueOf(mvalue);
    }

    public String getCaseid() {
        return caseid;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public String getMtype() {
        return mtype;
    }

    public void setMtype(String mtype) {
        this.mtype = mtype;
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public double getMvalue() {
        return mvalue;
    }

    public void setMvalue(double mvalue) {
        this.mvalue = mvalue;
    }
}