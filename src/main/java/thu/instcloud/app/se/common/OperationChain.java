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
    private static MatOperation operation;
    private MWNumericArray array;

    public OperationChain() {

        this(null);

    }

    public OperationChain(MWNumericArray array){

        this.array=array;

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

        try {
            array=(MWNumericArray)operation.substract(1,array,array1)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain add(Object array1) {

        try {
            array=(MWNumericArray)operation.add(1,array,array1)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain multiply(Object array1) {

        try {
            array = (MWNumericArray) operation.multiply(1, array, array1)[0];
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

    public OperationChain multiplyByElement(MWNumericArray array1){

        try {
            array=(MWNumericArray)operation.multiplyByElement(1,array,array1)[0];
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

    public OperationChain mergeRow(MWNumericArray... arrays) {

        try {

            for (int i = 0; i < arrays.length; i++) {

                array = (MWNumericArray) operation.mergeRow(1, array, arrays[i])[0];

            }

        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain mergeColumn(MWNumericArray... arrays) {

        try {
            for (int i = 0; i < arrays.length; i++) {

                array = (MWNumericArray) operation.mergeColumn(1, array, arrays[i])[0];

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

    public OperationChain clone() {

        OperationChain operationChain = null;
        try {
            operationChain = new OperationChain((MWNumericArray) this.array.clone());
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

    public MWNumericArray getArray() {
        return array;
    }
}
