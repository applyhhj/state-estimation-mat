package thu.instcloud.app.se.mpdata;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thu.instcloud.app.se.common.Constants;
import thu.instcloud.app.se.common.OperationChain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.common.Utils.Common.toDoubleArray;
import static thu.instcloud.app.se.common.Utils.Mat.disposeMatrix;

/**
 * Created on 2015/11/6.
 */
public class GeneratorData {

    private static Logger logger = LoggerFactory.getLogger(GeneratorData.class);

    private int[] number;

    private double[] Pg;

    private double[] Qg;

    private double[] Qmax;

    private double[] Qmin;

    private double[] Vg;

    private double[] mBase;

    private int[] status;

    private double[] Pmax;

    private double[] Pmin;

    private double[] Pc1;

    private double[] Pc2;

    private double[] Qc1min;

    private double[] Qc1max;

    private double[] Qc2min;

    private double[] Qc2max;

    private double[] ramp_agc;

    private double[] ramp_10;

    private double[] ramp_30;

    private double[] ramp_q;

    private double[] apf;

    private int n;

    private int paraNum;

    private List<Integer> runGenIds;

    private List<Integer> runNonePQGenIds;

    private List<Integer> runGenBusNumIn;

    private List<Integer> runNonePQGenBusNumIn;

    private List<Integer> offGenIds;

    private BusData busData;

    public GeneratorData(BusData busData) {

        runGenIds = new ArrayList<Integer>();

        runGenBusNumIn = new ArrayList<Integer>();

        runNonePQGenIds = new ArrayList<Integer>();

        runNonePQGenBusNumIn = new ArrayList<Integer>();

        offGenIds = new ArrayList<Integer>();

        this.busData = busData;

        paraNum = 21;

        n = 0;

    }

    public boolean loadData(List<String> dataStr) {

        String[] cols;

        int ntmp = dataStr.size();

        int[] numbertmp = new int[ntmp];

        double[] Pgtmp = new double[ntmp];

        double[] Qgtmp = new double[ntmp];

        double[] Qmaxtmp = new double[ntmp];

        double[] Qmintmp = new double[ntmp];

        double[] Vgtmp = new double[ntmp];

        double[] mBasetmp = new double[ntmp];

        int[] statustmp = new int[ntmp];

        double[] Pmaxtmp = new double[ntmp];

        double[] Pmintmp = new double[ntmp];

        double[] Pc1tmp = new double[ntmp];

        double[] Pc2tmp = new double[ntmp];

        double[] Qc1mintmp = new double[ntmp];

        double[] Qc1maxtmp = new double[ntmp];

        double[] Qc2mintmp = new double[ntmp];

        double[] Qc2maxtmp = new double[ntmp];

        double[] ramp_agctmp = new double[ntmp];

        double[] ramp_10tmp = new double[ntmp];

        double[] ramp_30tmp = new double[ntmp];

        double[] ramp_qtmp = new double[ntmp];

        double[] apftmp = new double[ntmp];

        for (int i = 0; i < dataStr.size(); i++) {

            cols = dataStr.get(i).trim().split(" +");

            if (cols.length != paraNum) {

                logger.error("Incorrect data format!");

                return false;

            }

            numbertmp[i] = Integer.parseInt(cols[0]);

            Pgtmp[i] = Double.parseDouble(cols[1]);

            Qgtmp[i] = Double.parseDouble(cols[2]);

            Qmaxtmp[i] = Double.parseDouble(cols[3]);

            Qmintmp[i] = Double.parseDouble(cols[4]);

            Vgtmp[i] = Double.parseDouble(cols[5]);

            mBasetmp[i] = Double.parseDouble(cols[6]);

            statustmp[i] = Integer.parseInt(cols[7]);

            Pmaxtmp[i] = Double.parseDouble(cols[8]);

            Pmintmp[i] = Double.parseDouble(cols[9]);

            Pc1tmp[i] = Double.parseDouble(cols[10]);

            Pc2tmp[i] = Double.parseDouble(cols[11]);

            Qc1mintmp[i] = Double.parseDouble(cols[12]);

            Qc1maxtmp[i] = Double.parseDouble(cols[13]);

            Qc2mintmp[i] = Double.parseDouble(cols[14]);

            Qc2maxtmp[i] = Double.parseDouble(cols[15]);

            ramp_agctmp[i] = Double.parseDouble(cols[16]);

            ramp_10tmp[i] = Double.parseDouble(cols[17]);

            ramp_30tmp[i] = Double.parseDouble(cols[18]);

            ramp_qtmp[i] = Double.parseDouble(cols[19]);

            apftmp[i] = Double.parseDouble(cols[20]);

        }

        setN(ntmp);

        setNumber(numbertmp);

        setPg(Pgtmp);

        setQg(Qgtmp);

        setQmax(Qmaxtmp);

        setQmin(Qmintmp);

        setVg(Vgtmp);

        setmBase(mBasetmp);

        setStatus(statustmp);

        setPmax(Pmaxtmp);

        setPmin(Pmintmp);

        setPc1(Pc1tmp);

        setPc2(Pc2tmp);

        setQc1min(Qc1mintmp);

        setQc1max(Qc1maxtmp);

        setQc2min(Qc2mintmp);

        setQc2max(Qc2maxtmp);

        setRamp_agc(ramp_agctmp);

        setRamp_10(ramp_10tmp);

        setRamp_30(ramp_30tmp);

        setRamp_q(ramp_qtmp);

        setApf(apftmp);

        return true;

    }

