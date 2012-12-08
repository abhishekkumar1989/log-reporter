package pw.server.logreporter.service.mongolog.reader;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.service.HTableLoggerPool;

import java.io.IOException;
import java.util.*;

import static java.util.Calendar.getInstance;
import static java.util.TimeZone.getTimeZone;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static pw.server.logreporter.util.ApplicationConstants.ColumnFamily.*;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_ERROR_COUNTER;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_NEW_LOG_TABLE;
import static pw.server.logreporter.util.ApplicationConstants.ReportingType.*;
import static pw.server.logreporter.util.NullChecker.isNotNull;

@Service
public class HBaseReader {

    private HTableLoggerPool pool;
    private static Map<String, byte[]> typeMapping;

    static {
        typeMapping = new HashMap<String, byte[]>(3);
        typeMapping.put(MONTHLY, CF_COUNTER_MONTHLY);
        typeMapping.put(YEARLY, CF_COUNTER_YEARLY);
        typeMapping.put(DAILY, CF_COUNTER_DAILY);
    }

    @Autowired
    public HBaseReader(HTableLoggerPool pool) {
        this.pool = pool;
    }

    public Object getErrorResults(String rowKey, String text, long minMillisTS, long maxMillisTS, int versions) throws IOException {
        Get get = new Get(toBytes(rowKey));
        get.addFamily(CF_LOG_DETAILS);
        get.setMaxVersions(versions);
        get.setTimeRange(getTimeStamp(minMillisTS), getTimeStamp(maxMillisTS));
        if (isNotNull(text))
            get.setFilter(getValueSearchFilter(text));
        List<KeyValue> errorList = pool.getTable(T_NEW_LOG_TABLE).get(get).getColumn(CF_LOG_DETAILS, toBytes(""));
        if (isNotNull(errorList)) {
            List<Map<String, String>> results = new ArrayList<Map<String, String>>(errorList.size());
            for (KeyValue kv : errorList) {
                results.add(getErrorMap(kv));
            }
            return results;
        }
        return Collections.singletonMap("Result", "No result found");
    }

    private Map<String, String> getErrorMap(KeyValue kv) {
        HashMap<String, String> errorMap = new HashMap<String, String>(2);
        errorMap.put("date", new Date(kv.getTimestamp()).toString());
        errorMap.put("error", Bytes.toString(kv.getValue()));
        return errorMap;
    }

    private Filter getValueSearchFilter(String text) {
        return new ValueFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator(text));
    }

    private long getTimeStamp(long ts) {
        Calendar calendar = getInstance(getTimeZone("GMT"));
        return calendar.getTimeInMillis() - ts;
    }

    public Object getCounts(String rowKey, String type, String colQualifier) throws IOException {
        HTableInterface table = pool.getTable(T_ERROR_COUNTER);
        Get get = new Get(toBytes(rowKey));
        if (isNotNull(type))
            get.addFamily(typeMapping.get(type));
        if (isNotNull(colQualifier))
            get.setFilter(getColQualifierFilter(type, colQualifier));
        Result result = table.get(get);
        if (!result.isEmpty()) {
            Map<String, List> resultCount = new HashMap<String, List>(3);
            resultCount.put("year", getCountList(result.getFamilyMap(CF_COUNTER_YEARLY).descendingMap()));
            resultCount.put("month", getCountList(result.getFamilyMap(CF_COUNTER_MONTHLY).descendingMap()));
            resultCount.put("day", getCountList(result.getFamilyMap(CF_COUNTER_DAILY).descendingMap()));
            return resultCount;
        }
        return Collections.singletonMap("Result", "No counter-result found for the searched key");
    }

    private Filter getColQualifierFilter(String type, String colQualifier) {
        return new ColumnPrefixFilter(toBytes(colQualifier));
    }

    private List<Map<String, Object>> getCountList(NavigableMap<byte[], byte[]> familyMap) {
        List<Map<String, Object>> countListMap = new ArrayList<Map<String, Object>>();
        for (Map.Entry<byte[], byte[]> map : familyMap.entrySet()) {
            countListMap.add(getMapCount(map));
        }
        return countListMap;
    }

    private Map<String, Object> getMapCount(Map.Entry<byte[], byte[]> map) {
        HashMap<String, Object> result = new HashMap<String, Object>(2);
        result.put("type", Bytes.toString(map.getKey()));
        result.put("value", Bytes.toLong(map.getValue()));
        return result;
    }

    public Object getAllCounters(String type) throws IOException {
        Map<String, List> result = new HashMap<String, List>();
        ResultScanner scanner = pool.getTable(T_ERROR_COUNTER).getScanner(typeMapping.get(type));
        for (Result rs : scanner) {
            result.put(Bytes.toString(rs.getRow()), getCountList(rs.getFamilyMap(typeMapping.get(type)).descendingMap()));
        }
        return result;
    }

}
