package thu.instcloud.app.se.estimator;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.common.EstimationOption;
import thu.instcloud.app.se.common.OperationChain;
import thu.instcloud.app.se.common.Utils;
import thu.instcloud.app.se.mpdata.MPData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static thu.instcloud.app.se.common.Utils.Mat.disposeMatrix;

/**
 * Created on 2015/11/7.
 */
public class PowerSystem {

    private String mpCaseDataPath;

    private MPData mpData;

    private YMatrix yMatrix;

    private PowerFlow powerFlow;

    private Estimator estimator;

    private MeasureSystem measureSystem;

    private MWNumericArray state;

    private EstimationOption option;

    public PowerSystem(String mpCaseDataPath, EstimationOption option) {

        if (option != null) {

            this.option = option;

        } else {

            this.option = new EstimationOption();

        }

        this.mpCaseDataPath = mpCaseDataPath;

        initData();

        this.estimator = new Estimator(this);

        this.measureSystem = new MeasureSystem(this);

    }

    public PowerSystem(String mpCaseDataPath) {

        this(mpCaseDataPath, null);

    }

    private void initData() {

        mpData = new MPData(mpCaseDataPath);

        yMatrix = new YMatrix(mpData);

        powerFlow = new PowerFlow(mpData, yMatrix);

//        flat start, include reference bus
        int[] dims = {mpData.getnBus(), 1};

        state = MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.COMPLEX);

        resetState();

    }

    public void run() {

        measureSystem.measure();

        estimator.estimate();

        if (estimator.isConverged()) {

            updateAllSystemState();

        }

    }

    private void updateAllSystemState() {

        MWNumericArray SbusNew, pdNew, qdNew, pGenNew, qGenNew;

        SbusNew = new OperationChain(state).multiplyByElement(
                new OperationChain(yMatrix.getYbus()
                ).multiply(state).conj()).getArray();

        pdNew = new OperationChain(SbusNew).selectRows(mpData.getBusData().getNpqIn().toArray())
                .getReal().multiply(-1 * mpData.getSbase()).getArray();

        qdNew = new OperationChain(SbusNew).selectRows(mpData.getBusData().getNpqIn().toArray())
                .getImag().multiply(-1 * mpData.getSbase()).getArray();

//        update pq bus injections
        mpData.getBusData().updatePD(pdNew.getDoubleData());

        mpData.getBusData().updateQD(qdNew.getDoubleData());

//        update pv bus injections, for generators PG
        pGenNew = new OperationChain(SbusNew).selectRows(mpData.getGenData().getRunGenBusNumIn().toArray())
                .getReal().multiply(mpData.getSbase()).add(new OperationChain(
                        new MWNumericArray(mpData.getBusData().getPD(), MWClassID.DOUBLE))
                        .selectRows(mpData.getGenData().getRunGenBusNumIn().toArray())).getArray();

        mpData.getGenData().updatePg(pGenNew.getDoubleData());

//        update none pq bus injections, QG
        qGenNew = new OperationChain(SbusNew).selectRows(mpData.getGenData().getRunNonePQGenBusNumIn().toArray())
                .getImag().multiply(mpData.getSbase()).add(new OperationChain(
                        new MWNumericArray(mpData.getBusData().getQD(), MWClassID.DOUBLE))
                        .selectRows(mpData.getGenData().getRunNonePQGenBusNumIn().toArray())).getArray();

        mpData.getGenData().updateQg(qGenNew.getDoubleData());

        mpData.getGenData().distributeQ();

        mpData.getGenData().updateRefBusGenP(SbusNew, mpData.getSbase());

        disposeMatrix(SbusNew, pdNew, qdNew, pGenNew, qGenNew);

    }

    public void resetState() {

        if (state == null) {

            return;

        }

        int[] idx = {1, 1};

        for (int i = 0; i < mpData.getnBus(); i++) {

            idx[0] = i + 1;

            state.set(idx, 1);

            state.setImag(idx, 0);

        }

    }

    public MPData getMpData() {
        return mpData;
    }

    public YMatrix getyMatrix() {
        return yMatrix;
    }

    public PowerFlow getPowerFlow() {
        return powerFlow;
    }

    public Estimator getEstimator() {
        return estimator;
    }

    public MeasureSystem getMeasureSystem() {
        return measureSystem;
    }

    public MWNumericArray getState() {
        return state;
    }

    public void setState(MWNumericArray state) {
        this.state = state;
    }

    public EstimationOption getOption() {
        return option;
    }

    public void printStateInExternalInPolarDegree() {

        System.out.print("\nBusNum       Vm(p.u.)        Va(degree)\n");

        MWNumericArray angles = new OperationChain(state).angleR().multiply(180 / Math.PI).getArray();

        MWNumericArray vms = new OperationChain(state).abs().getArray();

        List<Integer> sortExternalBusNum = new ArrayList<Integer>();

        for (Integer i : mpData.getBusData().getTOI().keySet()) {

            sortExternalBusNum.add(i);

        }

        Collections.sort(sortExternalBusNum, Utils.Common.comparator);


        int internalNum;

        int[] idx = {1, 1};

        for (int i = 0; i < sortExternalBusNum.size(); i++) {

            internalNum = mpData.getBusData().getTOI().get(sortExternalBusNum.get(i));

            idx[0] = internalNum;

            System.out.printf("%5d %8.4f   %8.4f\n", sortExternalBusNum.get(i),
                    vms.getDouble(idx),
                    angles.getDouble(idx));

        }

    }

}
