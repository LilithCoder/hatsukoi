package org.futurework.hatsukoi.connector.http;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * http响应
 * @author 高蔚霖
 * @since 2021/10/24
 * */
public class HttpResponse implements HttpServletResponse {
    /**
     * 输出流
     * */
    private OutputStream outputStream;
    /**
     * 与响应关联的请求
     * */
    private HttpRequest httpRequest = null;
    /**
     * 响应是否已经提交
     * */
    private boolean commited = false;
    /**
     * 响应头，key为响应头的name，value为响应头值的ArrayList
     * */
    protected HashMap headers = new HashMap();

    /**
     * 响应主体的长度
     * */
    protected int contentLength = -1;

    /**
     * 响应主体的类型
     * */
    protected String contentType = null;

    /**
     * 构造函数
     * */
    public HttpResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * 配置与响应关联的请求
     * */
    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * 响应是否已经提交
     * */
    public boolean isCommitted() {
        return this.commited;
    }

    // 设置响应主体的长度
    public void setContentLength(int contentLength) {
        if (isCommitted()) return;
        this.contentLength = contentLength;
    }

    // 设置响应内容的类型
    public void setContentType(String contentType) {
        if (isCommitted()) return;
        this.contentType = contentType;
    }

    /**
     * 设置响应头
     * @param name 响应头的名字
     * @param value 响应头的值
     */
    public void setHeader(String name, String value) {
        if (name == null || name.length() == 0 || value == null) return;
        if (isCommitted()) return;
        // 将name和value设置到headers
        ArrayList<String> values = new ArrayList<>();
        values.add(value);
        synchronized (this.headers) {
            this.headers.put(name, values);
        }
        // content-length和content-type单独设置
        String match = name.toLowerCase();
        switch (match) {
            case "content-length":
                int contentLength = -1;
                try {
                    contentLength = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (contentLength >= 0) {
                    setContentLength(contentLength);
                }
                break;
            case "content-type":
                setContentType(value);
        }
    }
}