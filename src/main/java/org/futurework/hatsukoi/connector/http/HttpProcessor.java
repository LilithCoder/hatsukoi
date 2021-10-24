package org.futurework.hatsukoi.connector.http;

import java.io.OutputStream;
import java.net.Socket;

/**
 * http处理器
 * @author 高蔚霖
 * @since 2021/10/24
 * */
public class HttpProcessor {
    // 该http处理器关联的http连接器
    private HttpConnector httpConnector = null;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private HttpRequestLine httpRequestLine = new HttpRequestLine();

    public HttpProcessor(HttpConnector httpConnector) {
        this.httpConnector = httpConnector;
    }

    /**
     * 通过传进来的套接字处理面临的HTTP请求
     * @param socket 与客户端形成连接通信的套接字
     * */
    public void process(Socket socket) {
        // 获取套接字的输入流和输出流，SocketInputStream这里继承了InputStream
        SocketInputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new SocketInputStream(socket.getInputStream(), 2048);
            outputStream = socket.getOutputStream();
            // 创建HttpRequest实例和HttpResponse实例
            httpRequest = new HttpRequest(inputStream);
            httpResponse = new HttpResponse(outputStream);
            // 解析请求和请求头
            parseRequest();
            parseHeader();
            // 配置响应
            httpResponse.setHttpRequest(httpRequest);
            httpResponse.setHeader("Server", "Hatsukoi");
            // 将请求和响应交给Servlet处理
            servletProcess();

            // 关闭套接字
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析http请求
     * @param inputStream 输入流
     * */
    public void parseRequest(SocketInputStream inputStream) {
        // 解析请求行
        inputStream.readRequestLine(this.httpRequestLine);
    }

    /**
     * 处理相关的servlet
     * @param httpRequest
     * @param httpResponse
     * */
    public void servletProcess(HttpRequest httpRequest, HttpResponse httpResponse) {

    }

}
