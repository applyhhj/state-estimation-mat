package thu.instcloud.app.se.storm.matworker;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hjh on 16-1-8.
 */
public class MatWorker {
    private Process process;
    private MatWorkerService.Client client;
    private TTransport transport;
    private ScheduledExecutorService heartBeatService;
    private int heartbeatInterval = 5;

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

        startHeartBeat();
    }

    private void startHeartBeat() {

        heartBeatService = Executors
                .newSingleThreadScheduledExecutor();
        Runnable heartbeat = new Runnable() {
            public void run() {
                // task to run goes here
                synchronized (transport) {
                    try {
                        openTransport();
                        client.heartbeat();
                        closeTransport();
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        heartBeatService.scheduleAtFixedRate(heartbeat, 0, heartbeatInterval, TimeUnit.SECONDS);
    }

    public int perform(String caseid, List<String> zoneids, String task) {
        int res = -1;
        synchronized (transport) {
            try {
                openTransport();
                res = client.runTask(caseid, zoneids, task);
                closeTransport();
            } catch (TException x) {
                x.printStackTrace();
            }
        }
        return res;
    }

    public void stop() {
        heartBeatService.shutdown();
        closeTransport();
        process.destroy();
    }

    private void openTransport() {
        if (!transport.isOpen()) {
            try {
                transport.open();
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeTransport() {
        if (transport.isOpen()) {
            transport.close();
        }
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
            closeTransport();
        }

        return opened;
    }

}
