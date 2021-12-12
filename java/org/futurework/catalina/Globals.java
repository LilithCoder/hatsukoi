package org.futurework.catalina;

/**
 * 适用于 Catalina 中多个包的全局常量
 *
 * @author 高蔚霖
 * */
public class Globals {

    /**
     * 包含hatsukoi产品安装路径的系统属性名称
     * java的系统属性可以认为是JVM虚拟机的环境变量
     * */
    public static final String CATALINA_HOME_PROP = "catalina.home";

    /**
     * 包含hatsukoi实例安装路径的系统属性名称
     * java的系统属性可以认为是JVM虚拟机的环境变量
     * */
    public static final String CATALINA_BASE_PROP = "catalina.base";
}
