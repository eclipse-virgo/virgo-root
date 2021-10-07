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

    static File getEclipsePhotonDir() {
        new File(System.getProperty('user.home'), '.gradle/eclipse-photon-sdk/eclipse')
    }

    static File getEclipsePhotonSdkArchive() {
        new File(System.getProperty('user.home'), '.gradle/eclipse-java-photon-R-linux-gtk-x86_64.tar.gz')
    }

    File getEquinoxLauncherJar() {
        new File(eclipsePhotonDir.path, '/plugins').listFiles().find { it.name.startsWith('org.eclipse.equinox.launcher_') }
    }

    // used by installProduct - old virgo-tools' p2.director simply isn't able to install products properly anymore :(
    static File getEclipsePhotonLauncherJar() {
        new File(eclipsePhotonDir.path, '/plugins').listFiles().find { it.name.startsWith('org.eclipse.equinox.launcher_') }
    }

    static File getJarProcessorJar() {
        new File(eclipsePhotonDir.path, '/plugins').listFiles().find { it.name.startsWith('org.eclipse.equinox.p2.jarprocessor_') }
    }
}
