#!/bin/sh

./gradlew -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true clean smokeTest -x test
