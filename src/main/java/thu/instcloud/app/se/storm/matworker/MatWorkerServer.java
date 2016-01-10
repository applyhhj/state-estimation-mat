package thu.instcloud.app.se.storm.matworker;

import org.apache.commons.cli.*;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hjh on 16-1-9.
 */
public class MatWorkerServer {

    public static MatWorkerHandler handler;
    public static MatWorkerService.Processor processor;
    private static String redisIp;
    private static String pass;
    private static String workerport;
    private static ScheduledExecutorService checkHeartBeatService;
    private static TServer server;
    private static int checkHbInterval = 60;
    private static String pid;

    public static void main(String[] args) {
        parseArgs(args);
        try {
            handler = new MatWorkerHandler(redisIp, pass);
            processor = new MatWorkerService.Processor(handler);

            Runnable worker = new Runnable() {
                @Override
                public void run() {
                    runWorker(processor);
                }
            };

            startCheckingHeartBeat();
            new Thread(worker).start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private static void runWorker(MatWorkerService.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(Integer.parseInt(workerport));
            server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
            pid = getPid();
            System.out.println("Starting the matWorker server at pid:" + pid);
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startCheckingHeartBeat() {
        checkHeartBeatService = Executors
                .newSingleThreadScheduledExecutor();

        Runnable checkHeartbeat = new Runnable() {
            public void run() {
                // task to run goes here
                if ((handler.getLastHeartBeat() + 1000 * checkHbInterval) < System.currentTimeMillis()) {
                    System.out.println("MatWorker server at pid:" + pid + " receives no heartbeat, will shutdown!");
                    server.stop();
                }
            }
        };

        checkHeartBeatService.scheduleAtFixedRate(checkHeartbeat, checkHbInterval, checkHbInterval, TimeUnit.SECONDS);
    }

    private static void parseArgs(String[] args) {
        Options options = new Options();

        options.addOption(StormUtils.MW.WORKER.HOST_ARG_NAME, "rhost", true, "host or ip of redis server");
        options.addOption(StormUtils.MW.WORKER.AUTH_ARG_NAME, "rauth", true, "password for redis server");
        options.addOption(StormUtils.MW.WORKER.WORKER_PORT_ARG_NAME, "port", true, "listening port of this worker");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption(StormUtils.MW.WORKER.HOST_ARG_NAME)) {
                redisIp = line.getOptionValue(StormUtils.MW.WORKER.HOST_ARG_NAME);
            }
            if (line.hasOption(StormUtils.MW.WORKER.AUTH_ARG_NAME)) {
                pass = line.getOptionValue(StormUtils.MW.WORKER.AUTH_ARG_NAME);
            }
            if (line.hasOption(StormUtils.MW.WORKER.WORKER_PORT_ARG_NAME)) {
                workerport = line.getOptionValue(StormUtils.MW.WORKER.WORKER_PORT_ARG_NAME);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static String getPid() throws IOException, InterruptedException {

        Vector<String> commands = new Vector<String>();
        commands.add("/bin/bash");
        commands.add("-c");
        commands.add("echo $PPID");
        ProcessBuilder pb = new ProcessBuilder(commands);

        Process pr = pb.start();
        pr.waitFor();
        if (pr.exitValue() == 0) {
            BufferedReader outReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            return outReader.readLine().trim();
        } else {
            System.out.println("Error while getting PID");
            return "";
        }
    }

    private void stop() {
        server.stop();
        checkHeartBeatService.shutdown();
    }

}
