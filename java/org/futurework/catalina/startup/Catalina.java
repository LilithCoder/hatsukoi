package org.futurework.catalina.startup;

import org.apache.commons.digester.Digester;

public class Catalina {
    protected ClassLoader parentClassLoader = Catalina.class.getClassLoader();
    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    /**
     *  创建digester实例，添加digester的处理规则(用来处理conf/server.xml文件)
     *  digester具体分析可以查看: https://www.jianshu.com/p/e153b2bc7811
     * */
    protected void parseServerXml() {
        long t1 = System.currentTimeMillis();
        Digester digester = new Digester();
        digester.setValidating(true);

        // <Server/>
        // 遇到Server标签根据类名进行反射实例化对象()，将对象压进对象栈
        digester.addObjectCreate("Server", "org.futurework.catalina.core.StandardServer", "className");
        // 为Server对象设置属性
        digester.addSetProperties("Server");
        // 遇到<Server/>时，Digester对象栈栈顶是生成的StandardServer实例，次栈顶对象是Catalina实例。开始解析前Catalina实例自身先入栈
        // SetNextRule在Catalina实例上调用setServer方法，参数是栈顶的StandardServer实例
        digester.addSetNext("Server", "setServer", "org.futurework.catalina.Server");

        // <Listener/>
        digester.addObjectCreate("Server/Listener", null, "className");
        digester.addSetProperties("Server/Listener");
        // SetNextRule在次栈顶的对象（Server）上调用方法(addLifecycleListener)，参数是栈顶的对象（Listener）
        digester.addSetNext("Server/Listener", "addLifecycleListener", "org.futurework.catalina.LifecycleListener");

        // <Service/>
        // 创建StandardService实例，实例被添加到了SnadardServer上。
        digester.addObjectCreate("Server/Service", "org.futurework.catalina.core.StandardService", "className");
        digester.addSetProperties("Server/Service");
        digester.addSetNext("Server/Service", "addService", "org.futurework.catalina.Service");


    }
    public void load() throws Exception {
        System.out.println("\033[42;37;1m" + "   [LOG]   " + "\033[0m" + " " + "Catalina instance has been loaded.");

    }
    public void start() {
        System.out.println("\033[42;37;1m" + "   [LOG]   " + "\033[0m" + " " + "Catalina instance has been started.");

    }
    public void stopServer() {
        System.out.println("\033[42;37;1m" + "   [LOG]   " + "\033[0m" + " " + "Catalina instance has been stopped.");

    }
}
