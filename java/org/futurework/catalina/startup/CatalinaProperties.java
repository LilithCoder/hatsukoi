package org.futurework.catalina.startup;

import java.io.File;
import java.util.Properties;

/**
 * 读取启动Catalina配置的工具类
 * */
public class CatalinaProperties {

    /**
     * 已加载的properties
     * */
    private static Properties properties = null;

    static {
        loadProperties();
    }

    /**
     * 通过.properties配置文件加载properties
     * */
    private static void loadProperties() {
        try {
            File home = new File(Bootstrap)
        }
    }

}
