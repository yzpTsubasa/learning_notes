#!/bin/bash
# 添加到定时任务中
# crontab -e
# */1 * * * * /xxxx/xxx/mysql_master_slave.sh >/dev/null 2>&1 &
HOST=localhost
USER=root
PASSWD=root
IO_SQL_STATUS=$(mysql -h$HOST -u$USER -p$PASSWD -e 'show slave status\G' 2>/dev/null | awk '/Slave_.*_Running:/{print $1$2}')
for i in $IO_SQL_STATUS; do
	THREAD_STATUS_NAME=${i%:*}
	THREAD_STATUS=${i#*:}
	if [ "$THREAD_STATUS" != "Yes" ]; then
		# echo "Error: MySQL Master-Slave $THREAD_STATUTS_NAME status is $THREAD_STATUS!"
		echo "Error: MySQL Master-Slave $THREAD_STATUTS_NAME status is $THREAD_STATUS!" | mail -s "Master-Slave Status“ abc@def.com
	fi
done
