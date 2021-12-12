package org.futurework.catalina.startup;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Catalina的引导加载器
 * 构造一个类加载器用于加载Catalina内部的类
 * (通过查找catalina.home的server目录下的所有jar包，将Catalina内部类远离系统类路径，对应用层面的类不可见）
 * 接着开始容器的执行
 *
 * @author 高蔚霖
 * */
public class Bootstrap {

    /**
     * 守护进程对象，引用一个Bootstrap实例
     * */
    private static Bootstrap daemon = null;

    private static HashMap<String, ArrayList<String>> classLoaderMap = new HashMap<>();

    static {
        // 获取命令行参数设置的Java系统属性(“-D”选项)
        // https://blog.csdn.net/u010675669/article/details/86150754
        String home = System.getProperty("catalina.home");
        String base = System.getProperty("catalina.base");
        classLoaderMap.put("common.loader", new ArrayList<>(Arrays.asList(home + "/lib", home + "/lib/*.jar", base + "/lib", base + "/lib/*.jar")));
        classLoaderMap.put("server.loader", new ArrayList<>());
        classLoaderMap.put("shared.loader", new ArrayList<>());
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


    /**
     * 初始化类加载器
     *
     * 创建三个类加载器
     * commonLoader: 父加载器是系统类加载器，common.loader为键的值表示的路径会被添加到commonLoader的查找路径中
     * catalinaLoader: 父加载器是commonLoader，common.loader为键的值表示的路径会被添加到catalinaLoader的查找路径中
     * sharedLoader: 父加载器是commonLoader，common.loader为键的值表示的路径会被添加到sharedLoader的查找路径中
     *
     * ${catalina.base}/lib目录下未打包的类和资源、${catalina.base}/lib目录下的jar
     * ${catalina.home}/lib目录下未打包的类和资源以及${catalina.home}/lib目录下的jar
     * 都会被添加到commonLoader的搜索路径中
     *
     * */
    private void initClassLoaders() {
        try {
            commonLoader = createClassLoader("common", null);
            if (commonLoader == null) {
                // 获取系统类加载器
                commonLoader = this.getClass().getClassLoader();
            }
            catalinaLoader = createClassLoader("server", commonLoader);
            sharedLoader = createClassLoader("shared", commonLoader);
        } catch (Throwable t) {
            // TODO:
            System.exit(1);
        }
    }

    /**
     * 创建类加载器
     *
     * 根据common.loader, shared.loader, server.loader 对应的值构造成Repository 列表
     * 再将Repository 列表传入ClassLoaderFactory.createClassLoader 方法
     * ClassLoaderFactory.createClassLoader 返回的是 URLClassLoader，而Repository列表就是这个URLClassLoader可以加载的类的路径
     *
     * createClassLoader 方法里如果没有值的话，就返回传入的 parent ClassLoader
     *
     * */
    private ClassLoader createClassLoader(String name, ClassLoader parent) throws Exception {
        ArrayList<String> repoPaths = classLoaderMap.get(name + ".loader");
        if (repoPaths.size() == 0) {
            return parent;
        }
        List<ClassLoaderFactory.Repository> repos = new ArrayList<>();
        for (String repoPath: repoPaths) {
            // 检查是否为远端jar
            try {
                URL url = new URL(repoPath);
                repos.add(new ClassLoaderFactory.Repository(repoPath, ClassLoaderFactory.RepositoryType.URL));
            } catch (MalformedURLException e) {
                // DO NOTHING
            }
            // 本地资源
            if (repoPath.endsWith("*.jar")) {
                repoPath = repoPath.substring(0, repoPath.length() - "*.jar".length());
                repos.add(new ClassLoaderFactory.Repository(repoPath, ClassLoaderFactory.RepositoryType.GLOB));
            } else if (repoPath.endsWith(".jar")) {
                repos.add(new ClassLoaderFactory.Repository(repoPath, ClassLoaderFactory.RepositoryType.JAR));
            } else {
                repos.add(new ClassLoaderFactory.Repository(repoPath, ClassLoaderFactory.RepositoryType.DIR));
            }
        }
        return ClassLoaderFactory.createClassLoader(repos, parent);
    }

    /**
     * 初始化守护进程
     *
     * 调用initClassLoaders方法初始化类加载器
     * 将catalinaLoader设置为自己的线程上下文类加载器
     * 利用catalinaLoader加载Catalina类并实例化对象
     *
     * @throws Exception 初始化错误
     * */
    public void init() throws Exception {
        // 初始化类加载器
        initClassLoaders();
        Thread.currentThread().setContextClassLoader(catalinaLoader);
    }

    /**
     * 启动类的main方法，hatsukoi的入口
     * */
    public static void main(String[] args) {
        if (daemon == null) {
            Bootstrap bootstrap = new Bootstrap();
            try {
                // TODO: init
                bootstrap.init();
            } catch (Throwable t) {
                // TODO:  handle throw
                t.printStackTrace();
                return;
            }
            daemon = bootstrap;
        } else {
            // TODO
        }
    }
}
