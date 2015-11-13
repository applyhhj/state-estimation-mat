package thu.instcloud.app.se.common;

/**
 * Created on 2015/11/13.
 */
public class ComplexNumber {

    private double real;

    private double imag;

    public ComplexNumber(double r,double i){

        real=r;

        imag=i;

    }

    public double getReal() {
        return real;
    }

    public void setReal(double real) {
        this.real = real;
    }

    public double getImag() {
        return imag;
    }

    public void setImag(double imag) {
        this.imag = imag;
    }
}
