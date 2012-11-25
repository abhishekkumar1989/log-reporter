package pw.server.logreporter.service;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.regionserver.StoreFile;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.service.coprocessor.ExampleUserCoprocessor;

import java.io.IOException;
import java.util.Collections;

import static pw.server.logreporter.util.ApplicationConstants.ColumnFamily.*;
import static pw.server.logreporter.util.ApplicationConstants.HBaseTableNames.*;

@Service
public class CreateTable {

    private HBaseConfig config;

    @Autowired
    public CreateTable(HBaseConfig config) {
        this.config = config;
        createIfNotExists();
    }

    private void createIfNotExists() {
        HBaseAdmin hBaseAdmin = null;
        try {
            hBaseAdmin = new HBaseAdmin(config);
            dropAll(hBaseAdmin);
            createLogTable(hBaseAdmin);
//            createDetailTable(hBaseAdmin);
            createCounterTable(hBaseAdmin);
        } catch (MasterNotRunningException e) {
            Logger.getLogger(getClass()).info("Master Not Running");
        } catch (ZooKeeperConnectionException e) {
            Logger.getLogger(getClass()).info("Zookeeper Not Running");
        } finally {
            try {
                hBaseAdmin.close();
            } catch (IOException e) {
                Logger.getLogger(getClass()).error("Error in closing the HBaseAdmin");
            }
        }

    }

    private void dropAll(HBaseAdmin hBaseAdmin) {
        try {
            hBaseAdmin.disableTable(T_LOG_TABLE);
            hBaseAdmin.disableTable(T_ERROR_COUNTER);
            hBaseAdmin.deleteTable(T_LOG_TABLE);
            hBaseAdmin.deleteTable(T_ERROR_COUNTER);
        } catch (IOException e) {
            Logger.getLogger(getClass()).error("Error in dropping the previous table");
        }
    }

    private void createLogTable(HBaseAdmin hBaseAdmin) {
        try {
            if (!hBaseAdmin.tableExists(T_LOG_TABLE)) {
                HTableDescriptor tableDescriptor = new HTableDescriptor(T_LOG_TABLE);
                HColumnDescriptor hColumnDescriptor = new HColumnDescriptor(CF_LOG_DETAILS);
                hColumnDescriptor.setCompressionType(Compression.Algorithm.GZ);
                hColumnDescriptor.setBloomFilterType(StoreFile.BloomType.ROW);
                tableDescriptor.addFamily(hColumnDescriptor);
                tableDescriptor.setMaxFileSize(1000000);
                tableDescriptor.setDeferredLogFlush(true);
                // TODO : which is the best one to use in the below
//                tableDescriptor.addCoprocessor(ExampleUserCoprocessor.getClass(), getJarPath(hBaseAdmin), Coprocessor.PRIORITY_USER, Collections.EMPTY_MAP);
//                tableDescriptor.setValue("COPROCESSOR$1", getJarPath(hBaseAdmin).toString() + "|" + ExampleUserCoprocessor.class.getCanonicalName() + "|" + Coprocessor.PRIORITY_USER);
                hBaseAdmin.createTable(tableDescriptor);
                Logger.getLogger(getClass()).info("The table descriptor set is [ " + hBaseAdmin.getTableDescriptor(T_LOG_TABLE) + " ]");
            }
        } catch (IOException e) {
            Logger.getLogger(getClass()).info("Error in creating table " + T_LOG_TABLE_STRING + " Not Running");
        }
    }

    private Path getJarPath(HBaseAdmin hBaseAdmin) {
        try {
            FileSystem fs = FileSystem.get(hBaseAdmin.getConfiguration());
            return new Path(fs.getUri() + Path.SEPARATOR + "test.jar");
        } catch (IOException e) {
            Logger.getLogger(getClass()).error("Error in getting the configuration for the coprocessor for reading the file");
        }
        return null;
    }

    private void createDetailTable(HBaseAdmin hBaseAdmin) {
        try {
            hBaseAdmin.getTableDescriptor(T_LOG_TABLE);
        } catch (Exception ex) {
            HTableDescriptor tableDescriptor = new HTableDescriptor(T_LOG_TABLE);
            tableDescriptor.addFamily(new HColumnDescriptor(CF_LOG_DETAILS));
            try {
                hBaseAdmin.createTable(tableDescriptor);
            } catch (IOException e) {
                Logger.getLogger(getClass()).info("Error in creating table " + T_LOG_TABLE_STRING + " Not Running");
            }
        }
    }

    private void createCounterTable(HBaseAdmin hBaseAdmin) {
        try {
            hBaseAdmin.getTableDescriptor(T_ERROR_COUNTER);
        } catch (Exception ex) {
            HTableDescriptor tableDescriptor = new HTableDescriptor(T_ERROR_COUNTER);
            tableDescriptor.addFamily(new HColumnDescriptor(CF_COUNTER_YEARLY));
            tableDescriptor.addFamily(new HColumnDescriptor(CF_COUNTER_MONTHLY));
            tableDescriptor.addFamily(new HColumnDescriptor(CF_COUNTER_DAILY));
            try {
                hBaseAdmin.createTable(tableDescriptor);
            } catch (IOException e) {
                Logger.getLogger(getClass()).info("Error in creating table [ " + T_ERROR_COUNTER_STRING + " ] Not Running");
            }
        }
    }


}
