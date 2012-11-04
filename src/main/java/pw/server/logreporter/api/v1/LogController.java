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
    private static final int defaultVersions = 1;
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
    @RequestMapping(value = "/get_row")
    public Map<String, Map> getRowDetails(@RequestParam(value = "row_key", required = false) String rowKey) throws IOException {
        return reader.getRowDetails(rowKey);
    }

    @ResponseBody
    @RequestMapping(value = "/get_rows")
    public Object getSortedRowResults(@RequestParam(value = "row_key", required = false) String rowKey,
                                      @RequestParam(value = "mins_back", required = false) final Long mins) throws IOException {
        return reader.scanRowInOrder(rowKey, isNull(mins) ? defaultMins : mins);
    }

    @ResponseBody
    @RequestMapping(value = "/get_raw_row")
    public Map<String, String> getRawRowDetails(@RequestParam(value = "row_key", required = false) String rowKey,
                                                @RequestParam(value = "mins_back", required = false) final Long mins,
                                                @RequestParam(value = "versions", required = false) final Integer versions) throws IOException {
        Map<String, String> rawDatas = reader.getRawDatas(rowKey, isNull(mins) ? defaultMillis : mins * minsToMillis, isNull(versions) ? defaultVersions : versions);
        Logger.getLogger(getClass()).debug("Responding with the result");
        return rawDatas;
    }



}
