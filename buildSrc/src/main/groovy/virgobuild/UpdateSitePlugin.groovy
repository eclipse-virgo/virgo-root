package virgobuild

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaPlugin

import eclipsebuild.FeaturePlugin

import static virgobuild.VirgoToolsPlugin.DOWNLOAD_ECLIPSE_PHOTON_SDK_TASK_NAME

// Derived from buildship Plugins
class UpdateSitePlugin implements Plugin<Project> {

    /**
     * Extension class to configure the UpdateSite plugin.
     */
    static class Extension {
        File siteDescriptor
        FileCollection extraResources
        // TOOD - remove this hook once all of our features are consolidated are migrated to feature projects
        Closure hook
        Closure signing
        Closure mutateArtifactsXml
    }

    // name of the root node in the DSL
    static final String DSL_EXTENSION_NAME = 'updateSite'

    // buildship task names (in order of execution)
    static final String COPY_BUNDLES_TASK_NAME = 'copyBundles'
    static final String NORMALIZE_BUNDLES_TASK_NAME = 'normalizeBundles'
    static final String SIGN_BUNDLES_TASK_NAME = 'signBundles'
    static final String COMPRESS_BUNDLES_TASK_NAME = 'compressBundles'
    static final String CREATE_P2_REPOSITORY_TASK_NAME = 'createP2Repository'

    // temporary folder names during build
    static final String PRE_NORMALIZED_BUNDLES_DIR_NAME = 'unconditioned-bundles'
    static final String UNSIGNED_BUNDLES_DIR_NAME = 'unsigned-bundles'
    static final String SIGNED_BUNDLES_DIR_NAME = 'signed-bundles'
    static final String COMPRESSED_BUNDLES_DIR_NAME = 'compressed-bundles'
    static final String FEATURES_DIR_NAME = 'features'
    static final String PLUGINS_DIR_NAME = 'plugins'
    static final String REPOSITORY_DIR_NAME = 'repository'

    @Override
    void apply(Project project) {
        configureProject(project)
        addTaskCopyBundles(project)
        addTaskNormalizeBundles(project)
        addTaskSignBundles(project)
        addTaskCompressBundles(project)
        addTaskCreateP2Repository(project)
    }

    static void configureProject(Project project) {
        // apply the Java plugin to have the life-cycle tasks
        project.plugins.apply(JavaPlugin)

        // create scopes for local and external plugins and features
        project.configurations.create('localPlugin')
        project.configurations.create('localFeature')
        project.configurations.create('externalPlugin')
        project.configurations.create('signedExternalPlugin')

        // add the 'updateSite' extension
        project.extensions.create(DSL_EXTENSION_NAME, Extension)
        project.updateSite.siteDescriptor = project.file('category.xml')
        project.updateSite.extraResources = project.files()
        project.updateSite.hook = null
        project.updateSite.signing = null
        project.updateSite.mutateArtifactsXml = null

        // validate the content
        validateRequiredFilesExist(project)
    }

    static void addTaskCopyBundles(Project project) {
        def copyBundlesTask = project.task(COPY_BUNDLES_TASK_NAME) {

            dependsOn ':3rd-party:bundles'
            dependsOn ':kernel:org.eclipse.virgo.kernel.services:rewriteJar'

            group = Constants.gradleTaskGroupName
            description = 'Collects the bundles that make up the update site.'
            outputs.dir new File(project.buildDir, PRE_NORMALIZED_BUNDLES_DIR_NAME)
            doLast { copyBundles(project) }
            doLast { project.updateSite.hook(project) }
        }

        // add inputs for each plugin/feature project once this build script has been evaluated (before that, the dependencies are empty)
        project.afterEvaluate {
            for (ProjectDependency projectDependency : project.configurations.localPlugin.dependencies.withType(ProjectDependency)) {
                // check if the dependent project is a bundle or feature, once its build script has been evaluated
                def dependency = projectDependency.dependencyProject
                //                if (dependency.plugins.hasPlugin(BundlePlugin)) {
                copyBundlesTask.inputs.files dependency.tasks.assemble.outputs.files
                //                } else {
                //                    dependency.afterEvaluate {
                //                        if (dependency.plugins.hasPlugin(BundlePlugin)) {
                //                            copyBundlesTask.inputs.files dependency.tasks.jar.outputs.files
                //                        }
                //                    }
                //                }
            }
        }

        project.afterEvaluate {
            for (ProjectDependency projectDependency : project.configurations.localFeature.dependencies.withType(ProjectDependency)) {
                // check if the dependent project is a bundle or feature, once its build script has been evaluated
                def dependency = projectDependency.dependencyProject
                if (dependency.plugins.hasPlugin(FeaturePlugin)) {
                    copyBundlesTask.inputs.files dependency.tasks.jar.outputs.files
                } else {
                    dependency.afterEvaluate {
                        if (dependency.plugins.hasPlugin(FeaturePlugin)) {
                            copyBundlesTask.inputs.files dependency.tasks.jar.outputs.files
                        }
                    }
                }
            }
        }
    }

