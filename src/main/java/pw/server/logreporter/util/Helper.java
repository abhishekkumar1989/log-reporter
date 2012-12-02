package pw.server.logreporter.util;

import java.util.Calendar;
import java.util.Date;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

public class Helper {

    private static final long millisInAMin = 60000;

    public static long getPastTime(long minsBefore) {
        return new Date().getTime() - minsBefore * millisInAMin;
    }

    public static byte[] getYearQualifier(Calendar instance) {
        int year = instance.get(Calendar.YEAR);
        return toBytes(Integer.toString(year));
    }

    public static byte[] getDailyQualifier(Calendar instance) {
        return toBytes(getDateParam(instance, Calendar.YEAR) + "-" + getDateParam(instance, Calendar.MONTH) + "-" + getDateParam(instance, Calendar.DATE));
    }

    public static byte[] getMonthQualifier(Calendar instance) {
        return toBytes(getDateParam(instance, Calendar.YEAR) + "-" + getDateParam(instance, Calendar.MONTH));
    }

    private static String getDateParam(Calendar instance, int type) {
        String param = Integer.toString(instance.get(type));
        return param.length() == 1 ? "0" + param : param;
    }
}
