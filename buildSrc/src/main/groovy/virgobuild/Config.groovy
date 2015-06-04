package virgobuild

import org.gradle.api.Project

class Config {

    private final Project project

    static Config on(Project project) {
        return new Config(project)
    }

    private Config(Project project) {
        this.project = project
    }

    File getVirgoBuildToolsBaseDir() {
        // to avoid configuration timing issues we don't cache the values in fields
        // @see buildship project eclipsebuild.Config
        project.hasProperty('virgoBuildToolsDir') ?
                new File(project.property('virgoBuildToolsDir') as String) :
                new File(System.getProperty('user.home'), '.tooling/virgo')
    }

    File getVirgoBuildToolsDir() {
        String virgoBuildToolsBaseName="virgo-build-tools-${Constants.virgoBuildToolsVersion}"
        new File(virgoBuildToolsBaseDir, virgoBuildToolsBaseName)
    }

    File getVirgoBuildToolsArchive() {
        new File(virgoBuildToolsDir, 'virgo-tools.zip')
    }
}
