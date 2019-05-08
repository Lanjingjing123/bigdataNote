#! /bin/bash
oldIFS=$IFS
IFS=$'\n'
for i in `du -a $1 | sort -nr`;do 
	filename=`echo $i | awk '{print $2}'`&&Bytes=`echo $i | awk '{print $1}'`;
	if [ -f $filename ];then 
		echo -e "${Bytes}\t${filename}" 
		break
	fi

done
IFS=$oldIFS
