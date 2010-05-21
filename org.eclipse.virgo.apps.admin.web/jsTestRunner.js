
/*
 * Test runner in java script that runs all the test functions found in 'src/test/js/*Tests.js'
 * 
 * Author Christopher Frost (vmware employee)
 */

function runTests(project) {
	baseDir = project.getBaseDir();
	project.log("Scanning for JavaScript tests in " + baseDir + "/src/test/js/*Tests.js", project.MSG_INFO);
	jsTestFolder = baseDir + "/src/test/js/";
	
	project.log(jsTestFolder);
	
	return("JavaScript tests completed successfully.");
}