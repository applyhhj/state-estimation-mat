package thu.instcloud.app.se.storm.matworker;

import org.apache.commons.cli.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import thrift.tutorial.MultiplicationService;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.List;

/**
 * Created by hjh on 16-1-9.
 */
public class MatWorkerClient {
    TTransport transport;
    TProtocol protocol;
    MatWorkerService.Client client;

    public MatWorkerClient(String serverHost, int serverPort) throws TException {
        transport = new TSocket(serverHost, serverPort);
        protocol = new TBinaryProtocol(transport);
        client = new MatWorkerService.Client(protocol);
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

}
