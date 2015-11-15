package thu.instcloud.app.se.estimator;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thu.instcloud.app.se.common.Constants;
import thu.instcloud.app.se.common.OperationChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static thu.instcloud.app.se.common.Utils.Common.getContinuousIds;
import static thu.instcloud.app.se.common.Utils.Mat.disposeMatrix;
import static thu.instcloud.app.se.common.Utils.Mat.toMeasurementVector;

/**
 * Created on 2015/11/7.
 */
public class Estimator {

    private static Logger logger = LoggerFactory.getLogger(Estimator.class);

    private MWNumericArray Vs;

    private MWNumericArray Yf;

    private MWNumericArray Yt;

    private MWNumericArray dSbDvmCplx;

    private MWNumericArray dSbDvaCplx;

    private MWNumericArray dSfDvmCplx;

    private MWNumericArray dSfDvaCplx;

    private MWNumericArray dStDvmCplx;

    private MWNumericArray dStDvaCplx;

    private MWNumericArray SfCplx;

    private MWNumericArray StCplx;

    private MWNumericArray HFReal;

    private MWNumericArray WInvReal;

    private MWNumericArray VpfNormCplx;

    private MWNumericArray VpfNormMatrixCplx;

    private MWNumericArray VpfCplx;

    private MWNumericArray VpfMatrixCplx;

    private PowerSystem powerSystem;

    private boolean converged;

    private boolean oneBadAtATime;

    private int ibad;

    private int maxItBadData;

    private int maxIt;

    private double badDataThreshold;

    public Estimator(PowerSystem powerSystem) {

        this.powerSystem = powerSystem;

        Yf = powerSystem.getyMatrix().getYf();

        Yt = powerSystem.getyMatrix().getYt();

        Vs = powerSystem.getState();

        computeDSbusDv();

        computeDSbrDv();

        composeFullHMatrix();

        oneBadAtATime = false;

        maxItBadData = 50;

        maxIt = 100;

        badDataThreshold = 6.25;

        print();

    }

    public void estimate() {

        OperationChain singleOp = new OperationChain();

        converged = false;

        WInvReal = powerSystem.getMeasureSystem().getWInvReal();

        ibad = 1;

        boolean hasBadData;

        MWNumericArray zestReal = computeEstimatedMeasurement(powerSystem);

        MWNumericArray deltzReal = new OperationChain(powerSystem.getMeasureSystem().getZmReal())
                .subtract(zestReal).getArray();

        MWNumericArray normFReal = new OperationChain(deltzReal).transpose()
                .multiply(WInvReal).multiply(deltzReal).getArray();

        if (powerSystem.getOption().isVerbose()) {

            System.out.printf("\n it     norm( F )       step size");

            System.out.printf("\n----  --------------  --------------");

            System.out.printf("\n%3d    %10.3f      %10.3f", 0, normFReal.get(1), 0.0);

        }

        while (!converged && ibad < maxItBadData) {

            hasBadData = false;

            MWNumericArray HH = getHH(HFReal);

            MWNumericArray WWInv = getWWInv(WInvReal);

            MWNumericArray ddeltz = getDdeltz(deltzReal);

            MWNumericArray VVs = getVVs(powerSystem.getState());

            MWNumericArray VVsa = new OperationChain(VVs).angleR().getArray();

            MWNumericArray VVsm = new OperationChain(VVs).abs().getArray();

            int i = 0;

            while (!converged && i++ < maxIt) {

                MWNumericArray b = singleOp.setArrayClone(HH).transpose().multiply(WWInv).multiply(ddeltz).getArray();

                MWNumericArray A = singleOp.setArrayClone(HH).transpose().multiply(WWInv).multiply(HH).getArray();

                MWNumericArray dx = singleOp.setArrayClone(A).solveLinear(b).getArray();

                VVsa = singleOp.setArrayClone(VVsa).add(getDdxa(dx)).getArray();

                VVsm = singleOp.setArrayClone(VVsm).add(getDdxm(dx)).getArray();

                refreshState(powerSystem, VVsa, VVsm, powerSystem.getMeasureSystem().getVbusIds());

                zestReal = computeEstimatedMeasurement(powerSystem);

                deltzReal = singleOp.setArrayClone(powerSystem.getMeasureSystem().getZmReal()).subtract(zestReal).getArray();

                ddeltz = getDdeltz(deltzReal);

                normFReal = singleOp.setArrayClone(ddeltz).transpose().multiply(WWInv).multiply(ddeltz).getArray();

                MWNumericArray dx2 = singleOp.setArrayClone(dx).transpose().multiply(dx).getArray();

                if (powerSystem.getOption().isVerbose()) {

                    System.out.printf("\n%3d    %10.3f      %10.3e", i, normFReal.getDouble(1), dx2.getDouble(1));

                }

                if (dx2.getDouble(1) < Constants.ESTIMATOR.TOL) {

                    converged = true;

                    if (powerSystem.getOption().isVerbose()) {

                        System.out.printf("\nState estimator converged in %d iterations.\n", i);

                    }

                }

            }

            if (!converged && powerSystem.getOption().isVerbose()) {

                System.out.printf("\nState estimator did not converged in %d iterations.\n", i);

            }

//            checking bad data
            List<Integer> baddata = badDataRecognition(WWInv, HH, ddeltz, oneBadAtATime);

            if (baddata.size() > 0) {

                hasBadData = true;

                converged = false;

                updateZIds(baddata, powerSystem.getMeasureSystem().getzIds());

            }

            if (!hasBadData) {

                converged = true;

                if (powerSystem.getOption().isVerbose()) {

                    logger.info("No remaining bad data, after discarding data {} time(s).", ibad - 1);

                }

            }

            ibad++;

        }

    }

