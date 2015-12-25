/*
 * MATLAB Compiler: 5.1 (R2014a)
 * Date: Fri Dec 25 22:00:38 2015
 * Arguments: "-B" "macro_default" "-W" "java:MatOperation,MatOperation" "-T" "link:lib" 
 * "-d" "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\MatOperation\\for_testing" 
 * "-v" "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\absJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\add.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\angleJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\conjugate.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\diagonal.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\divideByElement.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\eyeJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\getImag.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\getReal.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\invert.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\maxJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\mergeColumn.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\mergeRow.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\multiply.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\multiplyByElement.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\multiplyI.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\normJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\onesJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\selectColumns.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\selectRows.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\solveLinear.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\sparseMatrix.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\substract.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\sumJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\toSparse.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\transposeJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\MatOp\\zerosJ.m" 
 * "class{MatOperation:F:\\projects\\state-estimation-mat\\matlab\\MatOp\\absJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\add.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\angleJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\conjugate.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\diagonal.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\divideByElement.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\eyeJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\getImag.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\getReal.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\invert.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\maxJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\mergeColumn.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\mergeRow.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\multiply.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\multiplyByElement.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\multiplyI.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\normJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\onesJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\selectColumns.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\selectRows.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\solveLinear.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\sparseMatrix.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\substract.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\sumJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\toSparse.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\transposeJ.m,F:\\projects\\state-estimation-mat\\matlab\\MatOp\\zerosJ.m}" 
 */

package MatOperation;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class MatOperationMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "MatOperation_F6695AB58A9D97D9FE964A7A169FE3FF";
    
    /** Component name */
    private static final String sComponentName = "MatOperation";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(MatOperationMCRFactory.class)
        );
    
    
    private MatOperationMCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            MatOperationMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{8,3,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
