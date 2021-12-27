import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class DownloadEclipsePhoton extends DefaultTask {

    static File getEclipsePhotonSdkArchive() {
        new File(System.getProperty('user.home'), '.gradle/eclipse-java-photon-R-linux-gtk-x86_64.tar.gz')
    }
    static URL getEclipsePhotonSdkDownloadUrl() {
        return new URL("https://www.eclipse.org/downloads/download.php?file=/technology/epp/downloads/release/photon/R/eclipse-java-photon-R-linux-gtk-x86_64.tar.gz")
    }
    static String getVirgoBuildToolsVersion() {
        return '3.8.0.RELEASE'
    }

    @Internal
    File getVirgoBuildToolsBaseDir() {
        // to avoid configuration timing issues we don't cache the values in fields
        // @see buildship project eclipsebuild.Config
        project.hasProperty('virgoBuildToolsDir') ?
                new File(project.property('virgoBuildToolsDir') as String) :
                new File(System.getProperty('user.home'), '.tooling/virgo')
    }
    @Internal
    File getVirgoBuildToolsDir() {
        String virgoBuildToolsBaseName="virgo-build-tools-${virgoBuildToolsVersion}"
        new File(virgoBuildToolsBaseDir, virgoBuildToolsBaseName)
    }

    @TaskAction
    void downloadEclipsePhoton() {
        logger.info("Download Eclipse Photon SDK from '${eclipsePhotonSdkDownloadUrl}' to '${eclipsePhotonSdkArchive.absolutePath}'")
        downloadEclipsePhotonSdk()
    }

    void downloadEclipsePhotonSdk() {
        // if multiple builds start on the same machine (which is the case with a CI server)
        // we want to prevent them downloading the same file to the same destination
        def directoryLock = new FileSemaphore(getVirgoBuildToolsDir())
        directoryLock.lock()
        try {
            downloadEclipsePhotonSdkUnprotected()
        } finally {
            directoryLock.unlock()
        }
    }

    void downloadEclipsePhotonSdkUnprotected() {
        // download the archive
        ant.get(src: eclipsePhotonSdkDownloadUrl, dest: eclipsePhotonSdkArchive)

        // extract it to the same location where it was downloaded
        logger.info("Extract '$eclipsePhotonSdkArchive' to '$eclipsePhotonSdkArchive.parentFile.absolutePath'")

        File tarFile = new File(eclipsePhotonSdkArchive.parentFile, 'eclipse-photon-sdk.tar')
        ant.gunzip(src: eclipsePhotonSdkArchive, dest: tarFile)
        project.copy {
            from project.tarTree(tarFile)
            into new File(eclipsePhotonSdkArchive.parentFile, 'eclipse-photon-sdk')
        }
    }
}
