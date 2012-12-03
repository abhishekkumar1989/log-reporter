package pw.server.logreporter.api.v1;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pw.server.logreporter.api.BaseController;
import pw.server.logreporter.api.annotation.APIVersion;
import pw.server.logreporter.util.hbase.file.FileParser;
import pw.server.logreporter.service.mongolog.reader.HBaseReader;

import java.io.IOException;
import java.util.Map;

import static pw.server.logreporter.util.NullChecker.isNull;

@Controller("logController1_0")
@APIVersion(1.0f)
public class LogController extends BaseController {

    private FileParser fileParser;
    private HBaseReader reader;
    private static final long defaultMillis = 2592000000L;
    private static final long defaultMins = 1440L;
    private static final long minsToMillis = 60000;

    @Autowired
    public LogController(FileParser fileParser, HBaseReader reader) {
        this.fileParser = fileParser;
        this.reader = reader;
    }

    @ResponseBody
    @RequestMapping(value = "/start_logging")
    public String startFileParser(@RequestParam(required = false) String path) throws IOException {
        fileParser.statFileLogger(path);
        return "SUCCESS";
    }

    @ResponseBody
    @RequestMapping(value = "/get_error_details")
    public Map<String, String> getErrorDetails(@RequestParam(value = "row_key", required = false) String rowKey,
                                               @RequestParam(value = "start_time", required = false) final Long start_mins,
                                               @RequestParam(value = "stop_time", required = false) final Long stop_mins,
                                               @RequestParam(value = "value", required = false) final String text,
                                               @RequestParam(value = "versions", required = false) final Integer versions) throws IOException {
        Map<String, String> rawDatas = reader.getErrorResults(rowKey, text, isNull(start_mins) ? defaultMillis : start_mins * minsToMillis, stop_mins * minsToMillis, isNull(versions) ? Integer.MAX_VALUE : versions);
        Logger.getLogger(getClass()).debug("Responding with the result");
        return rawDatas;
    }

        @ResponseBody
    @RequestMapping(value = "/get_counter")
    public Object getCounter(@RequestParam(value = "row_key", required = true) String rowKey,
                             @RequestParam(value = "type", required = false) String type,
                             @RequestParam(value = "cf", required = false) String colQualifier) throws IOException {
        return reader.getCounts(rowKey, type, colQualifier);
    }

    @ResponseBody
    @RequestMapping(value = "/all_error_details")
    public Object getAllErrorCounter(@RequestParam(value = "type", required = false) String type) throws IOException {
        return reader.getAllCounters(isNull(type) ? "monthly" : type);
    }



}
