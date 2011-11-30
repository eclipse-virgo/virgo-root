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
	infoBox = new InfoBox();
	dataManager = new GeminiDataSource().setUp();
}

var GeminiDataSource = function(){

	this.relationships = 'bundles';

	$('side-bar').store('scroller', new Fx.Scroll($('side-bar')));

	this.fullRequest = [{
		"mbean" : "osgi.core:type=bundleState,version=1.5",
		"operation" : "listBundles",
		"arguments" : [],
		"type" : "exec"
	},{
		"mbean" : "org.eclipse.equinox.region.domain:type=Region,*",
		"type" : "read"
	}];

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
		util.doBulkQuery(this.fullRequest, function(response) {
			this.getOverview(response[0].value, response[1].value);
			util.pageReady();
		}.bind(this)); 
		return this;
	};
		
	this.getOverview = function(rawBundles, rawRegions){
		var regionsMap = {};
		Object.each(rawRegions, function(value, key){
			value.BundleIds.each(function(id){
				regionsMap[id] = value.Name;
			});
		});
		
		var bundlesTable = new HtmlTable({ 
			properties: {'id' : 'bundle-table'}, 
			headers : ['Id', 'Name', 'Version'], 
			rows : [],
			selectable : true,
			allowMultiSelect : false,
			defaultParser : 'number',
			sortable : true,
			zebra : true
		});
		
		this.bundles = {};
		this.packages = {};
		Object.each(rawBundles, function(value, key){
			this.processPackages(key, value.ImportedPackages, value.ExportedPackages);
			this.bundles[key] = new Bundle(value.SymbolicName, value.Version, regionsMap[key], key, value.State, value.Location, this.formatHeader(value.Headers), value.Fragment, value.Hosts, value.Fragments, value.ImportedPackages, value.ExportedPackages, value.RequiredBundles, value.RequiringBundles, value.RegisteredServices, value.ServicesInUse, this.bundleClicked);
			bundlesTable.push([key, value.SymbolicName, value.Version], {'key' : key, 'id' : 'bundle-' + key});
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
	
	this.processPackages = function(id, imports, exports) {
		imports.each(function(package) {
			if(this.packages[package]){
				this.packages[package].importers.push(id);
			} else {
				var nameAndVersion = package.split(';');
				this.packages[package] = {name : nameAndVersion[0], version : nameAndVersion[1], importers : [id], exporters : []};
			}
		}, this);
		exports.each(function(package) {
			if(this.packages[package]){
				this.packages[package].exporters.push(id);
			} else {
				var nameAndVersion = package.split(';');
				this.packages[package] = {name : nameAndVersion[0], version : nameAndVersion[1], importers : [], exporters : [id]};
			}
		}, this);
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
		bundle.importedPackages.each(function(package){
			this.packages[package].exporters.each(function(exporter){
				providers.push({'bundle' : this.bundles[exporter], 'info' : this.getPackageInfo, 'infoKey' : package, 'tooltip' : 'Package: ' + package});
			}, this);
		}, this);
		bundle.exportedPackages.each(function(package){
			this.packages[package].importers.each(function(importer){
				requirers.push({'bundle' : this.bundles[importer], 'info' : this.getPackageInfo, 'infoKey' : package, 'tooltip' : 'Package: ' + package});
			}, this);
		}, this);
		return [providers, requirers];
	};
	
	this.getPackageInfo = function(packageKey) {
		return 'testing package ' + packageKey;
	};
	
	this.getServiceRelationships = function(bundleId) {
		var bundle = this.bundles[bundleId];
		var providers = [];
		var requirers = [];
		bundle.consumedServices.each(function(consumedServiceId){
			Object.each(this.bundles, function(bundleToCheck){
				if(bundleToCheck.providedServices.contains(consumedServiceId)){
					providers.push({'bundle' : bundleToCheck, 'info' : this.getServiceInfo, 'infoKey' : consumedServiceId, 'tooltip' : 'Service Id: ' + consumedServiceId});
				}
			}.bind(this));
		}, this);
		bundle.providedServices.each(function(providedServiceId){
			Object.each(this.bundles, function(bundleToCheck){
				if(bundleToCheck.consumedServices.contains(providedServiceId)){
					requirers.push({'bundle' : bundleToCheck, 'info' : this.getServiceInfo, 'infoKey' : providedServiceId, 'tooltip' : 'Service Id: ' + providedServiceId});
				}
			}.bind(this));
		}, this);
		return [providers, requirers];
	};
	
	this.getServiceInfo = function(serviceId) {
		return 'testing service ' + serviceId;
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
	
	this.relationships = {};
	
	this.shuffle = function(bundleId, newRelationships){
		Object.each(this.relationships, function(oldRelationship){
			oldRelationship.remove();
		});
		this.relationships = {};
		this.hideAll();

		console.log("In", newRelationships[0].length);
		console.log("Out", newRelationships[1].length);
		
		var widthTop = this.renderBundlesRow(this.bundles[bundleId], false, newRelationships[0], -239);
		var widthBottom = this.renderBundlesRow(this.bundles[bundleId], true, newRelationships[1], 239);
		
		var newWidth = widthTop < widthBottom ? widthBottom : widthTop;
		newWidth < 900 ? paper.setSize(900, paper.height) : paper.setSize(newWidth, paper.height);
		this.bundles[bundleId].move((paper.width/2).round(), (paper.height/2).round());
		this.bundles[bundleId].show();
		
		new Fx.Scroll($('bundle-canvas')).set((paper.width/2).round() - 450, (paper.height/2).round());

		Object.each(this.relationships, function(relationship){
			relationship.display();
		});
		
		$('display').setStyle('visibility', 'visible');
	};
	
	this.addBundle = function(bundle){
		this.bundles[bundle.id] = bundle;
	};
	
	this.removeBundle = function(bundleId){
		this.bundles[bundleId].hide();
		Object.erase(this.bundles, bundleId);
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

	this.renderBundlesRow = function(focused, isFrom, relations, offSet){
		var yPos = (paper.height/2).round() + offSet;
		var xPos = this.bundleSpacing;
		relations.each(function(relation){
			if(relation.bundle.id == focused.id){
				//Add a back link
				//console.log('Self link for focused bundle ', bundle.id);
			} else {
				if(relation.bundle.isVisible){
					//console.log('Back link for bundle ', bundle.id);
				} else {
					xPos = xPos + (relation.bundle.boxWidth/2);
					relation.bundle.move(xPos, yPos);
					relation.bundle.show();
					xPos = xPos + (relation.bundle.boxWidth/2) + this.bundleSpacing;
					if(isFrom){
						var relationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
					}else{
						var relationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
					}
					this.relationships[relationship.key] = relationship;
					focused.addRelationship(relationship);
					relation.bundle.addRelationship(relationship);
				}
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
	
	this.relationships = {};
	
	//Display attributes
	this.bundleMargin = 8;
	this.x = 5;
	this.y = 5;
	
	this.summary = function(){
		return "[" + this.id + "] " + this.name + "\n" + this.version;
	};
	
	this.text = paper.text(this.x, this.y, this.summary()).attr({
		"text-anchor" : "start", 
		"font" : "12px Arial"
	}).hide();
	
	this.boxWidth = this.text.getBBox().width.round() + 2*this.bundleMargin;
	this.boxHeight = this.text.getBBox().height.round() + 2*this.bundleMargin;
	
	this.box = paper.rect(this.x, this.y, this.boxWidth, this.boxHeight, 8).attr({
		"fill" : "#E8F6FF", 
		"stroke" : "#002F5E"
	}).hide();
	
	this.box.toBack();
	
	this.box.dblclick(function(){this.dblClickCallback(this.id);}.bind(this));
	this.text.dblclick(function(){this.dblClickCallback(this.id);}.bind(this));
	
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
		this.x = x;
		this.y = y;
		this.box.attr({
			'x' : x - (this.boxWidth/2), 
			'y' : y - (this.boxHeight/2)
		});
		this.text.attr({
			'x' : x - (this.boxWidth/2) + this.bundleMargin, 
			'y' : y
		});
		
	};

	this.addRelationship = function(relationship){
		this.relationships[relationship.key] = relationship;
	};

	this.removeRelationship = function(relationshipKey){
		Object.erase(this.relationships, relationshipKey);
	};

};

var Relationship = function(fromBundle, toBundle, infoCallback, infoKey, tooltip) {

	this.fromBundle = fromBundle;
	this.toBundle = toBundle;
	this.infoCallback = infoCallback;
	this.infoKey = infoKey;
	this.tooltip = tooltip;
	this.key = 'from' + this.fromBundle.id + 'to' + this.toBundle.id;
	this.controlPointOffset = 100;

	this.setCoordinates = function(){
		this.startPoint = (fromBundle.x) + ',' + (fromBundle.y + fromBundle.boxHeight/2); 
		this.endPoint = (toBundle.x) + ',' + (toBundle.y - toBundle.boxHeight/2 - 1);	
		this.startPointControl = (fromBundle.x) + ',' + (fromBundle.y + fromBundle.boxHeight/2 + this.controlPointOffset); 
		this.endPointControl = (toBundle.x) + ',' + (toBundle.y - toBundle.boxHeight/2 - this.controlPointOffset);
	};
	
	this.setInfoPoint = function() {
		this.halfLength = this.visual.getTotalLength()/2;
		this.midPoint = this.visual.getPointAtLength(this.halfLength);
	};
	
	this.display = function() {
		if(this.visual){
			this.visual.remove();
		}
		if(this.infoPoint){
			this.infoPoint.remove();
		}
		if(this.infoPointText){
			this.infoPointText.remove();
		}
		this.setCoordinates();
		this.visual = paper.path('M' + this.startPoint + 'C' + this.startPointControl + ',' + this.endPointControl + ',' + this.endPoint).attr({
			'arrow-end' : 'block-wide-long',
			'stroke-width' : 3,
			'stroke' : '#002F5E'
		});
		
		this.setInfoPoint();
		this.infoPoint = paper.ellipse(this.midPoint.x, this.midPoint.y, 12, 8).attr({
			'fill' : '#002F5E', 
			'stroke' : 'none',
			'title' : this.tooltip
		}).rotate(this.midPoint.alpha);
		this.infoPointText = paper.text(this.midPoint.x, this.midPoint.y, '5').attr({
			'font' : '14px Arial', 
			'stroke' : '#FFFFFF',
			'title' : this.tooltip
		}).rotate(this.midPoint.alpha - 90);
		
		this.infoPoint.click(function(){this.displayInfoBox()}.bind(this));
		this.infoPointText.click(function(){this.displayInfoBox()}.bind(this));
	};
	
	this.displayInfoBox = function() {
		infoBox.go(this.infoCallback, this.infoKey);
	};
	
	this.remove = function() {
		this.visual.remove();
		this.infoPoint.remove();
		this.infoPointText.remove();
		this.fromBundle.removeRelationship(this.key);
		this.toBundle.removeRelationship(this.key);
	};

};

/**
 * Singleton
 */
var InfoBox = function() {
	
	this.mask = new Mask($('content'), {
		'id' : 'info-box-mask'
	});
	
	this.infoElement = new Element('div#info-box').inject(this.mask.toElement());
	
	new Element('div#info-box-content').inject(this.infoElement);
	var buttonContainer = new Element('div.button-container').inject(this.infoElement);
	new Element('div.control-cap-left').inject(buttonContainer);
	var okButton = new Element('div.button').inject(new Element('div.controls').inject(buttonContainer));
	new Element('div.button-cap-left-blue').inject(okButton);
	new Element('div.button-text').appendText('OK').inject(okButton);
	new Element('div.button-cap-right-blue').inject(okButton);
	new Element('div.control-cap-right').inject(buttonContainer);
	
	okButton.addEvent('click', function() {
		this.mask.hide();
	}.bind(this));
	
	this.go = function(contentCallback, key){
		$('info-box-content').empty();
		this.mask.show();
		$('info-box-content').appendText(contentCallback(key));
	};

};
