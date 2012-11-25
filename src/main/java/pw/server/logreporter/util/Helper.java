package pw.server.logreporter.util;

import org.apache.hadoop.hbase.util.Bytes;

import java.util.Calendar;
import java.util.Date;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

public class Helper {

    private static final long millisInAMin = 60000;

    public static long getPastTime(long minsBefore) {
        return new Date().getTime() - minsBefore * millisInAMin;
    }

    public static byte[] getYearQualifier(int year) {
        return toBytes(year);
    }

    public static byte[] getDailyQualifier(Calendar instance) {
        return toBytes(instance.get(Calendar.YEAR)*10000 + instance.get(Calendar.MONTH)*100 + instance.get(Calendar.DATE));
    }

    public static byte[] getMonthQualifier(Calendar instance) {
        return toBytes(instance.get(Calendar.YEAR)*100 + instance.get(Calendar.MONTH));
    }
}
