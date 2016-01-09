package thu.instcloud.app.se.storm.matworker;

import Estimator.Estimator;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.mathworks.toolbox.javabuilder.MWStructArray;
import org.apache.commons.cli.*;
import org.apache.commons.exec.ExecuteException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import redis.clients.jedis.*;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static thu.instcloud.app.se.storm.common.StormUtils.*;
import static thu.instcloud.app.se.storm.common.StormUtils.MW.disposeMatArrays;
import static thu.instcloud.app.se.storm.common.StormUtils.mkKey;

/**
 * Created by hjh on 16-1-8.
 */
public class MatWorker {
    private Process process;
    private MatWorkerService.Client client;
    private TTransport transport;

    public MatWorker(String redisIp, String pass, String workerHost, int workerPort) throws Exception {
//        start a server
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = MatWorkerServer.class.getCanonicalName();
        ProcessBuilder builder = new ProcessBuilder(
                javaBin, "-cp", classpath, className,
                "-rh", redisIp,
                "-ra", pass,
                "-p", workerPort + ""
        ).inheritIO();

        try {
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

//    start a client
        transport = new TSocket(workerHost, workerPort);
        TProtocol protocol = new TBinaryProtocol(transport);
        client = new MatWorkerService.Client(protocol);

        if (!serverIsOpen()) {
            throw new Exception("Server is not open!!");
        }
    }

    public int perform(String caseid, List<String> zoneids, String task) {
        int res = -1;
        try {
            transport.open();
            res = client.runTask(caseid, zoneids, task);
            transport.close();
        } catch (TException x) {
            x.printStackTrace();
        }
        return res;
    }

    public void stop() {
        if (transport.isOpen()) {
            transport.close();
        }
        process.destroy();
    }

    private boolean serverIsOpen() {
        int maxtry = 50;
        int tryinterval = 500;
        boolean opened = false;
        int i = 0;
        while (!opened && i++ < maxtry) {
            try {
                Thread.sleep(tryinterval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                transport.open();
            } catch (TTransportException e) {
                continue;
            }
            opened = true;
            transport.close();
        }

        return opened;
    }

}
