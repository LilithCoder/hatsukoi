package org.futurework.hatsukoi.util;

import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * 字符串管理器
 * 一个国际化/语言化的帮助类
 * tomcat会将properties文件划分到不同的包中
 * 每个字符串管理器实例都会读取某个包下的制定properties文件，以帮助在其中查找错误消息
 * 同一个包下的许多类会使用同一个StringManager实例
 * @author 高蔚霖
 * @since 2021/10/24
 * */
public class StringManager {
    /**
     * 存放StringManager的哈希表，以包名为key
     * */
    private static Hashtable managers = new Hashtable();

    /**
     * StringManager的资源bundle
     * */
    private ResourceBundle bundle;

    /**
     * 根据给定的包建造一个新的StringManager
     * StringManager是一个单例类，且只有这一个私有的构造函数，所以就不能在外部通过new来实例化它了，只能通过公共静态方法getManager()来获得其实例
     * */
    private StringManager(String packageName) {
        String bundleName = packageName + ".LocalStrings";
        this.bundle = ResourceBundle.getBundle(bundleName);
    }

    /**
     * 根据指定包名作为key获取StringManager实例，每个StringManager实例都会以报名作为key，存在一个哈希表中
     * 不存在给定包名的StringManager，那就创建一个
     * */
    public synchronized static StringManager getManager(String packageName) {
        StringManager stringManager = (StringManager)managers.get(packageName);
        if (stringManager == null) {
            stringManager = new StringManager(packageName);
            managers.put(packageName, stringManager);
        }
        return stringManager;
    }

    /**
     * 从底层资源包获取字符串
     * @param key
     */
    public String getString(String key) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        String str = null;
        try {
            str = bundle.getString(key);
        } catch (MissingResourceException e) {
            str = "Cannot find message associated with key '" + key + "'";
        }
        return str;
    }
}
