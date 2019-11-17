#!/bin/sh

./gradlew -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true clean packaging:nano:installFullDist -x test

cd packaging/nano/build/install/virgo-nano-full || exit
./bin/startup.sh
