#!/bin/sh

./gradlew -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true packaging:kernel:installDist -x test

cd packaging/kernel/build/install/virgo-kernel || exit
./bin/startup.sh
