package virgobuild

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

import eclipsebuild.FileSemaphore

// new comment
// Derived from buildship Plugins
class VirgoToolsPlugin implements Plugin<Project> {

    // virgo tasks names
    static final String DOWNLOAD_VIRGO_BUILD_TOOLS_TASK_NAME = "downloadVirgoBuildTools"

    static final String TASK_NAME_GENERATE_P2_INSTRUCTIONS = "generateP2Instructions"

    @Override
    public void apply(Project project) {
        Config config = Config.on(project)
        addTaskDownloadVirgoBuildTools(project, config)
    }

    static void generateP2Instructions(Project project, File assemblyFeatureDir) {
        project.logger.info("Generating p2 instructions for '${assemblyFeatureDir}'.")
        project.javaexec {
            main = 'org.eclipse.equinox.launcher.Main'
            classpath Config.on(project).equinoxLauncherJar
            args = [
                '-application',
                'org.eclipse.virgo.build.p2tools.instructions.P2InstructionGeneratorApplication',
                '-source',
                assemblyFeatureDir
            ]
        }
        project.logger.info("Generated p2 instructions for '${assemblyFeatureDir}'.")
    }

    static void publishProduct(Project project, File productFileLocation, File javaProfileLocation) {
        File repositoryDir = project.file("${project.rootProject.projectDir}/org.eclipse.virgo.site/build/repository/")
        publishProduct(project, repositoryDir, productFileLocation, javaProfileLocation)

    }

    // TODO remove unnecessary variable javaProfileLocation as this can be always oev.site/config/something?!
    @Deprecated
    static void publishProduct(Project project, File repositoryDir, File productFileLocation, File javaProfileLocation) {
        project.logger.info("Publishing Virgo ${productFileLocation} to '${repositoryDir}'.")
        project.javaexec {
            main = 'org.eclipse.equinox.launcher.Main'
            classpath Config.on(project).equinoxLauncherJar
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
        installProduct(project, productIu, repositoryDir, destinationDir)
    }

    @Deprecated
    static void installProduct(Project project, String productIu, File repositoryDir, File destinationDir) {
        project.logger.info("Installing Virgo '${productIu}' assembled from '${repositoryDir}' into '${destinationDir}'.")
        project.javaexec {
            main = 'org.eclipse.equinox.launcher.Main'
            classpath Config.on(project).equinoxLauncherJar
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

    static void addTaskDownloadVirgoBuildTools(Project project, Config config) {
        project.task(DOWNLOAD_VIRGO_BUILD_TOOLS_TASK_NAME) {
            group = Constants.gradleTaskGroupName
            description = "Downloads the Virgo Build Tools to perform P2 operations with."
            outputs.file config.virgoBuildToolsArchive
            doLast { downloadVirgoBuildTools(project, config) }
        }
    }

    static void downloadVirgoBuildTools(Project project, Config config) {
        // if multiple builds start on the same machine (which is the case with a CI server)
        // we want to prevent them downloading the same file to the same destination
        def directoryLock = new FileSemaphore(config.virgoBuildToolsDir)
        directoryLock.lock()
        try {
            downloadVirgoBuildToolsUnprotected(project, config)
        } finally {
            directoryLock.unlock()
        }
    }

    static void downloadVirgoBuildToolsUnprotected(Project project, Config config) {
        // download the archive
        File virgoBuildToolsArchive = config.virgoBuildToolsArchive
        project.logger.info("Download Virgo Build Tools from '${Constants.virgoBuildToolsDownloadUrl}' to '${virgoBuildToolsArchive.absolutePath}'")
        project.ant.get(src: Constants.virgoBuildToolsDownloadUrl, dest: virgoBuildToolsArchive)

        // extract it to the same location where it was extracted
        project.logger.info("Extract '$virgoBuildToolsArchive' to '$virgoBuildToolsArchive.parentFile.absolutePath'")
        project.ant.unzip(src: virgoBuildToolsArchive, dest: virgoBuildToolsArchive.parentFile, overwrite: true)
    }

}
