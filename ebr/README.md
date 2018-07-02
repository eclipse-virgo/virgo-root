## Publishing 3rd party bundles

We publish 3rd party OSGi bundles via Jenkins job [virgo-mirrored-master](https://ci.eclipse.org/virgo/view/Virgo/job/virgo-mirrored-master/)

### Building

Gradle default tasks:
 1. `clean` - Cleans old build output
 2. `bundlor` - Generates the OSGiifed MF and outputs jar bundles
 3. `publishToMavenLocal` - Publishes jar bundles to `[USER_HOME]/.m2/org/eclipse/virgo/mirrored/[artifactId]/[version]/[bundle].jar`
 4. `test` - Executes the PaxExam tests, the bundles are resolved against local Maven repo thus `publishToMavenLocal` is required to be executed before `test`
 5. `publishIvyPublicationToIvyRepository` - Publishes the jar bundles to `build.eclipse.org` ivy repo (only possible on HIPP)
 
Building locally:

    `./gradlew clean bundlor publishToMavenLocal test`

### Update Spring Framework

The Spring Framework BOM (Bill of Material) is a good point to start
[spring-framework-bom : 5.0.4.RELEASE](http://search.maven.org/#artifactdetails%7Corg.springframework%7Cspring-framework-bom%7C5.0.4.RELEASE%7Cpom)

### Writing PaxExam test for 3rd party bundle

TBD
