package org.futurework.catalina.startup;

public class Catalina {
    protected ClassLoader parentClassLoader = Catalina.class.getClassLoader();
    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }
}
