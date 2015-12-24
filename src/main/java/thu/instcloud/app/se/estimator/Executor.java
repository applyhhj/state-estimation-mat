package thu.instcloud.app.se.estimator;

import thu.instcloud.app.se.common.EstimationOption;

import java.util.List;
import java.util.Random;

import static thu.instcloud.app.se.common.Utils.Common.getValidFileNameList;
import static thu.instcloud.app.se.common.Utils.Common.isLinux;

/**
 * Created on 2015/11/7.
 *
 * ATTENTION: In Matpower excluded indices are specific to 30-bus system, so there there will be difference between result
 * from this program and the result from Matpower estimator.
 *
 */
public class Executor {

    public static void main(String[] args) throws InterruptedException {

        String fpath;

        Random random = new Random();

        if (isLinux()) {

            fpath = "/home/hjh/doc/powersystem/4bus/";

        } else {

            fpath = "F:\\projects\\data\\matpower-data-process\\data\\";

        }

        EstimationOption option = new EstimationOption();

        option.setVerbose(0);

        option.setDebug(false);

        List<String> fnames = getValidFileNameList(fpath);

        String fname = fnames.get(random.nextInt(fnames.size()) - 1);
//        for test
        fname = "case300.txt";

        PowerSystem powerSystem = new PowerSystem(fpath + fname, option);

        int i = 0;

        long start;

        while (i++ < Integer.MAX_VALUE) {

            System.out.printf("Current case: %s\n", fname);

            start = System.currentTimeMillis();

            powerSystem.run();

            System.out.printf("Estimation %d, duration: %d ms", i, System.currentTimeMillis() - start);

            powerSystem.printStateInExternalInPolarDegree();

            Thread.sleep(500);

        }

        System.out.print("\nDone!\n");

    }


}