    //    WARNNING: all exclude should be sorted in ascending order
    private void updateZIds(List<Integer> baddata, List<Integer> zIds) {

        List<Integer> ret = new ArrayList<Integer>();

        for (int i = 0; i < zIds.size(); i++) {

            if (baddata.contains(i)) {

                continue;

            }

            ret.add(zIds.get(i));

        }

        zIds.clear();

        zIds.addAll(ret);

    }

    //    just return the index
    private List<Integer> badDataRecognition(MWNumericArray WWInv, MWNumericArray HH, MWNumericArray ddeltz,
                                             boolean oneBadAtATime) {

        OperationChain op = new OperationChain();

        MWNumericArray WW = op.setArrayClone(WWInv).invert().getArray();

        MWNumericArray HTWHInv = op.setArrayClone(HH).transpose().multiply(WWInv).multiply(HH).invert().getArray();

        MWNumericArray WR = op.setArrayClone(WW).subtract(new OperationChain(HH).multiply(HTWHInv).multiply(
                new OperationChain(HH).transpose()).multiply(0.95)).getArray();

        MWNumericArray WRInvDiagVec = new OperationChain().eye(1, 1).divideByElement(new OperationChain(WR).diagonal()).getArray();

        MWNumericArray rn2 = op.setArrayClone(ddeltz).multiplyByElement(ddeltz).multiplyByElement(WRInvDiagVec).getArray();

        double maxBad = op.setArrayClone(rn2).maxIn2D().getArray().getDouble(1);

        List<Integer> ret = new ArrayList<Integer>();

        double threshold, tmp;

        if (oneBadAtATime) {

            threshold = maxBad;

        } else {

            threshold = badDataThreshold;

        }

        int[] idx = {1, 1};

        for (int i = 0; i < rn2.getDimensions()[0]; i++) {

            idx[0] = i + 1;

            tmp = rn2.getDouble(idx);

            if (tmp >= threshold) {

//                this is the index of the z list that should be excluded
                ret.add(i);

                if (oneBadAtATime) {

                    break;

                }

            }

        }

        return ret;

    }

    //    ids should be in ascending order
    private void refreshState(PowerSystem powerSystem, MWNumericArray vvsa, MWNumericArray vvsm, List<Integer> Vids) {

        MWNumericArray vs = powerSystem.getState();

        int[] idx = {1, 1};

        int[] idxVnew = {1, 1};

        double ph, mag;

        for (int i = 0; i < Vids.size(); i++) {

            idx[0] = Vids.get(i);

            idxVnew[0] = i + 1;

            ph = vvsa.getDouble(idxVnew);

            mag = vvsm.getDouble(idxVnew);

            vs.set(idx, mag * Math.cos(ph));

            vs.setImag(idx, mag * Math.sin(ph));

        }

    }

