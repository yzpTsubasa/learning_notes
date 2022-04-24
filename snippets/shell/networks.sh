#!/bin/bash

NIC=$1
if ! cat /proc/net/dev | grep "${NIC}:" &>/dev/null; then
	echo "Interface ${NIC} not found!"
	exit
fi
# echo -e " In ------ Out"
OLD_IN=$(awk '$0~"'$NIC'"{print $2}' /proc/net/dev)
OLD_OUT=$(awk '$0~"'$NIC'"{print $10}' /proc/net/dev)
# echo "$OLD_IN $OLD_OUT"
sleep 1
while true; do
	NEW_IN=$(awk '$0~"'$NIC'"{print $2}' /proc/net/dev)
	NEW_OUT=$(awk '$0~"'$NIC'"{print $10}' /proc/net/dev)
	IN=$(printf "%.1f%s" "$((($NEW_IN-$OLD_IN)/1024))" "KB/s")
	OUT=$(printf "%.1f%s" "$((($NEW_OUT-$OLD_OUT)/1024))" "KB/s")
	printf "\rReceive:${IN} Transmit:${OUT}\x1B[0K"
	# printf "$IN $OUT\n"
	sleep 1
	OLD_IN=$NEW_IN
	OLD_OUT=$NEW_OUT
done

