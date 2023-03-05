package controller;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;

public abstract class AbstractController implements Controller{
    /**
     * Controller를 직접 구현하는 것이 아닌, 추상화해서 GET, POST를 나눠서 구현한 경우
     * URL이 동일하더라도, HTTP Method에 따라 다른 서비스 로직을 수행할 수 있다.
     */
    @Override
    public void service(HttpRequest request, HttpResponse response) {
        HttpMethod method = request.getMethod();
        if (method.isGet()){
            doGet(request, response);
        }else if (method.isPost()){
            doPost(request, response);
        }
    }

    protected void doPost(HttpRequest request, HttpResponse response){

    }

    protected void doGet(HttpRequest request, HttpResponse response){

    }
}
