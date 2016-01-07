package thu.instcloud.app.se.storm.splitter;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static thu.instcloud.app.se.common.Utils.Common.*;
import static thu.instcloud.app.se.storm.common.StormUtils.OPTIONS.getDefualtOptions;
import static thu.instcloud.app.se.storm.common.StormUtils.getCaseFromFileName;

/**
 * Created by hjh on 15-12-26.
 */
public class CaseDataSpout extends BaseRichSpout {
    SpoutOutputCollector _collector;
    Random _rand;

    List<String> caseFiles;
    String casedir;
    int N;

    int caseidx;

    private boolean debug;
    private String debugcase = "case2869pegase";
    private long delay;

    public CaseDataSpout(boolean debug){
        this.debug=debug;
    }

    public CaseDataSpout(boolean debug, String caseid) {
        this.debug = debug;
        this.debugcase = caseid;
    }

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        _collector=spoutOutputCollector;
        _rand=new Random();

        if (isLinux()) {
            casedir = "/home/hjh/projects/data/";
        } else {
            casedir = "F:\\projects\\data\\matpower-data-process\\data\\";
        }

        caseFiles =getFileList(casedir);
        N=300;

        caseidx=0;

        delay=1000;
    }

    @Override
    public void nextTuple() {
        if (caseidx>=caseFiles.size()){
            System.out.println("All cases have already been emitted!");
            Utils.sleep(5000);
            return;
        }
        String currentCaseFile= caseFiles.get(caseidx++);
        if (debug) {
            currentCaseFile = debugcase + ".txt";
            delay=delay*10000;
        }
        List<String> caseDataStrs=readStringFromFile(casedir+currentCaseFile);
        System.out.printf("\nEmitted case file %5d/%d %25s.\n",caseidx,caseFiles.size(),currentCaseFile);

        _collector.emit(new Values(
                getCaseFromFileName(currentCaseFile),
                caseDataStrs,
                getDefualtOptions()
                ));

        Utils.sleep(delay);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(
                StormUtils.STORM.FIELDS.CASE_ID,
                StormUtils.STORM.FIELDS.CASE_DATA,
                StormUtils.STORM.FIELDS.OPTIONS_EST
        ));
    }
}
