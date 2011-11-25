/*******************************************************************************
* Copyright (c) 2011 David Normiongton
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Chris Frost - initial contribution
*   David Normington
*   
*******************************************************************************/

function pageinit() {
	util.loadScript('raphael', false);
	var width = 900;
	var height = 551;
	$('bundle-canvas').setStyles({'width' : width, 'height' : height + 18});
	paper = Raphael("bundle-canvas", width, height);
	dataManager = new GeminiDataSource();
	dataManager.setUp();
	util.pageReady();
}

var GeminiDataSource = function(){

	this.relationships = 'bundles';

	$('side-bar').store('scroller', new Fx.Scroll($('side-bar')));

	this.display = function(type){
		if(type == 'bundles') {
			$('view-bundles-button').addClass('button-selected');
			$('view-services-button').removeClass('button-selected');
			this.relationships = type;
			var currentRow = $('bundle-table').retrieve('HtmlTable').getSelected()[0];
			if(currentRow){
				$('bundle-table').retrieve('HtmlTable').selectNone();
				$('bundle-table').retrieve('HtmlTable').selectRow(currentRow);
			}
		} else if(type == 'services'){
			$('view-services-button').addClass('button-selected');
			$('view-bundles-button').removeClass('button-selected');
			this.relationships = type;
			var currentRow = $('bundle-table').retrieve('HtmlTable').getSelected()[0];
			if(currentRow){
				$('bundle-table').retrieve('HtmlTable').selectNone();
				$('bundle-table').retrieve('HtmlTable').selectRow(currentRow);
			}
		}
	};
	
	this.setUp = function(){
		new Request.JSON({
			url : util.getCurrentHost() + "/jolokia/exec/osgi.core:type=bundleState,version=1.5/listBundles",
			method : 'get',
			onSuccess : function(responseJSON) {
				this.getRegions(responseJSON.value);
			}.bind(this)
		}).send();
	};
	
	//Start private methods

	this.getRegions = function(bundles){
		new Request.JSON({
			url : util.getCurrentHost() + "/jolokia/read/org.eclipse.equinox.region.domain:type=Region,*",
			method : 'get',
			onSuccess : function(responseJSON) {
				this.getOverview(bundles, responseJSON.value);
			}.bind(this)
		}).send();
	};
	
	this.getOverview = function(rawBundles, regions){
		var regionsMap = {};
		Object.each(regions, function(value, key){
			value.BundleIds.each(function(id){
				regionsMap[id] = value.Name;
			});
		});
		
		var bundlesTable = new HtmlTable({ 
			properties: {'id' : 'bundle-table'}, 
			headers : ['Id', 'Name', 'Version', 'State'], 
			rows : [],
			selectable : true,
			allowMultiSelect : false,
			defaultParser : 'number',
			sortable : true,
			zebra : true
		});
		
		this.bundles = {};
		Object.each(rawBundles, function(value, key){
			this.bundles[key] = new Bundle(value.SymbolicName, value.Version, regionsMap[key], key, value.State, value.Location, this.formatHeader(value.Headers), value.Fragment, value.Hosts, value.Fragments, value.ImportedPackages, value.ExportedPackages, value.RequiredBundles, value.RequiringBundles, value.RegisteredServices, value.ServicesInUse, this.bundleClicked);
			bundlesTable.push([key, value.SymbolicName, value.Version, value.State], {'key' : key, 'id' : 'bundle-' + key});
		}.bind(this));

		this.layout = new Layout(this.bundles);

		bundlesTable.addEvent('rowFocus', function(tr){
			if(this.relationships == 'bundles'){
				this.layout.shuffle(tr.getProperty('key'), this.getBundleRelationships(tr.getProperty('key')));
			} else if(this.relationships == 'services') {	
				this.layout.shuffle(tr.getProperty('key'), this.getServiceRelationships(tr.getProperty('key')));
			}
		}.bind(this));
		
		$('side-bar').empty();
		bundlesTable.inject($('side-bar'));
	};
	
	this.formatHeader = function(rawHeaders){
		var result = {};
		Object.each(rawHeaders, function(header){
			result[header.Key] = header.Value;
		});
		return result;
	};
	
	this.bundleClicked = function(bundleId){
		console.log('Scrolling table to', $('bundle-' + bundleId));
		$('side-bar').retrieve('scroller').toElementCenter($('bundle-' + bundleId), 'y');
		$('bundle-table').retrieve('HtmlTable').selectNone();
		$('bundle-table').retrieve('HtmlTable').selectRow($('bundle-' + bundleId));
	};
	
	this.getBundleRelationships = function(bundleId){
		var bundle = this.bundles[bundleId];
		var providers = [];
		var requirers = [];
	
		return [providers, requirers];
	};
	
	this.getServiceRelationships = function(bundleId) {
		var bundle = this.bundles[bundleId];
		var providers = [];
		var requirers = [];
		bundle.providedServices.each(function(providedServiceId){
			Object.each(this.bundles, function(bundleToCheck){
				if(bundleToCheck.consumedServices.contains(providedServiceId)){
					requirers.push(bundleToCheck);
				}
			}.bind(this));
		}, this);
		bundle.consumedServices.each(function(consumedServiceId){
			Object.each(this.bundles, function(bundleToCheck){
				if(bundleToCheck.providedServices.contains(consumedServiceId)){
					providers.push(bundleToCheck);
				}
			}.bind(this));
		}, this);
		return [providers, requirers];
	};

};

