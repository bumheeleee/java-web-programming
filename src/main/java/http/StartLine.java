package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class StartLine {
    private static final Logger log = LoggerFactory.getLogger(StartLine.class);
    private Map<String, String> params = new HashMap<>();
    private String method;
    private String path;

    public StartLine(String startLine) {
        /**
         * startLine에서 method, path 뽑아내기
         */
        log.debug("startLine : {}", startLine);
        String[] token = startLine.split(" ");
        method = token[0];

        if (token.length != 3){
            throw new IllegalArgumentException(startLine + "이 올바르지 않습니다.");
        }

        if ("POST".equals(method)){
            path = token[1];
            return;
        }
        /**
         * GET일 경우 2가지 -> 쿼리스트링이 있는경우, 쿼리스트링이 없는경우
         */
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
    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getParams() {
        return params;
    }
}
