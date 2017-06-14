## Publishing 3rd party bundles

### Building 

Gradle default tasks:
 1. `clean` - Cleans old build output
 2. `bundlor` - Generates the OSGiifed MF and outputs jar bundles
 3. `publishToMavenLocal` - Publishes jar bundles to `[USER_HOME]/.m2/org/eclipse/virgo/mirrored/[artifactId]/[version]/[bundle].jar`
 4. `test` - Executes the PaxExam tests, the bundles are resolved against local Maven repo thus `publishToMavenLocal` is required to be executed before `test`
 5. `publishIvyPublicationToIvyRepository` - Publishes the jar bundles to `build.eclipse.org` ivy repo (only possible on HIPP)
 
Building locally:

    `./gradlew clean bundlor publishToMavenLocal test`

### Add new Version of Spring

TBD

### Writing PaxExam test for 3rd party bundle

TBD
