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
		"operation" : "listBundles()",
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
		imports.each(function(packageKey) {
			if(this.packages[packageKey]){
				this.packages[packageKey].importers.push(id);
			} else {
				var nameAndVersion = packageKey.split(';');
				this.packages[packageKey] = {name : nameAndVersion[0], version : nameAndVersion[1], importers : [id], exporters : []};
			}
		}, this);
		exports.each(function(packageKey) {
			if(this.packages[packageKey]){
				this.packages[packageKey].exporters.push(id);
			} else {
				var nameAndVersion = packageKey.split(';');
				this.packages[packageKey] = {name : nameAndVersion[0], version : nameAndVersion[1], importers : [], exporters : [id]};
			}
		}, this);
	};
	
	this.bundleClicked = function(bundleId){
		console.log('Scrolling table to', $('bundle-' + bundleId));
		$('side-bar').retrieve('scroller').toElementCenter($('bundle-' + bundleId), 'y');
		$('bundle-table').retrieve('HtmlTable').selectNone();
		$('bundle-table').retrieve('HtmlTable').selectRow($('bundle-' + bundleId));
	};
	
	// ****** BUNDLES VIEW ****** //
	
	this.getBundleRelationships = function(bundleId){
		var bundle = this.bundles[bundleId];
		var providers = [];
		var requirers = [];
		bundle.importedPackages.each(function(packageKey){
			this.packages[packageKey].exporters.each(function(exporter){
				providers.push({'bundle' : this.bundles[exporter], 'info' : this.getPackageInfo, 'infoKey' : packageKey, 'tooltip' : 'Package: ' + packageKey});
			}, this);
		}, this);
		bundle.exportedPackages.each(function(packageKey){
			this.packages[packageKey].importers.each(function(importer){
				requirers.push({'bundle' : this.bundles[importer], 'info' : this.getPackageInfo, 'infoKey' : packageKey, 'tooltip' : 'Package: ' + packageKey});
			}, this);
		}, this);
		return [providers, requirers];
	};
	
	this.getPackageInfo = function(packageKey, callBack) {
		var nameAndVersion = packageKey.split(';');
		var packageRequest = [{
			"mbean" : "osgi.core:type=packageState,version=1.5",
			"operation" : "isRemovalPending",
			"arguments" : [nameAndVersion[0], nameAndVersion[1]],
			"type" : "exec"
		},{
			"mbean" : "osgi.core:type=packageState,version=1.5",
			"operation" : "getImportingBundles",
			"arguments" : [nameAndVersion[0], nameAndVersion[1]],
			"type" : "exec"
		},{
			"mbean" : "osgi.core:type=packageState,version=1.5",
			"operation" : "getExportingBundles",
			"arguments" : [nameAndVersion[0], nameAndVersion[1]],
			"type" : "exec"
		}];
		util.doBulkQuery(packageRequest, function(response) {
		console.log(this.packages);
		console.log(response);
			callBack([response[0].value, response[1].value, response[2].value]);
		}.bind(this)); 
	}.bind(this);
	
	// ****** SERVICES VIEW ****** //
	
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
	
	this.getServiceInfo = function(serviceId, callBack) {
		var serviceRequest = [{
			"mbean" : "osgi.core:type=serviceState,version=1.5",
			"operation" : "getProperties",
			"arguments" : [serviceId],
			"type" : "exec"
		},{
			"mbean" : "osgi.core:type=serviceState,version=1.5",
			"operation" : "getUsingBundles",
			"arguments" : [serviceId],
			"type" : "exec"
		},{
			"mbean" : "osgi.core:type=serviceState,version=1.5",
			"operation" : "getObjectClass",
			"arguments" : [serviceId],
			"type" : "exec"
		},{
			"mbean" : "osgi.core:type=serviceState,version=1.5",
			"operation" : "getBundleIdentifier",
			"arguments" : [serviceId],
			"type" : "exec"
		}];
		util.doBulkQuery(serviceRequest, function(response) {
			var content = $('ul.infoContent');
			$('li').inject(content).appendText('Service provided by bundle: ' + this.bundles[response[3].value].summary());
			$('li').inject(content).appendText('Objectclass: ' + response[2].value);
			$('li').inject(content).appendText('Properties');
			var propertiesList = $('ul.infoContent').inject(content);
			Object.each(response[0].value, function(property){
				$('li').inject(propertiesList).appendText(property.Key + ' - ' + property.Value);
			});
			$('li').inject(content).appendText('Consumers');
			var consumersList = $('ul.infoContent').inject(content);
			response[1].value.each(function(bundleId){
				$('li').inject(consumersList).appendText(this.bundles[bundleId].summary());
			}.bind(this));
			callBack(content);
		}.bind(this)); 
	}.bind(this);

};

