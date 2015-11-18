package thu.instcloud.app.se.common;

import MatOperation.MatOperation;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import static thu.instcloud.app.se.common.Utils.Mat.getMatOperation;

/**
 * Created on 2015/11/13.
 */
public class OperationChain {

//    currently we use a static operation in all operation chain instances, in such way we can reduce the time for
//    initializing the operation instance. However, synchronization can be a problem.

    //    remember all operations except getArray and setArrayClone will dispose the original matrix
    private static MatOperation operation;

    private MWNumericArray array;

    public OperationChain() {

    }

    //    By default if array is instance of OperationChain we will dispose array after it is changed
    public OperationChain(Object array) {

        this();

        if (array instanceof OperationChain) {

            newOperationChain(array, false);

        } else {

            newOperationChain(array, true);

        }

    }

    public OperationChain(Object array, boolean clone) {

        newOperationChain(array, clone);

    }

    //    if clone is false array will be disposed after operation
    public void newOperationChain(Object array, boolean clone) {

        MWNumericArray arg;

        if (array instanceof OperationChain) {

            arg = ((OperationChain) array).getArray();

        } else {

            arg = (MWNumericArray) array;

        }

        try {

            if (clone) {

                this.array = (MWNumericArray) arg.clone();

            } else {

                this.array = arg;

            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        if (operation==null){

            operation = getMatOperation();

        }

    }

    public OperationChain transpose(){

        try {
            clearSetArray((MWNumericArray) operation.transposeJ(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain toSparse(){

        try {
            clearSetArray((MWNumericArray) operation.toSparse(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    //    default not dispose
    public OperationChain subtract(Object array1) {

//        if array1 is instance of OperationChain we assume it is a intermediate operand and dispose it automatically
        if (array1 instanceof OperationChain) {

            return subtract(array1, true);

        }

        return subtract(array1, false);

    }

    public OperationChain subtract(Object array1, boolean dispose) {

        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }

        try {
            clearSetArray((MWNumericArray) operation.substract(1, array, arg)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        if (dispose && arg instanceof MWNumericArray) {

            ((MWNumericArray) arg).dispose();

        }

        return this;

    }

    public OperationChain add(Object array1) {

        if (array1 instanceof OperationChain) {

            return add(array1, true);

        }

        return add(array1, false);

    }

    public OperationChain add(Object array1, boolean dispose) {
        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }
        try {
            clearSetArray((MWNumericArray) operation.add(1, array, arg)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        if (dispose && arg instanceof MWNumericArray) {

            ((MWNumericArray) arg).dispose();

        }

        return this;

    }

    public OperationChain multiply(Object array1) {

        if (array1 instanceof OperationChain) {

            return multiply(array1, true);

        }
        return multiply(array1, false);

    }

    public OperationChain multiply(Object array1, boolean dispose) {
        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }
        try {
            clearSetArray((MWNumericArray) operation.multiply(1, array, arg)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        if (dispose && arg instanceof MWNumericArray) {

            ((MWNumericArray) arg).dispose();

        }

        return this;

    }

    public OperationChain solveLinear(Object array1) {
        if (array1 instanceof OperationChain) {

            return solveLinear(array1, true);

        }
        return solveLinear(array1, false);

    }

    public OperationChain solveLinear(Object b, boolean dispose) {

        Object arg;

        if (b instanceof OperationChain) {

            arg = ((OperationChain) b).getArray();

        } else {

            arg = b;

        }

        try {
            clearSetArray((MWNumericArray) operation.solveLinear(1, array, arg)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        if (dispose && arg instanceof MWNumericArray) {

            ((MWNumericArray) arg).dispose();

        }

        return this;

    }

    public OperationChain conj(){

        try {
            clearSetArray((MWNumericArray) operation.conjugate(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain multiplyByElement(Object array1) {
        if (array1 instanceof OperationChain) {

            return multiplyByElement(array1, true);

        }
        return multiplyByElement(array1, false);

    }

    public OperationChain multiplyByElement(Object array1, boolean dispose) {

        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }

        try {
            clearSetArray((MWNumericArray) operation.multiplyByElement(1, array, arg)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        if (dispose && arg instanceof MWNumericArray) {

            ((MWNumericArray) arg).dispose();

        }

        return this;

    }

    public OperationChain getReal() {

        try {
            clearSetArray((MWNumericArray) operation.getReal(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain getImag() {

        try {
            clearSetArray((MWNumericArray) operation.getImag(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    //    only when all arrays are OperationChain we dispose automatically
    public OperationChain mergeRow(Object... arrays) {

        for (Object array : arrays) {

            if (!(array instanceof OperationChain)) {

                return mergeRow(false, arrays);

            }

        }

        return mergeRow(true, arrays);
    }

    public OperationChain mergeRow(boolean dispose, Object... arrays) {

        Object arg;

        try {

            for (int i = 0; i < arrays.length; i++) {

                if (arrays[i] instanceof OperationChain) {

                    arg = ((OperationChain) arrays[i]).getArray();

                } else {

                    arg = arrays[i];

                }

                clearSetArray((MWNumericArray) operation.mergeRow(1, array, arg)[0]);

                if (dispose && arg instanceof MWNumericArray) {

                    ((MWNumericArray) arg).dispose();

                }

            }

        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    //    only when all arrays are OperationChain we dispose automatically
    public OperationChain mergeColumn(Object... arrays) {

        for (Object array : arrays) {

            if (!(array instanceof OperationChain)) {

                return mergeColumn(false, arrays);

            }

        }

        return mergeColumn(true, arrays);

    }

    public OperationChain mergeColumn(boolean dispose, Object... arrays) {

        Object arg;

        try {
            for (int i = 0; i < arrays.length; i++) {
                if (arrays[i] instanceof OperationChain) {

                    arg = ((OperationChain) arrays[i]).getArray();

                } else {

                    arg = arrays[i];

                }

                clearSetArray((MWNumericArray) operation.mergeColumn(1, array, arg)[0]);

                if (dispose && arg instanceof MWNumericArray) {

                    ((MWNumericArray) arg).dispose();

                }

            }
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain abs() {

        try {
            clearSetArray((MWNumericArray) operation.absJ(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain angleR() {

        try {
            clearSetArray((MWNumericArray) operation.angleJ(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain clone() {

        OperationChain operationChain;

        operationChain = new OperationChain(this.array);

        return operationChain;

    }

    public OperationChain ones(int r, int c) {

        try {
            clearSetArray((MWNumericArray) operation.onesJ(1, r, c)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain zeros(int r, int c) {
        try {
            clearSetArray((MWNumericArray) operation.zerosJ(1, r, c)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;
    }

    public OperationChain eye(int r, int c) {
        try {
            clearSetArray((MWNumericArray) operation.eyeJ(1, r, c)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;
    }

    public OperationChain diagonal() {

        try {
            clearSetArray((MWNumericArray) operation.diagonal(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain invert() {

        try {
            clearSetArray((MWNumericArray) operation.invert(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain divideByElement(Object array1) {
        if (array1 instanceof OperationChain) {

            return divideByElement(array1, true);

        }
        return divideByElement(array1, false);

    }

    public OperationChain divideByElement(Object array1, boolean dispose) {

        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }
        try {
            clearSetArray((MWNumericArray) operation.divideByElement(1, array, arg)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        if (dispose && arg instanceof MWNumericArray) {

            ((MWNumericArray) arg).dispose();

        }

        return this;

    }

    public OperationChain norm() {

        try {
            clearSetArray((MWNumericArray) operation.normJ(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain multiplyI() {

        try {
            clearSetArray((MWNumericArray) operation.multiplyI(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    //    normally indices should be double
    public OperationChain sparseMatrix(Object idouble, Object jdouble, Object valdouble, double rows, double cols) {

        try {
            clearSetArray((MWNumericArray) operation.sparseMatrix(1, idouble, jdouble, valdouble, rows, cols)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    //    input should be array
    public OperationChain selectRows(Object list) {

        try {
            clearSetArray((MWNumericArray) operation.selectRows(1, array, list)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain selectColumns(Object list) {

        try {
            clearSetArray((MWNumericArray) operation.selectColumns(1, array, list)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain selectSubMatrix(Object rowIds, Object colIds) {

        try {
            clearSetArray((MWNumericArray) operation.selectRows(1, array, rowIds)[0]);
            clearSetArray((MWNumericArray) operation.selectColumns(1, array, colIds)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain maxIn2D() {

        try {
            clearSetArray((MWNumericArray) operation.maxJ(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain sum() {

        try {
            clearSetArray((MWNumericArray) operation.sumJ(1, array)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain sum(Object array1) {

        if (array1 instanceof OperationChain) {

            return sum(array1, true);

        }

        return sum(array1, false);

    }

    public OperationChain sum(Object array1, boolean dispose) {

        Object arg;

        if (array1 instanceof OperationChain) {

            arg = ((OperationChain) array1).getArray();

        } else {

            arg = array1;

        }
        try {
            clearSetArray((MWNumericArray) operation.sumJ(1, arg)[0]);
        } catch (MWException e) {
            e.printStackTrace();
        }

        if (dispose && arg instanceof MWNumericArray) {

            ((MWNumericArray) arg).dispose();

        }

        return this;

    }


    public MWNumericArray getArray() {
        return array;
    }

    private void clearSetArray(MWNumericArray array) {

        if (this.array != null) {

//            MWNumericArray can not be collected by garbage collector, we need to dispose it manually
            this.array.dispose();

        }

        this.array = array;

    }

    public OperationChain setArrayClone(MWNumericArray array) {
//      to avoid further operation to dispose the matrix
        try {
            this.array = (MWNumericArray) array.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return this;

    }

    public void dispose() {

        if (array != null) {
            array.dispose();
        }

    }

}
