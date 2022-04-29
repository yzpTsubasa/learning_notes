#!/bin/bash

LOG_DIR=/usr/local/nginx/logs
YESTERDAY_TIME=$(date -d "yesterday" +%F)
LOG_MONTH_DIR=$LOG_DIR/$(date +"%Y-%m")
LOG_FILES="access.log"
[ ! -d $LOG_MONTH_DIR ] && mkdir -p $LOG_MONTH_DIR

for LOG_FILE in $LOG_FILES; do
	mv $LOG_DIR/$LOG_FILE $LOG_MONTH_DIR/${LOG_FILE}_${YESTERDAY_TIME}
done

kill -USR1 $(cat /usr/local/nginx/logs/nginx.pid)

