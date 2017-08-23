#!/usr/bin/env groovy

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.*

def buildXmlFile = new File("build-kernel/build.xml")
def buildXml = loadXmlFile(buildXmlFile);
def relativeLocations = readProjectLocations(buildXml);

def cloverResultFiles = []

relativeLocations.each {
	cloverResultFiles.add(new File(buildXmlFile.parent, it + "/target/clover/clover.xml"))
}

println ""

cloverResultFiles.each {
	
	def coverage = determineCoverage(it);
	
	outputCoverage (it.parentFile.parentFile.parentFile.name, coverage);
}

def overallCoverage = determineCoverage(new File(buildXmlFile.parent, "/target/clover/clover.xml"))

println ""
outputCoverage("Overall", overallCoverage);
println ""

def loadXmlFile(File xmlFile) {
	builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
	inputStream = new FileInputStream(xmlFile)
	return builder.parse(inputStream).documentElement
}

def readProjectLocations(def buildXml) {
	def xpath = XPathFactory.newInstance().newXPath()
	def nodes = xpath.evaluate( '//path[@id="unit.test.bundles"]/pathelement', buildXml, XPathConstants.NODESET)

	def locations = []

	nodes.each {
	  locations.add(xpath.evaluate( '@location', it ))
	}
	
	return locations
}

double determineCoverage(def cloverXmlFile) {
	if (!cloverXmlFile.exists()) {
		return 0;
	}
	
	def cloverXml = loadXmlFile(cloverXmlFile);
	def xpath = XPathFactory.newInstance().newXPath()
	def metrics = xpath.evaluate( '/coverage/project/metrics', cloverXml, XPathConstants.NODESET).item(0)

	coveredElements = xpath.evaluate('@coveredelements', metrics).toDouble()
	elements = xpath.evaluate('@elements', metrics).toDouble()

	return (coveredElements / elements) * 100
}

def outputCoverage(def name, def coverage) {
	println String.format("%-48s: %5.2f%%", name, coverage)
}
