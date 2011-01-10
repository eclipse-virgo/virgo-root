#!/bin/bash

# make sure we have JAVA_HOME set
if [ -z "$JAVA_HOME" ]
then
    echo The JAVA_HOME environment variable is not defined
    exit 1
fi

CLASSPATH=

#  Create the classpath for bootstrapping the Server from all the JARs in lib
for file in $KERNEL_HOME/lib/*
do
	if [[ $file == *.jar ]]
	then
        CLASSPATH=$CLASSPATH:$KERNEL_HOME/lib/${file##*/}
	fi
done

# make sure we have CLASSPATH set
if [ -z "$CLASSPATH" ]
then
    echo No JAR files found in $KERNEL_HOME/lib
    exit 1
fi
