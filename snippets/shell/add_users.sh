#!/bin/bash
USER_FILE=./user.profile
for USER in user{1..10}; do
	if ! id $USER &>/dev/null; then
		PASS=$(echo $RANDOM | md5sum | cut -c 9-24)
		useradd $USER
		echo $PASS | passwd --stdin $USER
		echo "$USER  $PASS" >> $USER_FILE
		echo "User $USER is created sucessfully."
	else
		echo "User $USER already exists!"
	fi
done
