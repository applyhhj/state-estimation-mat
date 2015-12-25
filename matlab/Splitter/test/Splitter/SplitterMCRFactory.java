/*
 * MATLAB Compiler: 5.1 (R2014a)
 * Date: Fri Dec 25 22:06:19 2015
 * Arguments: "-B" "macro_default" "-W" "java:Splitter,Splitter" "-T" "link:lib" "-d" 
 * "F:\\projects\\state-estimation-mat\\matlab\\Splitter\\test" "-v" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\BFSDivideGraph.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\mergeZones.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\piecewise.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\reassignZone.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\splitLargeZone.m" 
 * "class{Splitter:F:\\projects\\state-estimation-mat-piec-api\\graph\\BFSDivideGraph.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\mergeZones.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\piecewise.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\reassignZone.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\splitLargeZone.m}" 
 * "-a" "F:\\projects\\state-estimation-mat-grid-piecewise\\diffRep.m" "-a" 
 * "F:\\projects\\matpower5.1\\e2i_data.m" "-a" "F:\\projects\\matpower5.1\\e2i_field.m" 
 * "-a" "F:\\projects\\matpower5.1\\ext2int.m" "-a" 
 * "F:\\projects\\matpower5.1\\get_reorder.m" "-a" 
 * "F:\\projects\\state-estimation-mat-grid-piecewise\\getBusType.m" "-a" 
 * "F:\\projects\\matpower5.1\\idx_brch.m" "-a" "F:\\projects\\matpower5.1\\idx_bus.m" 
 * "-a" "F:\\projects\\matpower5.1\\idx_gen.m" "-a" 
 * "F:\\projects\\state-estimation-mat-grid-piecewise\\intersectRep.m" "-a" 
 * "F:\\projects\\matpower5.1\\run_userfcn.m" 
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
    private static final String sComponentId = "Splitter_BCFBBE63E9BB8B4945A027A21EB5F183";
    
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
