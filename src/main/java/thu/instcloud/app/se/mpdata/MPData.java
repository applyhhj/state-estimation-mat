package thu.instcloud.app.se.mpdata;

import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.common.Constants;

import java.util.ArrayList;
import java.util.List;

import static thu.instcloud.app.se.common.Utils.Common.readStringFromFile;

/**
 * Created on 2015/11/6.
 */
public class MPData {

//    private static Logger logger = LoggerFactory.getLogger(MPData.class);

    private BranchData branchData;

    private BusData busData;

    private GeneratorData genData;

    private double sbase;

    private String version;

    private int nBranch;

    private int nBus;

    public MPData(List<String> data){
        init();
        importData(data);
    }

    public MPData(String filepath) {
        init();
        importData(filepath);
    }

    private void init(){
        branchData = new BranchData();
        busData = new BusData();
        genData = new GeneratorData(busData);
    }

    private void initThen(){

        busData.reorderBusNumbers(branchData);
        genData.ClassifyGenBusNumberIn();
        nBranch = branchData.getN();
        nBus = busData.getN();
    }

    private void importData(String filepath){
        List<String> fileContent = readStringFromFile(filepath);
        importData(fileContent);
    }

    private void importData(List<String>  fileContent) {

        String entry;

        List<String> data = new ArrayList<String>();

        boolean end;

        int i = 0;

        while (i < fileContent.size()) {

            entry = fileContent.get(i);

            if (entry.startsWith("#")) {

                i++;

                continue;

            }

            end = false;

            if (entry.contains(Constants.MPC.MPC_VERSION)) {

                version = fileContent.get(++i);

                if (fileContent.get(++i).contains(Constants.MPC.MPC_VERSION_END)) {

                    end = true;

                }

            } else if (entry.contains(Constants.MPC.MPC_BASEMVA)) {

                sbase = Double.parseDouble(fileContent.get(++i));

                if (fileContent.get(++i).contains(Constants.MPC.MPC_BASEMVA_END)) {

                    end = true;

                }

            } else if (entry.contains(Constants.MPC.MPC_BUS)) {

                data.clear();

                while (++i < fileContent.size()) {

                    if (fileContent.get(i).contains(Constants.MPC.MPC_BUS_END)) {

                        end = true;

                        break;

                    } else {

                        data.add(fileContent.get(i));

                    }

                }

                if (end) {

                    if (!busData.loadData(data)) {

                        break;

                    }

                }

            } else if (entry.contains(Constants.MPC.MPC_GEN)) {

                data.clear();

                while (++i < fileContent.size()) {

                    if (fileContent.get(i).contains(Constants.MPC.MPC_GEN_END)) {

                        end = true;

                        break;

                    } else {

                        data.add(fileContent.get(i));

                    }

                }

                if (end) {

                    if (!genData.loadData(data)) {

                        break;

                    }

                }

            } else if (entry.contains(Constants.MPC.MPC_BRANCH)) {

                data.clear();

                while (++i < fileContent.size()) {

                    if (fileContent.get(i).contains(Constants.MPC.MPC_BRANCH_END)) {

                        end = true;

                        break;

                    } else {

                        data.add(fileContent.get(i));

                    }

                }

                if (end) {

                    if (!branchData.loadData(data)) {

                        break;

                    }

                }

            } else {

                end = true;

            }

            if (!end) {

                System.out.print("Error: Section " + entry + " has no end");

                return;

            }

            i++;

        }

        initThen();

    }

    public MWNumericArray getSbaseMat(){
        int[] dims={1,1};
        MWNumericArray res=MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
        res.set(1,sbase);
        return res;
    }

    public double getSbase() {
        return sbase;
    }

    public BranchData getBranchData() {
        return branchData;
    }

    public BusData getBusData() {
        return busData;
    }

    public String getVersion() {
        return version;
    }

    public int getnBranch() {
        return nBranch;
    }

    public int getnBus() {
        return nBus;
    }

    public GeneratorData getGenData() {
        return genData;
    }
}

