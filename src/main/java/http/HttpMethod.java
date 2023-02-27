package http;

public enum HttpMethod {
    GET, POST;

    public Boolean isGet(){
        return this == GET;
    }

    public Boolean isPost(){
        return this == POST;
    }
}
