package virgobuild

class Constants {

    static String getGradleTaskGroupName() {
        return "Virgo Build"
    }

    static String getVirgoBuildToolsVersion() {
        return '1.5.0.RELEASE'
    }

    static URL getVirgoBuildToolsDownloadUrl() {
        return new URL("https://build.eclipse.org/rt/virgo/zips/release/VB/${virgoBuildToolsVersion}/virgo-build-tools-${virgoBuildToolsVersion}.zip")
    }
}
