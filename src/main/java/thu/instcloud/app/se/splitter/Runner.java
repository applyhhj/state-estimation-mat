package thu.instcloud.app.se.splitter;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.mpdata.MPData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 2015/12/21.
 */
public class Runner {

    public static void main(String[] args)throws MWException {

        String filepath;

        String os = System.getProperty("os.name").toLowerCase();

        if (os != null && os.startsWith("windows")) {
            filepath = "F:\\projects\\data\\matpower-data-process\\data\\case1354pegase.txt";
        }else {
            filepath = "/home/hjh/projects/data/case1354pegase.txt";
        }

        MPData mpData=new MPData(filepath);

        SplitMPData splitMPData=new SplitMPData(mpData,300);

        MWNumericArray newBus=splitMPData.getBus();

        int[] dims=newBus.getDimensions();

        int[] ids=new int[2];

        Map<Integer,Integer> zoneNumMap=new HashMap<Integer, Integer>();

        for (int i = 1; i <= dims[0]; i++) {

            ids[0]=i;

            ids[1]=11;

            Double zonedouble=(Double) newBus.get(ids);

            Integer zone=zonedouble.intValue();

            if(!zoneNumMap.keySet().contains(zone)){

                zoneNumMap.put(zone,1);

            }else {

                zoneNumMap.put(zone,zoneNumMap.get(zone)+1);

            }

        }

        for (Map.Entry<Integer,Integer> e:zoneNumMap.entrySet()){

            System.out.printf("%3d:%3d  ",e.getKey(),e.getValue());

        }

        System.out.print("\nDone\n");

    }

}
