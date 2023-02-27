package http;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class StartLineParsingTest {

    @Test
    public void create_method_get() {
        StartLineParsing line = new StartLineParsing("GET /index.html HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_method_post() {
        StartLineParsing line = new StartLineParsing("POST /index.html HTTP/1.1");
        assertEquals("/index.html", line.getPath());
    }

    @Test
    public void create_path_and_params() {
        StartLineParsing line = new StartLineParsing("GET /user/create?userId=javajigi&password=pass HTTP/1.1");
        assertEquals("GET", line.getMethod());
        assertEquals("/user/create", line.getPath());
        Map<String, String> params = line.getParams();
        assertEquals(2, params.size());
    }
}