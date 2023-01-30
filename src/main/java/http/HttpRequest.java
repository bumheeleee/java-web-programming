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

    private StartLine startLine;

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
            startLine = new StartLine(line);

            /**
             * header를 Map에 저장
             */
            line = buffer.readLine();
            while (!line.equals("")) {
                log.debug("header : {}", line);
                String[] header = line.split(": ");
                httpHeaders.put(header[0], header[1]);
                line = buffer.readLine();
            }

            if (getMethod().equals("POST")) {
                String body = IOUtils.readData(buffer,
                        Integer.parseInt(httpHeaders.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }else{
                params = startLine.getParams();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getMethod() {
        return startLine.getMethod();
    }

    public String getPath() {
        return startLine.getPath();
    }

    public String getHeader(String name) {
        return httpHeaders.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}