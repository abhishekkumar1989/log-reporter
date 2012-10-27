package pw.server.logreporter.util.hbase;

import org.apache.hadoop.hbase.client.HTablePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HTableLoggerPool extends HTablePool {

    @Autowired
    public HTableLoggerPool(HBaseConfig hBaseConfig) {
        super(hBaseConfig, 10);
    }
}
