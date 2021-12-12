package org.futurework.catalina.startup;

import java.io.File;

/**
 * Catalina的引导加载器
 * 构造一个类加载器用于加载Catalina内部的类
 * (通过查找catalina.home的server目录下的所有jar包，将Catalina内部类保持在系统类路径下，对应用层面的类不可见）
 * 接着开始容器的执行
 *
 * @author 高蔚霖
 * */
public class Bootstrap {

    /**
     * 守护进程对象
     * */
    private static Bootstrap daemon = null;

//    private static final File catalinaBaseFile;


    static {
        String base = System.getProperty()
    }


    // ----------------- 成员变量 -----------------


    /**
     * Common类加载器：
     * 以System为父类加载器，是位于Tomcat应用服务器顶层的公用类加载器
     * 负责加载Tomcat和Web应用都复用的类
     * */
    ClassLoader commonLoader = null;

    /**
     * Catalina类加载器：
     * 负责加载Tomcat专用的类，而这些被加载的类在Web应用中将不可见
     * 以Common为父加载器，是用于加载Tomcat应用服务器的类加载器
     * */
    ClassLoader catalinaLoader = null;

    /**
     * Shared类加载器：
     * 负责加载Tomcat下所有的Web应用程序都复用的类
     * 而这些被加载的类在Tomcat中将不可见，以Common为父加载器，是所有web应用的父加载器
     * */
    ClassLoader sharedLoader = null;


    // ----------------- 私有方法 -----------------


    /**
     * 初始化daemon
     * @throws Exception 初始化错误
     * */
    private void init() throws Exception {
        // 1.初始化类加载器
        initClassLoaders();
    }


    /**
     * 初始化类加载器
     * */
    private void initClassLoaders() {
//        try {
//            commonLoader =
//        }
    }


    /**
     * 类加载器的创建
     * */
    private ClassLoader createClassLoader(String name, ClassLoader parent) throws Exception {
//        String value =
    }


    public static void main(String[] args) {
        if (daemon == null) {
            Bootstrap bootstrap = new Bootstrap();

        }
    }

    public static String getCatalinaBase() {

    }
}
