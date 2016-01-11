package thu.instcloud.app.se.storm.common;

import backtype.storm.tuple.Tuple;

import java.util.Collection;

/**
 * Created by hjh on 16-1-11.
 */
public class MeasureDataFactory {
    public static Collection<String> validTypeSet;

    public MeasureDataFactory() {
        if (validTypeSet == null) {
            validTypeSet.add(StormUtils.MEASURE.TYPE.PF);
            validTypeSet.add(StormUtils.MEASURE.TYPE.PT);
            validTypeSet.add(StormUtils.MEASURE.TYPE.PBUS);
            validTypeSet.add(StormUtils.MEASURE.TYPE.VA);
            validTypeSet.add(StormUtils.MEASURE.TYPE.QF);
            validTypeSet.add(StormUtils.MEASURE.TYPE.QT);
            validTypeSet.add(StormUtils.MEASURE.TYPE.QBUS);
            validTypeSet.add(StormUtils.MEASURE.TYPE.VM);

        }
    }

    public MeasureData getMeasureData(Tuple tuple) {
        if (!validTypeSet.contains(tuple.getStringByField(StormUtils.STORM.FIELDS.MEASURE_TYPE))) {
            return null;
        } else {
            return new MeasureData(tuple);
        }
    }
}
