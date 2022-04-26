#!/bin/bash
URL_LIST="www.baidu.com www.google.com"
for URL in $URL_LIST; do
	FAIL_COUNT=0
	RETRY_TIMES=3
	for ((i=1;i<=$RETRY_TIMES;i++)); do
		HTTP_CODE=$(curl -o /dev/null --connect-timeout 3 -s -w"%{http_code}" $URL)
		if [ $HTTP_CODE -eq 200 ]; then
			# echo "OK"
			break
		else
			echo "Retry $URL ($i / $RETRY_TIMES)"
			let FAIL_COUNT++
		fi
	done
	if [ $FAIL_COUNT -eq $RETRY_TIMES ]; then
		echo "WARNING: Failed to access $URL!"
	else
		echo "SUCCESS: Access $URL successfully!"
	fi
done
