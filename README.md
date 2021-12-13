# hatsukoi
## 目录
- [hatsukoi是什么](#hatsukoi是什么)
- [如何启动hatsukoi](#如何启动hatsukoi)
- [hatsukoi的启动过程](#hatsukoi的启动过程)
## hatsukoi是什么
hatsukoi是一个类tomcat的web服务器+servlet容器
## 如何启动hatsukoi
### 预先步骤
在编译和启动hatsukoi前有几个预备步骤和配置文件需要知晓
1. 安装JDK (>=8)：设置环境变量JAVA_HOME为安装JDK的路径 [Oracle官网🔗](https://www.oracle.com/java/technologies/downloads/)
2. 安装Ant (>=1.9.5)：设置环境变量ANT_HOME为安装Ant的路径，并将ANT_HOME/bin加入环境变量PATH，这样ant命令行脚本就可以用来执行编译了
3. build.properties文件（可暂时忽略）：设置属性base.path为hatsukoi编译需要的依赖存放的路径
4. build.xml文件：Ant构建文件，定义了hatsukoi这个project的构建过程，默认任务为deploy，其中包含了很多target用来定义任务并且根据依赖顺序执行
    - 预编译（build-prepare）：在hatsukoi创建目录output/build和output/classes，build用来放置编译打包后的二进制文件，classes放置class文件
    - 编译（compile）：将java文件夹下所有java文件编译，结果class文件放到output/class
    - 打包（package）：将output/class内的class文件打包，放到output/build/lib或者/bin中，类加载器会从这里的jar加载类
    - 部署（deploy）：将bin/下的脚本复制到output/build/bin，并修改文件执行权限

### 编译构建项目
编译后，可执行的二进制文件被放置在了output目录下，根目录下其余文件为源码
```bash
$ cd ${hatsukoi.home}
$ ant
```
### 启动hatsukoi
通过startup.sh启动hatsukoi，启动后，hatsukoi就可以在[http://localhost:8080](http://localhost:8080/)访问了
```bash
$ cd ${hatsukoi.home}/output/build/bin
$ startup.sh start
```
## hatsukoi的启动过程
### 启动脚本
1. bin/startup.sh：

    判断catalina.sh文件是否存在且可执行，并启动catalina.sh脚本并附带参数
1. bin/catalina.sh：

    设置CATALINA相关环境变量（CATALINA_HOME、CATALINA_BASE、CATALINA_OUT），设置CLASSPATH、JAVA_HOME、JRE_HOME、_RUNJAVA、_RUNJDB等环境变量，打印一些提示信息，最后start模式启动hatsukoi的启动类org.futurework.catalina.startup.Bootstrap，执行main方法

    由于CLASSPATH追加了bootstrap.jar配置，启动指令可以从类路径中找到Bootstrap启动类，开启服务，且将输出log输出到CATALINA_OUT
### Bootstrap启动类
main方法主入口：
1. 初始化守护进程daemon为新建的Bootstrap对象
2. 调用initClassLoaders方法初始化三个类加载器（Common类加载器, Common类加载器, Shared类加载器），catalinaLoader 和 sharedLoader 的 parentClassLoader 是 commonLoader，且指定搜索路径添加在其中，设置当前的线程的上下文类加载器为catalinaLoader
3. 类加载器的创建方法：根据解析common.loader得到路径的列表，然后构造成Repository 列表，再将Repository 列表传入ClassLoaderFactory.createClassLoader 方法，ClassLoaderFactory.createClassLoader 返回的是 URLClassLoader，而Repository 列表就是这个URLClassLoader 可以加载的类的路径
4. 初始化完三个ClassLoader对象后，init() 方法就使用 catalinaClassLoader 加载了org.apache.catalina.startup.Catalina 类(Catalina.jar就在Catalina类加载器${catalina.home}/lib下)，并创建了一个对象，然后通过反射调用这个对象的 setParentClassLoader 方法，传入的参数是 sharedClassLoader，通过反射加载Catalina类，并设置Catalina的父类加载器为sharedLoader。最后把这个 Catalina 对象复制给 catalinaDaemon 属性
5. 处理命令行参数，如果是start，调用catalina的load和start方法；如果是stopServer，调用catalina的stopServer方法

<!-- ![](./uml_diagram1.svg) -->