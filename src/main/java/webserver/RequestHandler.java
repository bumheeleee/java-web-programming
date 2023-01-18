package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

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
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in,"UTF-8"));

            ArrayList<String> httpHeaders = new ArrayList<>();
            String line = buffer.readLine();

            if (line == null){
                return;
            }

            while (!line.equals("")){
                System.out.println("line = " + line);
                httpHeaders.add(line);
                line = buffer.readLine();
            }

            String[] startLine = httpHeaders.get(0).split(" ");
            String method = startLine[0];
            String path = startLine[1];

            int contentLength = 0;
            for (String httpHeader:
                 httpHeaders) {
                String[] header = httpHeader.split(" ");
                if (header[0].contains("Content-Length:")){
                    contentLength = Integer.parseInt(header[1]);
                }
            }

            if (method.equals("GET")){
                if (path.equals("/index.html")){
                    byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    DataOutputStream dos = new DataOutputStream(out);
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }

                if (path.equals("/user/form.html")){
                    byte[] body = Files.readAllBytes(new File("./webapp" + path).toPath());
                    DataOutputStream dos = new DataOutputStream(out);
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }

                if (path.contains("?")){
                    int idx = path.indexOf("?");
                    String queryParam = path.substring(idx + 1);
                    Map<String, String> queryParamMap = HttpRequestUtils.parseQueryString(queryParam);

                    String userId = queryParamMap.get("userId");
                    String password = queryParamMap.get("password");
                    String name = queryParamMap.get("name");
                    String email = queryParamMap.get("email");

                    User user = new User(userId, password, name, email);
                    System.out.println("user = " + user);
                }
            }

            if (method.equals("POST")){
                if (path.equals("/user/create")){
                    String body = IOUtils.readData(buffer, contentLength);
                    Map<String, String> queryParamMap = HttpRequestUtils.parseQueryString(body);

                    String userId = queryParamMap.get("userId");
                    String password = queryParamMap.get("password");
                    String name = queryParamMap.get("name");
                    String email = queryParamMap.get("email");

                    User user = new User(userId, password, name, email);
                    System.out.println("user = " + user);

                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
