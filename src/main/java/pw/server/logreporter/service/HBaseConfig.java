package pw.server.logreporter.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.log4j.Logger;

import java.io.IOException;

public class HBaseConfig extends Configuration {
    public HBaseConfig(String zookeperQuorum, String zookeperClientPort, String masterAddr) throws IOException {
        super();
//        Configuration config = HBaseConfiguration.create();
        // TODO : how to do authentication
        this.clear();
        this.set("hbase.zookeper.quorum", zookeperQuorum);
        this.set("hbase.zookeeper.property.clientPort", zookeperClientPort);
        this.set("hbase.master", masterAddr);
        HBaseAdmin.checkHBaseAvailable(this);
        Logger.getLogger(getClass()).info("HBase : HBase is Running..!!");
    }

}
