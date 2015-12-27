package thu.instcloud.app.se.splitter;

import Splitter.Splitter;
import com.mathworks.toolbox.javabuilder.*;
import thu.instcloud.app.se.mpdata.MPData;

/**
 * Created on 2015/12/21.
 */
public class SplitMPData {

//    not sure if matlab java built method is thread safe or not
    private Splitter splitter;
    private MWNumericArray bus;
    private MWNumericArray gen;
    private MWNumericArray branch;
    private MWNumericArray N;
    private MWNumericArray baseMVA;
    private String caseID;
    private int Nint;
    private MPData mpData;

    private MWStructArray zones;

    public SplitMPData(MPData mpData, int N) {
        try {
            if (splitter == null) {
                splitter = new Splitter();
            }
            this.mpData = mpData;
            this.Nint = N;
            split();
        }catch (MWException e){
            e.printStackTrace();
        }

    }

//    all keeps original bus number
    public void split() throws MWException {
        clear();
        bus = mpData.getBusData().toOriginalMatArray();
        branch = mpData.getBranchData().toOriginalMatArray();
        gen = mpData.getGenData().toOriginalMatArray();
        baseMVA=mpData.getSbaseMat();
        N = initN();

        MWNumericArray newbus = (MWNumericArray) splitter.reassignZone(1, bus, gen, branch, N)[0];
        bus.dispose();
        bus=newbus;

        zones=(MWStructArray)splitter.piecewise(1,baseMVA,bus,gen,branch)[0];
    }

    private MWNumericArray initN() {
        int[] dims = {1, 1};
        MWNumericArray res = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
        res.set(1, Nint);
        return res;
    }

    public void clear() {
        if (bus != null) {
            bus.dispose();
        }
        if (gen != null) {
            gen.dispose();
        }
        if (branch != null) {
            branch.dispose();
        }
        if (N != null) {
            N.dispose();
        }
        if(zones!=null){
            zones.dispose();
        }
        if(baseMVA!=null){
            baseMVA.dispose();
        }
    }

    public MWNumericArray getBus() {
        return bus;
    }

    public MWNumericArray getGen() {
        return gen;
    }

    public MWNumericArray getBranch() {
        return branch;
    }

    public void setNint(int nint) {
        Nint = nint;
    }

    public MWStructArray getZones() {
        return zones;
    }

    public String getCaseID() {
        return caseID;
    }

    public void setCaseID(String caseID) {
        this.caseID = caseID;
    }

    public MWNumericArray getBaseMVA() {
        return baseMVA;
    }
}
