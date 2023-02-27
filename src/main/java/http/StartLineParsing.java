package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class StartLineParsing {
    private static final Logger log = LoggerFactory.getLogger(StartLineParsing.class);
    private Map<String, String> params = new HashMap<>();
    private HttpMethod method;
    private String path;

    public StartLineParsing(String startLine) {
        /**
         * startLine에서 method, path 뽑아내기
         */
        log.debug("startLine : {}", startLine);
        String[] token = startLine.split(" ");
        method = HttpMethod.valueOf(token[0]);

        if (token.length != 3){
            throw new IllegalArgumentException(startLine + "이 올바르지 않습니다.");
        }

        /**
         * POST 방식일 경우
         */
        if (method.isPost()){
            path = token[1];
            return;
        }

        /**
         * GET일 경우 2가지 -> 쿼리스트링이 있는경우, 쿼리스트링이 없는경우
         */
        if (method.isGet()){
            int idx = token[1].indexOf("?");
            // 쿼리스트링이 없는경우
            if (idx == -1){
                path = token[1];
            }else{
                // 쿼리스트링이 있는경우
                path = token[1].substring(0, idx);
                params = HttpRequestUtils.parseQueryString(token[1].substring(idx+1));
            }
        }

    }
    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
