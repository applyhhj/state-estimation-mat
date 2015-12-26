/*
 * MATLAB Compiler: 5.1 (R2014a)
 * Date: Sat Dec 26 12:02:46 2015
 * Arguments: "-B" "macro_default" "-W" "java:Splitter,Splitter" "-T" "link:lib" "-d" 
 * "/home/hjh/projects/state-estimation-mat/matlab/Splitter/test" "-v" 
 * "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/piecewise.m" 
 * "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/reassignZone.m" 
 * "class{Splitter:/home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/piecewise.m,/home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/reassignZone.m}" 
 * "-a" "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/BFSDivideGraph.m" 
 * "-a" "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/extra/diffRep.m" "-a" 
 * "/home/hjh/software/matpower5.1/e2i_data.m" "-a" 
 * "/home/hjh/software/matpower5.1/e2i_field.m" "-a" 
 * "/home/hjh/software/matpower5.1/ext2int.m" "-a" 
 * "/home/hjh/software/matpower5.1/get_reorder.m" "-a" 
 * "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/extra/getBusType.m" "-a" 
 * "/home/hjh/software/matpower5.1/idx_brch.m" "-a" 
 * "/home/hjh/software/matpower5.1/idx_bus.m" "-a" 
 * "/home/hjh/software/matpower5.1/idx_gen.m" "-a" 
 * "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/extra/intersectRep.m" "-a" 
 * "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/mergeZones.m" "-a" 
 * "/home/hjh/software/matpower5.1/run_userfcn.m" "-a" 
 * "/home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/splitLargeZone.m" "-a" 
 * "/home/hjh/projects/state-estimation-mat-piec-api/graph/splitLargeZone.m" 
 */

package Splitter;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class SplitterMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "Splitter_5359120D2AD87B5A6D982D0C74C57970";
    
    /** Component name */
    private static final String sComponentName = "Splitter";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(SplitterMCRFactory.class)
        );
    
    
    private SplitterMCRFactory()
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
            SplitterMCRFactory.class, 
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
