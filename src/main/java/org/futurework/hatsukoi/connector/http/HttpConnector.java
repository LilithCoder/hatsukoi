package org.futurework.hatsukoi.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * http连接器
 * HttpConnector类实现了Runnable接口，当启动服务器程序时，HttpConnector实例会被创建，且在新线程上运行它的run方法
 * @author 高蔚霖
 * @since 2021/10/24
 * */
public class HttpConnector implements Runnable{

    boolean shutdown;

    @Override
    public void run() {
        // 创建服务器套接字，接收请求队列的最大数为1(backlog)，侦听本地机器的绑定地址127.0.0.1接收到的连接请求
        ServerSocket serverSocket = null;
        int portNumber = 8080;
        try {
            serverSocket = new ServerSocket(portNumber, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            // 如果serverSocket创建失败，直接停止当前运行的JVM
            System.exit(1);
        }

        // 服务器运行中
        while (!shutdown) {
            Socket socket = null;
            // 服务器套接字等待网络请求，监听到网络请求且形成连接后返回一个Socket实例
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            // 创建一个HttpProcessor并传入套接字，使套接字和该处理器相关联
            HttpProcessor httpProcessor = new HttpProcessor(this);
            httpProcessor.process(socket);
        }
    }
    // 创建并开始连接器的线程
    public void start() {
        Thread thread = new Thread();
        thread.start();
    }
}
