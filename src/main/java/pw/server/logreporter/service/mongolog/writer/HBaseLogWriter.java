package pw.server.logreporter.service.mongolog.writer;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Increment;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.service.HTableLoggerPool;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.getInstance;
import static java.util.TimeZone.getTimeZone;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static pw.server.logreporter.util.ApplicationConstants.ColumnFamily.*;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_ERROR_COUNTER;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_NEW_LOG_TABLE;
import static pw.server.logreporter.util.Helper.*;
import static pw.server.logreporter.util.NullChecker.isNotEmpty;
import static pw.server.logreporter.util.NullChecker.isNotNull;

@Service
public class HBaseLogWriter {

    // TODO
    private static final int CURRENT_YEAR = getInstance().get(Calendar.YEAR);
    private final List<String> errorRowKeys = Arrays.asList("DBClientInterface", "assertion", "User Assertion", "memory leak", "SocketException", "DBClientCursor", "DBClientBase", "OutOfMemoryException", "IndexOutOfBoundsException");
    private HTableLoggerPool hTableLoggerPool;
    private long lastMillisParsed;

    @Autowired
    public HBaseLogWriter(HTableLoggerPool hTableLoggerPool) {
        this.hTableLoggerPool = hTableLoggerPool;
    }

    public void insertNewLogMessage(String logMessage) {
        if (isNotNull(logMessage) && isNotEmpty(logMessage)) {
            put(logMessage);
        }
    }

    private void put(String logMessage) {
        String rowKeyString = getRowKey(logMessage);
        if (isNotNull(rowKeyString)) {
            Put put = new Put(toBytes(rowKeyString));
            Date date = getDate(logMessage.substring(0, 19));
            addRowDetails(put, logMessage.substring(19, logMessage.length()), date);
            insert(put, date);
        }
    }

    private void addRowDetails(Put put, String logMessage, Date date) {
        if (isNotNull(date))
            put.add(CF_LOG_DETAILS, toBytes(""), date.getTime(), toBytes(logMessage));
        else {
            Logger.getLogger("Ignoring the logMessage [ " + logMessage + " ] because of no time-stamp");
        }
    }

    private Date getDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss");

        try {
            Calendar instance = getInstance(getTimeZone("GMT"));
            instance.setTime(format.parse(dateString));
            instance.set(Calendar.YEAR, CURRENT_YEAR);
            return instance.getTime();
        } catch (ParseException e) {
            Logger.getLogger(getClass()).error("The message didn't start with a data field [ " + dateString + " ]");
            return null;
        }
    }

    private void insert(Put put, Date date) {
        // TODO : Everytime opening and closing the connection, which is not good
        HTableInterface logTable = hTableLoggerPool.getTable(T_NEW_LOG_TABLE);
        try {
            logTable.put(put);
            incrementCounter(date, put.getRow());
        } catch (IOException ex) {
            Logger.getLogger(getClass()).error("Error in inserting the log : " + put.toString());
        } catch (Exception ex) {
            Logger.getLogger(getClass()).error("The Log error [ " + Bytes.toString(put.getRow()) + " ] is without time-stamp");
        } finally {
            try {
                logTable.close();
            } catch (IOException e) {
                Logger.getLogger(getClass()).error("Unable to close the HTable connection");
            }
        }
    }

    private void incrementCounter(Date date, byte[] rowKey) {
        Calendar instance = getInstance(getTimeZone("GMT"));
        instance.setTime(date);
        HTableInterface logTable = hTableLoggerPool.getTable(T_ERROR_COUNTER);
        try {
            Increment increment = new Increment(rowKey)
                    .addColumn(CF_COUNTER_YEARLY, getYearQualifier(instance), 1)
                    .addColumn(CF_COUNTER_MONTHLY, getMonthQualifier(instance), 1)
                    .addColumn(CF_COUNTER_DAILY, getDailyQualifier(instance), 1);
            // Result is returned, its better not to return anything
            logTable.increment(increment);
        } catch (IOException e) {
            Logger.getLogger(getClass()).error("Error in incrementing the counter for the row-key [ " + Bytes.toString(rowKey) + " ]");
        } finally {
            try {
                logTable.close();
            } catch (IOException e) {
                Logger.getLogger(getClass()).error("Unable to close the HTable connection");
            }
        }
    }

    private String getRowKey(String logMessage) {
        for (String errorType : errorRowKeys) {
            if (logMessage.contains(errorType))
                return getModifiedKey(errorType);
        }
        return null;
    }

    private String getModifiedKey(String error) {
        return error.contains(" ") ? error.replaceAll(" ", "_") : error;
    }

}
