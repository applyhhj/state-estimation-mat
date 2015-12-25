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
import java.util.*;

/**
 * The <code>Splitter</code> class provides a Java interface to the M-functions
 * from the files:
 * <pre>
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\BFSDivideGraph.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\mergeZones.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\piecewise.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\reassignZone.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\splitLargeZone.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>Splitter</code> instance 
 * when it is no longer needed to ensure that native resources allocated by this class 
 * are properly freed.
 * @version 0.0
 */
public class Splitter extends MWComponentInstance<Splitter>
{
    /**
     * Tracks all instances of this class to ensure their dispose method is
     * called on shutdown.
     */
    private static final Set<Disposable> sInstances = new HashSet<Disposable>();

    /**
     * Maintains information used in calling the <code>BFSDivideGraph</code> M-function.
     */
    private static final MWFunctionSignature sBFSDivideGraphSignature =
        new MWFunctionSignature(/* max outputs = */ 1,
                                /* has varargout = */ false,
                                /* function name = */ "BFSDivideGraph",
                                /* max inputs = */ 2,
                                /* has varargin = */ false);
    /**
     * Maintains information used in calling the <code>mergeZones</code> M-function.
     */
    private static final MWFunctionSignature sMergeZonesSignature =
        new MWFunctionSignature(/* max outputs = */ 1,
                                /* has varargout = */ false,
                                /* function name = */ "mergeZones",
                                /* max inputs = */ 3,
                                /* has varargin = */ false);
    /**
     * Maintains information used in calling the <code>piecewise</code> M-function.
     */
    private static final MWFunctionSignature sPiecewiseSignature =
        new MWFunctionSignature(/* max outputs = */ 1,
                                /* has varargout = */ false,
                                /* function name = */ "piecewise",
                                /* max inputs = */ 4,
                                /* has varargin = */ false);
    /**
     * Maintains information used in calling the <code>reassignZone</code> M-function.
     */
    private static final MWFunctionSignature sReassignZoneSignature =
        new MWFunctionSignature(/* max outputs = */ 1,
                                /* has varargout = */ false,
                                /* function name = */ "reassignZone",
                                /* max inputs = */ 4,
                                /* has varargin = */ false);
    /**
     * Maintains information used in calling the <code>splitLargeZone</code> M-function.
     */
    private static final MWFunctionSignature sSplitLargeZoneSignature =
        new MWFunctionSignature(/* max outputs = */ 1,
                                /* has varargout = */ false,
                                /* function name = */ "splitLargeZone",
                                /* max inputs = */ 4,
                                /* has varargin = */ false);

    /**
     * Shared initialization implementation - private
     */
    private Splitter (final MWMCR mcr) throws MWException
    {
        super(mcr);
        // add this to sInstances
        synchronized(Splitter.class) {
            sInstances.add(this);
        }
    }

    /**
     * Constructs a new instance of the <code>Splitter</code> class.
     */
    public Splitter() throws MWException
    {
        this(SplitterMCRFactory.newInstance());
    }
    
    private static MWComponentOptions getPathToComponentOptions(String path)
    {
        MWComponentOptions options = new MWComponentOptions(new MWCtfExtractLocation(path),
                                                            new MWCtfDirectorySource(path));
        return options;
    }
    
    /**
     * @deprecated Please use the constructor {@link #Splitter(MWComponentOptions componentOptions)}.
     * The <code>com.mathworks.toolbox.javabuilder.MWComponentOptions</code> class provides API to set the
     * path to the component.
     * @param pathToComponent Path to component directory.
     */
    public Splitter(String pathToComponent) throws MWException
    {
        this(SplitterMCRFactory.newInstance(getPathToComponentOptions(pathToComponent)));
    }
    
    /**
     * Constructs a new instance of the <code>Splitter</code> class. Use this constructor 
     * to specify the options required to instantiate this component.  The options will 
     * be specific to the instance of this component being created.
     * @param componentOptions Options specific to the component.
     */
    public Splitter(MWComponentOptions componentOptions) throws MWException
    {
        this(SplitterMCRFactory.newInstance(componentOptions));
    }
    
    /** Frees native resources associated with this object */
    public void dispose()
    {
        try {
            super.dispose();
        } finally {
            synchronized(Splitter.class) {
                sInstances.remove(this);
            }
        }
    }
  
