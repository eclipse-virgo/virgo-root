package virgobuild

class Constants {

    static String getGradleTaskGroupName() {
        return "Virgo Build"
    }

    static String getVirgoBuildToolsVersion() {
        return '3.8.0.RELEASE'
    }

    static URL getEclipsePhotonSdkDownloadUrl() {
        return new URL("https://ftp.halifax.rwth-aachen.de/eclipse/technology/epp/downloads/release/photon/R/eclipse-java-photon-R-linux-gtk-x86_64.tar.gz")
    }
}
