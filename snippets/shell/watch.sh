#!/bin/bash
WATCH_DIR=$1
# 先通过 EPEL(Extra Packages for Enterprise Linux) (yum install -y epel-release)
# 安装 inotify-tools (yum install - y inotify-tools)
inotifywait -mqr --format %f -e create $WATCH_DIR |\
while read files; do
	echo "$(date +'$F %T') $files" >> watch.log
done
