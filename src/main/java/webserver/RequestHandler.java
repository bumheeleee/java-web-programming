package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private static final int headerKey = 0;

    private static final int headerValue = 1;

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        /**
         * 구현 하는 곳
         * in : 클라이언트에서 서버로 요청을 보낼때 사용, out : 서버에서 클라이언트로 데이터를 보낼때 사용
         */
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in,"UTF-8"));

            String line = buffer.readLine();
            if (line == null){
                return;
            }

            ArrayList<String> httpHeaders = new ArrayList<>();
            while (!line.equals("")){
                System.out.println("header = " + line);
                httpHeaders.add(line);
                line = buffer.readLine();
            }

            String[] startLine = httpHeaders.get(0).split(" ");
            String method = startLine[0];
            String path = startLine[1];

            int contentLength = 0;
            for (String httpHeader:
                 httpHeaders) {
                String[] header = httpHeader.split(" ");
                if (header[headerKey].contains("Content-Length:")){
                    contentLength = Integer.parseInt(header[headerValue]);
                }
            }

            if (method.equals("GET")){
                if (path.equals("/index.html")){
                    response(out, path);
                }

                if (path.equals("/user/form.html")){
                    response(out, path);
                }

                if (path.equals("/user/login.html")){
                    response(out, path);
                }

                if (path.equals("/user/login_failed.html")){
                    response(out, path);
                }

                if (path.contains("?")){
                    int idx = path.indexOf("?");
                    String queryParam = path.substring(idx + 1);
                    createUser(queryParam, getQueryParamMap(queryParam));
                }
            }

            if (method.equals("POST")){
                if (path.equals("/user/create")){
                    String body = IOUtils.readData(buffer, contentLength);
                    createUser(body, getQueryParamMap(body));
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos, "http://localhost:9090/index.html", "false");
                }

                if (path.equals("/user/login")){
                    String body = IOUtils.readData(buffer, contentLength);
                    Map<String, String> queryParamMap = getQueryParamMap(body);
                    DataOutputStream dos = new DataOutputStream(out);
                    String userId = queryParamMap.get("userId");

                    if (DataBase.findUserById(userId) != null){
                        response302Header(dos, "http://localhost:9090/index.html", "true");
                    }else{
                        response302Header(dos, "http://localhost:9090/user/login_failed.html", "false");
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response(OutputStream out, String path) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
        DataOutputStream dos = new DataOutputStream(out);
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void createUser(String body, Map<String, String> queryParamMap) {
        String userId = queryParamMap.get("userId");
        String password = queryParamMap.get("password");
        String name = queryParamMap.get("name");
        String email = queryParamMap.get("email");

        User user = new User(userId, password, name, email);
        DataBase.addUser(user);
        System.out.println("user = " + user);
    }

    private static Map<String, String> getQueryParamMap(String body) {
        Map<String, String> queryParamMap = HttpRequestUtils.parseQueryString(body);
        return queryParamMap;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + cookie + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
