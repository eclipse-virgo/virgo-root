./gradlew -Dci.build=true -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true --stacktrace clean jar build distZip fullDistZip rapDistZip installDist installFullDist installRapDist smokeTest -x test -x findBugsMain -x findBugsTest

