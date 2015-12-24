package thu.instcloud.app.se.debug;

import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.common.Constants;
import thu.instcloud.app.se.common.OperationChain;
import thu.instcloud.app.se.common.Utils;
import thu.instcloud.app.se.estimator.PowerSystem;
import thu.instcloud.app.se.mpdata.MPData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static thu.instcloud.app.se.common.Utils.Common.readStringFromFile;

/**
 * Created on 2015/11/19.
 */
public class Test {

    public static void useTestData(MWNumericArray state){

        List<String> dataStr=readStringFromFile("F:\\projects\\data\\testdata\\test.txt");

        int[] idx={1,1};

        for (int i = 0; i < dataStr.size(); i++) {

            idx[0]=i+1;

            String[] entr=dataStr.get(i).trim().split(" +");

            double vr=Double.parseDouble(entr[0]);

            double vi=Double.parseDouble(entr[1]);

            state.set(idx,vr);

            state.setImag(idx,vi);

        }

    }

    public static int getNRefIn(int[] types){

        for (int i = 0; i < types.length; i++) {

            if (types[i]== Constants.MPC.BusTypes.REF){

                return i+1;

            }

        }

        return -1;

    }

    public static void testReorderBusNumber(int[] nums,Map<Integer,Integer> TOA,Map<Integer,Integer> TOI,Map<Integer,Integer> TIO){

        TOA.clear();

        TIO.clear();

        TOI.clear();

        for (int i = 0; i < nums.length; i++) {

            TOA.put(nums[i],i);

            TIO.put(i+1,nums[i]);

            TOI.put(nums[i],i+1);

        }

    }

    //    test
    public static String resultToString(PowerSystem powerSystem){

        MPData mpData=powerSystem.getMpData();

        MWNumericArray state=powerSystem.getState();

        String out;

        out="Bus\r\n";

        MWNumericArray angles = new OperationChain(state).angleR().multiply(180 / Math.PI).getArray();

        MWNumericArray vms = new OperationChain(state).abs().getArray();

        List<Integer> sortExternalBusNum = new ArrayList<Integer>();

        for (Integer i : mpData.getBusData().getTOI().keySet()) {

            sortExternalBusNum.add(i);

        }

        Collections.sort(sortExternalBusNum, Utils.Common.comparator);

        int internalNum;

        int[] idx = {1, 1};

        for (int i = 0; i < sortExternalBusNum.size(); i++) {

            internalNum = mpData.getBusData().getTOI().get(sortExternalBusNum.get(i));

            idx[0] = internalNum;

            out = out+String.format("%5d %8.4f %8.4f\r\n", sortExternalBusNum.get(i),
                    vms.getDouble(idx),
                    angles.getDouble(idx));

        }

        out=out+"Bus.End\r\n"+"Branch\r\n";

        for (int i = 0; i < mpData.getnBranch(); i++) {

            out = out+String.format("%5d %5d %5d    %8.4f %8.4f %8.4f %8.4f\r\n",
                    i + 1,
                    mpData.getBranchData().getI()[i],
                    mpData.getBranchData().getJ()[i],
                    mpData.getBranchData().getPFe()[i],
                    mpData.getBranchData().getQFe()[i],
                    mpData.getBranchData().getPTe()[i],
                    mpData.getBranchData().getQTe()[i]);

        }

        out=out+"Branch.End\r\n";

        return out;

    }

    public static void printInOutNum(MWNumericArray array, Map<Integer,Integer> TOI,boolean real){

        List<Integer> sortExternalBusNum = new ArrayList<Integer>();

        for (Integer i :TOI.keySet()) {

            sortExternalBusNum.add(i);

        }

        Collections.sort(sortExternalBusNum, Utils.Common.comparator);

        int internalNum;

        int cols=array.getDimensions()[1];

        int[] idx = {1, 1};

        String out=null;

        for (int i = 0; i < sortExternalBusNum.size(); i++) {

            for (int j = 0; j < cols; j++) {

                internalNum = TOI.get(sortExternalBusNum.get(i));

                idx[0] = internalNum;

                idx[1]=j+1;

                if (!real){

                    out = String.format("%8.4f+%8.4fi", array.getDouble(idx),array.getImagDouble(idx));

                }else {

                    out=String.format("%8.4f", array.getDouble(idx));

                }


            }

            System.out.printf(out + "\n");

        }

    }


    public static void printInOutNum(double[] array, Map<Integer,Integer> TOI){

        List<Integer> sortExternalBusNum = new ArrayList<Integer>();

        for (Integer i :TOI.keySet()) {

            sortExternalBusNum.add(i);

        }

        Collections.sort(sortExternalBusNum, Utils.Common.comparator);

        int internalNum;

        String out=null;

        for (int i = 0; i < sortExternalBusNum.size(); i++) {

                internalNum = TOI.get(sortExternalBusNum.get(i));

                out = String.format("%8.4f", array[internalNum-1]);

            System.out.printf(out + "\n");

        }

    }


    public static void writeToFile(String fpath,String data){

        try{

            File file =new File(fpath);

            //if file doesnt exists, then create it
            if(!file.exists()){

                file.createNewFile();

            }

            FileWriter fileWritter = new FileWriter(file.getAbsoluteFile(),false);

            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);

            bufferWritter.write(data);

            bufferWritter.close();

        }catch(IOException e){

            e.printStackTrace();

        }

    }

    public static List<String> getFileList(String fpath) {
        List<String> ret=new ArrayList<String>();

        File file = new File(fpath);

        File[] fileList = file.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isFile()) {
                ret.add(fileList[i].getName());
            }

//            if (fileList[i].isDirectory()) {
//                String fileName = fileList[i].getName();
//                System.out.println("目录：" + fileName);
//            }
        }

        return ret;

    }


}
