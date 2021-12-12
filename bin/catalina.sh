#!/bin/sh

# -------------------------------------------------------------------------------------
# 设置各种环境变量、最后启动Bootstrap
# author: 高蔚霖
# -------------------------------------------------------------------------------------

# darwin: macOS
darwin=false
hpux=false
# 通过uname命令来判断tomcat所处的操作系统环境
case "$(uname)" in
Darwin*) darwin=true ;;
HP-UX*) hpux=true;;
esac

# --------------------------------------------------
# 设置CATALINA相关环境变量
# --------------------------------------------------

PRG="$0"
PRGDIR=$(dirname "$PRG") # 获取本脚本的目录地址

# 判断CATALINA_HOME和CATALINA_BASE是否存在，不存在则赋值为catalina.sh的上一级目录
# 如果存在标准输出则输出到/dev/null避免在控制台打印信息，cd不存在的路径的时候会有输出
# 然后通过pwd获取目录path
[ -z "$CATALINA_HOME" ] && CATALINA_HOME=$(
  cd "$PRGDIR/.." >/dev/null
  pwd
)
[ -z "$CATALINA_BASE" ] && CATALINA_BASE="$CATALINA_HOME"

# 判断CATALINA_HOME和CATALINA_BASE中是否包含冒号,如果存在冒号则退出
# 原因：JAVA类路径中也可能存在冒号用作分隔符，JAVA没有对冒号转义的机制
case $CATALINA_HOME in
*:*)
  echo "Using CATALINA_HOME:   $CATALINA_HOME"
  echo "Unable to start as CATALINA_HOME contains a colon (:) character"
  exit 1
  ;;
esac
case $CATALINA_BASE in
*:*)
  echo "Using CATALINA_BASE:   $CATALINA_BASE"
  echo "Unable to start as CATALINA_BASE contains a colon (:) character"
  exit 1
  ;;
esac

# 为CATALINA_OUT设置默认值
if [ -z "$CATALINA_OUT" ]; then
  CATALINA_OUT="$CATALINA_BASE"/logs/catalina.out
fi

# 为CATALINA_TMPDIR设置默认值
if [ -z "$CATALINA_TMPDIR" ]; then
  CATALINA_TMPDIR="$CATALINA_BASE"/temp
fi

# --------------------------------------------------
# 设置CLASSPATH、JAVA_HOME、JRE_HOME等环境变量
# --------------------------------------------------

# setenv.sh为tomcat设置CLASSPATH变量
# 如果在CATALINA_BASE/bin以及CATALINA_HOME/bin下存在setenv.sh脚本则执行脚本
CLASSPATH=

if [ -r "$CATALINA_HOME/bin/setenv.sh" ]; then
  . "$CATALINA_HOME/bin/setenv.sh"
elif [ -r "$CATALINA_BASE/bin/setenv.sh" ]; then
  . "$CATALINA_BASE/bin/setenv.sh"
fi

# CLASSPATH追加bootstrap.jar配置
if [ ! -z "$CLASSPATH" ]; then
  CLASSPATH="$CLASSPATH"
fi
CLASSPATH="$CLASSPATH""$CATALINA_HOME"/bin/bootstrap.jar

# 如果CATALINA_BASE/bin/hatsukoi-juli.jar存在且可读则追加到CLASSPATH当中，否则追加CATALINA_HOME/bin/tomcat-juli.jar
if [ -r "$CATALINA_BASE/bin/hatsukoi-juli.jar" ]; then
  CLASSPATH=$CLASSPATH:$CATALINA_BASE/bin/hatsukoi-juli.jar
else
  CLASSPATH=$CLASSPATH:$CATALINA_HOME/bin/hatsukoi-juli.jar
fi

# 设置以及查验JAVA_HOME以及JRE_HOME环境变量
# 执行CATALINA_HOME/bin目录下的setclasspath.sh脚本
if [ -r "$CATALINA_HOME"/bin/setclasspath.sh ]; then
  . "$CATALINA_HOME"/bin/setclasspath.sh
else
  echo "Cannot find $CATALINA_HOME/bin/setclasspath.sh"
  echo "This file is needed to run this program"
  exit 1
fi

# --------------------------------------------------
# 设置各种环境变量
# --------------------------------------------------

# 为JSSE_OPTS设置默认值
if [ -z "$JSSE_OPTS" ] ; then
  JSSE_OPTS="-Djdk.tls.ephemeralDHKeySize=2048"
fi
# 为JAVA_OPTS追加JSSE_OPTS
JAVA_OPTS="$JAVA_OPTS $JSSE_OPTS"
JAVA_OPTS="$JAVA_OPTS -Djava.protocol.handler.pkgs=org.apache.catalina.webresources"

if [ -z "$LOGGING_CONFIG" ]; then
  if [ -r "$CATALINA_BASE"/conf/logging.properties ]; then
    LOGGING_CONFIG="-Djava.util.logging.config.file=$CATALINA_BASE/conf/logging.properties"
  else
    LOGGING_CONFIG="-Dnop"
  fi
fi

if [ -z "$LOGGING_MANAGER" ]; then
  LOGGING_MANAGER="-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager"
