package thu.instcloud.app.se.estimator;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.common.OperationChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static thu.instcloud.app.se.common.Utils.Mat.toMeasurementVector;

/**
 * Created on 2015/11/7.
 */
public class MeasureSystem {

    private PowerSystem powerSystem;

    private MWNumericArray sfCplx;

    private MWNumericArray stCplx;

    private MWNumericArray sbusCplx;

    private MWNumericArray VpfmReal;

    private MWNumericArray VpfaReal;

    private MWNumericArray zTrueReal;

    private MWNumericArray zmReal;

    private MWNumericArray sigmaReal;

    private MWNumericArray WInvReal;

    private double fullscale;

    private int nz;

    private List<Integer> zIds;

    private List<Integer> VbusIds;

    private List<Integer> stateIds;

    private List<Integer> zFullIds;

    private List<Integer> VbusFullIds;

    private List<Integer> VmVaFullIds;

    private Random random;

    public MeasureSystem(PowerSystem powerSystem) {

        this.powerSystem = powerSystem;

        zIds = new ArrayList<Integer>();

        VbusIds = new ArrayList<Integer>();

        stateIds = new ArrayList<Integer>();

        zFullIds = new ArrayList<Integer>();

        VbusFullIds = new ArrayList<Integer>();

        VmVaFullIds = new ArrayList<Integer>();

        fullscale = 30;

        random = new Random();

        sfCplx = powerSystem.getEstimator().getSfCplx();

        stCplx = powerSystem.getEstimator().getStCplx();

        sbusCplx = powerSystem.getPowerFlow().getSbus();

        VpfmReal = powerSystem.getPowerFlow().getVm();

        VpfaReal = powerSystem.getPowerFlow().getVa();

        nz = 4 * powerSystem.getMpData().getnBranch() + 4 * powerSystem.getMpData().getnBus();

        int[] dims = {nz, 1};

        zmReal = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);

        importTrueMeasurement();

        generateSigma();

        computeWInv();

        computeIndices();

        powerSystem.getEstimator().setWInvReal(WInvReal);

