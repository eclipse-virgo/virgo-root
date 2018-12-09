# Publishing 3rd party bundles

We use our JIPP instance to publish 3rd party bundles.
The job [virgo-mirrored-master](https://ci.eclipse.org/virgo/view/Virgo/job/virgo-mirrored-master/) publishes the artifacts created with subproject `ebr`.

## Building 

Gradle default tasks:
 1. `clean` - Cleans old build output
 2. `bundlor` - Generates the OSGi metadata plus jar bundles
 3. `publishToMavenLocal` - Publishes jar bundles to `[USER_HOME]/.m2/org/eclipse/virgo/mirrored/[artifactId]/[version]/[bundle].jar`
 4. `test` - Executes the PaxExam tests, the bundles are resolved against local Maven repo thus `publishToMavenLocal` is required to be executed before `test`
 5. `publishIvyPublicationToIvyRepository` - Publishes the jar bundles to `build.eclipse.org` ivy repo (only possible on HIPP)

Build and test locally:

    `./gradlew clean bundlor publishToMavenLocal test`

## Update Version of a Spring Framework stream

The Spring Framework BOM (Bill of Material) is a good point to start
[spring-framework-bom : 5.0.4.RELEASE](http://search.maven.org/#artifactdetails%7Corg.springframework%7Cspring-framework-bom%7C5.0.4.RELEASE%7Cpom)

* Rename the directories to the new version of Spring.
* Check and update the versions of the dependencies in `gradle.properties`, if required.

## Add a new Spring Framework stream

* Create new folder structure e.g. `5.1.<version>`
