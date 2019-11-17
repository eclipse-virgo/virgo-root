#!/bin/sh

./gradlew -Dskip.normalize.bundles=true -Dskip.compress.bundles=true -Dskip.local.signing=true -Dci.build.signjars=false -Dtest.ignoreFailures=true clean test smokeTest distZip fullDistZip
