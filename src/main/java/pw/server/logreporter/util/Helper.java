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

    public static byte[] getDailyQualifier(Calendar instance) {                           // its because giving 1 less value
        return toBytes(getDateParam(instance.get(Calendar.YEAR)) + "-" + getDateParam(instance.get(Calendar.MONTH) + 1) + "-" + getDateParam(instance.get(Calendar.DATE)));
    }

    public static byte[] getMonthQualifier(Calendar instance) {
        return toBytes(getDateParam(instance.get(Calendar.YEAR)) + "-" + getDateParam(instance.get(Calendar.MONTH) + 1));
    }

    private static String getDateParam(int typeValue) {
        String param = Integer.toString(typeValue);
        return param.length() == 1 ? "0" + param : param;
    }
}
