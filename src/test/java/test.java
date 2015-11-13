import thu.instcloud.app.se.estimator.YMatrix;
import thu.instcloud.app.se.mpdata.MPData;

import static thu.instcloud.app.se.common.Utils.Common.isLinux;

/**
 * Created on 2015/11/13.
 */
public class test {

    public static void main(String[] args) {

        String fpath;

        if (isLinux()) {

            fpath = "/home/hjh/doc/powersystem/4bus/ieee4cdftest.txt";

        } else {

            fpath = "F:\\projects\\data\\matpower-data-process\\data\\case14.txt";

        }

        MPData mpData=new MPData(fpath);

        YMatrix yMatrix=new YMatrix(mpData);

    }

}