    private MWNumericArray getDdxa(MWNumericArray dx) {

        if (dx.getDimensions()[0] % 2 != 0 || dx.getDimensions()[1] != 1) {

            logger.error("Number of state variable should be even and dx should be single column!");

            return null;

        }

        return new OperationChain(dx).selectRows(getContinuousIds(1, dx.getDimensions()[0] / 2).toArray()).getArray();

    }

    private MWNumericArray getDdxm(MWNumericArray dx) {

        if (dx.getDimensions()[0] % 2 != 0 || dx.getDimensions()[1] != 1) {

            logger.error("Number of state variable should be even and dx should be single column!");

            return null;

        }

        return new OperationChain(dx).
                selectRows(getContinuousIds(dx.getDimensions()[0] / 2 + 1, dx.getDimensions()[0]).toArray()).getArray();

    }

    private MWNumericArray getHH(MWNumericArray HF) {

        return new OperationChain(HF).selectSubMatrix(
                powerSystem.getMeasureSystem().getzIds().toArray(),
                powerSystem.getMeasureSystem().getStateIds().toArray()).getArray();

    }

    private MWNumericArray getWWInv(MWNumericArray WInv) {

        return new OperationChain(WInv).selectSubMatrix(
                powerSystem.getMeasureSystem().getzIds().toArray(),
                powerSystem.getMeasureSystem().getzIds().toArray()).getArray();

    }

    private MWNumericArray getDdeltz(MWNumericArray deltz) {

        return new OperationChain(deltz).selectRows(
                powerSystem.getMeasureSystem().getzIds().toArray()).getArray();

    }

    private MWNumericArray getVVs(MWNumericArray Vs) {

        return new OperationChain(Vs).selectRows(
                powerSystem.getMeasureSystem().getVbusIds().toArray()).getArray();

    }

    public MWNumericArray computeEstimatedMeasurement(PowerSystem powerSystem) {

        MWNumericArray Vsf = getVft(
                powerSystem.getMpData().getBranchData().getI(),
                powerSystem.getState(),
                powerSystem.getMpData().getBusData().getTOI());

        MWNumericArray Vst = getVft(
                powerSystem.getMpData().getBranchData().getJ(),
                powerSystem.getState(),
                powerSystem.getMpData().getBusData().getTOI());

        MWNumericArray sfe, ste, sbuse;

        MWNumericArray Vsa, Vsm;

        sfe = new OperationChain(Vsf).multiplyByElement(new OperationChain(powerSystem.getyMatrix().getYf())
                .multiply(Vs).conj()).getArray();

        ste = new OperationChain(Vst).multiplyByElement(new OperationChain(powerSystem.getyMatrix().getYt())
                .multiply(Vs).conj()).getArray();

        sbuse = new OperationChain(Vs).multiplyByElement(new OperationChain(powerSystem.getyMatrix().getYbus())
                .multiply(Vs).conj()).getArray();

        Vsa = new OperationChain(Vs).angleR().getArray();

        Vsm = new OperationChain(Vs).abs().getArray();

        MWNumericArray ret = toMeasurementVector(
                sfe,
                ste,
                sbuse,
                Vsa,
                Vsm);

        disposeMatrix(Vsf, Vst, sfe, ste, sbuse, Vsa, Vsm);

        return ret;

    }

    public void print() {

        if (!powerSystem.getOption().isVerbose()) {

            return;

        }

        printWithTitle("dSbus_dVm", dSbDvmCplx);

        printWithTitle("dSbus_dVa", dSbDvaCplx);

        printWithTitle("dSf_dVm", dSfDvmCplx);

        printWithTitle("dSf_dVa", dSfDvaCplx);

        printWithTitle("dSt_dVm", dStDvmCplx);

        printWithTitle("dSt_dVa", dStDvaCplx);

        printWithTitle("SfCplx", SfCplx);

        printWithTitle("StCplx", StCplx);

        printWithTitle("Full H matrix", HFReal);

    }

    public void printWithTitle(String title, MWNumericArray matrix) {

        System.out.print("\n" + title + "\n");
        System.out.print(matrix + "\n");

    }

