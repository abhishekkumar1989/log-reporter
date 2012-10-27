package pw.server.logreporter.util.hbase.file;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pw.server.logreporter.util.hbase.HBaseLogger;

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
        try{
        fileLogger(null);
        } catch (IOException ex) {
            Logger.getLogger(getClass()).error("Error in Reading file for logging ");
        }

    }

    public void fileLogger(String path) throws IOException {
        if (isNull(path))
            path = "/Users/abhishek/history.log.1";
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String strLine;
        while (isNotNull(strLine = reader.readLine())) {
            hBaseLogger.insertLogMessage(strLine);
        }
    }

}
