package thu.instcloud.app.se.splitter;

import Estimator.Estimator;
import com.mathworks.toolbox.javabuilder.*;
import thu.instcloud.app.se.mpdata.MPData;

/**
 * Created on 2015/12/21.
 */
public class SplitMPData {

//    not sure if matlab java built method is thread safe or not
private Estimator estimator;
    private MWNumericArray bus;
    private MWNumericArray gen;
    private MWNumericArray branch;
    private MWNumericArray N;
    private MWNumericArray baseMVA;
    private String caseID;
    private int Nint;
    private MPData mpData;
    private int nb;
    private int nbr;

    private MWStructArray zones;

    public SplitMPData(String caseid,MPData mpData, int N) {
        try {
            if (estimator == null) {
                estimator = new Estimator();
            }
            this.mpData = mpData;
            this.Nint = N;
            this.nb = mpData.getnBus();
            this.nbr = mpData.getnBranch();
            split();
        }catch (MWException e){
            e.printStackTrace();
        }

        this.caseID=caseid;

    }

//    all keeps original bus number
    public void split() throws MWException {
        clear();
        bus = mpData.getBusData().toOriginalMatArray();
        branch = mpData.getBranchData().toOriginalMatArray();
        gen = mpData.getGenData().toOriginalMatArray();
        baseMVA=mpData.getSbaseMat();
        N = initN();

        MWNumericArray newbus = (MWNumericArray) estimator.api_split(1, bus, gen, branch, N)[0];
        bus.dispose();
        bus=newbus;

        zones = (MWStructArray) estimator.api_piecewise(1, baseMVA, bus, gen, branch)[0];
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

    public MWNumericArray getBaseMVA() {
        return baseMVA;
    }

    public MPData getMpData() {
        return mpData;
    }

    public int getNbr() {
        return nbr;
    }

    public int getNb() {
        return nb;
    }
}
