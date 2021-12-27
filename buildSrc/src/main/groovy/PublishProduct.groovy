import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class PublishProduct extends DefaultTask {

    @Input
    abstract public Property<String> getProduct();

    @Input
    abstract public Property<File> getProductFileLocation();

    @Input
    abstract public Property<File> getJavaProfileLocation();

    @TaskAction
    public void resolveLatestVersion() {
        System.out.println("TODO build " + getProduct().get());

//        File repositoryDir = project.file("${project.rootProject.projectDir}/org.eclipse.virgo.site/build/repository/")
//        internalPublishProduct(project, repositoryDir, productFileLocation)
    }
}
