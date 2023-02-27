package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import http.HttpRequest;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
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
            HttpRequest httpRequest = new HttpRequest(in);
            String path = getPath(httpRequest.getPath());

            /**
             * method : GET
             */
            if (httpRequest.getMethod().isGet()){
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

                if (path.equals("/user/create")){
                    /**
                     * GET 방식으로 create user
                     */
                    User user = new User(
                            httpRequest.getParameter("userId"),
                            httpRequest.getParameter("password"),
                            httpRequest.getParameter("name"),
                            httpRequest.getParameter("email")
                    );
                    createUser(user);
                }

                if (path.equals("/user/list")){
                    if (isLogin(httpRequest.getHeader("Cookie"))){
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
                    byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    responseCss(out, body);
                }
            }

            /**
             * method : POST
             */
            if (httpRequest.getMethod().isPost()){
                if (path.equals("/user/create")){
                    User user = new User(
                            httpRequest.getParameter("userId"),
                            httpRequest.getParameter("password"),
                            httpRequest.getParameter("name"),
                            httpRequest.getParameter("email")
                    );
                    createUser(user);
                    DataOutputStream dos = new DataOutputStream(out);
                    response302Header(dos, "http://15.165.185.73:9090");
                }

                if (path.equals("/user/login")){
                    String userId = httpRequest.getParameter("userId");
                    String password = httpRequest.getParameter("password");
                    DataOutputStream dos = new DataOutputStream(out);
                    User findUser = DataBase.findUserById(userId);

                    if (findUser.getUserId().equals(userId) && findUser.getPassword().equals(password)){
                        response302Header(dos, "http://15.165.185.73:9090", "true");
                    }else{
                        response302Header(dos, "http://15.165.185.73:9090/user/login_failed.html", "false");
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private static String getPath(String path) {
        if (path.equals("/")){
            return "/index.html";
        }else{
            return path;
        }
    }

    private static boolean isLogin(String cookieValue) {
        Map<String, String> stringStringMap = HttpRequestUtils.parseCookies(cookieValue);
        boolean login = Boolean.parseBoolean(stringStringMap.get("logined"));
        if (login) {
            return true;
        }else{
            return false;
        }
    }

    private void response(OutputStream out, byte[] body) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        response200Header(dos, body.length);
        responseBody(dos, body);
    }

    private void responseCss(OutputStream out, byte[] body) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        response200HeaderCss(dos, body.length);
        responseBody(dos, body);
    }

    private void createUser(User user) {
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
