package controller;

import db.DataBase;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

public class CreateUserController extends AbstractController{
    @Override
    protected void doPost(HttpRequest request, HttpResponse response) {
        User user = new User(
                request.getParameter("userId"), request.getParameter("password"),
                request.getParameter("name"), request.getParameter("email")
        );
        DataBase.addUser(user);
        response.sendRedirect("http://15.165.185.73:9090");
    }
}
