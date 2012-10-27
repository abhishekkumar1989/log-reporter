package pw.server.logreporter.api.annotation.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

//@Component
public class MetaTokenArgumentResolver implements WebArgumentResolver {

//    private HBaseLogger hBaseLogger;

//    @Autowired
//    public MetaTokenArgumentResolver(HBaseLogger hBaseLogger) {
//        this.hBaseLogger = hBaseLogger;
//    }

//    @Override
//    public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
//           return UNRESOLVED;
//       }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
