How to build Virgo locally
==========================

Since 3.7.0.M02 Virgo is built with Gradle.

During `./gradlew <TODO_PROVIDE_BUILD_ALL_TASK>` we
* build the Virgo sources
* create a temporary p2 repository
* and package the Virgo deliverables.




Build the Virgo deliverables
============================

To build distributable zipped files run:

    ::::sh
    $ ./gradlew clean jar build distZip fullDistZip rapDistZip

There is an additional task to install the zips, too. This comes quite handy to quickly check the distributables:
    
    ::::sh
    $ ./gradlew installDist installFullDist installRapDist

To speed up local builds you can skip some time-consuming processes like follows:
 * `-Dskip.normalize.bundles=true`
 * `-Dskip.local.signing=true`
 * `-Dskip.compress.bundles=true`

If you are only interested in the distribution(s) you might additionally want to skip some Gradle tasks, too: `-x test -x findBugsMain -x findBugsTest`. 

Run the basic smoke tests
=========================

TBD

Upload a p2 update site
=======================

You can upload the create p2 update site with `./gradlew uploadUpdateSite`. This will
* normalize, compress and sign
* then upload the bundles into a composite repository at eclipse.org.

Please review the configuration located in `org.eclipse.virgo.updatesite/gradle.properties`. You can test your setup with `./gradlew testEclipseConnection`:

```
$ ./gradlew testEclipseConnection
...
:org.eclipse.virgo.site:testEclipseConnection
eclipseDotOrg|Linux build 3.0.101-0.47.52-default #1 SMP Thu Mar 26 10:55:49 UTC 2015 (0e3c7c8) x86_64 x86_64 x86_64 GNU/Linux
Linux build 3.0.101-0.47.52-default #1 SMP Thu Mar 26 10:55:49 UTC 2015 (0e3c7c8) x86_64 x86_64 x86_64 GNU/Linux

BUILD SUCCESSFUL

Total time: 12.935 secs
```

Once the configuration is successfully tested you can do the actual upload of a snapshot site with `./gradlew uploadUpdateSite`.

```
$ ./gradlew -Dci.build=true clean uploadUpdateSite
...
:org.eclipse.virgo.site:copyBundles
:org.eclipse.virgo.site:normalizeBundles
:org.eclipse.virgo.site:signBundles
...
:org.eclipse.virgo.site:compressBundles
:org.eclipse.virgo.site:createP2Repository
Generating metadata for ..
Generation completed with success [3 seconds].
Generating metadata for ..
Generation completed with success [0 seconds].
> Building 99% > :org.eclipse.virgo.site:uploadUpdateSite
```
