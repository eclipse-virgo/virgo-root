package virgobuild

class Constants {

    static String getGradleTaskGroupName() {
        return "Virgo Build"
    }

    static String getVirgoBuildToolsVersion() {
        return '1.2.1.RELEASE'
    }

    static URL getVirgoBuildToolsDownloadUrl() {
        return new URL("https://mirror.dkm.cz/eclipse/virgo/release/VB/1.2.1/virgo-build-tools-1.2.1.RELEASE.zip")
    }
}
