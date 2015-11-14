package thu.instcloud.app.se.estimator;

import MatOperation.MatOperation;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import thu.instcloud.app.se.common.ComplexNumber;
import thu.instcloud.app.se.common.OperationChain;
import thu.instcloud.app.se.mpdata.MPData;

import java.util.ArrayList;
import java.util.List;

import static thu.instcloud.app.se.common.Utils.Mat.getMatOperation;

/**
 * Created on 2015/11/6.
 */
public class YMatrix {

    private List<ComplexNumber> Ytt;

    private List<ComplexNumber> Yff;

    private List<ComplexNumber> Ytf;

    private List<ComplexNumber> Yft;

    private MWNumericArray cf;

    private MWNumericArray ct;

    private MWNumericArray YSh;

//    private BasicMatrix YShB;

    private MWNumericArray Yf;

//    private BasicMatrix YfB;

    private MWNumericArray Yt;

//    private BasicMatrix YtB;

    private MWNumericArray Ybus;

//    private Matrix YB;

//    private ComplexMatrix Ybus;

    private MPData mpData;

    private int nbr;

    private int nb;

    public YMatrix(MPData mpData) {

        this.mpData = mpData;

        nbr=mpData.getnBranch();

        nb=mpData.getnBus();

        Ytt = new ArrayList<ComplexNumber>();

        Yff = new ArrayList<ComplexNumber>();

        Ytf = new ArrayList<ComplexNumber>();

        Yft = new ArrayList<ComplexNumber>();

        computeYMatrix();

        releaseMem();

//        print();

    }

    private void computeYMatrix() {

        double Gtt, Btt, Gff, Bff, Gft, Bft, Gtf, Btf, Gs, Bs, r, x, zm2, Bc, t, tsh;

        for (int i = 0; i < mpData.getBranchData().getN(); i++) {

            r = mpData.getBranchData().getR()[i];

            x = mpData.getBranchData().getX()[i];

            Bc = mpData.getBranchData().getB()[i];

            t = mpData.getBranchData().getRatio()[i];

            tsh = mpData.getBranchData().getAngle()[i];

            if (t <= 0) {

                t = 1;

            }

            zm2 = r * r + x * x;

            Gs = r / zm2;

            Bs = -x / zm2;

            Gtt = Gs;

            Btt = Bc / 2 + Bs;

            Gff = Gtt / t / t;

            Bff = Btt / t / t;

            if (tsh == 0) {

                Gtf = Gft = -Gs / t;

                Btf = Bft = -Bs / t;

            } else {

                Gtf = -(Gs * Math.cos(tsh) + Bs * Math.sin(tsh)) / t;

                Btf = (Gs * Math.sin(tsh) - Bs * Math.cos(tsh)) / t;

                Gft = (Bs * Math.sin(tsh) - Gs * Math.cos(tsh)) / t;

                Bft = -(Gs * Math.sin(tsh) + Bs * Math.cos(tsh)) / t;

            }

            Ytt.add(new ComplexNumber(Gtt, Btt));

            Yff.add(new ComplexNumber(Gff, Bff));

            Yft.add(new ComplexNumber(Gft, Bft));

            Ytf.add(new ComplexNumber(Gtf, Btf));

        }

        getConnectionMatrix();

        getYfYt();

        getYSparseSh();

        Ybus=new OperationChain(cf).transpose().multiply(Yf).add(
                new OperationChain(ct).transpose().multiply(Yt).getArray()
        ).add(YSh).toSparse().getArray();

    }

    private void releaseMem(){

        Ytt=null;

        Yff=null;

        Ytf=null;

        Yft=null;

        if (cf!=null){

            cf.dispose();

        }

        if (ct!=null){

            ct.dispose();

        }

        if (YSh!=null){

            YSh.dispose();

        }

    }

    private void print() {

        System.out.print("Yf\n" + Yf.toString() + "\n");

        System.out.print("Yt\n" + Yt.toString() + "\n");

        System.out.print("Ybus\n" + Ybus.toString() + "\n");

    }

