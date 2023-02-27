package http;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class StartLineHandlerTest {

    @Test
    public void create_method_get() {
        StartLineHandler line = new StartLineHandler("GET /index.html HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_method_post() {
        StartLineHandler line = new StartLineHandler("POST /index.html HTTP/1.1");
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_path_and_params() {
        StartLineHandler line = new StartLineHandler("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/user/create", line.getPath());
        Map<String, String> params = line.getParams();
        assertEquals(2, params.size());
    }
}