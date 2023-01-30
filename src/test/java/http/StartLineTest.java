package http;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class StartLineTest{

    @Test
    public void create_method_get() {
        StartLine line = new StartLine("GET /index.html HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_method_post() {
        StartLine line = new StartLine("POST /index.html HTTP/1.1");
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_path_and_params() {
        StartLine line = new StartLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/user/create", line.getPath());
        Map<String, String> params = line.getParams();
        assertEquals(2, params.size());
    }
}