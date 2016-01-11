import Estimator.Estimator;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2015/11/13.
 */
public class test {
    public static Estimator estimator;
    public static void main(String[] args) throws InterruptedException {
        boolean a = true;
        String aboo = a + "";
        System.out.println(a);

//        String caseid = "case9241pegase";
//        long sleeptime = 3;
//        MWNumericArray mat = getMatArr();
//        Thread.sleep(sleeptime);
//        mat.dispose();
//        System.out.println("mat disposed!");
//        Thread.sleep(sleeptime);
//
//        List<MWNumericArray> mats = getMatArrList();
//        Thread.sleep(sleeptime);
//        MWNumericArray matele = mats.get(0);
//        matele.dispose();
//        System.out.println("mat from mats disposed");
//        Thread.sleep(sleeptime);
//
//        List<MWNumericArray> mats1 = getMatArrList();
//        Thread.sleep(sleeptime);
//        mats1.get(0).dispose();
//        System.out.println("dispose from list get!");
//        Thread.sleep(sleeptime);


    }

    public static List<MWNumericArray> getMatArrList() {
        List<MWNumericArray> mats = new ArrayList<>();
        mats.add(getMatArr());
        return mats;
    }

    public static MWNumericArray getMatArr() {
        MWNumericArray mat = null;
        try {
            estimator = new Estimator();
            Double r = Double.valueOf(100);
            Double c = Double.valueOf(100);

            mat = (MWNumericArray) estimator.test_mem(1, r, c)[0];

        } catch (MWException e) {
            e.printStackTrace();
        }

        return mat;
    }


}