    private void getYSparseSh() {

        int[] rowidx=new int[nb];

        double[] valueReal=new double[nb];

        double[] valueImag=new double[nb];

        int idx;

        for (int i = 0; i < nb; i++) {

//            this is the index, however i is index should convert to internal bus number
            idx = mpData.getBusData().getTOA().get(mpData.getBusData().getTIO().get(i + 1));

//            matlab index start from 1
            rowidx[i]=i+1;

            valueReal[i]=mpData.getBusData().getGs()[idx] / mpData.getSbase();

            valueImag[i]=mpData.getBusData().getBs()[idx] / mpData.getSbase();

        }

        YSh = MWNumericArray.newSparse(rowidx,rowidx,valueReal,valueImag,nb,nb,MWClassID.DOUBLE);

    }

    private void getYfYt() {

        int idxi, idxj;

        int[] rowidx=new int[2*nbr];

        int[] colidx=new int[2*nbr];

        List<Double> valueReal=new ArrayList<Double>();

        List<Double> valueImag=new ArrayList<Double>();

        int j=0;

        for (int i = 0; i < nbr; i++) {

//            in matlab index start from 1, all in internal number
            rowidx[j]=(i+1);

            idxi= mpData.getBusData().getTOI().get(mpData.getBranchData().getI()[i]);

            colidx[j]=(idxi);

            valueReal.add(Yff.get(i).getReal());

            valueImag.add(Yff.get(i).getImag());

            j++;

            rowidx[j]=i+1;

            idxj=mpData.getBusData().getTOI().get(mpData.getBranchData().getJ()[i]);

            colidx[j]=idxj;

            valueReal.add(Yft.get(i).getReal());

            valueImag.add(Yft.get(i).getImag());

            j++;

        }

        Yf = MWNumericArray.newSparse(rowidx,colidx,valueReal.toArray(),valueImag.toArray(),
                nbr,nb,MWClassID.DOUBLE);

        j=0;

        valueImag.clear();

        valueReal.clear();

        for (int i = 0; i < nbr; i++) {

//            in matlab index start from 1, all in internal number
            rowidx[j]=(i+1);

            idxi= mpData.getBusData().getTOI().get(mpData.getBranchData().getI()[i]);

            colidx[j]=(idxi);

            valueReal.add(Ytf.get(i).getReal());

            valueImag.add(Ytf.get(i).getImag());

            j++;

            rowidx[j]=i+1;

            idxj=mpData.getBusData().getTOI().get(mpData.getBranchData().getJ()[i]);

            colidx[j]=idxj;

            valueReal.add(Ytt.get(i).getReal());

            valueImag.add(Ytt.get(i).getImag());

            j++;

        }

        Yt = MWNumericArray.newSparse(rowidx,colidx,valueReal.toArray(),valueImag.toArray(),
                nbr,nb,MWClassID.DOUBLE);

    }

    private void getConnectionMatrix() {

        int[] rowidx=new int[nbr];

        int[] colidxf=new int[nbr];

        int[] colidxt=new int[nbr];

        double[] values=new double[nbr];

        for (int i = 0; i < nbr; i++) {

//            in matlab all index start from 1
            rowidx[i]=i+1;

            colidxf[i]=mpData.getBusData().getTOI().get(mpData.getBranchData().getI()[i]);

            colidxt[i]=mpData.getBusData().getTOI().get(mpData.getBranchData().getJ()[i]);

            values[i]=1;

        }

        cf=MWNumericArray.newSparse(rowidx,colidxf,values,nbr,nb,MWClassID.DOUBLE);

        ct = MWNumericArray.newSparse(rowidx,colidxt,values,nbr,nb,MWClassID.DOUBLE);

    }

    public MWNumericArray getYbus() {
        return Ybus;
    }

    public MWNumericArray getYt() {
        return Yt;
    }

    public MWNumericArray getYf() {
        return Yf;
    }
}
