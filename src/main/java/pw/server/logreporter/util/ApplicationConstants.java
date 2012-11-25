package pw.server.logreporter.util;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

public interface ApplicationConstants {
    interface HBaseTableNames {
        byte[] T_LOG_TABLE = toBytes("log_table");
        byte[] T_NEW_LOG_TABLE = toBytes("new_log_table");
        byte[] T_ERROR_DETAILS = toBytes("error_details");
        byte[] T_ERROR_COUNTER = toBytes("error_count");
        String T_LOG_TABLE_STRING = "log_table";
        String T_ERROR_COUNTER_STRING = "log_table";
    }

    interface ColumnFamily {
        byte[] CF_LOG_DETAILS = toBytes("d");
        byte[] CF_COUNTER_YEARLY = toBytes("y");
        byte[] CF_COUNTER_MONTHLY = toBytes("m");
        byte[] CF_COUNTER_DAILY = toBytes("d");
    }

    interface LogDetailFamilyQualifier {
        byte[] Q_DETAIL_FAMILY_MESSAGE_QUALIFIER = toBytes("m");

//        byte[] Q_DETAIL_FAMILY_MESSAGE_QUALIFIER = toBytes("m");
//        byte[] Q_DETAIL_FAMILY_MESSAGE_QUALIFIER = toBytes("m");
    }
}