/**
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
				var relationship = new SelfRelationship(focused, relation.info, relation.infoKey, relation.tooltip);
				this.relationships[relationship.key] = relationship;
				focused.addRelationship(relationship);
			} else {
				if(relation.bundle.isVisible){
					if(isFrom){
						var relationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
					}else{
						var relationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
					}
					var existingRelationship = this.relationships[relationship.key];
					if(existingRelationship){
						existingRelationship.increaseCount(relation.tooltip);
					} else {
						console.log('Extra', isFrom, relation.tooltip);
						
						if(isFrom){
							var backRelationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
						}else{
							var backRelationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
						}
						
						
						this.relationships[backRelationship.key] = backRelationship;
						focused.addRelationship(backRelationship);
						relation.bundle.addRelationship(backRelationship);
					}
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
 * Bundle
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
	this.count = 1;
	this.key = 'from' + this.fromBundle.id + 'to' + this.toBundle.id;
	this.controlPointOffset = 100;

	this.setCoordinates = function(){
		this.startPoint = {'x' : fromBundle.x, 'y' : fromBundle.y + fromBundle.boxHeight/2};
		this.endPoint = {'x' : toBundle.x, 'y' : toBundle.y - toBundle.boxHeight/2};
		this.startPointControl = {'x' : this.startPoint.x, 'y' : this.startPoint.y + this.controlPointOffset}; 
		this.endPointControl = {'x' : this.endPoint.x, 'y' : this.endPoint.y - this.controlPointOffset};
		this.midPoint = this.calculateMidpoint(this.startPoint.x, this.startPoint.y, this.endPoint.x, this.endPoint.y); 
	};
	
	this.calculateMidpoint = function(startX, startY, endX, endY){
		if(startX < endX){
			var midX = startX + (endX - startX)/2;
		} else {
			var midX = endX + (startX - endX)/2;
		}
		if(startY < endY){
			var midY = startY + (endY - startY)/2;
		} else {
			var midY = endY + (startY - endY)/2;
		}
		return {'x' : midX, 'y' : midY};
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
		this.visual = paper.path('M' + this.startPoint.x + ',' + this.startPoint.y + 
									'C' + this.startPointControl.x + ',' + this.startPointControl.y + 
									',' + this.endPointControl.x + ',' + this.endPointControl.y + 
									',' + this.endPoint.x + ',' + this.endPoint.y).attr({
			'arrow-end' : 'block-wide-long',
			'stroke-width' : 3,
			'stroke' : '#002F5E'
		}).toBack();
		this.infoPoint = paper.circle(this.midPoint.x, this.midPoint.y, 10).attr({
			'fill' : '#BAD9EC', 
			'stroke' : 'none',
			'title' : this.tooltip
		});
		this.infoPointText = paper.text(this.midPoint.x, this.midPoint.y, this.count).attr({
			'font' : '13px Arial', 
			'stroke' : '#002F5E',
			'title' : this.tooltip
		});
		
		this.infoPoint.click(function(){this.displayInfoBox()}.bind(this));
		this.infoPointText.click(function(){this.displayInfoBox()}.bind(this));
		
		this.infoPoint.hover(function(){this.glow = this.visual.glow()}, function(){this.glow.remove()}, this, this);
		this.infoPointText.hover(function(){this.glow = this.visual.glow()}, function(){this.glow.remove()}, this, this);
	};
	
	this.displayInfoBox = function() {
		infoBox.go(this.infoCallback, this.infoKey);
	};
	
	this.increaseCount = function(tooltip) {
		this.count = this.count + 1;
		this.tooltip = this.tooltip + ' ' + tooltip;
		if(this.infoPointText){
			this.infoPointText.attr({
				'text' : this.count
			});
		};
	};
	
	this.remove = function() {
		this.visual.remove();
		this.infoPoint.remove();
		this.infoPointText.remove();
		this.fromBundle.removeRelationship(this.key);
		this.toBundle.removeRelationship(this.key);
	};

};

var SelfRelationship = function(bundle, infoCallback, infoKey, tooltip) {
	
	this.bundle = bundle;
	this.infoCallback = infoCallback;
	this.infoKey = infoKey;
	this.tooltip = tooltip;
	this.count = 1;
	this.key = 'self' + this.bundle.id;
	this.offset = 10;
	this.offset2 = 100;
	
	this.setCoordinates = function(){
		this.startPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2, 'y' : this.bundle.y - this.offset};
		this.endPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2, 'y' : this.bundle.y + this.offset};
		
		this.startPointControl = {'x' : this.startPoint.x + this.offset2, 'y' : this.startPoint.y - this.offset2}; 
		this.endPointControl = {'x' : this.endPoint.x + this.offset2, 'y' : this.endPoint.y + this.offset2};
		
		this.midPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2 + this.offset2*0.74, 'y' : this.bundle.y}; 
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
		this.visual = paper.path('M' + this.startPoint.x + ',' + this.startPoint.y + 
									'C' + this.startPointControl.x + ',' + this.startPointControl.y + 
									',' + this.endPointControl.x + ',' + this.endPointControl.y + 
									',' + this.endPoint.x + ',' + this.endPoint.y).attr({
			'arrow-end' : 'block-wide-long',
			'stroke-width' : 3,
			'stroke' : '#002F5E'
		}).toBack();
		this.infoPoint = paper.circle(this.midPoint.x, this.midPoint.y, 10).attr({
			'fill' : '#BAD9EC', 
			'stroke' : 'none',
			'title' : this.tooltip
		});
		this.infoPointText = paper.text(this.midPoint.x, this.midPoint.y, this.count).attr({
			'font' : '13px Arial', 
			'stroke' : '#002F5E',
			'title' : this.tooltip
		});
		
		this.infoPoint.click(function(){this.displayInfoBox()}.bind(this));
		this.infoPointText.click(function(){this.displayInfoBox()}.bind(this));
		
		this.infoPoint.hover(function(){this.glow = this.visual.glow()}, function(){this.glow.remove()}, this, this);
		this.infoPointText.hover(function(){this.glow = this.visual.glow()}, function(){this.glow.remove()}, this, this);
		
	};
	
	this.increaseCount = function(tooltip) {
		this.count = this.count + 1;
		this.tooltip = this.tooltip + ' ' + tooltip;
		if(this.infoPointText){
			this.infoPointText.attr({
				'text' : this.count
			});
		};
	};
	
	this.remove = function() {
		this.visual.remove();
		this.infoPoint.remove();
		this.infoPointText.remove();
	};
	
};

/**
 * Singleton
 */
var InfoBox = function() {
	
	this.mask = new Mask($('content'), {
		'id' : 'info-box-mask'
	});
	
	this.infoElement = $('div#info-box').inject(this.mask.toElement());
	
	$('div#info-box-content').inject(this.infoElement);
	var buttonContainer = $('div.button-container').inject(this.infoElement);
	$('div.control-cap-left').inject(buttonContainer);
	var okButton = $('div.button').inject($('div.controls').inject(buttonContainer));
	$('div.button-cap-left-blue').inject(okButton);
	$('div.button-text').appendText('OK').inject(okButton);
	$('div.button-cap-right-blue').inject(okButton);
	$('div.control-cap-right').inject(buttonContainer);
	
	okButton.addEvent('click', function() {
		this.mask.hide();
	}.bind(this));
	
	this.go = function(contentCallback, key){
		$('info-box-content').empty();
		$('info-box-content').appendText('Loading...');
		this.mask.show();
		contentCallback(key, this.showContent);
	};
	
	this.showContent = function(content) {
		$('info-box-content').empty();
		content.inject($('info-box-content'));
	};
	
	this.hide = function() {
		this.mask.hide();
	};

};