    //    WARNING: In this program we assume all generators are in service
    public void ClassifyGenBusNumberIn() {

        runGenIds.clear();

        runNonePQGenIds.clear();

        offGenIds.clear();

        int busNumIn, busIdx;

        for (int i = 0; i < status.length; i++) {

            busNumIn = busData.getTOI().get(number[i]);

            busIdx = busData.getTOA().get(number[i]);

            if (status[i] > 0) {

                runGenIds.add(i);

                runGenBusNumIn.add(busNumIn);

                if (busData.getType()[busIdx] != Constants.MPC.BusTypes.PQ) {

                    runNonePQGenIds.add(i);

                    runNonePQGenBusNumIn.add(busNumIn);

                }

            } else {

                offGenIds.add(i);

            }

        }

    }

    public List<Integer> getRunGenBusNumIn() {
        return runGenBusNumIn;
    }

    public List<Integer> getRunNonePQGenBusNumIn() {
        return runNonePQGenBusNumIn;
    }

    public void updatePg(double[] pgNew) {

        for (int i = 0; i < runGenIds.size(); i++) {

            Pg[runGenIds.get(i)] = pgNew[i];

        }

        for (int i = 0; i < offGenIds.size(); i++) {

            Pg[offGenIds.get(i)] = 0;

        }

    }

    public void updateQg(double[] qgNew) {

        for (int i = 0; i < runNonePQGenIds.size(); i++) {

            Qg[runNonePQGenIds.get(i)] = qgNew[i];

        }

        for (int i = 0; i < offGenIds.size(); i++) {

            Qg[offGenIds.get(i)] = 0;

        }

    }

    public void distributeQ() {

        if (runNonePQGenIds.size() <= 1) {

            return;

        }

        MWNumericArray connGValues, connG, ngg, cmin, cmax, QgTot, QgMin, QgMax, genOnQ, genOnQmin, genOnQmax;

        Map<Integer, Double> qgenFixed = new HashMap<Integer, Double>();

        int nb = busData.getN();

        int ngon = runNonePQGenIds.size();

        double[] rowIds = new double[ngon];

        for (int i = 0; i < ngon; i++) {

            rowIds[i] = i + 1;

        }

        double[] runNonePQGenBusNumInArr = toDoubleArray(runNonePQGenBusNumIn);

        connGValues = new OperationChain().ones(ngon, 1).getArray();

        connG = new OperationChain().sparseMatrix(rowIds, runNonePQGenBusNumInArr, connGValues, ngon, nb).getArray();

        ngg = new OperationChain(connG).multiply(new OperationChain().sum(connG).transpose()).getArray();

        avgQg(ngg.getDoubleData());

        genOnQmax = selArrayToMat(runNonePQGenIds, Qmax);

        genOnQmin = selArrayToMat(runNonePQGenIds, Qmin);

        genOnQ = selArrayToMat(runNonePQGenIds, Qg);

        cmax = new OperationChain().sparseMatrix(rowIds, runNonePQGenBusNumInArr,
                genOnQmax, ngon, nb).getArray();

        cmin = new OperationChain().sparseMatrix(rowIds, runNonePQGenBusNumInArr,
                genOnQmin, ngon, nb).getArray();

        QgTot = new OperationChain(connG).transpose().multiply(genOnQ).getArray();

        QgMax = new OperationChain().sum(cmax).transpose().getArray();

        QgMin = new OperationChain().sum(cmin).transpose().getArray();

        findFixedGenIds(qgenFixed, connG, QgMin, QgMax);

//        release mem
        genOnQ.dispose();

        genOnQ = new OperationChain(genOnQmin).add(new OperationChain(connG).multiply(
                new OperationChain(QgTot).subtract(QgMin)
                        .divideByElement(new OperationChain(QgMax).subtract(QgMin).add(Constants.ESTIMATOR.eps))
        ).multiplyByElement(new OperationChain(genOnQmax).subtract(genOnQmin))).getArray();

        restoreQGen(qgenFixed, genOnQ);

        disposeMatrix(connGValues, connG, ngg, cmin, cmax, QgTot, QgMin, QgMax, genOnQ, genOnQmin, genOnQmax);

    }

