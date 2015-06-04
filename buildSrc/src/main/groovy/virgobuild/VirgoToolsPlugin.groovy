package virgobuild

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

import eclipsebuild.FileSemaphore

// Derived from buildship's BuildDefinitionPlugin
class VirgoToolsPlugin implements Plugin<Project> {

    // task names
    static final String TASK_NAME_DOWNLOAD_VIRGO_BUILD_TOOLS = "downloadVirgoBuildTools"
    static final String GLOBAL_TASK_NAME_DOWNLOAD_VIRGO_BUILD_TOOLS = ':' + TASK_NAME_DOWNLOAD_VIRGO_BUILD_TOOLS
    @Override
    public void apply(Project project) {
        Config config = Config.on(project)
        addTaskDownloadVirgoBuildTools(project, config)
    }

    static void addTaskDownloadVirgoBuildTools(Project project, Config config) {
        project.task(TASK_NAME_DOWNLOAD_VIRGO_BUILD_TOOLS) {
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
