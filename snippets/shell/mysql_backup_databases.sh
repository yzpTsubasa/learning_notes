#!/bin/bash
DATE=$(date +%F_%H-%M-%S)
HOST=192.168.10.154
USER=root
PASS=root
BACKUP_DIR=/tmp/db_backup
[ ! -d $BACK_DIR ] &&  mkdir -p $BACKUP_DIR
DB_LIST=$(mysql -h$HOST -u$USER -p$PASS -s -e "show databases;" 2>/dev/null | egrep -v "Database|information_schema|mysql|performance_schema|sys")

for DB in $DB_LIST; do
	BACKUP_NAME=$BACKUP_DIR/${DB}_${DATE}.sql
	if ! mysqldump -h$HOST -u$USER -p$PASS -B $DB > $BACKUP_NAME 2>/dev/null; then
		echo "Backup $BACKUP_NAME failed!"
	else
		echo "Backup $BACKUP_NAME successfully!"
	fi
done
