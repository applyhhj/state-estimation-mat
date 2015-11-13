package thu.instcloud.app.se.estimator;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.common.EstimationOption;
import thu.instcloud.app.se.mpdata.MPData;

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

        for (int i = 0; i < mpData.getnBus(); i++) {

            state.set(i + 1, 1);

            state.setImag(i + 1, 0);

        }

    }

    public void run() {

        measureSystem.measure();

        estimator.estimate();

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

    //    use a complex matrix to store state in polar axis, real part is the magnitude and imaginary part is the phase angle
//    public BasicMatrix printStateInExternalInPolarDegree() {
//
//        System.out.print("\nBusNum       Vm(p.u.)        Va(degree)\n");
//
//        basicComplexMatrixBuilder = basicComplexMatrixFactory.getBuilder((int) state.countRows());
//
//        PhysicalStore<ComplexNumber> cplxState = state.toComplexStore();
//
//        for (int i = 0; i < state.countRows(); i++) {
//
//            for (int j = 0; j < state.countColumns(); j++) {
//
//                basicComplexMatrixBuilder.set(i, j, new ComplexNumber(
//                        cplxState.get(i, j).getModulus(),
//                        cplxState.get(i, j).phase()));
//
//            }
//
//        }
//
//        List<Integer> sortExternalBusNum = new ArrayList<Integer>();
//
//        for (Integer i : mpData.getBusData().getTOI().keySet()) {
//
//            sortExternalBusNum.add(i);
//
//        }
//
//        sortExternalBusNum.sort(Utils.Common.comparator);
//
//        BasicMatrix ret = basicComplexMatrixBuilder.build();
//
//        PhysicalStore<ComplexNumber> retCplx = ret.toComplexStore();
//
//        int internalNum;
//
//        for (int i = 0; i < sortExternalBusNum.size(); i++) {
//
//            internalNum = mpData.getBusData().getTOI().get(sortExternalBusNum.get(i));
//
//            System.out.printf("%5d %8.4f   %8.4f\n", sortExternalBusNum.get(i),
//                    retCplx.get(internalNum - 1, 0).getReal(),
//                    retCplx.get(internalNum - 1, 0).getImaginary() * 180 / Math.PI);
//
//        }
//
//        return ret;
//
//    }

}
