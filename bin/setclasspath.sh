#!/bin/sh

# -------------------------------------------------------------------------------------
# 设置JAVA_HOME, JRE_HOME, _RUNJAVA, _RUNJDB
# author: 高蔚霖
# -------------------------------------------------------------------------------------

# 如果JAVA_HOME和JRE_HOME都为空，则设置JAVA_HOME JRE_HOME环境变量
if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
  # MacOS的JAVA路径不同于普通的linux，所以特殊处理来获取JAVA_HOME
  if $darwin; then
    # -x表示文件存在且可执行
    if [ -x '/usr/libexec/java_home' ] ; then
      export JAVA_HOME=`/usr/libexec/java_home`
    elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    # 通过which命令来获取java可执行文件的位置 "2>/dev/null"表示过滤错误输出
    JAVA_PATH=`which java 2>/dev/null`
    # JAVA_PATH非空，则通过dirname获取其所在文件夹路径赋值给JAVA_PATH以及JRE_HOME
    if [ "x$JAVA_PATH" != "x" ]; then
      JAVA_PATH=`dirname $JAVA_PATH 2>/dev/null`
      JRE_HOME=`dirname $JAVA_PATH 2>/dev/null`
    fi
    # 如果JRE_HOME变量没有设置，且存在/usr/bin/java可执行文件，则默认JRE_HOME设置为/usr
    if [ "x$JRE_HOME" = "x" ]; then
      if [ -x /usr/bin/java ]; then
        JRE_HOME=/usr
      fi
    fi
  fi

  # 如果到这一步JAVA_HOME都JRE_HOME都为空，则直接退出
  if [ -z "$JAVA_HOME" -a -z "$JRE_HOME" ]; then
    echo "Neither the JAVA_HOME nor the JRE_HOME environment variable is defined"
    echo "At least one of these environment variable is needed to run this program"
    exit 1
  fi
fi

# 判断是否以debug模式启动tomcat，如果是debug模式则要求JAVA_HOME环境变量不能为空，否则直接退出
if [ -z "$JAVA_HOME" -a "$1" = "debug" ]; then
  echo "JAVA_HOME should point to a JDK in order to run in debug mode."
  exit 1
fi

# 如果JRE_HOME环境变量为空，则默认赋值JAVA_HOME的值
if [ -z "$JRE_HOME" ]; then
  JRE_HOME="$JAVA_HOME"
fi

# 如果debug模式启动tomcat则需要完整jdk而不仅仅jre，所以需要判断对应的可执行文件是否存在
if [ ! -x "$JAVA_HOME"/bin/java -o ! -x "$JAVA_HOME"/bin/jdb -o ! -x "$JAVA_HOME"/bin/javac ]; then
  echo "The JAVA_HOME environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  echo "NB: JAVA_HOME should point to a JDK not a JRE"
  exit 1
fi

# 设置_RUNJAVA _RUNJDB变量值，之后用于启动org.apache.catalina.startup.Bootstrap
# _RUNJAVA使用java命令启动
# _RUNJDB使用jdb命令启动
if [ -z "$_RUNJAVA" ]; then
  _RUNJAVA="$JRE_HOME"/bin/java
fi
if [ -z "$_RUNJDB" ]; then
  _RUNJDB="$JAVA_HOME"/bin/jdb
fi