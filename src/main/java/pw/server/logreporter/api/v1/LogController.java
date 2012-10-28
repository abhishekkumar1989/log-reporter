package pw.server.logreporter.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pw.server.logreporter.api.BaseController;
import pw.server.logreporter.api.annotation.APIVersion;
import pw.server.logreporter.util.NullChecker;
import pw.server.logreporter.util.hbase.file.FileParser;
import pw.server.logreporter.util.hbase.mongolog.reader.HBaseReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static pw.server.logreporter.util.NullChecker.isNotNull;

@Controller("logController1_0")
@APIVersion(1.0f)
public class LogController extends BaseController {

    private FileParser fileParser;
    private HBaseReader reader;
    private static final long defaultMillis = 2592000000L;

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
    @RequestMapping(value = "/get_raw_row")
    public Map<String, String> getRawRowDetails(@RequestParam(value = "row_key", required = false) String rowKey,
//                                                @RequestParam(value = "millis_back", required = false, defaultValue = defaultMillis) long millis) throws IOException {
                                                @RequestParam(value = "millis_back", required = false) long millis) throws IOException {
        return reader.getRawDatas(rowKey, isNotNull(millis) ? millis : defaultMillis);
    }



}
