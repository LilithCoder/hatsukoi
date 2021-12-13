package org.futurework.catalina.startup;

import java.io.File;
import java.lang.reflect.Method;
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
     * Bootstrap的守护进程对象
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
     * Catalina的守护进程对象
     * */
    private Object catalinaDaemon = null;

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

        // 通过反射加载Catalina类，并设置Catalina的父类加载器为sharedLoader
        Class<?> startupClass = catalinaLoader.loadClass("org.futurework.catalina.startup.Catalina");
        Object startupInstance = startupClass.newInstance();
        String methodName = "setParentClassLoader";
        // 方法的入参类型
        Class<?>[] paramType = new Class[1];
        paramType[0] = Class.forName("java.lang.ClassLoader");
        // 方法的入参值
        Object[] paramValues = new Object[1];
        paramValues[0] = sharedLoader;
        Method method = startupInstance.getClass().getMethod(methodName, paramType);
        method.invoke(startupInstance, paramValues);
        catalinaDaemon = startupInstance;
    }

    /**
     * 调用Catalina守护进程对象的load()，且带入命令行参数
     * */
    private void load(String[] arguments) throws Exception {
        String methodName = "load";
        Object[] param;
        Class<?>[] paramTypes;
        if (arguments == null || arguments.length == 0) {
            param = null;
            paramTypes = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method = catalinaDaemon.getClass().getMethod(methodName, paramTypes);
        method.invoke(catalinaDaemon, param);
    }

    /**
     * 调用Catalina守护进程对象的start()
     * */
    public void start() throws Exception {
        Method method = catalinaDaemon.getClass().getMethod("start", (Class [] )null);
        method.invoke(catalinaDaemon, (Object [])null);
    }

    /**
     * 调用Catalina守护进程对象的stopServer()
     * */
    public void stopServer(String[] arguments) throws Exception {
        Object param[];
        Class<?> paramTypes[];
        if (arguments==null || arguments.length==0) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method =
                catalinaDaemon.getClass().getMethod("stopServer", paramTypes);
        method.invoke(catalinaDaemon, param);
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
            // 既然已经有了Bootstrap守护进程对象
            Thread.currentThread().setContextClassLoader(daemon.catalinaLoader);
        }

        try {
            String command = "start";
            if (args.length > 0) {
                command = args[args.length - 1];
            }
            if (command.equals("start")) {
                daemon.load(args);
                daemon.start();
            } else if (command.equals("end")) {

            }
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
