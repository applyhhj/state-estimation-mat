/*
 * MATLAB Compiler: 5.1 (R2014a)
 * Date: Mon Dec 21 17:39:47 2015
 * Arguments: "-B" "macro_default" "-W" "java:Splitter,Splitter" "-T" "link:lib" "-d" 
 * "F:\\projects\\state-estimation-mat\\matlab\\Splitter\\test" "-v" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\BFSDivideGraph.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\mergeZones.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\piecewise.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\reassignZone.m" 
 * "F:\\projects\\state-estimation-mat-piec-api\\graph\\splitLargeZone.m" 
 * "class{Splitter:F:\\projects\\state-estimation-mat-piec-api\\graph\\BFSDivideGraph.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\mergeZones.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\piecewise.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\reassignZone.m,F:\\projects\\state-estimation-mat-piec-api\\graph\\splitLargeZone.m}" 
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
    private static final String sComponentId = "Splitter_5B64F1FA47EE1664A9F772B92D5C49A3";
    
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
