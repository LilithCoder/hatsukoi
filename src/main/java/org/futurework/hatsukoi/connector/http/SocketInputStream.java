package org.futurework.hatsukoi.connector.http;

import org.futurework.hatsukoi.util.StringManager;

import java.io.IOException;
import java.io.InputStream;

public class SocketInputStream extends InputStream {
    /**
     * CR常量(Carriage Return)回车
     */
    private static final byte CR = (byte) '\r';
    /**
     * LF常量(Line Feed)换行
     */
    private static final byte LF = (byte) '\n';

    protected InputStream inputStream;
    /**
     * 内部缓冲区
     * */
    protected byte buffer[];
    /**
     * 缓冲区中当前位置
     * */
    protected int position;
    /**
     * http包内的StringManager
     * */
    protected static StringManager stringManager = StringManager.getManager(Constants.Package);

    public SocketInputStream(InputStream inputStream, int bufferSize) {
        this.inputStream = inputStream;
        this.buffer = new byte[bufferSize];
    }

    /**
     * 读取请求行，复制到给定的缓冲区
     * */
    public void readRequestLine(HttpRequestLine httpRequestLine) {
        // 重置下这个请求行实例
        if (httpRequestLine.methodEnd != 0) {
            httpRequestLine.recycle();
        }
        // 检查空行

    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}