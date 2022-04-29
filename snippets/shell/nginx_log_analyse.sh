#!/bin/bash
LOG_FILE=$1
if [ "$LOG_FILE" == "" ]; then
	echo "请传入 日志文件 参数"
	exit
fi
echo "访问最多的10个IP"
awk '{a[$1]++}END{print "UV:",length(a);for(v in a)print v,a[v]}' $LOG_FILE | sort -k2 -nr | head -10
echo "------------"

echo "时间段访问最多的IP"
awk '$4>="[28/Apr/2022:18:40:53" && $4<="[28/Apr/2022:20:40:53"{a[$1]++}END{for(v in a)print v,a[v]}' $LOG_FILE | sort -k2 -nr | head -10
echo "------------"

echo "访问最多的10个页面"
awk '{a[$7]++}END{print "PV:",length(a);for(v in a){if(a[v]>10) print v,a[v]}}' $LOG_FILE | sort -k2 -nr
echo "------------"


echo "访问页面状态码数量"
awk '{a[$7" "$9]++}END{for(v in a){if(a[v]>5) print v,a[v]}}' $LOG_FILE | sort -k3 -nr
echo "------------"
