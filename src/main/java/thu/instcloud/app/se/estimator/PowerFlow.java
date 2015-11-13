package thu.instcloud.app.se.estimator;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.common.OperationChain;
import thu.instcloud.app.se.mpdata.MPData;

/**
 * Created on 2015/11/6.
 */
public class PowerFlow {

    private MPData mpData;

    private YMatrix yMatrix;
//
//    private BasicMatrix Vr;
//
//    private BasicMatrix Vi;

    private MWNumericArray PF;

    private MWNumericArray QF;

    private MWNumericArray PT;

    private MWNumericArray QT;

//    private BasicMatrix SbusP;
//
//    private BasicMatrix SbusQ;

    private MWNumericArray V;

    private MWNumericArray Vm;

    private MWNumericArray Va;

    private MWNumericArray Sbus;

    private int nbr;

    private int nb;

    public PowerFlow(MPData mpData, YMatrix yMatrix) {

        this.mpData = mpData;

        this.yMatrix = yMatrix;

        nbr=mpData.getnBranch();

        nb=mpData.getnBus();

        importV();

        importPQ();

        computeSbus();

//        print();

    }

    //    internal bus numbering
    private void computeSbus() {

        Sbus=new OperationChain(V).multiplyByElement(
                new OperationChain(yMatrix.getYbus()).multiply(V).conj().getArray()
        ).getArray();

    }

    //    internal bus numbering
    private void importV() {

        int idx;

        double vm, va;

        int[] dims={nb,1};

        Vm=MWNumericArray.newInstance(dims, MWClassID.DOUBLE,MWComplexity.REAL);

        Va=MWNumericArray.newInstance(dims, MWClassID.DOUBLE,MWComplexity.REAL);

        V=MWNumericArray.newInstance(dims, MWClassID.DOUBLE,MWComplexity.COMPLEX);

        for (int i = 0; i < nb; i++) {

            idx = mpData.getBusData().getTOA().get(mpData.getBusData().getTIO().get(i + 1));

            vm = mpData.getBusData().getVoltage()[idx];

            va = mpData.getBusData().getAngle()[idx];

//            convert to internal bus number
            Vm.set(i+1,vm);

            Va.set(i+1,va);

            V.set(i+1,vm * Math.cos(va));

            V.setImag(i+1,vm * Math.sin(va));

        }

    }

    //    for comparison
    private void importPQ() {

        int[] dims={nbr,1};

        for (int j = 0; j < 4; j++) {

            MWNumericArray tmp=MWNumericArray.newInstance(dims, MWClassID.DOUBLE,MWComplexity.REAL);

            for (int i = 0; i < mpData.getnBranch(); i++) {

                switch (j) {

                    case 0:
                        tmp.set(i+1,mpData.getBranchData().getPF()[i]);
                        break;

                    case 1:
                        tmp.set(i+1,mpData.getBranchData().getQF()[i]);
                        break;

                    case 2:
                        tmp.set(i+1,mpData.getBranchData().getPT()[i]);
                        break;

                    case 3:
                        tmp.set(i+1,mpData.getBranchData().getQT()[i]);
                        break;

                }

            }

            switch (j) {

                case 0:
                    PF = tmp;
                    break;

                case 1:
                    QF = tmp;
                    break;

                case 2:
                    PT = tmp;
                    break;

                case 3:
                    QT = tmp;
                    break;

            }

        }


    }

    private void print() {

        System.out.print("V\n" + V.toString() + "\n");

//        System.out.print("Vi\n" + Vi.toString() + "\n");

        System.out.print("Sbus\n" + Sbus.toString() + "\n");

//        System.out.print("SbusQ\n" + SbusQ.toString() + "\n");

    }

    public MWNumericArray getV() {
        return V;
    }

    public MWNumericArray getSbus() {
        return Sbus;
    }

    public MWNumericArray getVm() {
        return Vm;
    }

    public MWNumericArray getVa() {
        return Va;
    }
}
