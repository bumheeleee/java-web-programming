package http;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private Map<String, String> httpHeaders = new HashMap<>();

    private Map<String, String> params = new HashMap<>();

    private String method;

    private String path;

    public HttpRequest(InputStream is) {
        BufferedReader buffer = null;
        try {
            buffer = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            String line = buffer.readLine();
            if (line == null){
                return;
            }
            /**
             * path, method 정보
             */
            processStartLine(line);

            /**
             * header를 Map에 저장
             */
            line = buffer.readLine();
            while (!line.equals("")) {
                log.debug("header : {}", line);
                String[] header = line.split(":");
                httpHeaders.put(header[0], header[1]);
                line = buffer.readLine();
            }

            if (method.equals("POST")) {
                String body = IOUtils.readData(buffer,
                        Integer.parseInt(httpHeaders.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void processStartLine(String startLine){
        /**
         * startLine에서 method, path 뽑아내기
         */
        log.debug("startLine : {}", startLine);
        String[] token = startLine.split(" ");
        method = token[0];

        if ("POST".equals(method)){
            path = token[1];
            return;
        }else{
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
                params = HttpRequestUtils.parseQueryString(token[1].substring(idx));
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String name) {
        return httpHeaders.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}