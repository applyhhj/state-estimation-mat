import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.storm.common.StormUtils;

import java.util.ArrayList;
import java.util.List;

import static thu.instcloud.app.se.storm.common.StormUtils.mkByteKey;

/**
 * Created on 2015/11/13.
 */
public class test {
    public static void main(String[] args) throws InterruptedException {
        String caseid = "case9241pegase";


        List<String> res = new ArrayList<>();
        res.add("0.0");
        res.add("0.5");

        MWNumericArray resmat = new MWNumericArray(res.get(1), MWClassID.DOUBLE);
        Object[] resarr = resmat.toArray();
//        System.out.print(resmat.getDouble());


    }


}
