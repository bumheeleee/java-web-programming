package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class LoginController extends AbstractController{
    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
        User findUser = DataBase.findUserById(request.getParameter("userId"));
        if (findUser != null) {
            if (findUser.getPassword().equals(request.getParameter("password"))) {
                response.addHeader("Set-Cookie", "logined=true");
                response.sendRedirect("http://15.165.185.73:9090");
            } else {
                response.sendRedirect("http://15.165.185.73:9090/user/login_failed.html");
            }
        } else {
            response.sendRedirect("http://15.165.185.73:9090/user/login_failed.html");
        }
    }
}
