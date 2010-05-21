
function testLocalStuff(bundles) {
	return ("No tests yet");
}

function runTests(unitTestBundles) {
	baseDir = buildServlet.getBaseDir();
	for(var i = 0; i < unitTestBundles.length; i++){
		buildServlet.log(unitTestBundles[i] + "/n", buildServlet.MSG_INFO);
	}
	return(baseDir.toString() + "/src/test/js/*.Tests.js " + unitTestBundles.toString());
}