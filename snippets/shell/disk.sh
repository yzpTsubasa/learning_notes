#!/bin/bash
HOST_INFO=host.profile
for IP in $(awk '/^[^#]/{print $1}'  $HOST_INFO); do
	USER=$(awk -v ip=$IP 'ip==$1{print $2}' $HOST_INFO)
	PORT=$(awk -v ip=$IP 'ip==$1{print $3}' $HOST_INFO)
	TMP_FILE=/tmp/disk.tmp
	ssh -p $PORT $USER@$IP 'df -h' > $TMP_FILE
	# USE_RATES=$(awk 'BEGIN{OFS="="}/^\/dev/{print $NF,int($5)}' $TMP_FILE)
	# USE_RATES=$(awk -v OFS="=" '/^\/dev/{print $NF,int($5)}' $TMP_FILE)
	USE_RATES=$(awk '/^\/dev/{print $NF"="int($5)}' $TMP_FILE)
	for USE_RATE in $USE_RATES; do
		PART_NAME=${USE_RATE%=*}
		USE_RATE=${USE_RATE#*=}
		if [ $USE_RATE -ge 90 ]; then
			echo "ERROR: ${IP} $PART_NAME Partition usage $USE_RATE%!"
		elif [ $USE_RATE -ge 30 ]; then
			echo "WARNING: ${IP} $PART_NAME Partition usage $USE_RATE%!"
		else
			echo "SUCCESS: ${IP} $PART_NAME Partition usage $USE_RATE%!"
		fi
	done	
done
