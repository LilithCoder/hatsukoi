package org.futurework.hatsukoi.connector.http;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * http请求
 * @author 高蔚霖
 * @since 2021/10/24
 * */
public class HttpRequest implements HttpServletRequest {
    private InputStream inputStream;
    public HttpRequest(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