    /**
     * Invokes the first m-function specified by MCC, with any arguments given on
     * the command line, and prints the result.
     */
    public static void main (String[] args)
    {
        try {
            MWMCR mcr = SplitterMCRFactory.newInstance();
            mcr.runMain( sBFSDivideGraphSignature, args);
            mcr.dispose();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    /**
     * Calls dispose method for each outstanding instance of this class.
     */
    public static void disposeAllInstances()
    {
        synchronized(Splitter.class) {
            for (Disposable i : sInstances) i.dispose();
            sInstances.clear();
        }
    }

    /**
     * Provides the interface for calling the <code>BFSDivideGraph</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % from http://blog.csdn.net/zhangzhengyi03539/article/details/47858979
     * % 连通分量(广度优先搜索)
     * % Graph 图连通矩阵,无向图，对称矩阵
     * </pre>
     * </p>
     * @param lhs List in which to return outputs. Number of outputs (nargout) is
     * determined by allocated size of this List. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs List containing inputs. Number of inputs (nargin) is determined
     * by the allocated size of this List. Input arguments may be passed as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or
     * as arrays of any supported Java type. Arguments passed as Java types are
     * converted to MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void BFSDivideGraph(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sBFSDivideGraphSignature);
    }

    /**
     * Provides the interface for calling the <code>BFSDivideGraph</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % from http://blog.csdn.net/zhangzhengyi03539/article/details/47858979
     * % 连通分量(广度优先搜索)
     * % Graph 图连通矩阵,无向图，对称矩阵
     * </pre>
     * </p>
     * @param lhs array in which to return outputs. Number of outputs (nargout)
     * is determined by allocated size of this array. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs array containing inputs. Number of inputs (nargin) is
     * determined by the allocated size of this array. Input arguments may be
     * passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void BFSDivideGraph(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sBFSDivideGraphSignature);
    }

    /**
     * Provides the standard interface for calling the <code>BFSDivideGraph</code>
     * M-function with 2 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % from http://blog.csdn.net/zhangzhengyi03539/article/details/47858979
     * % 连通分量(广度优先搜索)
     * % Graph 图连通矩阵,无向图，对称矩阵
     * </pre>
     * </p>
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] BFSDivideGraph(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sBFSDivideGraphSignature), 
                    sBFSDivideGraphSignature);
        return lhs;
    }
    /**
     * Provides the interface for calling the <code>mergeZones</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % [F_BUS, T_BUS, BR_R, BR_X, BR_B, RATE_A, RATE_B, RATE_C, ...
     * %     TAP, SHIFT, BR_STATUS, PF, QF, PT, QT, MU_SF, MU_ST, ...
     * %     ANGMIN, ANGMAX, MU_ANGMIN, MU_ANGMAX] = idx_brch;
     * </pre>
     * </p>
     * @param lhs List in which to return outputs. Number of outputs (nargout) is
     * determined by allocated size of this List. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs List containing inputs. Number of inputs (nargin) is determined
     * by the allocated size of this List. Input arguments may be passed as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or
     * as arrays of any supported Java type. Arguments passed as Java types are
     * converted to MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void mergeZones(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sMergeZonesSignature);
    }

    /**
     * Provides the interface for calling the <code>mergeZones</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % [F_BUS, T_BUS, BR_R, BR_X, BR_B, RATE_A, RATE_B, RATE_C, ...
     * %     TAP, SHIFT, BR_STATUS, PF, QF, PT, QT, MU_SF, MU_ST, ...
     * %     ANGMIN, ANGMAX, MU_ANGMIN, MU_ANGMAX] = idx_brch;
     * </pre>
     * </p>
     * @param lhs array in which to return outputs. Number of outputs (nargout)
     * is determined by allocated size of this array. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs array containing inputs. Number of inputs (nargin) is
     * determined by the allocated size of this array. Input arguments may be
     * passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void mergeZones(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sMergeZonesSignature);
    }

    /**
     * Provides the standard interface for calling the <code>mergeZones</code>
     * M-function with 3 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % [F_BUS, T_BUS, BR_R, BR_X, BR_B, RATE_A, RATE_B, RATE_C, ...
     * %     TAP, SHIFT, BR_STATUS, PF, QF, PT, QT, MU_SF, MU_ST, ...
     * %     ANGMIN, ANGMAX, MU_ANGMIN, MU_ANGMAX] = idx_brch;
     * </pre>
     * </p>
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] mergeZones(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sMergeZonesSignature), 
                    sMergeZonesSignature);
        return lhs;
    }
    /**
     * Provides the interface for calling the <code>piecewise</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % add branch ids
     * </pre>
     * </p>
     * @param lhs List in which to return outputs. Number of outputs (nargout) is
     * determined by allocated size of this List. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs List containing inputs. Number of inputs (nargin) is determined
     * by the allocated size of this List. Input arguments may be passed as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or
     * as arrays of any supported Java type. Arguments passed as Java types are
     * converted to MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void piecewise(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sPiecewiseSignature);
    }

    /**
     * Provides the interface for calling the <code>piecewise</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % add branch ids
     * </pre>
     * </p>
     * @param lhs array in which to return outputs. Number of outputs (nargout)
     * is determined by allocated size of this array. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs array containing inputs. Number of inputs (nargin) is
     * determined by the allocated size of this array. Input arguments may be
     * passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void piecewise(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sPiecewiseSignature);
    }

    /**
     * Provides the standard interface for calling the <code>piecewise</code>
     * M-function with 4 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % add branch ids
     * </pre>
     * </p>
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] piecewise(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sPiecewiseSignature), 
                    sPiecewiseSignature);
        return lhs;
    }
    /**
     * Provides the interface for calling the <code>reassignZone</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * %% define named indices into bus, branch matrices
     * </pre>
     * </p>
     * @param lhs List in which to return outputs. Number of outputs (nargout) is
     * determined by allocated size of this List. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs List containing inputs. Number of inputs (nargin) is determined
     * by the allocated size of this List. Input arguments may be passed as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or
     * as arrays of any supported Java type. Arguments passed as Java types are
     * converted to MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void reassignZone(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sReassignZoneSignature);
    }

    /**
     * Provides the interface for calling the <code>reassignZone</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * %% define named indices into bus, branch matrices
     * </pre>
     * </p>
     * @param lhs array in which to return outputs. Number of outputs (nargout)
     * is determined by allocated size of this array. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs array containing inputs. Number of inputs (nargin) is
     * determined by the allocated size of this array. Input arguments may be
     * passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void reassignZone(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sReassignZoneSignature);
    }

    /**
     * Provides the standard interface for calling the <code>reassignZone</code>
     * M-function with 4 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * %% define named indices into bus, branch matrices
     * </pre>
     * </p>
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] reassignZone(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sReassignZoneSignature), 
                    sReassignZoneSignature);
        return lhs;
    }
    /**
     * Provides the interface for calling the <code>splitLargeZone</code> M-function 
     * where the first input, an instance of List, receives the output of the M-function and
     * the second input, also an instance of List, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % the first zone is the reference bus
     * </pre>
     * </p>
     * @param lhs List in which to return outputs. Number of outputs (nargout) is
     * determined by allocated size of this List. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs List containing inputs. Number of inputs (nargin) is determined
     * by the allocated size of this List. Input arguments may be passed as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or
     * as arrays of any supported Java type. Arguments passed as Java types are
     * converted to MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void splitLargeZone(List lhs, List rhs) throws MWException
    {
        fMCR.invoke(lhs, rhs, sSplitLargeZoneSignature);
    }

    /**
     * Provides the interface for calling the <code>splitLargeZone</code> M-function 
     * where the first input, an Object array, receives the output of the M-function and
     * the second input, also an Object array, provides the input to the M-function.
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % the first zone is the reference bus
     * </pre>
     * </p>
     * @param lhs array in which to return outputs. Number of outputs (nargout)
     * is determined by allocated size of this array. Outputs are returned as
     * sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>.
     * Each output array should be freed by calling its <code>dispose()</code>
     * method.
     *
     * @param rhs array containing inputs. Number of inputs (nargin) is
     * determined by the allocated size of this array. Input arguments may be
     * passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     * @throws MWException An error has occurred during the function call.
     */
    public void splitLargeZone(Object[] lhs, Object[] rhs) throws MWException
    {
        fMCR.invoke(Arrays.asList(lhs), Arrays.asList(rhs), sSplitLargeZoneSignature);
    }

    /**
     * Provides the standard interface for calling the <code>splitLargeZone</code>
     * M-function with 4 input arguments.
     * Input arguments may be passed as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of
     * any supported Java type. Arguments passed as Java types are converted to
     * MATLAB arrays according to default conversion rules.
     *
     * <p>M-documentation as provided by the author of the M function:
     * <pre>
     * % the first zone is the reference bus
     * </pre>
     * </p>
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     * @return Array of length nargout containing the function outputs. Outputs
     * are returned as sub-classes of
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>. Each output array
     * should be freed by calling its <code>dispose()</code> method.
     * @throws MWException An error has occurred during the function call.
     */
    public Object[] splitLargeZone(int nargout, Object... rhs) throws MWException
    {
        Object[] lhs = new Object[nargout];
        fMCR.invoke(Arrays.asList(lhs), 
                    MWMCR.getRhsCompat(rhs, sSplitLargeZoneSignature), 
                    sSplitLargeZoneSignature);
        return lhs;
    }
}
