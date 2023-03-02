package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Collection;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            HttpResponse httpResponse = new HttpResponse(out);
            String path = httpRequest.getPath();

            /**
             * method : GET
             */
            if (httpRequest.getMethod().isGet()){
                if (path.equals("/index.html")){
                    httpResponse.forward(path);
                }

                if (path.equals("/user/form.html")){
                    httpResponse.forward(path);
                }

                if (path.equals("/user/login.html")){
                    httpResponse.forward(path);
                }

                if (path.equals("/user/login_failed.html")){
                    httpResponse.forward(path);
                }

                if (path.endsWith(".css")){
                    httpResponse.forward(path);
                }

                if (path.equals("/user/list")){
                    if (httpRequest.isLogin()){
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
                        httpResponse.forwardBody(sb.toString());
                    }else {
                        httpResponse.sendRedirect("http://15.165.185.73:9090/user/login.html");
                    }
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
                    httpResponse.sendRedirect("http://15.165.185.73:9090");
                }

                if (path.equals("/user/login")){
                    String userId = httpRequest.getParameter("userId");
                    String password = httpRequest.getParameter("password");
                    DataOutputStream dos = new DataOutputStream(out);
                    User findUser = DataBase.findUserById(userId);

                    if (findUser.getUserId().equals(userId) && findUser.getPassword().equals(password)){
                        httpResponse.sendRedirect("http://15.165.185.73:9090");
                    }else{
                        httpResponse.sendRedirect("http://15.165.185.73:9090/user/login_failed.html");
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void createUser(User user) {
        DataBase.addUser(user);
        log.debug("User -> userId : {}, password : {}, name : {}, email : {}"
                ,user.getUserId(), user.getPassword(), user.getName(), user.getEmail());
    }
}
