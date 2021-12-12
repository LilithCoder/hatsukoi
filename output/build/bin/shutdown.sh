#!/bin/sh
# /bin/sh为执行该脚本的需要的解释器程序的路径

# -------------------------------------------------------------------------------------
# 关闭脚本
# author: 高蔚霖
# -------------------------------------------------------------------------------------

PRG="$0"
PRGDIR=`dirname "$PRG"`  # dirname命令获取脚本所在的源目录
EXECUTABLE=catalina.sh

# 判断catalina.sh文件是否存在且可执行
if [ ! -x "$PRGDIR"/"$EXECUTABLE" ]; then
  echo "Cannot find $PRGDIR/$EXECUTABLE"
  echo "The file is absent or does not have execute permission"
  echo "This file is needed to run this program"
  exit 1
fi

# 启动catalina.sh脚本并附带参数：start以及启动当前脚本时的所有参数
exec "$PRGDIR"/"$EXECUTABLE" stop "$@"