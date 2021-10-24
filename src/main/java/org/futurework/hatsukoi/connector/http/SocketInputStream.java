package org.futurework.hatsukoi.connector.http;

import java.io.IOException;
import java.io.InputStream;

public class SocketInputStream extends InputStream {

    protected InputStream inputStream;
    // 内部缓冲区
    protected byte buffer[];

    public SocketInputStream(InputStream inputStream, int bufferSize) {
        this.inputStream = inputStream;
        this.buffer = new byte[bufferSize];
    }

    @Override
    public int read() throws IOException {
        return 0;
    }
}