/**
 *
 * Take a map of bundles to use for generating the display.
 *
 */
var Layout = function(bundles){

	this.bundles = bundles;
	
	this.bundleSpacing = 10; //Pixels to leave between bundles when rendering
	
	this.shuffle = function(bundleId, relationships){
		this.hideAll();

		console.log("In", relationships[0].length);
		console.log("Out", relationships[1].length);
		
		var widthTop = this.renderBundlesRow(bundleId, relationships[0], -239);
		var widthBottom = this.renderBundlesRow(bundleId, relationships[1], 239);
		
		var newWidth = widthTop < widthBottom ? widthBottom : widthTop;
		newWidth < 900 ? paper.setSize(900, paper.height) : paper.setSize(newWidth, paper.height);
		this.bundles[bundleId].move((paper.width/2).round(), (paper.height/2).round());
		this.bundles[bundleId].show();
		
		new Fx.Scroll($('bundle-canvas')).set((paper.width/2).round() - 450, (paper.height/2).round());
		
		$('display').setStyle('visibility', 'visible');
	};
	
	this.addBundle = function(bundle){
		this.bundles[bundle.id] = bundle;
	};
	
	this.removeBundle = function(bundleId){
		this.bundles[bundleId].hide();
		this.bundles[bundleId] = undefined;
	};
	
	this.empty = function(){
		this.hideAll();
		this.bundles = {};
	};
	
	this.hide = function(bundleId){
		this.bundles[bundleId].hide();
	};
	
	this.hideAll = function(){
		Object.each(this.bundles, function(value){
			value.hide();
		});
	};

	this.renderBundlesRow = function(focused, inBundles, offSet){
		var yPos = (paper.height/2).round() + offSet;
		var xPos = this.bundleSpacing;
		inBundles.each(function(bundle){
			if(bundle.isVisible){
				//Add a back link
			
			} else if(bundle.id != focused.id){
				xPos = xPos + (bundle.boxWidth/2);
				bundle.move(xPos, yPos);
				bundle.show();
				xPos = xPos + (bundle.boxWidth/2) + this.bundleSpacing;
			}
		}, this);
		return xPos;
	};
	
};

/**
 * this.name - String
 * this.version -String
 * this.region - String
 * this.id - Number
 * this.state - String;
 * this.location - String
 * this.headers - Object of key<String> value<String> pairs 
 * this.isFragment - boolean
 * this.hosts - array of bundlesIds
 * this.fragments = array of bundleIds
 * this.importedPackages - array of Strings 'packageName;packageVersion'
 * this.exportedPackages - array of Strings 'packageName;packageVersion'
 * this.requiredBundles - array of bundleIds
 * this.requiringBundles - array of bundleIds
 * this.providedServices - array of serviceIds
 * this.consumedServices - array of serviceIds
 */
