./gradlew -Dci.build=true -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true --stacktrace clean jar build distZip fullDistZip installDist installFullDist smokeTest -x test -x findBugsMain -x findBugsTest

