/*
 * MATLAB Compiler: 5.1 (R2014a)
 * Date: Wed Nov 18 10:26:08 2015
 * Arguments: "-B" "macro_default" "-W" "java:MatOperation,MatOperation" "-T" "link:lib" 
 * "-d" "F:\\projects\\state-estimation-mat\\matlab\\MatOperation\\for_testing" "-v" 
 * "F:\\projects\\state-estimation-mat\\matlab\\absJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\add.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\angleJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\conjugate.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\diagonal.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\divideByElement.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\eyeJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\getImag.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\getReal.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\invert.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\maxJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\mergeColumn.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\mergeRow.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\multiply.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\multiplyByElement.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\multiplyI.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\normJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\onesJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\selectColumns.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\selectRows.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\solveLinear.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\sparseMatrix.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\substract.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\sumJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\toSparse.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\transposeJ.m" 
 * "F:\\projects\\state-estimation-mat\\matlab\\zerosJ.m" 
 * "class{MatOperation:F:\\projects\\state-estimation-mat\\matlab\\absJ.m,F:\\projects\\state-estimation-mat\\matlab\\add.m,F:\\projects\\state-estimation-mat\\matlab\\angleJ.m,F:\\projects\\state-estimation-mat\\matlab\\conjugate.m,F:\\projects\\state-estimation-mat\\matlab\\diagonal.m,F:\\projects\\state-estimation-mat\\matlab\\divideByElement.m,F:\\projects\\state-estimation-mat\\matlab\\eyeJ.m,F:\\projects\\state-estimation-mat\\matlab\\getImag.m,F:\\projects\\state-estimation-mat\\matlab\\getReal.m,F:\\projects\\state-estimation-mat\\matlab\\invert.m,F:\\projects\\state-estimation-mat\\matlab\\maxJ.m,F:\\projects\\state-estimation-mat\\matlab\\mergeColumn.m,F:\\projects\\state-estimation-mat\\matlab\\mergeRow.m,F:\\projects\\state-estimation-mat\\matlab\\multiply.m,F:\\projects\\state-estimation-mat\\matlab\\multiplyByElement.m,F:\\projects\\state-estimation-mat\\matlab\\multiplyI.m,F:\\projects\\state-estimation-mat\\matlab\\normJ.m,F:\\projects\\state-estimation-mat\\matlab\\onesJ.m,F:\\projects\\state-estimation-mat\\matlab\\selectColumns.m,F:\\projects\\state-estimation-mat\\matlab\\selectRows.m,F:\\projects\\state-estimation-mat\\matlab\\solveLinear.m,F:\\projects\\state-estimation-mat\\matlab\\sparseMatrix.m,F:\\projects\\state-estimation-mat\\matlab\\substract.m,F:\\projects\\state-estimation-mat\\matlab\\sumJ.m,F:\\projects\\state-estimation-mat\\matlab\\toSparse.m,F:\\projects\\state-estimation-mat\\matlab\\transposeJ.m,F:\\projects\\state-estimation-mat\\matlab\\zerosJ.m}" 
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
    private static final String sComponentId = "MatOperation_E82B9F86F739576C40CE862389BA6AF9";
    
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