var Bundle = function(name, version, region, id, state, location, headers, isFragment, hosts, fragments, importedPackages, exportedPackages, requiredBundles, requiringBundles, providedServices, consumedServices, dblClickCallback){

	//Data about the bundle
	this.name = name;
	this.version = version;
	this.region = region;
	this.id = id;
	this.state = state;
	this.location = location;
	this.headers = headers; 
	this.isFragment = isFragment;
	this.hosts = hosts;
	this.fragments = fragments;
	this.importedPackages = importedPackages;
	this.exportedPackages = exportedPackages;
	this.requiredBundles = requiredBundles;
	this.requiringBundles = requiringBundles;
	this.providedServices = providedServices;
	this.consumedServices = consumedServices;
	this.dblClickCallback = dblClickCallback;
	
	this.isVisible = false; 
	
	//Display attributes
	this.bundleMargin = 8;
	this.x = 5;
	this.y = 5;
	
	this.summary = function(){
		return "[" + this.id + "] " + this.name + " " + this.version + " " + this.state;
	};
	
	this.text = paper.text(this.x + this.bundleMargin, this.y + this.bundleMargin + 8, this.summary()).attr({
		"text-anchor" : "start", 
		"font" : "12px Arial"
	}).hide();
	
	this.boxWidth = this.text.getBBox().width.round() + 2*this.bundleMargin;
	this.boxHeight = this.text.getBBox().height.round() + 2*this.bundleMargin;
	
	this.box = paper.rect(this.x, this.y, this.boxWidth, this.boxHeight, 8).attr({
		"fill" : "90-#dfdfdf-#fff", 
		"stroke" : "#002F5E"
	}).hide();
	
	this.box.toBack();
	
	this.box.dblclick(function(){
		this.dblClickCallback(this.id);
	}.bind(this));
	this.text.dblclick(function(){
		this.dblClickCallback(this.id);
	}.bind(this));
	
	this.hide = function(){
		this.text.hide();
		this.box.hide();
		this.isVisible = false; 
	};
	
	this.show = function(){
		this.text.show();
		this.box.show();
		this.isVisible = true; 
	};
	
	this.move = function(x, y) {
		//console.log('Moving bundle to ' + x + ', ' + y);
		this.box.attr({
			'x' : x - (this.boxWidth/2), 
			'y' : y - (this.boxHeight/2)
		});
		this.text.attr({
			'x' : x - (this.boxWidth/2) + this.bundleMargin, 
			'y' : y - (this.boxHeight/2) + this.bundleMargin + 8
		});
	};

	//Start private methods

};

/**
 * The main Explorer class for bundle creation and assigning event handlers.
 * 
 * Draws the initial grid of bundles
 */