    static void copyBundles(Project project) {
        def rootDir = new File(project.buildDir, PRE_NORMALIZED_BUNDLES_DIR_NAME)
        def pluginsDir = new File(rootDir, PLUGINS_DIR_NAME)
        def featuresDir = new File(rootDir, FEATURES_DIR_NAME)

        // delete old content
        if (rootDir.exists()) {
            project.logger.info("Delete bundles directory '${rootDir.absolutePath}'")
            rootDir.deleteDir()
        }

        // iterate over all the project dependencies to populate the update site with the plugins and features
        project.logger.info("Copy features and plugins to bundles directory '${rootDir.absolutePath}'")

        for (ProjectDependency projectDependency : project.configurations.localPlugin.dependencies.withType(ProjectDependency)) {
            def dependency = projectDependency.dependencyProject

            // copy the output jar for each plugin project dependency
            //            if (dependency.plugins.hasPlugin(BundlePlugin)) {
            //                project.logger.debug("Copy plugin project '${dependency.name}' with jar '${dependency.tasks.jar.outputs.files.singleFile.absolutePath}' to '${pluginsDir}'")
            project.copy {
                from dependency.tasks.jar.outputs.files.singleFile
                into pluginsDir
            }
            //            }
        }
        for (ProjectDependency projectDependency : project.configurations.localFeature.dependencies.withType(ProjectDependency)) {
            def dependency = projectDependency.dependencyProject
            // copy the output jar for each feature project dependency
            if (dependency.plugins.hasPlugin(FeaturePlugin)) {
                project.logger.debug("Copy feature project '${dependency.name}' with jar '${dependency.tasks.jar.outputs.files.singleFile.absolutePath}' to '${pluginsDir}'")
                project.copy {
                    from dependency.tasks.jar.outputs.files.singleFile
                    into featuresDir
                }
            }
        }

        // iterate over all external dependencies and add them to the plugins (this includes the transitive dependencies)
        project.copy {
            from project.configurations.externalPlugin
            into pluginsDir
        }
    }

    static void addTaskNormalizeBundles(Project project) {
        project.task(NORMALIZE_BUNDLES_TASK_NAME, dependsOn: [
            COPY_BUNDLES_TASK_NAME,
            ":${DOWNLOAD_ECLIPSE_PHOTON_SDK_TASK_NAME}"
        ]) {
            group = Constants.gradleTaskGroupName
            description = 'Repacks the bundles that make up the update site using the pack200 tool.'
            inputs.dir new File(project.buildDir, PRE_NORMALIZED_BUNDLES_DIR_NAME)
            outputs.dir new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
            doLast { normalizeBundles(project) }
        }
    }