fi

# 设置默认的umask值，umask表示新建文件的权限需要削减的值
if [ -z "$UMASK" ]; then
    UMASK="0027"
fi
# 执行umask使之生效
umask $UMASK

if [ -z "$USE_NOHUP" ]; then
    if $hpux; then
        USE_NOHUP="true"
    else
        USE_NOHUP="false"
    fi
fi
unset _NOHUP
if [ "$USE_NOHUP" = "true" ]; then
    _NOHUP=nohup
fi

# 通过tty命令来判断是否存在虚拟终端、虚拟控制台
have_tty=0
if [ "$(tty)" != "not a tty" ]; then
  have_tty=1
fi

# 如果存在终端或是串口则向外打印一些提示信息
if [ $have_tty -eq 1 ]; then
  echo "Using CATALINA_BASE:   $CATALINA_BASE"
  echo "Using CATALINA_HOME:   $CATALINA_HOME"
  echo "Using CATALINA_TMPDIR: $CATALINA_TMPDIR"
  if [ "$1" = "debug" ]; then
    echo "Using JAVA_HOME:       $JAVA_HOME"
  else
    echo "Using JRE_HOME:        $JRE_HOME"
  fi
  echo "Using CLASSPATH:       $CLASSPATH"
  if [ ! -z "$CATALINA_PID" ]; then
    echo "Using CATALINA_PID:    $CATALINA_PID"
  fi
fi

# --------------------------------------------------
# 最终：命令执行
# --------------------------------------------------

# start模式启动tomcat，将输出log输出到CATALINA_OUT
if [ "$1" = "start" ]; then
  # 判断CATALINA_PID配置的文件是否存在
  if [ ! -z "$CATALINA_PID" ]; then
    # 判断CATALINA_PID配置的文件是否存在
    if [ -f "$CATALINA_PID" ]; then
      # 判断CATALINA_PID配置的文件是否存在且非空
      if [ -s "$CATALINA_PID" ]; then
        echo "Existing PID file found during start."
        # 判断CATALINA_PID文件是否可读
        if [ -r "$CATALINA_PID" ]; then
          # 读取PID并判断该PID是否有进程使用
          PID=$(cat "$CATALINA_PID")
          # File descriptor 1 is the standard output (stdout)
          # File descriptor 2 is the standard error (stderr)
          # 2>&1的意思是 Redirect standard error to standard output
          # ps -p $PID表示读取PID ps表示process status  -p通过pid指定了某个进程
          ps -p $PID >/dev/null 2>&1
          # $? = was last command successful. Answer is 0 which means 'yes'
          if [ $? -eq 0 ]; then
            # tomcat还在这个进程上运行
            echo "Tomcat appears to still be running with PID $PID. Start aborted."
            echo "If the following process is not a Tomcat process, remove the PID file and try again:"
            ps -f -p $PID
            exit 1
          else
            echo "Removing/clearing stale PID file."
            # 删除CATALINA_PID文件
            rm -f "$CATALINA_PID" >/dev/null 2>&1
            if [ $? != 0 ]; then
              if [ -w "$CATALINA_PID" ]; then
                # 如果CATALINA_PID文件可写则置空
                cat /dev/null >"$CATALINA_PID"
              else
                # CATALINA_PID存在且非空 删除失败 且没有写入权限 否则结束运行
                echo "Unable to remove or clear stale PID file. Start aborted."
                exit 1
              fi
            fi
          fi
        else
          #CATALINA_PID存在非空且不可读 否则结束运行
          echo "Unable to read PID file. Start aborted."
          exit 1
        fi
      else
        #如果CATALINA_PID存在且为空 则尝试删除 如果删除失败 判断是否有写入权限 否则结束运行
        rm -f "$CATALINA_PID" >/dev/null 2>&1
        if [ $? != 0 ]; then
          if [ ! -w "$CATALINA_PID" ]; then
            echo "Unable to remove or write to empty PID file. Start aborted."
            exit 1
          fi
        fi
      fi
    fi
  fi
  # shift用于移动$指向的参数位置 $1本来指向第一个参数 shift一次 $1变为指向第二个参数
  shift
  touch "$CATALINA_OUT"
  echo $_RUNJAVA
  eval $_NOHUP "\"$_RUNJAVA\"" "\"$LOGGING_CONFIG\"" $LOGGING_MANAGER $JAVA_OPTS $CATALINA_OPTS \
      -classpath "\"$CLASSPATH\"" \
      -Dcatalina.base="\"$CATALINA_BASE\"" \
      -Dcatalina.home="\"$CATALINA_HOME\"" \
      -Djava.io.tmpdir="\"$CATALINA_TMPDIR\"" \
      org.futurework.catalina.startup.Bootstrap "$@" start \

  # 写入PID到PIDFILE
  # $! contains the process ID of the most recently executed background pipeline.
  if [ ! -z "$CATALINA_PID" ]; then
    echo $! >"$CATALINA_PID"
  fi

  echo "Tomcat started."

#elif [ "$1" = "stop" ]; then
#  # TODO
fi
