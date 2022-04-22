#!/bin/bash
USER_LIST=$@
USER_FILE=./user.profile
# for USER in user{1..10}; do
for USER in $USER_LIST; do
	if ! id $USER &>/dev/null; then
		PASS=$(echo $RANDOM | md5sum | cut -c 9-16)
		useradd $USER
		echo $PASS | passwd --stdin $USER &>/dev/null
		echo "$USER  $PASS" >> $USER_FILE
		echo "User $USER is created sucessfully."
	else
		echo "User $USER already exists!"
	fi
done
