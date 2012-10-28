package pw.server.logreporter.util.hbase;

import org.apache.hadoop.hbase.client.HTablePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static pw.server.logreporter.service.ApplicationConstants.HBaseTableNames.LOG_TABLE;
import static pw.server.logreporter.service.ApplicationConstants.HBaseTableNames.LOG_TABLE_STRING;

@Service
public class HTableLoggerPool extends HTablePool {

    @Autowired
    public HTableLoggerPool(HBaseConfig hBaseConfig) {
        super(hBaseConfig, 10);
    }

}
