package docbook
//
//import org.apache.fop.apps.Fop
//import org.apache.fop.apps.FopFactory
//import org.apache.fop.apps.MimeConstants
//
//import java.io.File
//
//import javax.xml.parsers.SAXParserFactory
//import javax.xml.transform.Result
//import javax.xml.transform.Source
//import javax.xml.transform.Transformer
//import javax.xml.transform.TransformerFactory
//import javax.xml.transform.sax.SAXSource
//import javax.xml.transform.sax.SAXResult
//import javax.xml.transform.stream.StreamResult
//import javax.xml.transform.stream.StreamSource
//
//import org.apache.tools.ant.filters.ReplaceTokens
//import org.apache.xml.resolver.CatalogManager
//import org.apache.xml.resolver.tools.CatalogResolver
//import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
//import org.gradle.api.tasks.OutputDirectory
//import org.gradle.api.tasks.TaskAction
//import org.xml.sax.InputSource
//import org.xml.sax.XMLReader
//
//import com.icl.saxon.TransformerFactoryImpl
//
class Docbook implements Plugin<Project> {

    public void apply(Project project) {
//        project.extensions.create("docbook", DocbookExtension)
//        project.docbook.with {
//        }
//
//        project.plugins.apply('base')
//
//        project.configurations { docbook }
//
//        project.repositories.mavenCentral()
//
//        project.dependencies {
//            docbook 'net.sf.docbook:docbook-xsl:1.78.1:resources@zip'
//            docbook 'net.sf.docbook:docbook-xml:5.0:xsd-resources@zip'
//        }
//
//        def tasks = project.tasks
//
//        def prepareDocbookResources = tasks.create("prepareDocbookResources", PrepareDocbookResources)
//        def prepareDocSources = tasks.create("prepareDocSources", PrepareDocSources)
//        prepareDocSources.dependsOn([prepareDocbookResources])
//
//        def single = tasks.create("referenceHtmlSingle", DocbookReferenceTask)
//        single.dependsOn([prepareDocSources])
//        single.description('Generates single-page HTML documentation.')
//        single.customStylesheetLocation = "html/custom.xsl"
//        single.outputFormat = 'html-single'
//        single.extension = 'html'
//
//        def multi = tasks.create("referenceHtmlMulti", DocbookReferenceTask)
//        multi.dependsOn([prepareDocSources])
//        multi.description('Generates multi-page HTML documentation.')
//        multi.customStylesheetLocation = "html-multi/custom.xsl"
//        multi.outputFormat = 'html-multi'
//        multi.extension = 'html'
//
//        def fo = tasks.create("referenceFo", DocbookReferenceTask)
//        fo.dependsOn([prepareDocSources])
//        fo.description('Generates FO intermediate documentation.')
//        fo.customStylesheetLocation = "fo/custom.xsl"
//        fo.outputFormat = 'pdf'
//        fo.extension = 'fo'
//
//        def pdf = tasks.create("referencePdf", FopTask)
//        pdf.dependsOn([fo])
//        pdf.description('Generates PDF documentation.')
//
//        def reference = tasks.create("reference") {
//            group = 'Documentation'
//            description = "Generates HTML and PDF reference documentation."
//            dependsOn([single, multi, pdf])
//            outputs.dir new File(project.buildDir, "reference")
//        }
    }
}
//
//class PrepareDocbookResources extends DefaultTask {
//
//    @OutputDirectory
//    File outputDir = new File(project.getBuildDir(), "docbook")
//
//    @TaskAction
//    public final void prepareDocbookResources() {
//        File docbookZip = project.configurations.docbook.files.find { File file -> file.name.contains('docbook-xsl-')};
//
//        project.copy {
//            from project.zipTree(docbookZip)
//            include 'docbook/**'
//            into project.getBuildDir()
//        }
//    }
//}
//
//class PrepareDocSources extends DefaultTask {
//
//    @OutputDirectory
//    File outputDir = new File(project.getBuildDir(), "work")
//
//    @TaskAction
//    public final void prepareDocSources() {
//        def version = project.docbook.version
//        def tokenMap = [
//            'project.name': 'Virgo',
//            'runtime.category': 'EclipseRT',
//            'umbrella.product.name': 'Virgo Runtime Environment',
//            'umbrella.product.name.short': 'Virgo Runtime',
//            'tomcat.product.name': 'Virgo Server for Apache Tomcat',
//            'tomcat.product.name.short': 'VTS',
//            'jetty.product.name': 'Virgo Jetty Server',
//            'jetty.product.name.short': 'VJS',
//            'kernel.product.name': 'Virgo Kernel',
//            'kernel.product.name.short': 'VK',
//            'nano.product.name': 'Virgo Nano',
//            'nano.product.name.short': 'VN',
//            'nanoweb.product.name': 'Virgo Nano Web',
//            'snaps.product.name': 'Virgo Snaps',
//            'snaps.product.name.short': 'VS',
//            'ebr': 'SpringSource Enterprise Bundle Repository',
//            'p2repo': "http://download.eclipse.org/virgo/updatesite/${version}".toString(),
//            'bundle.version': "${version}".toString(),
//        ]
//
//        project.copy {
//            from project.file(project.docbook.docSourceDirName)
//            into outputDir
//            filter(ReplaceTokens, tokens: tokenMap)
//        }
//        // copy DocBook images and...
//        project.copy {
//            from "${project.buildDir}/docbook/images"
//            into new File(outputDir, 'images')
//        }
//        // project images.
//        project.copy {
//            from project.file(project.docbook.imageSourceDirName)
//            into new File(outputDir, 'images')
//        }
//    }
//}
//
//class DocbookReferenceTask extends DefaultTask {
//
//    File inputDir
//
//    File outputDir
//
//    String customStylesheetLocation
//
//    String outputFormat
//
//    String extension
//
//    @OutputDirectory
//    File outputBaseDir = new File(project.getBuildDir(), "reference")
//
//    @TaskAction
//    public final void transform() {
//        inputDir = new File(project.buildDir, 'work')
//        outputDir = new File(outputBaseDir, outputFormat)
//
//        SAXParserFactory factory = new org.apache.xerces.jaxp.SAXParserFactoryImpl()
//        factory.setXIncludeAware(true)
//
//        CatalogResolver resolver = new CatalogResolver(createCatalogManager())
//
//        TransformerFactory transformerFactory = new TransformerFactoryImpl()
//        transformerFactory.setURIResolver(resolver)
//
//        File stylesheetFile = new File(inputDir, "styles/${customStylesheetLocation}")
//        URL url = stylesheetFile.toURI().toURL()
//
//        Transformer transformer = transformerFactory.newTransformer(new StreamSource(url.openStream(), url.toExternalForm()))
//        transformer.setParameter("highlight.source", "1")
//        transformer.setParameter("highlight.xslthl.config", new File("${project.buildDir}/docbook/highlighting", "xslthl-config.xml").toURI().toURL())
//        transformer.setParameter("root.filename", project.docbook.baseName)
//        transformer.setParameter("base.dir", outputDir)
//
//        File inputFile = new File(inputDir, "${project.docbook.baseName}.xml")
//        File outputFile = new File(outputDir, "${project.docbook.baseName}.${extension}")
//
//        XMLReader reader = factory.newSAXParser().getXMLReader()
//        reader.setEntityResolver(resolver)
//
//        transformer.transform(new SAXSource(reader, new InputSource(inputFile.getAbsolutePath())), new StreamResult(outputFile.getAbsolutePath()))
//
//        project.copy {
//            from "${inputDir}/images"
//            into new File(outputDir, "images")
//        }
//        project.copy {
//            from "${inputDir}/css"
//            into new File(outputDir, "css")
//        }
//    }
//
//    private CatalogManager createCatalogManager() {
//        CatalogManager manager = new CatalogManager()
//        manager.setIgnoreMissingProperties(true)
//        ClassLoader classLoader = this.getClass().getClassLoader()
//        URL docbookCatalog = classLoader.getResource("docbook/catalog.xml")
//        manager.setCatalogFiles(docbookCatalog.toExternalForm())
//        return manager
//    }
//}
//
//class FopTask extends DefaultTask {
//
//    File inputDir = new File(project.getBuildDir(), "reference/pdf")
//
//    @TaskAction
//    public final void foToPdf() {
//        File foFile = new File(project.getBuildDir(), "reference/pdf/${project.docbook.baseName}.fo")
//
//        project.copy {
//            from new File(project.buildDir, "work/images")
//            into new File(inputDir, 'images')
//        }
//
//        FopFactory fopFactory = FopFactory.newInstance()
//        fopFactory.setBaseURL(inputDir.toURI().toURL().toExternalForm())
//
//        final File pdfFile = new File(foFile.parent, "${project.docbook.baseName}.pdf")
//
//        logger.debug("Transforming 'fo' file " + foFile + " to PDF: " + pdfFile)
//
//        OutputStream out = null
//        try {
//            out = new BufferedOutputStream(new FileOutputStream(pdfFile))
//
//            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out)
//
//            TransformerFactory factory = TransformerFactory.newInstance()
//            Transformer transformer = factory.newTransformer()
//
//            Source src = new StreamSource(foFile)
//            src.setSystemId(foFile.toURI().toURL().toExternalForm())
//
//            Result res = new SAXResult(fop.getDefaultHandler())
//            transformer.transform(src, res)
//        } finally {
//            if (out != null) {
//                out.close()
//            }
//        }
//
//        if (!foFile.delete()) {
//            logger.warn("Failed to delete temporary 'fo' file " + foFile)
//        }
//        if (!project.delete(new File(inputDir, 'images'))) {
//            logger.warn("Failed to delete 'images' path " + new File(inputDir, 'images'))
//        }
//        if (!project.delete(new File(inputDir, 'css'))) {
//            logger.warn("Failed to delete 'css' path " + new File(inputDir, 'css'))
//        }
//    }
//}
