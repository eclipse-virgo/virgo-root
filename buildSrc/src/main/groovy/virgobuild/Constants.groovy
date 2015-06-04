package virgobuild

class Constants {

    static String getGradleTaskGroupName() {
        return "Virgo Build"
    }

    static String getVirgoBuildToolsVersion() {
        return '1.4.1.RELEASE'
    }

    static URL getVirgoBuildToolsDownloadUrl() {
        return new URL("http://build.eclipse.org/rt/virgo/zips/release/VB/${virgoBuildToolsVersion}/virgo-build-tools-${virgoBuildToolsVersion}.zip")
    }
}
