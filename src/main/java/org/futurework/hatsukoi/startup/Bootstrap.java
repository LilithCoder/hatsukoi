package org.futurework.hatsukoi.startup;

import org.futurework.hatsukoi.connector.http.HttpConnector;

/**
 * hatsukoi启动类
 * tomcat服务器启动的地方
 * @author 高蔚霖
 * @since 2021/10/24
 * */
public class Bootstrap {
    public static void main(String[] args) {
        HttpConnector httpConnector = new HttpConnector();
        httpConnector.start();
    }
}
