package pw.server.logreporter.observer;

import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.BaseRegionObserver;
import org.apache.hadoop.hbase.coprocessor.ObserverContext;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;
import org.apache.hadoop.hbase.regionserver.wal.WALEdit;
import org.apache.log4j.Logger;

import java.io.IOException;

public class RegionObserverCheck extends BaseRegionObserver{
    @Override
    public void preDelete(ObserverContext<RegionCoprocessorEnvironment> e, Delete delete, WALEdit edit, boolean writeToWAL) throws IOException {
        Logger.getLogger(getClass()).info("The trigger is getting called before the delete operation");
        if(delete.isEmpty())
            throw new NullPointerException("Delete can't be null");
//        super.preDelete(e, delete, edit, writeToWAL);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void prePut(ObserverContext<RegionCoprocessorEnvironment> e, Put put, WALEdit edit, boolean writeToWAL) throws IOException {
        Logger.getLogger(getClass()).info("The trigger is getting called before the put operation for [ " + put.toJSON() + " ]");
    }
}
