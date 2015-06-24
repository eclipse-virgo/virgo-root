package aspectj

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

class AspectJPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.plugins.apply(JavaPlugin)

        for (projectSourceSet in project.sourceSets) {
            if (!projectSourceSet.allJava.isEmpty()) {
                def aspectTaskName = "compileAspect"
                def javaTaskName = "compileJava"
                def configurationName = "aspectpath"
                if (!projectSourceSet.name.equals('main')) {
                    aspectTaskName = "compile${projectSourceSet.name.capitalize()}Aspect"
                    javaTaskName = "compile${projectSourceSet.name.capitalize()}Java"
                    configurationName = "${projectSourceSet.name}Aspectpath"
                }

                project.tasks.create(name: aspectTaskName, overwrite: true, description: "Compiles AspectJ Source for ${projectSourceSet.name} source set", type: Ajc) {
                    sourceSet = projectSourceSet
                    inputs.files(sourceSet.allJava)
                    outputs.dir(sourceSet.output.classesDir)
                    aspectpath = project.configurations.findByName(configurationName)
                }

                project.tasks[aspectTaskName].setDependsOn(project.tasks[javaTaskName].dependsOn)
                project.tasks[aspectTaskName].dependsOn(project.tasks[aspectTaskName].aspectpath)
                project.tasks[javaTaskName].deleteAllActions()
                project.tasks[javaTaskName].dependsOn(project.tasks[aspectTaskName])
            }
        }
    }
}

class Ajc extends DefaultTask {

    SourceSet sourceSet

    FileCollection aspectpath

    Ajc() {
        logging.captureStandardOutput(LogLevel.INFO)
    }

    @TaskAction
    def compile() {
        def iajcArgs = [
            classpath           : sourceSet.compileClasspath.asPath,
            destDir             : sourceSet.output.classesDir.absolutePath,
            source              : '1.7',
            target              : '1.7',
            aspectPath          : aspectpath.asPath,
            sourceRootCopyFilter: '**/*.java,**/*.aj',
            showWeaveInfo       : 'true',
            debug               : 'true',
            debugLevel          : 'lines,vars,source',
            checkRuntimeVersion : 'false',
            X                   : '',
        ]

        ant.taskdef(resource: "org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties", classpath: project.configurations.ajtools.asPath)
        ant.iajc(iajcArgs) {
            sourceRoots {
                sourceSet.java.srcDirs.each {
                    pathelement(location: it.absolutePath)
                }
            }
        }
    }
}
