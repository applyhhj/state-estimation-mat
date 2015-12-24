import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.util.ArrayList;
import java.util.List;

import static thu.instcloud.app.se.common.Utils.Common.getDigitStr;
import static thu.instcloud.app.se.common.Utils.Common.isLinux;
import static thu.instcloud.app.se.debug.Test.getFileList;
import static thu.instcloud.app.se.debug.Test.writeToFile;

/**
 * Created on 2015/11/13.
 */
public class test {
    public static void main(String[] args) throws InterruptedException {
        int r=5;
        int c=3;
        int[] dims={r,c};
        MWNumericArray res=MWNumericArray.newInstance(dims, MWClassID.DOUBLE, MWComplexity.REAL);
        for (int i = 1; i <= r; i++) {
            for (int j = 1; j <= c; j++) {
                int[] ids={i,j};
                res.set(ids,i+j);
            }
        }

        byte[] resbytes=res.serialize();
        System.out.print(resbytes.length+"\n");

        MWNumericArray resdeser=(MWNumericArray)MWNumericArray.deserialize(resbytes);

        System.out.print(resdeser);


//
//        String fpath, fdestpath;
//
//        fdestpath = "F:\\projects\\data\\testdata\\";
//
//        List<String> fnames = new ArrayList<String>();
//
//        if (isLinux()) {
//
//            fpath = "/home/hjh/doc/powersystem/4bus/ieee4cdftest.txt";
//
//        } else {
//
//            fpath = "F:\\projects\\data\\matpower-data-process\\data\\";
//
//        }
//
//        fnames = getFileList(fpath);
//
//        int i = 0;
//
//        boolean valid;
//
//        while (i < fnames.size()) {
//
//            valid = true;
//
//            for (Long e : getDigitStr(fnames.get(i))) {
//
//                if (e == 118 || e > 300) {
//
//                    valid = false;
//
//                    System.out.printf("ignore file %s\n", fnames.get(i));
//
//                    break;
//
//                }
//
//            }
//
//            if (!valid) {
//
//                i++;
//
//                continue;
//
//            }
//
//            String fdestfilepath = fdestpath + fnames.get(i) + ".result";
//
//            writeToFile(fdestfilepath, "test");
//
//            i++;
//
//        }
//
//        System.out.print("\nDone!\n");

    }


}
