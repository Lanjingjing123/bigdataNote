#! /bin/bash
[ ! $# -eq 1 ] && echo "args error!!!" &&exit 3
id $1 >& /dev/null&& echo "User Existed"&& exit 4
useradd $1 >& /dev/null && echo $1 |passwd --stdin $1 >& /dev/null && echo "User added successfully"&&exit 5
echo "add user fail!!!"&&exit 6

