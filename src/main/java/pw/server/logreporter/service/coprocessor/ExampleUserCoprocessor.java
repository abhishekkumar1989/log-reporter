package pw.server.logreporter.service.coprocessor;

import org.apache.hadoop.hbase.Coprocessor;
import org.apache.hadoop.hbase.CoprocessorEnvironment;

import java.io.IOException;

public class ExampleUserCoprocessor implements Coprocessor {
    @Override
    public void start(CoprocessorEnvironment env) throws IOException {
//        env.

    }

    @Override
    public void stop(CoprocessorEnvironment env) throws IOException {

    }
}
