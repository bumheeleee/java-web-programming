package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
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
                log.debug("header : {}", line);
                httpHeaders.add(line);
                line = buffer.readLine();
            }

            String[] startLine = httpHeaders.get(0).split(" ");
            String method = startLine[0];
            String path = startLine[1];

            int contentLength = 0;
            String cookie = "";
            for (String httpHeader:
                    httpHeaders) {
                String[] header = httpHeader.split(" ");
                if (header[headerKey].contains("Content-Length:")){
                    contentLength = Integer.parseInt(header[headerValue]);
                }
                if (header[headerKey].contains("Cookie:")){
                    cookie = header[headerValue];
                }
            }

            /**
             * method : GET
             */
            if (method.equals("GET")){
                if (path.equals("/index.html")){
                    byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    response(out, body);
                }

                if (path.equals("/user/form.html")){
                    byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    response(out, body);
                }

                if (path.equals("/user/login.html")){
                    byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    response(out, body);
                }

                if (path.equals("/user/login_failed.html")){
                    byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    response(out, body);
                }

                if (path.startsWith("/user/create")){
                    /**
                     * GET 방식으로 create user
                     */
                    if (path.contains("?")){
                        int idx = path.indexOf("?");
                        String queryParam = path.substring(idx + 1);
                        createUser(queryParam, getQueryParamMap(queryParam));
                    }
                }


                if (path.equals("/user/list")){
                    Map<String, String> stringStringMap = HttpRequestUtils.parseCookies(cookie);
                    boolean logined = Boolean.parseBoolean(stringStringMap.get("logined"));

                    if (logined){
                        Collection<User> users = DataBase.findAll();
                        StringBuilder sb = new StringBuilder();
                        sb.append("<table>");
                        for (User user:
                                users) {
                            sb.append("<tr>");
                            sb.append("<td>" + user.getUserId() + "</td>");
                            sb.append("<td>" + user.getName() + "</td>");
                            sb.append("<td>" + user.getEmail() + "</td>");
                            sb.append("</tr>");
                        }
                        sb.append("</table>");
                        byte[] body = sb.toString().getBytes();
                        response(out, body);
                    }else {
                        response302Header(new DataOutputStream(out), "/user/login.html");
                    }

                }

                if (path.endsWith(".css")){
                    responseCss(out, path);
                }
            }

            /**
             * method : POST
             */
            if (method.equals("POST")){
                if (path.equals("/user/create")){
                    String body = IOUtils.readData(buffer, contentLength);
                    createUser(body, getQueryParamMap(body));
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos, "http://localhost:9090/index.html");
                }

                if (path.equals("/user/login")){
                    String body = IOUtils.readData(buffer, contentLength);
                    Map<String, String> queryParamMap = getQueryParamMap(body);
                    DataOutputStream dos = new DataOutputStream(out);
                    String userId = queryParamMap.get("userId");
                    String password = queryParamMap.get("password");

                    User findUser = DataBase.findUserById(userId);

                    if (findUser.getUserId().equals(userId) && findUser.getPassword().equals(password)){
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

    private void response(OutputStream out, byte[] body) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void responseCss(OutputStream out, String path) throws IOException {
        byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
        DataOutputStream dos = new DataOutputStream(out);
        response200HeaderCss(dos, body.length);
        responseBody(dos, body);
    }

    private void createUser(String body, Map<String, String> queryParamMap) {
        String userId = queryParamMap.get("userId");
        String password = queryParamMap.get("password");
        String name = queryParamMap.get("name");
        String email = queryParamMap.get("email");

        User user = new User(userId, password, name, email);
        DataBase.addUser(user);
        log.debug("User -> userId : {}, password : {}, name : {}, email : {}"
                ,user.getUserId(), user.getPassword(), user.getName(), user.getEmail());
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

    private void response200HeaderCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String url, String cookie) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + url + "\r\n");
            dos.writeBytes("Set-Cookie: logined=" + cookie + ";" + "\r\n");
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
