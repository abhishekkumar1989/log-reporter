package pw.server.logreporter.service.mongolog.reader;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.ColumnRangeFilter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.service.HTableLoggerPool;

import java.io.IOException;
import java.util.*;

import static org.apache.hadoop.hbase.util.Bytes.toBytes;
import static org.apache.hadoop.hbase.util.Bytes.toLong;
import static pw.server.logreporter.util.ApplicationConstants.ColumnFamily.*;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_ERROR_COUNTER;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.T_LOG_TABLE;
import static pw.server.logreporter.util.ApplicationConstants.LogDetailFamilyQualifier.Q_DETAIL_FAMILY_MESSAGE_QUALIFIER;
import static pw.server.logreporter.util.Helper.getPastTime;
import static pw.server.logreporter.util.NullChecker.isNotNull;

@Service
public class HBaseReader {

    private HTableLoggerPool pool;

    @Autowired
    public HBaseReader(HTableLoggerPool pool) {
        this.pool = pool;
    }

    public Map<String, String> getRawDatas(String rowKey, long millisBack, int versions) throws IOException {
        Get get = new Get(toBytes(rowKey));
//        get.ad
        get.setMaxVersions(versions);
        get.setTimeRange(new Date().getTime() - millisBack, new Date().getTime());
        NavigableMap<byte[], byte[]> familyMap = pool.getTable(T_LOG_TABLE).get(get).getFamilyMap(CF_LOG_DETAILS);
        if (isNotNull(familyMap)) {
            Map<String, String> qualifiers = new HashMap<String, String>(familyMap.size());
            for (Map.Entry<byte[], byte[]> qualifier : familyMap.entrySet()) {
                qualifiers.put(Bytes.toString(qualifier.getKey()), Bytes.toString(qualifier.getValue()));
            }
            return qualifiers;
        }
        return Collections.singletonMap("Result", "No result found");
    }

    public Object scanRowInOrder(String rowKey, long minBefore) throws IOException {
        Scan scan = new Scan(toBytes(rowKey), toBytes(rowKey));
        // TODO : Read about Filters
//        new TreeSet<KeyValue>(KeyValue.COMPARATOR);
        scan.setFilter(getScanFilter(minBefore));
        scan.addFamily(CF_LOG_DETAILS);
        // TODO : since there is only one row this worked, but for set of rows returned this will just get you the only row
        // As the data is huge, its better to set the batch from the client side, which tells how many columns to retrieve for that row in one RPC
        Result scanner = pool.getTable(T_LOG_TABLE).getScanner(scan).next();
//        HTable table = new HTable(T_LOG_TABLE).pre
        if (isNotNull(scanner)) {
//            checkTemp(scanner);        // TODO : info about version is lost because of the getFamilyMap() call
            NavigableMap<byte[], byte[]> resultMap = scanner.getFamilyMap(CF_LOG_DETAILS).descendingMap();
            Map<String, String> qualifiers = new LinkedHashMap<String, String>(resultMap.size());
            for (Map.Entry<byte[], byte[]> qualifier : resultMap.entrySet()) {
                qualifiers.put(new Date(toLong(qualifier.getKey())).toString(), Bytes.toString(qualifier.getValue()));
            }
            return qualifiers;
        }
        return Collections.singletonMap("Result", "No result found for the searched key");
    }

    private Map<String, String> checkTemp(Result scanner) {
        KeyValue[] raw = scanner.raw();
        // TODO : the result instance is for a particular row only
        // TODO : NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>>
        // TODO : // column-family,   // column-qualifier   // timestamp
//        scanner.getMap();
        Map<String, String> qualifiers = new LinkedHashMap<String, String>(raw.length);
        for (int i = raw.length - 1; i >= 0; i++) {
            qualifiers.put(new Date(toLong(raw[i].getKey())).toString(), Bytes.toString(raw[i].getValue()));
        }
        return qualifiers;
    }

    private Filter getScanFilter(long minBefore) {
        return new ColumnRangeFilter(toBytes(getPastTime(minBefore)), true, toBytes(getPastTime(0)), false);
    }

    public Map<String, Map> getRowDetails(String rowKey) throws IOException {
        Get get = new Get(toBytes(rowKey));

        get.setMaxVersions(5);

        Result response = pool.getTable(T_LOG_TABLE).get(get);
//        response.get
        List<KeyValue> column = response.getColumn(CF_LOG_DETAILS, Q_DETAIL_FAMILY_MESSAGE_QUALIFIER);
        Map<String, Map> result = new HashMap<String, Map>();
        for (KeyValue message : column) {
            if (result.containsKey(message.getFamily().toString())) {
                Map map = result.get(message.getFamily().toString());
                if (map.containsKey(message.getQualifier().toString())) {
                    ((List) map.get(message.getQualifier().toString())).add(getMap(message));
                } else {
                    map.put(message.getQualifier().toString(), getList(message));
                }
            } else {
                Map map = new HashMap();
                map.put(message.getQualifier(), getList(message));
                result.put(message.getFamily().toString(), map);
            }
        }
        return result;
    }

    private ArrayList getList(KeyValue message) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(getMap(message));
        return arrayList;
    }

    private Map getMap(KeyValue message) {
        Map map = new HashMap(1);
        map.put(message.getTimestamp(), message.getValue().toString());
        return map;
    }

    public Object getCounts(String rowKey) throws IOException {
        HTableInterface table = pool.getTable(T_ERROR_COUNTER);
//        Row row = new Exec(table.getConfiguration(), rowKey, );
        Get get = new Get(toBytes(rowKey));
        Result result = table.get(get);
        Map<String, Map> resultCount = new HashMap<String, Map>();
        resultCount.put("year", getMap(result.getFamilyMap(CF_COUNTER_YEARLY)));
        resultCount.put("month", getMap(result.getFamilyMap(CF_COUNTER_MONTHLY)));
        resultCount.put("day", getMap(result.getFamilyMap(CF_COUNTER_DAILY)));
        return resultCount;
    }

    private Map getMap(NavigableMap<byte[], byte[]> familyMap) {
        Map result = new HashMap();
        for (Map.Entry<byte[], byte[]> map : familyMap.entrySet()) {
            result.put(Bytes.toInt(map.getKey()), Bytes.toLong(map.getValue()));
        }
        return result;
    }

}
