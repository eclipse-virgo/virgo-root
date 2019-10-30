#!/bin/sh

./gradlew -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true packaging:tomcat-server:installDist -x test

cd packaging/tomcat-server/build/install/virgo-tomcat-server || exit
./bin/startup.sh