    //        update pg for slack gens, currently we assume there is only one reference(slack) bus
//        only the first generator is responsible for adjust the power
    public void updateRefBusGenP(MWNumericArray sbus, double sbase) {

        MWNumericArray sbusNew;

        List<Integer> refGenIds = new ArrayList<Integer>();

        for (int i = 0; i < runNonePQGenBusNumIn.size(); i++) {

            if (runNonePQGenBusNumIn.get(i) == busData.getNrefI()) {

                refGenIds.add(runNonePQGenIds.get(i));

            }

        }

        sbusNew = new OperationChain(sbus).selectRows(runGenBusNumIn.toArray()).getReal().getArray();

//        internal bus numbers use natural order
        Pg[refGenIds.get(0)] = sbusNew.getDouble(refGenIds.get(0) + 1) * sbase + busData.getPD()[busData.getNrefI() - 1];

        for (int i = 1; i < refGenIds.size(); i++) {

            Pg[refGenIds.get(0)] = Pg[refGenIds.get(0)] - Pg[refGenIds.get(i)];

        }

        disposeMatrix(sbusNew);

    }

    private void avgQg(double[] ngg) {

        int idx;

        for (int i = 0; i < runNonePQGenIds.size(); i++) {

            idx = runNonePQGenIds.get(i);

            Qg[idx] = Qg[idx] / ngg[idx];

        }

    }

    private MWNumericArray selArrayToMat(List<Integer> ids, double[] values) {

        double[] selVals = new double[ids.size()];

        for (int i = 0; i < ids.size(); i++) {

            selVals[i] = values[ids.get(i)];

        }

        return new OperationChain(new MWNumericArray(selVals, MWClassID.DOUBLE)).transpose().getArray();

    }

    private void findFixedGenIds(Map<Integer, Double> data, MWNumericArray cg, MWNumericArray Qgmin, MWNumericArray Qgmax) {

        MWNumericArray qgenmin, qgenmax;

        qgenmin = new OperationChain(cg).multiply(Qgmin).getArray();

        qgenmax = new OperationChain(cg).multiply(Qgmax).getArray();

        data.clear();

        int[] dims = qgenmin.getDimensions();

        int[] ids = {1, 1};

        int idx;

        for (int i = 0; i < dims[0]; i++) {

            ids[0] = i + 1;

            if (qgenmin.get(ids) == qgenmax.get(ids)) {

                idx = runNonePQGenIds.get(i);

                data.put(idx, Qg[idx]);

            }

        }

        disposeMatrix(qgenmin, qgenmax);

    }

    private void restoreQGen(Map<Integer, Double> data, MWNumericArray QgenOn) {

        int[] idx = {1, 1};

        for (int i = 0; i < runNonePQGenIds.size(); i++) {

            idx[0] = i + 1;

            Qg[runNonePQGenIds.get(i)] = QgenOn.getDouble(idx);

        }

        for (Map.Entry<Integer, Double> e : data.entrySet()) {

            Qg[e.getKey()] = e.getValue();

        }

    }

    public MWNumericArray toOriginalMatArray(){

        int[] dims={number.length,paraNum};

        MWNumericArray array=MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);

        int[] ids=new int[2];