var Explorer = {
	/**
	 * The initiation function. Creates the canvas, and requests JSON from server.
	 */
	init : function() {
		this.canvas = Raphael("expl_canvas", "100px", "500px");
		this.tip = {
			'set' : false,
			'div' : null
		};
		this.jsonRequest = new Request.JSON({
			url : util.getCurrentHost() + "/jolokia/exec/osgi.core:type=bundleState,version=1.5/listBundles",
			method : 'get',
			onSuccess : function(responseJSON, responseText) {
				Explorer.bundleConfig(responseJSON);
			}
		}).send();
	},

	/**
	 * Takes URL parameter if there is one and auto-loads that bundle
	 */
	go : function() {
		var loc = window.location.href;
		if (loc.indexOf("#") != -1) {
			var key = loc.split("#")[1];
			var keyNotExist = Object.every(this.bundles, function(objValue, objKey) {
				return objValue.key != key;
			});
			if (!keyNotExist) {
				new LayoutManager(this.bundles, key, this.arrows);
			} else {
				alert("That Bundle Id doesn't exist.");
			}
		}
	},

	/**
	 * Main success handler for the JSON response
	 * 
	 * @param json (JSON Object) - Raw JSON of bundle information
	 */
	bundleConfig : function(responseJson) {
		var kernelBundles, userBundles;
		new Request.JSON({
			url : util.getCurrentHost() + "/jolokia/read/org.eclipse.equinox.region.domain:type=Region,*",
			method : 'get',
			onSuccess : function(JSON) {
				Object.each(JSON.value, function(value, key) {
					key.indexOf("kernel") != -1 ? kernelBundles = value.BundleIds : userBundles = value.BundleIds;
				});
				Explorer.createBundleOverview(responseJson.value, kernelBundles, userBundles);
			}
		}).send();
	},

	/**
	 * Gets the coordinates for the next bundle
	 * 
	 * @param i (Number) - The iterator of the bundles loop
	 * @returns (Object) - The x and y coordinates
	 */
	getNextCoords : function(i) {
		if (i == 0) {
			this.xRefactor = 0;
			this.yRefactor = 0;
		}
		var xColWidth = 360, yColHeight = 450;

		// Re-factoring is to set the new x & y values for a new column
		i > 0 && i % 15 == 0 ? this.xRefactor = this.xRefactor + xColWidth : this.xRefactor;
		i > 0 && i % 15 == 0 ? this.yRefactor = this.yRefactor - yColHeight : this.yRefactor;

		// Set x & y coordinates
		var x = 3 + this.xRefactor;
		var y = (i * 30) + this.yRefactor;

		return {
			"x" : x,
			"y" : y
		};
	},

	/**
	 * Sets the bundles region
	 * 
	 * @param kernelIds - List of kernel bundle Ids
	 * @param userIds - List of user bundle Ids
	 * @param bundle (Object) - A single bundle objext
	 * @returns (Object) - returns the bundle
	 */
	setBundleRegion : function(kernelIds, userIds, bundle) {
		if (Object.contains(kernelIds, bundle.Identifier)) {
			bundle.Region = "Kernel";
		} else if (Object.contains(userIds, bundle.Identifier)) {
			bundle.Region = "User";
		} else {
			bundle.Region = "";
		}

		return bundle;
	},

	/**
	 * Creates the starting Overview perspective.
	 * 
	 * @param bundleObj (Object) - Main JS object from JSON
	 */
	createBundleOverview : function(bundleObj, kernelIds, userIds) {
		var i = 0;
		this.arrows = new Object();
		this.bundles = new Object();
		this.bundles.widest = 0;
		Object.each(bundleObj, function(value, key) {
			var coords = this.getNextCoords(i);
			var bundle = this.setBundleRegion(kernelIds, userIds, value);

			this.bundles[key] = {
				"key" : key,
				"visual" : this.createBundle(bundle, coords['x'], coords['y']),
				"bundle" : bundle,
				"service" : {
					"serviceState" : "",
					"properties" : null
				}
			};
			
			this.bundles.lastAdded = this.bundles[key];
			if(this.bundles[key].visual.rect.attrs.width > this.bundles.widest){
				this.bundles.widest = this.bundles[key].visual.rect.attrs.width;
			}

			// Add tooltip
			var tooltipTxt =  "<strong>Identifier:</strong> " + bundle.Identifier + "<br />"
							+ "<strong>SymbolicName:</strong> " + bundle.SymbolicName + "<br />"
							+ "<strong>Version:</strong> " + bundle.Version + "<br />"
							+ "<strong>Region:</strong> " + bundle.Region + " <br />"
							+ "<strong>State:</strong> " + bundle.State + "<br />"
							+ "<strong>Location:</strong> " + bundle.Location + "<br />";
			util.tooltip(this.bundles[key].visual['text'].node, tooltipTxt);

			// Add event handlers
			this.bundles[key].visual['set'].dblclick(function() {
				new LayoutManager(this.bundles, key, this.arrows);
			}, this);

			i++;
		}, this);

		this.resizeCanvas();
		this.go();
	},

	/**
	 * Resizes canvas depending on width of last bundle (inc. col width) For overview only.
	 */
	resizeCanvas : function() {
		if (Object.getLength(this.bundles) > 0){
			var lastBox = this.bundles.lastAdded.visual['set'].getBBox();
			this.canvas.setSize(lastBox.x + this.bundles.widest + 10, this.canvas.height);
		}
	},

	/**
	 * Creates and returns the Raphael set of the bundle
	 * 
	 * @param bundle (Object) - JS object of bundle info
	 * @param x (Number) - x coordinate
	 * @param y (Number) - y coordinate
	 * @returns (Object) - The raphael set and separate rect element
	 */
	createBundle : function(bundle, x, y) {
		var bundleBgHeight = 25, xMargin = 10;

		// Set up raphael set
		var st = this.canvas.set();

		// Add to raphael set, rect and text
		var rect = this.createBundleBg(x, y, 1, bundleBgHeight, 5);
		var text = this.createTextObj(x, xMargin, y, bundleBgHeight / 2, this.getBundleText(bundle));
		rect.attr("width", text.getBBox().width + 2 * xMargin);

		st.push(text, rect);

		return {
			'set' : st,
			'rect' : rect,
			'text' : text
		};
	},

	/**
	 * Creates and returns the Raphael object of a rect
	 * 
	 * @param x (Number) - x coordinate
	 * @param y (Number) - y coordinate
	 * @param width (Number) - width in pixels
	 * @param height (Number) - height in pixels
	 * @param cornerRadius (Number) - Radius for rounded corner
	 * @returns (Raphael Object) - a rect with a fill and stroke
	 */
	createBundleBg : function(x, y, width, height, cornerRadius) {
		return this.canvas.rect(x, y, width, height, cornerRadius).attr({
			fill : "90-#dfdfdf-#fff",
			stroke : "#ccc"
		});
	},

	/**
	 * Creates the text node and returns the Raphael object
	 * 
	 * @param x (Number) - The x coordinate to position at
	 * @param xMargin (Number) - The x offset
	 * @param y (Number) - The y coordinate to position at
	 * @param yMargin (Number) - The y offset
	 * @param text (String) - The text to add to the element
	 * @returns (Raphael Object)
	 */
	createTextObj : function(x, xMargin, y, yMargin, text) {
		return this.canvas.text(x + xMargin, y + yMargin, text).attr({
			"text-anchor" : "start",
			"font" : "10px Arial"
		});
	},

	/**
	 * @param bundle (object) - JS object from JSON of single bundle
	 * @return (string) - The full bundle name of SymbolicName + Version + ID
	 */
	getBundleText : function(bundle) {
		return bundle.SymbolicName + " " + bundle.Version + " " + bundle.Identifier;
	}
};