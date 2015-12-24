package thu.instcloud.app.se.debug;

import thu.instcloud.app.se.common.EstimationOption;
import thu.instcloud.app.se.estimator.PowerSystem;

import java.util.ArrayList;
import java.util.List;

import static thu.instcloud.app.se.common.Utils.Common.getDigitStr;
import static thu.instcloud.app.se.common.Utils.Common.isLinux;
import static thu.instcloud.app.se.debug.Test.*;

/**
 * Created on 2015/11/7.
 */
public class ExecutorTest {

    public static void main(String[] args) throws InterruptedException {

        String fpath,fdestpath;

        fdestpath="F:\\projects\\data\\testdata\\";

        List<String> fnames=new ArrayList<String>();

        if (isLinux()) {

            fpath = "/home/hjh/doc/powersystem/4bus/ieee4cdftest.txt";

        } else {

            fpath = "F:\\projects\\data\\matpower-data-process\\data\\";

        }

        fnames=getFileList(fpath);

        EstimationOption option = new EstimationOption();

        option.setVerbose(0);

        option.setDebug(true);

        int i = 0;

        boolean valid;

        while (i < fnames.size()) {

            valid=true;

            for (Long e:getDigitStr(fnames.get(i))){

                if (e==118||e>300){

                    valid=false;

                    break;

                }

            }

            if (!valid){

                i++;

                continue;

            }

            System.out.printf("Run case %s\n",fnames.get(i));

            PowerSystem powerSystem = new PowerSystem(fpath+fnames.get(i), option);

            powerSystem.run();

            String fdestfilepath=fdestpath+fnames.get(i)+".result";

            writeToFile(fdestfilepath,resultToString(powerSystem));

            i++;

        }

        System.out.print("\nDone!\n");

    }


}
