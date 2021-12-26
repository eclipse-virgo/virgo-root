#!/usr/bin/env sh

mkdir mavenRepository || echo "directory already exists"

./mvnw install:install-file \
  -Dfile=javax.servlet-3.1.0.20200621.jar \
  -DgroupId=org.eclipse.virgo.mirrored \
  -DartifactId=javax.servlet \
  -Dversion=3.1.0.20200621 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=oevm.org.apache.commons.httpclient-3.1.0.jar \
  -DgroupId=org.eclipse.virgo.mirrored \
  -DartifactId=oevm.org.apache.commons.httpclient \
  -Dversion=3.1.0 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

for jar in aop aspects beans context.support context core expression jdbc jms messaging orm oxm transaction web webmvc.portlet webmvc websocket; do
  ./mvnw install:install-file \
    -Dfile=org.springframework.$jar-4.3.27.RELEASE.jar \
    -DgroupId=org.eclipse.virgo.mirrored \
    -DartifactId=org.springframework.$jar \
    -Dversion=4.3.27.RELEASE \
    -Dpackaging=jar \
    -DlocalRepositoryPath=mavenRepository
done

for jar in core jaspic.fragment tomcat; do
  ./mvnw install:install-file \
    -Dfile=org.eclipse.gemini.web.$jar-3.0.6.RELEASE.jar \
    -DgroupId=org.eclipse.gemini \
    -DartifactId=org.eclipse.gemini.web.$jar \
    -Dversion=3.0.6.RELEASE \
    -Dpackaging=jar \
    -DlocalRepositoryPath=mavenRepository
done

./mvnw install:install-file \
  -Dfile=osgi.enterprise-4.2.0.v201108120515.jar \
  -DgroupId=org.osgi \
  -DartifactId=osgi.enterprise \
  -Dversion=4.2.0.v201108120515 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.osgi-3.9.1.v20140110-1610.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.osgi \
  -Dversion=3.9.1.v20140110-1610 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.osgi.services-3.3.100.v20130513-1956.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.osgi.services \
  -Dversion=3.3.100.v20130513-1956 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.cm-1.0.400.v20130327-1442.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.cm \
  -Dversion=1.0.400.v20130327-1442 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.common-3.6.200.v20130402-1505.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.common \
  -Dversion=3.6.200.v20130402-1505 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.ds-1.4.200.v20131126-2331.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.ds \
  -Dversion=1.4.200.v20131126-2331 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.console-1.0.100.v20130429-0953.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.console \
  -Dversion=1.0.100.v20130429-0953 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.console.ssh-1.0.0.v20130515-2026.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.console.ssh \
  -Dversion=1.0.0.v20130515-2026 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.event-1.3.0.v20130327-1442.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.event \
  -Dversion=1.3.0.v20130327-1442 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.region-1.1.101.v20130722-1314.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.region \
  -Dversion=1.1.101.v20130722-1314 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.simpleconfigurator-1.0.400.v20130327-2119.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.simpleconfigurator \
  -Dversion=1.0.400.v20130327-2119 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.simpleconfigurator.manipulator-2.0.0.v20130327-2119.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.simpleconfigurator.manipulator \
  -Dversion=1.0.400.v20130327-2119 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.equinox.util-1.0.500.v20130404-1337.jar \
  -DgroupId=org.eclipse.platform \
  -DartifactId=org.eclipse.equinox.util \
  -Dversion=1.0.500.v20130404-1337 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository

./mvnw install:install-file \
  -Dfile=org.eclipse.jdt.core.compiler.batch-3.12.3.v20170228-1205.jar \
  -DgroupId=org.eclipse.virgo.mirrored \
  -DartifactId=org.eclipse.jdt.core.compiler.batch \
  -Dversion=3.12.3.v20170228-1205 \
  -Dpackaging=jar \
  -DlocalRepositoryPath=mavenRepository
