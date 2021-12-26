package virgobuild

import org.gradle.api.Plugin
import org.gradle.api.Project
import FileSemaphore

// new comment
// Derived from buildship Plugins
class VirgoToolsPlugin implements Plugin<Project> {

    // virgo tasks names
    static final String DOWNLOAD_ECLIPSE_PHOTON_SDK_TASK_NAME = "downloadEclipsePhotonSdk"

    @Override
    void apply(Project project) {
        Config config = Config.on(project)
        addTaskDownloadEclipsePhotonSdk(project, config)
    }

    static void publishProduct(Project project, File productFileLocation) {
        File repositoryDir = project.file("${project.rootProject.projectDir}/org.eclipse.virgo.site/build/repository/")
        internalPublishProduct(project, repositoryDir, productFileLocation)
    }

    private static void internalPublishProduct(Project project, File repositoryDir, File productFileLocation) {
        project.logger.info("Publishing Virgo ${productFileLocation} to '${repositoryDir}'.")
        project.javaexec {
            main = 'org.eclipse.equinox.launcher.Main'
            classpath Config.on(project).eclipsePhotonLauncherJar
            args = [
                '-application',
                'org.eclipse.equinox.p2.publisher.ProductPublisher',
                '-metadataRepository',
                "file:${repositoryDir}",
                '-artifactRepository',
                "file:${repositoryDir}",
                '-append',
                '-compress',
                '-publishArtifacts',
                '-productFile',
                productFileLocation,
//                '-jreLocation',
//                "${javaProfileLocation}",
                '-configs',
                'ANY.ANY.ANY',
                '-flavor',
                'tooling'
            ]
        }
        project.logger.info("Published Virgo ${productFileLocation} to '${repositoryDir}'.")
    }

    static void installProduct(Project project, String productIu, File destinationDir) {
        File repositoryDir = project.file("${project.rootProject.projectDir}/org.eclipse.virgo.site/build/repository/")
        project.logger.info("Installing Virgo '${productIu}' assembled from '${repositoryDir}' into '${destinationDir}'.")
        project.javaexec {
            main = 'org.eclipse.equinox.launcher.Main'
            classpath Config.on(project).eclipsePhotonLauncherJar
            args = [
                '-application',
                'org.eclipse.equinox.p2.director',
                '-repository',
                "file:${repositoryDir}",
                '-installIU',
                productIu,
                '-tag',
                'InitialState',
                '-destination',
                destinationDir,
                '-profile',
                'VIRGOProfile',
                '-roaming'
            ]
        }
        project.logger.info("Installed Virgo '${productIu}' assembled from '${repositoryDir}' into '${destinationDir}'.")
    }


    static void addTaskDownloadEclipsePhotonSdk(Project project, Config config) {
        project.task(DOWNLOAD_ECLIPSE_PHOTON_SDK_TASK_NAME) {
            group = Constants.gradleTaskGroupName
            description = "Downloads the Eclipse Photon SDK to perform P2 operations with."
            outputs.file config.eclipsePhotonSdkArchive
            doLast { downloadEclipsePhotonSdk(project, config) }
        }
    }

    static void downloadEclipsePhotonSdk(Project project, Config config) {
        // if multiple builds start on the same machine (which is the case with a CI server)
        // we want to prevent them downloading the same file to the same destination
        def directoryLock = new FileSemaphore(config.virgoBuildToolsDir)
        directoryLock.lock()
        try {
            downloadEclipsePhotonSdkUnprotected(project, config)
        } finally {
            directoryLock.unlock()
        }
    }

    static void downloadEclipsePhotonSdkUnprotected(Project project, Config config) {
        // download the archive
        File eclipsePhotonSdkArchive = config.eclipsePhotonSdkArchive
        project.logger.info("Download Eclipse Photon SDK from '${Constants.eclipsePhotonSdkDownloadUrl}' to '${eclipsePhotonSdkArchive.absolutePath}'")
        project.ant.get(src: Constants.eclipsePhotonSdkDownloadUrl, dest: eclipsePhotonSdkArchive)

        // extract it to the same location where it was downloaded
        project.logger.info("Extract '$eclipsePhotonSdkArchive' to '$eclipsePhotonSdkArchive.parentFile.absolutePath'")

        File tarFile = new File(eclipsePhotonSdkArchive.parentFile, 'eclipse-photon-sdk.tar')
        project.ant.gunzip(src: eclipsePhotonSdkArchive, dest: tarFile)
        project.copy {
            from project.tarTree(tarFile)
            into new File(eclipsePhotonSdkArchive.parentFile, 'eclipse-photon-sdk')
        }
    }

}