    static void normalizeBundles(Project project) {
        if (System.properties['skip.normalize.bundles'] == 'true') {
            project.logger.warn("Skipping normalization of bundles!")
            project.copy {
                from new File(project.buildDir, PRE_NORMALIZED_BUNDLES_DIR_NAME)
                into new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
            }
        } else {
            project.javaexec {
                main = 'org.eclipse.equinox.internal.p2.jarprocessor.Main'
                classpath Config.on(project).jarProcessorJar
                args = [
                    '-processAll',
                    '-repack',
                    '-outputDir',
                    new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME),
                    new File(project.buildDir, PRE_NORMALIZED_BUNDLES_DIR_NAME)
                ]
            }
        }
    }

    static void addTaskSignBundles(Project project) {
        project.task(SIGN_BUNDLES_TASK_NAME, dependsOn: NORMALIZE_BUNDLES_TASK_NAME) {
            group = Constants.gradleTaskGroupName
            description = 'Signs the bundles that make up the update site.'
            inputs.dir new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
            outputs.dir new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME)
            doLast { project.updateSite.signing(new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME), new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME)) }
            doLast { copyOverAlreadySignedBundles(project) }
            onlyIf { project.updateSite.signing != null }
        }
    }

    static void copyOverAlreadySignedBundles(Project project) {
        project.copy {
            from project.configurations.signedExternalPlugin
            into new File(project.buildDir, "$SIGNED_BUNDLES_DIR_NAME/$PLUGINS_DIR_NAME")
        }
    }

    static void addTaskCompressBundles(Project project) {
        project.task(COMPRESS_BUNDLES_TASK_NAME, dependsOn: [
            NORMALIZE_BUNDLES_TASK_NAME,
            SIGN_BUNDLES_TASK_NAME,
            ":${DOWNLOAD_ECLIPSE_PHOTON_SDK_TASK_NAME}"
        ]) {
            group = Constants.gradleTaskGroupName
            description = 'Compresses the bundles that make up the update using the pack200 tool.'
            project.afterEvaluate { inputs.dir project.updateSite.signing != null ? new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME) : new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME) }
            outputs.dir  new File(project.buildDir, COMPRESSED_BUNDLES_DIR_NAME)
            doLast { compressBundles(project) }
        }
    }

    static void compressBundles(Project project) {
        File uncompressedBundles = project.updateSite.signing != null ? new File(project.buildDir, SIGNED_BUNDLES_DIR_NAME) : new File(project.buildDir, UNSIGNED_BUNDLES_DIR_NAME)
        File compressedBundles = new File(project.buildDir, COMPRESSED_BUNDLES_DIR_NAME)

        // copy over all bundles
        project.copy {
            from uncompressedBundles
            into compressedBundles
        }

        if (System.properties['skip.compress.bundles'] == 'true') {
            project.logger.warn("Skipping compression of bundles!")
        } else {
            // compress and store them in the same folder
            project.javaexec {
                main = 'org.eclipse.equinox.internal.p2.jarprocessor.Main'
                classpath Config.on(project).jarProcessorJar
                args = [
                    '-pack',
                    '-outputDir',
                    compressedBundles,
                    compressedBundles
                ]
            }
        }
    }

    static void addTaskCreateP2Repository(Project project) {
        def createP2RepositoryTask = project.task(CREATE_P2_REPOSITORY_TASK_NAME, dependsOn: [
            COMPRESS_BUNDLES_TASK_NAME,
            ':3rd-party:checkEclipse', // download bnd-platform Eclipse, if necessary
            ":${DOWNLOAD_ECLIPSE_PHOTON_SDK_TASK_NAME}"
        ]) {
            group = Constants.gradleTaskGroupName
            description = 'Generates the P2 repository.'
            inputs.file project.updateSite.siteDescriptor
            inputs.files project.updateSite.extraResources
            inputs.dir new File(project.buildDir, COMPRESSED_BUNDLES_DIR_NAME)
            outputs.dir new File(project.buildDir, REPOSITORY_DIR_NAME)
            doLast { createP2Repository(project) }
        }

        project.tasks.assemble.dependsOn createP2RepositoryTask
    }

    static void createP2Repository(Project project) {
        def repositoryDir = new File(project.buildDir, REPOSITORY_DIR_NAME)

        // delete old content
        if (repositoryDir.exists()) {
            project.logger.info("Delete P2 repository directory '${repositoryDir.absolutePath}'")
            repositoryDir.deleteDir()
        }

        // create the P2 update site
        publishContentToLocalP2Repository(project, repositoryDir)
    }

    static void publishContentToLocalP2Repository(Project project, File repositoryDir) {
        def rootDir = new File(project.buildDir, COMPRESSED_BUNDLES_DIR_NAME)

        // publish features/plugins to the update site
        project.logger.info("Publish plugins and features from '${rootDir.absolutePath}' to the update site '${repositoryDir.absolutePath}'")
        project.javaexec {
            main = 'org.eclipse.equinox.launcher.Main'
            classpath Config.on(project).bndPlatformLauncherJar
            args = [
                '-application',
                'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher',
                '-metadataRepository',
                repositoryDir.toURI().toURL(),
                '-artifactRepository',
                repositoryDir.toURI().toURL(),
                '-source',
                rootDir,
                '-compress',
                '-publishArtifacts',
                '-reusePack200Files',
                '-configs',
                'ANY'
            ]
        }

        // publish P2 category defined in the category.xml to the update site
        project.logger.info("Publish categories defined in '${project.updateSite.siteDescriptor.absolutePath}' to the update site '${repositoryDir.absolutePath}'")
        project.javaexec {
            main = 'org.eclipse.equinox.launcher.Main'
            classpath Config.on(project).bndPlatformLauncherJar
            args = [
                '-application',
                'org.eclipse.equinox.p2.publisher.CategoryPublisher',
                '-metadataRepository',
                repositoryDir.toURI().toURL(),
                '-categoryDefinition',
                project.updateSite.siteDescriptor.toURI().toURL(),
                '-compress'
            ]
        }

        // copy the extra resources to the update site
        project.copy {
            from project.updateSite.extraResources
            into repositoryDir
        }
    }

    static void validateRequiredFilesExist(Project project) {
        project.gradle.taskGraph.whenReady {
            // make sure the required descriptors exist
            assert project.file(project.updateSite.siteDescriptor).exists()
        }
    }

}
