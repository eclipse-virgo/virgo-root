#!/bin/sh

./gradlew -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true --stacktrace clean smokeTest -x test
