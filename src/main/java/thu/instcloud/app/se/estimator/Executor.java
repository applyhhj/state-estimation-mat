package thu.instcloud.app.se.estimator;

import thu.instcloud.app.se.common.EstimationOption;

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

        if (isLinux()) {

            fpath = "/home/hjh/doc/powersystem/4bus/";

        } else {

            fpath = "F:\\projects\\data\\matpower-data-process\\data\\";

        }

        EstimationOption option = new EstimationOption();

        option.setVerbose(0);

        option.setDebug(false);

        PowerSystem powerSystem = new PowerSystem(fpath, option);

        int i = 0;

        long start;

        while (i++ < 1) {

            start = System.currentTimeMillis();

            powerSystem.run();

            powerSystem.printStateInExternalInPolarDegree();

            System.out.printf("\nEstimate %d duration: %d ms", i, System.currentTimeMillis() - start);

            Thread.sleep(1500);

        }

        System.out.print("\nDone!\n");

    }


}
