#!/bin/bash

echo "------ CPU ------"
ps -eo pid,pcpu,pmem,args --sort=-pcpu | head -n 10
echo "------ MEM ------"	
ps -eo pid,pcpu,pmem,args --sort=-pmem | head -n 10	
