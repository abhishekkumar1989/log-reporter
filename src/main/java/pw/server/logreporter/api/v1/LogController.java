package pw.server.logreporter.api.v1;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pw.server.logreporter.api.BaseController;
import pw.server.logreporter.api.annotation.APIVersion;

import java.util.HashMap;
import java.util.Map;

@Controller("logController1_0")
@APIVersion(1.0f)
public class LogController extends BaseController {

    @ResponseBody
    @RequestMapping(value = "/testing")
    public Map getTestKey() {
        Map map = new HashMap(2);
        map.put("name", "Abhishek");
        map.put("age", 21);
        return map;
    }


}
