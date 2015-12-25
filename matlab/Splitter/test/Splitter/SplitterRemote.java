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

import com.mathworks.toolbox.javabuilder.pooling.Poolable;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>SplitterRemote</code> class provides a Java RMI-compliant interface to the 
 * M-functions from the files:
 * <pre>
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\BFSDivideGraph.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\mergeZones.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\piecewise.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\reassignZone.m
 *  F:\\projects\\state-estimation-mat-piec-api\\graph\\splitLargeZone.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>SplitterRemote</code> 
 * instance when it is no longer needed to ensure that native resources allocated by this 
 * class are properly freed, and the server-side proxy is unexported.  (Failure to call 
 * dispose may result in server-side threads not being properly shut down, which often 
 * appears as a hang.)  
 *
 * This interface is designed to be used together with 
 * <code>com.mathworks.toolbox.javabuilder.remoting.RemoteProxy</code> to automatically 
 * generate RMI server proxy objects for instances of Splitter.Splitter.
 */
public interface SplitterRemote extends Poolable
{
    /**
     * Provides the standard interface for calling the <code>BFSDivideGraph</code> 
     * M-function with 2 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * M-documentation as provided by the author of the M function:
     * <pre>
     * % from http://blog.csdn.net/zhangzhengyi03539/article/details/47858979
     * % 连通分量(广度优先搜索)
     * % Graph 图连通矩阵,无向图，对称矩阵
     * </pre>
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] BFSDivideGraph(int nargout, Object... rhs) throws RemoteException;
    /**
     * Provides the standard interface for calling the <code>mergeZones</code> M-function 
     * with 3 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * M-documentation as provided by the author of the M function:
     * <pre>
     * % [F_BUS, T_BUS, BR_R, BR_X, BR_B, RATE_A, RATE_B, RATE_C, ...
     * %     TAP, SHIFT, BR_STATUS, PF, QF, PT, QT, MU_SF, MU_ST, ...
     * %     ANGMIN, ANGMAX, MU_ANGMIN, MU_ANGMAX] = idx_brch;
     * </pre>
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] mergeZones(int nargout, Object... rhs) throws RemoteException;
    /**
     * Provides the standard interface for calling the <code>piecewise</code> M-function 
     * with 4 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * M-documentation as provided by the author of the M function:
     * <pre>
     * % add branch ids
     * </pre>
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] piecewise(int nargout, Object... rhs) throws RemoteException;
    /**
     * Provides the standard interface for calling the <code>reassignZone</code> 
     * M-function with 4 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * M-documentation as provided by the author of the M function:
     * <pre>
     * %% define named indices into bus, branch matrices
     * </pre>
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] reassignZone(int nargout, Object... rhs) throws RemoteException;
    /**
     * Provides the standard interface for calling the <code>splitLargeZone</code> 
     * M-function with 4 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * M-documentation as provided by the author of the M function:
     * <pre>
     * % the first zone is the reference bus
     * </pre>
     *
     * @param nargout Number of outputs to return.
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] splitLargeZone(int nargout, Object... rhs) throws RemoteException;
  
    /** Frees native resources associated with the remote server object */
    void dispose() throws RemoteException;
}
