package pw.server.logreporter.util.hbase;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.api.ErrorCodes;
import pw.server.logreporter.exception.ApplicationException;
import pw.server.logreporter.util.NullChecker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static pw.server.logreporter.service.ApplicationConstants.HBaseTableNames.LOG_TABLE;
import static pw.server.logreporter.service.ApplicationConstants.LogColumnFamily.LOG_DETAILS_FAMILY;
import static pw.server.logreporter.service.ApplicationConstants.LogDetailFamilyQualifier.DETAIL_FAMILY_MESSAGE_QUALIFIER;

@Service
public class HBaseLogger {

    private final List<String> errorRowKeys = Arrays.asList("OutOfMemoryException", "IndexOutOfBoundsException");
    private HTableLoggerPool hTableLoggerPool;

    @Autowired
    public HBaseLogger(HTableLoggerPool hTableLoggerPool) {
        this.hTableLoggerPool = hTableLoggerPool;
    }

    public void insertLogMessage(String logMessage) {
        if (NullChecker.isNotNull(logMessage) && NullChecker.isNotEmpty(logMessage)) {
            put(logMessage);
        }
    }

    private void put(String logMessage) {
        byte[] rowKey = toBytes(getRowKey(logMessage));
        Put put = new Put(rowKey);
        addRowDetails(put, logMessage);
        insert(put);
    }

    private void addRowDetails(Put put, String logMessage) {
        put.add(LOG_DETAILS_FAMILY, DETAIL_FAMILY_MESSAGE_QUALIFIER, toBytes(logMessage));
    }

    private void insert(Put put) {
        HTableInterface logTable = hTableLoggerPool.getTable(LOG_TABLE);
        try{
        logTable.put(put);
        } catch (IOException ex) {
            Logger.getLogger(getClass()).error("Error in inserting the log : "+put.toString());
        }
    }

    private String getRowKey(String logMessage) {
        for(String error : errorRowKeys) {
            if(logMessage.contains(error))
                return error;
        }
        throw new ApplicationException("Unknown Key to be Handled", ErrorCodes.SC_BAD_REQUEST);
    }

}
