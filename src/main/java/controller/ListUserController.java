package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.util.Collection;

public class ListUserController implements Controller{
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        if (request.isLogin()) {
            Collection<User> users = DataBase.findAll();
            StringBuilder sb = new StringBuilder();
            sb.append("<table>");
            for (User user :
                    users) {
                sb.append("<tr>");
                sb.append("<td>" + user.getUserId() + "</td>");
                sb.append("<td>" + user.getName() + "</td>");
                sb.append("<td>" + user.getEmail() + "</td>");
                sb.append("</tr>");
            }
            sb.append("</table>");
            response.forwardBody(sb.toString());
        } else {
            response.sendRedirect("http://15.165.185.73:9090/user/login.html");
        }
    }
}