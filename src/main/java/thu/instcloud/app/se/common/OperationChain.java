package thu.instcloud.app.se.common;

import MatOperation.MatOperation;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import org.omg.CORBA.OBJ_ADAPTER;

import static thu.instcloud.app.se.common.Utils.Mat.getMatOperation;

/**
 * Created on 2015/11/13.
 */
public class OperationChain {

//    currently we use a static operation in all operation chain instances, in such way we can reduce the time for
//    initializing the operation instance. However, synchronization can be a problem.
    private static MatOperation operation;

    private MWNumericArray array;

    public OperationChain() {

        this(null);

    }

    public OperationChain(Object array) {

        MWNumericArray arg;

        if (array instanceof OperationChain) {

            arg = ((OperationChain) array).getArray();

        } else {

            arg = (MWNumericArray) array;

        }

        this.array = arg;

        if (operation==null){

            operation = getMatOperation();

        }

    }

    public OperationChain transpose(){

        try {
            array=(MWNumericArray)operation.transposeJ(1,array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain toSparse(){

        try {
            array=(MWNumericArray)operation.toSparse(1,array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain subtract(Object array1) {

        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }

        try {
            array = (MWNumericArray) operation.substract(1, array, arg)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain add(Object array1) {
        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }
        try {
            array = (MWNumericArray) operation.add(1, array, arg)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain multiply(Object array1) {
        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }
        try {
            array = (MWNumericArray) operation.multiply(1, array, arg)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain solveLinear(MWNumericArray b){

        try {
            array=(MWNumericArray)operation.solveLinear(1,array,b)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain conj(){

        try {
            array=(MWNumericArray)operation.conjugate(1,array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain multiplyByElement(Object array1) {

        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }

        try {
            array = (MWNumericArray) operation.multiplyByElement(1, array, arg)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain getReal() {

        try {
            array = (MWNumericArray) operation.getReal(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain getImag() {

        try {
            array = (MWNumericArray) operation.getImag(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain mergeRow(Object... arrays) {

        Object arg;

        try {

            for (int i = 0; i < arrays.length; i++) {

                if (arrays[i] instanceof OperationChain) {

                    arg = ((OperationChain) arrays[i]).getArray();

                } else {

                    arg = arrays[i];

                }

                array = (MWNumericArray) operation.mergeRow(1, array, arg)[0];

            }

        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain mergeColumn(Object... arrays) {

        Object arg;

        try {
            for (int i = 0; i < arrays.length; i++) {
                if (arrays[i] instanceof OperationChain) {

                    arg = ((OperationChain) arrays[i]).getArray();

                } else {

                    arg = arrays[i];

                }
                array = (MWNumericArray) operation.mergeColumn(1, array, arg)[0];

            }
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain abs() {

        try {
            array = (MWNumericArray) operation.absJ(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain angleR() {

        try {
            array = (MWNumericArray) operation.angleJ(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain clone() {

        OperationChain operationChain = null;
        try {
            operationChain = new OperationChain(this.array.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return operationChain;

    }

    public OperationChain ones(int r, int c) {

        try {
            array = (MWNumericArray) operation.onesJ(1, r, c)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain zeros(int r, int c) {
        try {
            array = (MWNumericArray) operation.zerosJ(1, r, c)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;
    }

    public OperationChain eye(int r, int c) {
        try {
            array = (MWNumericArray) operation.eyeJ(1, r, c)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;
    }

    public OperationChain diagonal() {

        try {
            array = (MWNumericArray) operation.diagonal(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain invert() {

        try {
            array = (MWNumericArray) operation.invert(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain divideByElement(Object array1) {
        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }
        try {
            array = (MWNumericArray) operation.divideByElement(1, array, arg)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain norm() {

        try {
            array = (MWNumericArray) operation.normJ(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain multiplyI() {

        try {
            array = (MWNumericArray) operation.multiplyI(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain sparseMatrix(double[] idouble, double[] jdouble, Object valdouble, double rows, double cols) {

        try {
            array = (MWNumericArray) operation.sparseMatrix(1, idouble, jdouble, valdouble, rows, cols)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain selectRows(Object list) {

        try {
            array = (MWNumericArray) operation.selectRows(1, array, list)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain selectColumns(Object list) {

        try {
            array = (MWNumericArray) operation.selectColumns(1, array, list)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain selectSubMatrix(Object rowIds, Object colIds) {

        try {
            array = (MWNumericArray) operation.selectRows(1, array, rowIds)[0];
            array = (MWNumericArray) operation.selectColumns(1, array, colIds)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain maxIn2D() {

        try {
            array = (MWNumericArray) operation.maxJ(1, array)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public MWNumericArray getArray() {
        return array;
    }

    public OperationChain setArray(MWNumericArray array) {

        this.array = array;

        return this;
    }
}