        print();

    }

    public void print() {

        if (powerSystem.getOption().getVerbose() > 1) {

            System.out.print("\nReal measurement:\n" + zTrueReal.toString());

        }

    }

    private void importTrueMeasurement() {

        zTrueReal = toMeasurementVector(sfCplx, stCplx, sbusCplx, VpfaReal, VpfmReal);

    }

    private void generateSigma() {

        sigmaReal = new OperationChain(sfCplx).abs().multiply(0.02).add(0.0052 * fullscale).mergeColumn(
                new OperationChain(stCplx).abs().multiply(0.02).add(0.0052 * fullscale),
                new OperationChain(sbusCplx).abs().multiply(0.02).add(0.0052 * fullscale),
                new OperationChain().ones(powerSystem.getMpData().getnBus(), 1).multiply(0.2 * Math.PI / 180 * 3),
                new OperationChain(sfCplx).abs().multiply(0.02).add(0.0052 * fullscale),
                new OperationChain(stCplx).abs().multiply(0.02).add(0.0052 * fullscale),
                new OperationChain(sbusCplx).abs().multiply(0.02).add(0.0052 * fullscale),
                new OperationChain(VpfmReal).multiply(0.02).add(0.0052 * 1.1)).multiply(1 / 3.0).getArray();

    }

    public void measure() {

        int[] dims = {1, 1};

        for (int i = 0; i < nz; i++) {

            dims[0] = i + 1;

            zmReal.set(dims, getMeasureI(i + 1));

        }

    }

    private double getMeasureI(int i) {

        int[] dims = {i, 1};

        if (powerSystem.getOption().isDebug()) {

//            use true measurement to debug
            return zTrueReal.getDouble(dims);

        }

        return random.nextGaussian() * sigmaReal.getDouble(dims) + zTrueReal.getDouble(dims);

    }

    private void computeWInv() {

        int[] rowidx = new int[nz];

        double[] values = new double[nz];

        int[] dims = {1, 1};

        double sig;

        for (int i = 0; i < nz; i++) {

            dims[0] = i + 1;

            rowidx[i] = i + 1;

            sig = sigmaReal.getDouble(dims);

            values[i] = 1 / sig / sig;

        }

        WInvReal = MWNumericArray.newSparse(rowidx, rowidx, values, nz, nz, MWClassID.DOUBLE);

    }

    private void computeIndices() {

        List<Integer> excludeIdxSf = new ArrayList<Integer>();

        List<Integer> excludeIdxSt = new ArrayList<Integer>();

        List<Integer> VbusExcludeIds = new ArrayList<Integer>();

        List<Integer> zExcludeIds = new ArrayList<Integer>();

        List<Integer> VmVaExcludeIds = new ArrayList<Integer>();

        int nbr = powerSystem.getMpData().getnBranch();

        int nb = powerSystem.getMpData().getnBus();

        int refNumI = powerSystem.getMpData().getBusData().getNrefI();

        int[] I = powerSystem.getMpData().getBranchData().getI();

        int[] J = powerSystem.getMpData().getBranchData().getJ();

        Map<Integer, Integer> TOI = powerSystem.getMpData().getBusData().getTOI();

        for (int i = 0; i < nbr; i++) {

            if (TOI.get(I[i]) == refNumI) {

                excludeIdxSf.add(i);

            }

            if (TOI.get(J[i]) == refNumI) {

                excludeIdxSt.add(i);

            }

        }

        VbusExcludeIds.add(refNumI - 1);

        zExcludeIds.clear();

//        include magnitude and angle
        VmVaExcludeIds.clear();

        int exIdx;

        for (int i = 0; i < excludeIdxSf.size(); i++) {

            exIdx = excludeIdxSf.get(i);

//            Pf
            zExcludeIds.add(exIdx);

//            Qf
            zExcludeIds.add(exIdx + 2 * (nbr + nb));

        }

        for (int i = 0; i < excludeIdxSt.size(); i++) {

            exIdx = excludeIdxSt.get(i);

//            Pt
            zExcludeIds.add(exIdx + nbr);

//            Qt
            zExcludeIds.add(exIdx + 3 * nbr + 2 * nb);

        }

        for (int i = 0; i < VbusExcludeIds.size(); i++) {

            exIdx = VbusExcludeIds.get(i);

//            Pbus
            zExcludeIds.add(exIdx + 2 * nbr);

//            Qbus
            zExcludeIds.add(exIdx + 4 * nbr + 2 * nb);

//            Va
            zExcludeIds.add(exIdx + 2 * nbr + nb);

//            Vm
            zExcludeIds.add(exIdx + 4 * nbr + 3 * nb);

            VmVaExcludeIds.add(exIdx);

            VmVaExcludeIds.add(nb + exIdx);

        }

        getFullIds();

        zIds = getValidIds(zFullIds, zExcludeIds);

        stateIds = getValidIds(VmVaFullIds, VmVaExcludeIds);

        VbusIds = getValidIds(VbusFullIds, VbusExcludeIds);

    }

    private void getFullIds() {

        int nb = powerSystem.getMpData().getnBus();

        zFullIds.clear();

        VbusFullIds.clear();

        VmVaFullIds.clear();

//        matlab index start from 1
        for (int i = 1; i <= nz; i++) {

            zFullIds.add(i);

        }

        for (int i = 1; i <= nb; i++) {

            VbusFullIds.add(i);

        }

        for (int i = 1; i <= 2 * nb; i++) {

            VmVaFullIds.add(i);

        }

    }

    private List<Integer> getValidIds(List<Integer> fullIds, List<Integer> excIds) {

        List<Integer> ret = new ArrayList<Integer>();

        int idx;

//        make sure all ids are in ascending order
        for (int i = 0; i < fullIds.size(); i++) {

            idx = fullIds.get(i);

//            excluded indices start from 0, full indices start from 1 so need to minus 1
            if (!excIds.contains(idx - 1)) {

                ret.add(idx);

            }

        }

        return ret;

    }

    public int getNz() {
        return nz;
    }

    public MWNumericArray getWInvReal() {
        return WInvReal;
    }

    public MWNumericArray getZmReal() {
        return zmReal;
    }

    public List<Integer> getzIds() {
        return zIds;
    }

    public List<Integer> getVbusIds() {
        return VbusIds;
    }

    public List<Integer> getStateIds() {
        return stateIds;
    }
}
