package thu.instcloud.app.se.estimator;

import thu.instcloud.app.se.common.EstimationOption;

import static thu.instcloud.app.se.common.Utils.Common.isLinux;

/**
 * Created on 2015/11/7.
 */
public class Executor {

    public static void main(String[] args) throws InterruptedException {

        String fpath;

        if (isLinux()) {

            fpath = "/home/hjh/doc/powersystem/4bus/ieee4cdftest.txt";

        } else {

            fpath = "F:\\projects\\data\\matpower-data-process\\data\\case300.txt";

        }

        EstimationOption option = new EstimationOption();

        option.setVerbose(0);

        option.setDebug(false);

        PowerSystem powerSystem = new PowerSystem(fpath, option);

        int i = 0;

        long start;

        while (i++ < Integer.MAX_VALUE) {

            start = System.currentTimeMillis();

            powerSystem.run();

            powerSystem.resetState();

            System.out.printf("\nEstimate %d duration: %d ms", i, System.currentTimeMillis() - start);

            Thread.sleep(50);

        }


        System.out.print("\nDone!\n");

    }


}
