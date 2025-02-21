#!/bin/bash
tmp=""

#使用 $() 和 拼接结果字符串 的方式找到所有需要的进程id
tmp=$(ps -aux | grep SentyBuild | grep -v grep | awk '{print $2}')

#将进程id拆成数组 tmp的是在一行内，要写成换行符号裂成数组
readarray -t ADDR <<< "$(echo "$tmp" | tr ' ' '\n')"

#遍历数组kill进程
for ids in "${ADDR[@]}"; do
    kill -9 "$ids"
    echo "kill by $ids"
done

echo "哨兵程序已停止！！！"
exit 0