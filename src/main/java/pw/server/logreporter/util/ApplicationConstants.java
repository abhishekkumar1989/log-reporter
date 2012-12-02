package pw.server.logreporter.util;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

public interface ApplicationConstants {
    interface HBaseTableNames {
        byte[] T_LOG_TABLE = toBytes("log_table");
        byte[] T_NEW_LOG_TABLE = toBytes("new_log_table");
        byte[] T_ERROR_COUNTER = toBytes("error_count");
        String T_LOG_TABLE_STRING = "log_table";
        String T_NEW_LOG_TABLE_STRING = "log_table";
        String T_ERROR_COUNTER_STRING = "log_table";
    }

    interface ColumnFamily {
        byte[] CF_LOG_DETAILS = toBytes("d");
        byte[] CF_COUNTER_YEARLY = toBytes("y");
        byte[] CF_COUNTER_MONTHLY = toBytes("m");
        byte[] CF_COUNTER_DAILY = toBytes("d");
    }

    interface ReportingType {
        String MONTHLY = "monthly";
        String DAILY = "daily";
        String YEARLY = "yearly";
    }

}
