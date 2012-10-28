package pw.server.logreporter.util.hbase.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.util.hbase.mongolog.writer.HBaseLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static pw.server.logreporter.util.NullChecker.isNotNull;
import static pw.server.logreporter.util.NullChecker.isNull;

@Service
public class FileParser {

    private HBaseLogger hBaseLogger;

    @Autowired
    public FileParser(HBaseLogger hBaseLogger) {
        this.hBaseLogger = hBaseLogger;
    }

    public void statFileLogger(String path) throws IOException {
        if (isNull(path))
            path = "r1.log";
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String strLine;
        while (isNotNull(strLine = reader.readLine())) {
            hBaseLogger.insertLogMessage(strLine);
        }
    }

}
