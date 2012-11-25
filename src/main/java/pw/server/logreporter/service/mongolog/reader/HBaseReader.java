package pw.server.logreporter.service.mongolog.reader;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.service.HTableLoggerPool;

import java.io.IOException;
import java.util.*;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static pw.server.logreporter.util.ApplicationConstants.ColumnFamily.*;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_ERROR_COUNTER;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_NEW_LOG_TABLE;
import static pw.server.logreporter.util.NullChecker.isNotNull;

@Service
public class HBaseReader {

    private HTableLoggerPool pool;

    @Autowired
    public HBaseReader(HTableLoggerPool pool) {
        this.pool = pool;
    }

    public Map<String, String> getRangeResults(String rowKey, long minTS, long maxTS, int versions) throws IOException {
        Get get = new Get(toBytes(rowKey));
        get.addFamily(CF_LOG_DETAILS);
        get.setMaxVersions(versions);
        get.setTimeRange(getTimeStamp(minTS), getTimeStamp(maxTS));
        List<KeyValue> errorList = pool.getTable(T_NEW_LOG_TABLE).get(get).getColumn(CF_LOG_DETAILS, Bytes.toBytes(""));
        if (isNotNull(errorList)) {
            Map<String, String> qualifiers = new HashMap<String, String>(errorList.size());
            for (KeyValue kv : errorList) {
                qualifiers.put(new Date(kv.getTimestamp()).toString(), Bytes.toString(kv.getValue()));
            }
            return qualifiers;
        }
        return Collections.singletonMap("Result", "No result found");
    }

    private long getTimeStamp(long ts) {
        return new Date().getTime() - ts;
    }

    public Object getCounts(String rowKey) throws IOException {
        HTableInterface table = pool.getTable(T_ERROR_COUNTER);
        Get get = new Get(toBytes(rowKey));
        Result result = table.get(get);
        if (!result.isEmpty()) {
            Map<String, Map> resultCount = new LinkedHashMap<String, Map>(3);
            resultCount.put("year", getCountMap(result.getFamilyMap(CF_COUNTER_YEARLY)));
            resultCount.put("month", getCountMap(result.getFamilyMap(CF_COUNTER_MONTHLY)));
            resultCount.put("day", getCountMap(result.getFamilyMap(CF_COUNTER_DAILY)));
            return resultCount;
        }
        return Collections.singletonMap("Result", "No counter-result found for the searched key");
    }

    private Map<String, Long> getCountMap(NavigableMap<byte[], byte[]> familyMap) {
        Map<String, Long> result = new LinkedHashMap<String, Long>();
        for (Map.Entry<byte[], byte[]> map : familyMap.entrySet()) {
            result.put(Bytes.toString(map.getKey()), Bytes.toLong(map.getValue()));
        }
        return result;
    }

}
