package http;

import java.io.*;
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

    private StartLineParsing startLineParsing;

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
            startLineParsing = new StartLineParsing(line);

            /**
             * header를 Map에 저장
             */
            line = buffer.readLine();
            while (!line.equals("")) {
                log.debug("header : {}", line);
                //": " => 공백이 하나 들어감 (파싱할때 주의)
                String[] header = line.split(": ");
                httpHeaders.put(header[0], header[1]);
                line = buffer.readLine();
            }

            /**
             * GET, POST 방식에 따라 Param 설정
             */
            if (getMethod().isGet()){
                params = startLineParsing.getParams();
            }

            if (getMethod().isPost()) {
                String body = IOUtils.readData(buffer,
                        Integer.parseInt(httpHeaders.get("Content-Length")));
                params = HttpRequestUtils.parseQueryString(body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Boolean isLogin(){
        if (getHeader("Cookie") != null){
            String cookie_value = getHeader("Cookie");
            Map<String, String> cookies = HttpRequestUtils.parseCookies(cookie_value);
            boolean login = Boolean.parseBoolean(cookies.get("logined"));
            if (login){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public HttpMethod getMethod() {
        return startLineParsing.getMethod();
    }

    public String getPath() {
        return startLineParsing.getPath();
    }

    public String getHeader(String name) {
        return httpHeaders.get(name);
    }

    public String getParameter(String name) {
        return params.get(name);
    }
}