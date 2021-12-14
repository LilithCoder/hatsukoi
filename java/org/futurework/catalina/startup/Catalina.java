package org.futurework.catalina.startup;

import org.apache.commons.digester.Digester;

public class Catalina {
    protected ClassLoader parentClassLoader = Catalina.class.getClassLoader();
    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }
    public void load() throws Exception {
        long t1 = System.nanoTime();
        Digester digester = new Digester();
        digester.setValidating(true);
        System.out.println("\033[42;37;1m" + "   [LOG]   " + "\033[0m" + " " + "Catalina instance has been loaded.");

    }
    public void start() {
        System.out.println("\033[42;37;1m" + "   [LOG]   " + "\033[0m" + " " + "Catalina instance has been started.");

    }
    public void stopServer() {
        System.out.println("\033[42;37;1m" + "   [LOG]   " + "\033[0m" + " " + "Catalina instance has been stopped.");

    }
}
