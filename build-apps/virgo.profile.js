// How to build the dojo libary
// author Christopher Frost (vmware employee)

// The virgo dojo build is run by calling:
// ./build.sh profile=virgo action=clean,release
// from the /util/buildscripts directory of dojo source

dependencies = {
	stripConsole: "normal",

	layers: [
		{
			name: "virgo-dojo.js",
			dependencies: [
				"dijit.TitlePane",
				"dijit.tree.ForestStoreModel",
				"dijit.Tree",
				"dojox.data.QueryReadStore",
				"dijit.form.Button"
			]
		}
	],

	prefixes: [
		[ "dijit", "../dijit" ],
		[ "dojox", "../dojox" ]
	]
}
// After running this to produce the required build layers the following can be deleted.
// ./dojox
// much more can be deleted but there is no need to do so as it will only mean extra work adding them back in if these features are required later.