    /*  this is the measurement jacobi matrix
    *          ang1      ang2 ....   angNb   Vm1   Vm2 ....  VmNb
    *  PF1  dPF1/dAng1
    *  PF2
    *  .
    *  .
    *  PFN
    *  PT1
    *  .
    *  .
    *  PTN
    *  PB1
    *  .
    *  .
    *  PBN
    *  ang1
    *  ang2
    *  .
    *  .
    *  angNb
    *  QF1
    *  .
    *  .
    *  QFN
    *  QT1
    *  .
    *  .
    *  QTN
    *  QB1
    *  .
    *  .
    *  QBN
    *  Vm1
    *  .
    *  .
    *  VmNb
    *
    *
    * */
    private void composeFullHMatrix() {

        int nb = powerSystem.getMpData().getnBus();

//        use OperationChain to dispose matrix automatically
        OperationChain eyenb = new OperationChain().eye(nb, nb);

        OperationChain zeronb = new OperationChain().zeros(nb, nb);

        OperationChain HFRealCol1 = new OperationChain(new OperationChain(dSfDvaCplx).getReal())
                .mergeColumn(
                        new OperationChain(dStDvaCplx).getReal(),
                        new OperationChain(dSbDvaCplx).getReal(),
                        eyenb,
                        new OperationChain(dSfDvaCplx).getImag(),
                        new OperationChain(dStDvaCplx).getImag(),
                        new OperationChain(dSbDvaCplx).getImag(),
                        zeronb);

//        former operation dispose the matrix so we need to recreate the matrix
        eyenb = new OperationChain().eye(nb, nb);

        zeronb = new OperationChain().zeros(nb, nb);

        OperationChain HFRealCol2 = new OperationChain(new OperationChain(dSfDvmCplx).getReal())
                .mergeColumn(
                        new OperationChain(dStDvmCplx).getReal(),
                        new OperationChain(dSbDvmCplx).getReal(),
                        zeronb,
                        new OperationChain(dSfDvmCplx).getImag(),
                        new OperationChain(dStDvmCplx).getImag(),
                        new OperationChain(dSbDvmCplx).getImag(),
                        eyenb);

        HFReal = new OperationChain(HFRealCol1).mergeRow(HFRealCol2).getArray();

    }

    private void computeDSbrDv() {

        MWNumericArray If, It, IfMatrix, ItMatrix, Vf, Vt, VfNorm, VtNorm, VfMatrix, VtMatrix;

        If = new OperationChain(Yf).multiply(VpfCplx).getArray();

        It = new OperationChain(Yt).multiply(VpfCplx).getArray();

        IfMatrix = new OperationChain(If).diagonal().getArray();

        ItMatrix = new OperationChain(It).diagonal().getArray();

        Vf = getVft(powerSystem.getMpData().getBranchData().getI(), VpfCplx, powerSystem.getMpData().getBusData().getTOI());

        Vt = getVft(powerSystem.getMpData().getBranchData().getJ(), VpfCplx, powerSystem.getMpData().getBusData().getTOI());

        VfNorm = getVft(powerSystem.getMpData().getBranchData().getI(), VpfNormCplx, powerSystem.getMpData().getBusData().getTOI());

        VtNorm = getVft(powerSystem.getMpData().getBranchData().getJ(), VpfNormCplx, powerSystem.getMpData().getBusData().getTOI());

        VfMatrix = new OperationChain(Vf).diagonal().getArray();

        VtMatrix = new OperationChain(Vt).diagonal().getArray();

        int NBranch = powerSystem.getMpData().getnBranch();

        int NBus = powerSystem.getMpData().getnBus();

        double[] idxBranch = new double[NBranch];

        double[] idxBusFInter = new double[NBranch];

        double[] idxBusTInter = new double[NBranch];

        for (int i = 0; i < NBranch; i++) {

            idxBranch[i] = i + 1;

            idxBusFInter[i] = powerSystem.getMpData().getBusData().getTOI().get(
                    powerSystem.getMpData().getBranchData().getI()[i]);

            idxBusTInter[i] = powerSystem.getMpData().getBusData().getTOI().get(
                    powerSystem.getMpData().getBranchData().getJ()[i]);

        }

        MWNumericArray VNbrNbF, VNbrNbT, VNbrNbNormF, VNbrNbNormT;

        VNbrNbF = new OperationChain().sparseMatrix(idxBranch, idxBusFInter, Vf, NBranch, NBus).getArray();

        VNbrNbT = new OperationChain().sparseMatrix(idxBranch, idxBusTInter, Vt, NBranch, NBus).getArray();

        VNbrNbNormF = new OperationChain().sparseMatrix(idxBranch, idxBusFInter, VfNorm, NBranch, NBus).getArray();

        VNbrNbNormT = new OperationChain().sparseMatrix(idxBranch, idxBusTInter, VtNorm, NBranch, NBus).getArray();

        dSfDvaCplx = new OperationChain(IfMatrix).conj().multiply(VNbrNbF).subtract(
                new OperationChain(VfMatrix).multiply(
                        new OperationChain(Yf).multiply(VpfMatrixCplx).conj())
        ).multiplyI().getArray();

        dSfDvmCplx = new OperationChain(VfMatrix).multiply(new OperationChain(Yf).multiply(VpfNormMatrixCplx).conj())
                .add(new OperationChain(IfMatrix).conj().multiply(VNbrNbNormF)).getArray();

        dStDvaCplx = new OperationChain(ItMatrix).conj().multiply(VNbrNbT).subtract(
                new OperationChain(VtMatrix).multiply(new OperationChain(Yt).multiply(VpfMatrixCplx).conj()))
                .multiplyI().getArray();

        dStDvmCplx = new OperationChain(VtMatrix).multiply(new OperationChain(Yt).multiply(VpfNormMatrixCplx).conj())
                .add(new OperationChain(ItMatrix).conj().multiply(VNbrNbNormT)).getArray();

        SfCplx = new OperationChain(Vf).multiplyByElement(new OperationChain(If).conj()).getArray();

        StCplx = new OperationChain(Vt).multiplyByElement(new OperationChain(It).conj()).getArray();

        disposeMatrix(If, It, IfMatrix, ItMatrix, Vf, Vt, VfNorm, VtNorm, VfMatrix, VtMatrix,
                VNbrNbF, VNbrNbT, VNbrNbNormF, VNbrNbNormT);

    }

