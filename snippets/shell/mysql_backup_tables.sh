#!/bin/bash
DATE=$(date +%F_%H-%M-%S)
HOST=192.168.10.154
USER=root
PASS=root
BACKUP_DIR=/tmp/db_backup
DB_LIST=$(mysql -h$HOST -u$USER -p$PASS -s -e "show databases;" 2>/dev/null | egrep -v "Database|infomation_schema|mysql|performance_schema|sys")

for DB in $DB_LIST; do
	BACKUP_DB_DIR=$BACKUP_DIR/${DB}_${DATE}
	# 目录不存在则创建		
	[ ! -d $BACKUP_DB_DIR ] && mkdir -p $BACKUP_DB_DIR &>/dev/null
	TABLE_LIST=$(mysql -h$HOST -u$USER -p$PASS -s -e "use $DB;show tables;" 2>/dev/null)
	for TABLE in $TABLE_LIST; do
		BACKUP_NAME=$BACKUP_DB_DIR/${TABLE}.sql
		if ! mysqldump -h$HOST -u$USER -p$PASS $DB $TABLE > $BACKUP_NAME 2>/dev/null; then
			echo "Backup $BACKUP_NAME failed!"
		else
			echo "Backup $BACKUP_NAME successfully!"
		fi
	done
done

