#!/bin/bash

LOG_DIR=/usr/local/nginx/logs
LOG_FILES="access.log"

ABNORMAL_IP=$(tail -n5000 $LOG_DIR/$LOG_FILES | grep -P "\[$(date +%d/%b/%Y:%H:%M):\\d{2}" | awk '{a[$1]++}END{for(i in a)if(a[i]>10)print i}')
for IP in $ABNORMAL_IP; do
	if [ $(iptables -vnL | grep -c "$IP") -eq 0 ]; then
		iptables -I INPUT -s $IP -j DROP
		# 恢复访问权限
		# iptables -D INPUT -s $IP -j DROP
		echo "$(date +'%F_%T') $IP" >> /tmp/drop_ip.log
	fi
done
