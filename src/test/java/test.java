import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2015/11/13.
 */
public class test {
    public static void main(String[] args) throws InterruptedException {
//        String casedir;
//        if (isLinux()) {
//            casedir = "/home/hjh/projects/data/";
//        } else {
//            casedir = "F:\\projects\\data\\matpower-data-process\\data\\";
//        }
//        List<String> caseFiles =getFileList(casedir);
//
//
//        for (int i=0;i<caseFiles.size();i++) {
//            List<String> caseDataStrs = readStringFromFile(casedir + caseFiles.get(i));
//            System.out.printf("Process file %25s.  %5d/%d\n",caseFiles.get(i),i+1,caseFiles.size());
//            MPData mpData = new MPData(caseDataStrs);
//        }
//
//        System.out.println("-----------------------\n");
//
//        int k=1;
//        for (String casename:caseFiles) {
//            List<String> caseDataStrs = readStringFromFile(casedir + casename);
//            System.out.printf("Process file %25s.  %5d/%d\n",casename,k++,caseFiles.size());
//            MPData mpData = new MPData(caseDataStrs);
//        }
        List<String> res = new ArrayList<>();
        res.add("0.0");
        res.add("0.5");

        MWNumericArray resmat = new MWNumericArray(res.toArray(), MWClassID.DOUBLE);
        Object[] resarr = resmat.toArray();
        System.out.print(resmat.getDouble());

    }


}
