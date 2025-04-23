#!/bin/bash

# 获取脚本的实际路径
SCRIPT_PATH=$(readlink -f "$0")

# 获取程序的base路径
SCRIPT_DIR=$(dirname "$SCRIPT_PATH")
BASE_DIR=$(dirname "$SCRIPT_DIR")

# 判断是否需要生成log路径
LOG_DIR="${BASE_DIR}/log"
if [ ! -d "$LOG_DIR" ]; then
  # 如果目录不存在，则创建它
  mkdir -p "$LOG_DIR"
  echo "日志目录已创建:${LOG_DIR}"
else
  echo "日志目录已存在:${LOG_DIR}"
fi

#启动哨兵程序
nohup ${JAVA_HOME}/bin/java -Dconf.dir=${BASE_DIR}/conf -cp "${BASE_DIR}/lib/*" com.wangyang.SentyBuild >> ${LOG_DIR}/senty-start.out 2>${LOG_DIR}/senty-start.err &

#使用 $() 和 拼接结果字符串 的方式找到所有需要的进程id
PID=$(ps -aux | grep SentyBuild | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "哨兵程序已启动，PID: ${PID}"
    exit 0
else
    echo "哨兵程序未启动"
    exit 1
fi