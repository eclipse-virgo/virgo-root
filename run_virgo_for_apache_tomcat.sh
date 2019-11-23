#!/bin/sh

./gradlew -Dskip.normalize.bundles=true -Dskip.local.signing=true -Dskip.compress.bundles=true packaging:tomcat-server:installDist -x test

# build and deploy test app(s)

./gradlew -Dskip.normalize.bundles=true -Dskip.compress.bundles=true -Dskip.local.signing=true -Dci.build.signjars=false web:test-apps:osgi-webapp:jar
cp web/test-apps/osgi-webapp/build/libs/osgi-webapp-3.8.0.BUILD-SNAPSHOT.jar packaging/tomcat-server/build/install/virgo-tomcat-server/pickup

#./gradlew -Dskip.normalize.bundles=true -Dskip.compress.bundles=true -Dskip.local.signing=true -Dci.build.signjars=false web:test-apps:classpath-context-config-locations:war
#cp web/test-apps/classpath-context-config-locations/build/libs/classpath-context-config-locations-3.8.0.BUILD-SNAPSHOT.war packaging/tomcat-server/build/install/virgo-tomcat-server/pickup

cd packaging/tomcat-server/build/install/virgo-tomcat-server || exit
./bin/startup.sh
