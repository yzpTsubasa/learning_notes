#!/bin/bash
USER_FILE=./user.profile
for USER in user{1..10}; do
	if id $USER &>/dev/null; then
		userdel -rf $USER
		sed -i "/^$USER  /d" $USER_FILE
		echo "User $USER is deleted sucessfully."
	else
		echo "User $USER doesn't exist!"
	fi
done