    private void computeDSbusDv() {

        MWNumericArray IbusMatrix;

        VpfCplx = powerSystem.getPowerFlow().getV();

        VpfNormCplx = computeVnorm(VpfCplx);

        VpfMatrixCplx = new OperationChain(VpfCplx).diagonal().getArray();

        IbusMatrix = new OperationChain(
                new OperationChain(powerSystem.getyMatrix().getYbus()).multiply(VpfCplx))
                .diagonal().getArray();

        VpfNormMatrixCplx = new OperationChain(VpfNormCplx).diagonal().getArray();

        dSbDvmCplx = new OperationChain(VpfMatrixCplx).multiply(
                new OperationChain(powerSystem.getyMatrix().getYbus()).multiply(VpfNormMatrixCplx).conj()
        ).add(new OperationChain(IbusMatrix).conj().multiply(VpfNormMatrixCplx)).getArray();

        dSbDvaCplx = new OperationChain(VpfMatrixCplx).multiplyI().multiply(new OperationChain(IbusMatrix).subtract(
                new OperationChain(powerSystem.getyMatrix().getYbus()).multiply(VpfMatrixCplx)
                ).conj()
        ).getArray();

//        dispose local instance
        IbusMatrix.dispose();

    }

    private MWNumericArray computeVnorm(MWNumericArray v) {

        return new OperationChain(v).divideByElement(new OperationChain(v).abs()).getArray();

    }

    //    input are external numbers, will convert to internal numbering
    private MWNumericArray getVft(int[] ijExternal, MWNumericArray VvecCplx, Map<Integer, Integer> TOI) {

        if (VvecCplx.getDimensions()[1] != 1) {

            logger.error("Not a vector!");

            return null;

        }

        int n = ijExternal.length;

        int[] dims = {n, 1};

        int[] ids = {1, 1};

        int[] valueIds = {1, 1};

        MWNumericArray res = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.COMPLEX);

        int internalIdx;

        for (int i = 0; i < n; i++) {

            internalIdx = TOI.get(ijExternal[i]);

            ids[0] = i + 1;

            valueIds[0] = internalIdx;

            res.set(ids, VvecCplx.get(valueIds));

            res.setImag(ids, VvecCplx.getImag(valueIds));

        }

        return res;

    }

    public MWNumericArray getdSbDvaCplx() {
        return dSbDvaCplx;
    }

    public MWNumericArray getdSbDvmCplx() {
        return dSbDvmCplx;
    }

    public MWNumericArray getSfCplx() {
        return SfCplx;
    }

    public MWNumericArray getStCplx() {
        return StCplx;
    }

}
