package thu.instcloud.app.se.common;

/**
 * Created on 2015/11/7.
 */
public class EstimationOption {

    private int verbose;

    private boolean debug;

    public EstimationOption() {

        verbose = 0;

        debug = false;

    }

    public int getVerbose() {
        return verbose;
    }

    public void setVerbose(int verbose) {
        this.verbose = verbose;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