        for (int k = 0; k < number.length; k++) {

            ids[0]=k+1;

            ids[1]=1;

            array.set(ids,number[k]);

            ids[1]=2;

            array.set(ids,Pg[k]);

            ids[1]=3;

            array.set(ids,Qg[k]);

            ids[1]=4;

            array.set(ids,Qmax[k]);

            ids[1]=5;

            array.set(ids,Qmin[k]);

            ids[1]=6;

            array.set(ids,Vg[k]);

            ids[1]=7;

            array.set(ids,mBase[k]);

            ids[1]=8;

            array.set(ids,status[k]);

            ids[1]=9;

            array.set(ids,Pmax[k]);

            ids[1]=10;

            array.set(ids,Pmin[k]);

            ids[1]=11;

            array.set(ids,Pc1[k]);

            ids[1]=12;

            array.set(ids,Pc2[k]);

            ids[1]=13;

            array.set(ids,Qc1min[k]);

            ids[1]=14;

            array.set(ids,Qc1max[k]);

            ids[1]=15;

            array.set(ids,Qc2min[k]);

            ids[1]=16;

            array.set(ids,Qc2max[k]);

            ids[1]=17;

            array.set(ids,ramp_agc[k]);

            ids[1]=18;

            array.set(ids,ramp_10[k]);

            ids[1]=19;

            array.set(ids,ramp_30[k]);

            ids[1]=20;

            array.set(ids,ramp_q[k]);

            ids[1]=21;

            array.set(ids,apf[k]);

        }

        return array;

    }

    public int[] getNumber() {
        return number;
    }

    public void setNumber(int[] number) {
        this.number = number;
    }

    public double[] getPg() {
        return Pg;
    }

    public void setPg(double[] pg) {
        Pg = pg;
    }

    public double[] getQg() {
        return Qg;
    }

    public void setQg(double[] qg) {
        Qg = qg;
    }

    public double[] getQmax() {
        return Qmax;
    }

    public void setQmax(double[] qmax) {
        Qmax = qmax;
    }

    public double[] getQmin() {
        return Qmin;
    }

    public void setQmin(double[] qmin) {
        Qmin = qmin;
    }

    public double[] getVg() {
        return Vg;
    }

    public void setVg(double[] vg) {
        Vg = vg;
    }

    public double[] getmBase() {
        return mBase;
    }

    public void setmBase(double[] mBase) {
        this.mBase = mBase;
    }

    public int[] getStatus() {
        return status;
    }

    public void setStatus(int[] status) {
        this.status = status;
    }

    public double[] getPmax() {
        return Pmax;
    }

    public void setPmax(double[] pmax) {
        Pmax = pmax;
    }

    public double[] getPmin() {
        return Pmin;
    }

    public void setPmin(double[] pmin) {
        Pmin = pmin;
    }

    public double[] getPc1() {
        return Pc1;
    }

    public void setPc1(double[] pc1) {
        Pc1 = pc1;
    }

    public double[] getPc2() {
        return Pc2;
    }

    public void setPc2(double[] pc2) {
        Pc2 = pc2;
    }

    public double[] getQc1min() {
        return Qc1min;
    }

    public void setQc1min(double[] qc1min) {
        Qc1min = qc1min;
    }

    public double[] getQc1max() {
        return Qc1max;
    }

    public void setQc1max(double[] qc1max) {
        Qc1max = qc1max;
    }

    public double[] getQc2min() {
        return Qc2min;
    }

    public void setQc2min(double[] qc2min) {
        Qc2min = qc2min;
    }

    public double[] getQc2max() {
        return Qc2max;
    }

    public void setQc2max(double[] qc2max) {
        Qc2max = qc2max;
    }

    public double[] getRamp_agc() {
        return ramp_agc;
    }

    public void setRamp_agc(double[] ramp_agc) {
        this.ramp_agc = ramp_agc;
    }

    public double[] getRamp_10() {
        return ramp_10;
    }

    public void setRamp_10(double[] ramp_10) {
        this.ramp_10 = ramp_10;
    }

    public double[] getRamp_30() {
        return ramp_30;
    }

    public void setRamp_30(double[] ramp_30) {
        this.ramp_30 = ramp_30;
    }

    public double[] getRamp_q() {
        return ramp_q;
    }

    public void setRamp_q(double[] ramp_q) {
        this.ramp_q = ramp_q;
    }

    public double[] getApf() {
        return apf;
    }

    public void setApf(double[] apf) {
        this.apf = apf;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getParaNum() {
        return paraNum;
    }

    public void setParaNum(int paraNum) {
        this.paraNum = paraNum;
    }
}
