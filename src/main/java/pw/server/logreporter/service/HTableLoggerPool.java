package pw.server.logreporter.service;

import org.apache.hadoop.hbase.client.HTablePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HTableLoggerPool extends HTablePool {

    @Autowired
    public HTableLoggerPool(HBaseConfig hBaseConfig) {
                        // maxSize of the pool = 10, i.e. 10 atmost HTable references are there
        super(hBaseConfig, 10);
    }

}
