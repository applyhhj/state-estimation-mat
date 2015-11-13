package thu.instcloud.app.se.common;

import MatOperation.MatOperation;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import static thu.instcloud.app.se.common.Utils.Mat.getMatOperation;

/**
 * Created on 2015/11/13.
 */
public class OperationChain {

    private MWNumericArray array;

//    currently we use a static operation in all operation chain instances, in such way we can reduce the time for
//    initializing the operation instance. However, synchronization can be a problem.
    private static MatOperation operation;

    public OperationChain(MWNumericArray array){

        this.array=array;

        if (operation==null){

            this.operation=getMatOperation();

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

    public OperationChain subtract(MWNumericArray array1){

        try {
            array=(MWNumericArray)operation.substract(1,array,array1)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain add(MWNumericArray array1){

        try {
            array=(MWNumericArray)operation.add(1,array,array1)[0];
        } catch (MWException e) {
            e.printStackTrace();
        }

        return this;

    }

    public OperationChain multiply(MWNumericArray array1){

        try {
            array=(MWNumericArray)operation.multiply(1,array,array1)[0];
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

    public MWNumericArray getArray() {
        return array;
    }
}
