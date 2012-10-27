package pw.server.logreporter.service;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;

public interface ApplicationConstants {
    interface HBaseTableNames {
        byte[] LOG_TABLE = toBytes("log_table");
    }

    interface LogColumnFamily {
        byte[] LOG_DETAILS_FAMILY = toBytes("detail");
    }

    interface LogDetailFamilyQualifier {
        byte[] DETAIL_FAMILY_STATUS_QUALIFIER = toBytes("sc");
        byte[] DETAIL_FAMILY_MESSAGE_QUALIFIER = toBytes("m");
    }
}
