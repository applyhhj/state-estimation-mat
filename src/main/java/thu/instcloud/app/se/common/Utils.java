package thu.instcloud.app.se.common;

import MatOperation.MatOperation;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2015/11/12.
 */
public class Utils {

    public static class Common {

        public static IntComparator comparator = new IntComparator();

        public static List<Long> getDigitStr(String text) {

            List<Long> digitList = new ArrayList<Long>();

            Pattern p = Pattern.compile("(\\d+)");

            Matcher m = p.matcher(text);

            while (m.find()) {

                String find = m.group(1).toString();

                digitList.add(Long.valueOf(find));

            }

            return digitList;

        }

        public static List<String> getValidFileNameList(String fpath) {

            List<String> ret = new ArrayList<String>();

            File file = new File(fpath);

            File[] fileList = file.listFiles();

            boolean valid;

            for (int i = 0; i < fileList.length; i++) {

                valid = true;

                if (fileList[i].isFile()) {

                    String fname = fileList[i].getName();

                    for (Long e : getDigitStr(fname)) {

                        if (e == 118 || e > 300) {

                            valid = false;

                            break;

                        }

                    }

                    if (valid) {

                        ret.add(fname);

                    }

                }

            }

            return ret;

        }

        public static double[] toDoubleArray(List<Integer> intList) {

            double[] ret = new double[intList.size()];

            for (int i = 0; i < intList.size(); i++) {

                ret[i] = intList.get(i);

            }

            return ret;

        }

        public static List<Integer> getContinuousIds(int start, int end) {

            List<Integer> ret = new ArrayList<Integer>();

            for (int i = start; i <= end; i++) {

                ret.add(i);

            }

            return ret;

        }

        public static boolean isLinux() {

            return System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;

        }

        public static List<String> readStringFromFile(String FILE_IN) {

            List<String> ret = new ArrayList<String>();

            File file = new File(FILE_IN);

            try {

                FileInputStream is = new FileInputStream(file);

                InputStreamReader isr = new InputStreamReader(is);

                BufferedReader in = new BufferedReader(isr);

                String line = null;

                while ((line = in.readLine()) != null) {
                    ret.add(line.toString());

                }

                in.close();

                is.close();

            } catch (Exception e) {

                // TODO Auto-generated catch block

                e.printStackTrace();

            }

            return ret;

        }

        public static class IntComparator implements Comparator<Integer> {

            public int compare(Integer o1, Integer o2) {
                if (o1 == o2) {

                    return 0;

                } else if (o1 > o2) {

                    return 1;

                } else {

                    return -1;

                }
            }

        }

    }

    public static class Mat{

        private static MatOperation matOperation=null;

        public static void disposeMatrix(Object... vars) {

            for (Object var : vars) {

                if (var != null) {

                    if (var instanceof MWNumericArray) {

                        ((MWNumericArray) var).dispose();

                    }

                    if (var instanceof OperationChain) {

                        ((OperationChain) var).dispose();

                    }

                }

            }

        }

        public static MatOperation getMatOperation(){

            if (matOperation==null){

                try {

                    matOperation= new MatOperation();

                } catch (MWException e) {

                    e.printStackTrace();

                }

            }

            return matOperation;

        }

        public static MWNumericArray toMeasurementVector(MWNumericArray sf_, MWNumericArray st_, MWNumericArray sbus_,
                                                         MWNumericArray Va_, MWNumericArray Vm_) {

            OperationChain s = new OperationChain(sf_).mergeColumn(st_, sbus_);

            OperationChain sr = s.clone().getReal();

            OperationChain si = s.getImag();

            MWNumericArray ret = null;
            try {
                ret = (MWNumericArray) sr.mergeColumn(Va_).mergeColumn(si.getArray()).mergeColumn(Vm_)
                        .getArray().clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            s.dispose();

            sr.dispose();

            si.dispose();

            return ret;

        }

    }

}
