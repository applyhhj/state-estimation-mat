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

import com.mathworks.toolbox.javabuilder.pooling.Poolable;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>SplitterRemote</code> class provides a Java RMI-compliant interface to the 
 * M-functions from the files:
 * <pre>
 *  /home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/piecewise.m
 *  /home/hjh/projects/state-estimation-mat-piec-api/apiv1/graph/reassignZone.m
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
  
    /** Frees native resources associated with the remote server object */
    void dispose() throws RemoteException;
}
