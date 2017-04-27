#!/bin/bash

# Change this to your netid
netid=agm095020

# Root directory of your project
PROJDIR=$HOME/aos/Distributed-Critical-Section

# Directory where the config file is located on your local system
CONFIGLOCAL=$HOME/aos/Distributed-Critical-Section/config.txt

# Identity File
IDENTITY=$HOME/.ssh/UTDCS

# Directory your java classes are in
BINDIR=$PROJDIR/src

# Your main project class
PROG=primary.Program

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read i
    i=$(echo $i | awk '{print $1;}' )
    echo $i
    while [ $n -lt $i ] 
    do
    	read line
    	n=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )

	echo $netid@$host java -cp $BINDIR $PROG $n

    cat /dev/null > $PROJDIR/$n.txt
	
    screen -d -m ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i $IDENTITY $netid@$host "cd $PROJDIR; java -cp $BINDIR $PROG $n"
        n=$(( n + 1 ))
    done
)


# add the "&>> $n.txt" to log all the outputs from the programs.
#screen -d -m ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i $IDENTITY $netid@$host "cd $PROJDIR; java -cp $BINDIR $PROG $n RandA &>> $n.txt"
