#!/bin/bash
USER_LIST=$@
USER_FILE=./user.profile
# for USER in user{1..10}; do
for USER in $USER_LIST; do
	if id $USER &>/dev/null; then
		userdel -rf $USER
		sed -i "/^$USER  /d" $USER_FILE
		echo "User $USER is deleted successfully."
	else
		echo "User $USER doesn't exist!"
	fi
done
