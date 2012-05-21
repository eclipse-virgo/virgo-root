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
	util.loadScript('bundles-gui', function(){
		var width = 900;
		var height = 551;
		$('#bundle-canvas').css({'width' : width, 'height' : height + 18});
		
		var dataSource = new GeminiDataSource();
		dataSource.updateData(function(){
			layoutManager = new LayoutManager(width, height, dataSource);
			new SideBar(layoutManager, dataSource).init();
			util.pageReady();
		});
	});
}

var SideBar = function(layoutManager, dataSource){

	var self = this;
	
	self.dataSource = dataSource;
	
	self.layoutManager = layoutManager;
	
	self.layoutManager.setFocusListener(function(bundleId){
		//Scroll to the selected bundleId
	});
	
	self.init = function(){
		var tRows = new Array();
		$.each(dataSource.bundles, function(id, bundle){
			tRows.push([id, bundle.SymbolicName, bundle.Version]);
		});
		
		self.bundlesTable = util.makeTable({ 
			id: 'bundle-table',
			headers: ['Id', 'Name', 'Version'], 
			rows: tRows,
			sortable : true,
			sortIndex: 0,
			selectable : self.clickEvent
		});
		$('#side-bar').append(self.bundlesTable);
	};
	
	self.clickEvent = function(eventData){
		
	};
	
};

/**
 * As a datasource to the bundles gui layout manager this object must provide the following methods.
 * 
 * 
 */
var GeminiDataSource = function(){

	var self = this;

	self.bundles = {};
	
	self.updateData = function(callback){
		util.doQuery('search/org.eclipse.equinox.region.domain:type=Region,*', function(response){
			
			var bundlesRequest = new Array();
			$.each(response.value, function(index, region){
				bundlesRequest.push({	
					"mbean" : "osgi.core:type=bundleState,version=1.5,region=" + util.readObjectName(region).get('name'),
					"operation" : "listBundles()",
					"arguments" : [],
					"type" : "exec"
				});
			});

			self.bundles = {};
			
			util.doBulkQuery(bundlesRequest, function(response){
				$.each(response, function(index, regionBundles){
					var region = util.readObjectName(regionBundles.request.mbean).get('region');
					$.each(regionBundles.value, function(bundleId, bundle){
						if(self.bundles[bundleId]){
							self.bundles[bundleId].Region.push(region);
						}else{
							self.bundles[bundleId] = bundle;
							self.bundles[bundleId].Region = [region];
						}
					});
				});
				callback();
			});
			
		});
	};
	

	
//	
//	
//		
//	this.getOverview = function(rawBundles, rawRegions){
//
//		
//		this.bundles = {};
//		this.packages = {};
//		$.each(rawBundles, function(key, value){
//			this.processPackages(key, value.ImportedPackages, value.ExportedPackages);
//			this.bundles[key] = new Bundle(value.SymbolicName, value.Version, regionsMap[key], key, value.State, value.Location, this.formatHeader(value.Headers), value.Fragment, value.Hosts, value.Fragments, value.ImportedPackages, value.ExportedPackages, value.RequiredBundles, value.RequiringBundles, value.RegisteredServices, value.ServicesInUse, this.bundleClicked);
//			bundlesTable.push([key, value.SymbolicName, value.Version], {'key' : key, 'id' : 'bundle-' + key});
//		});
//
//		this.layout = new Layout(this.bundles);
//
//		bundlesTable.addEvent('rowFocus', function(tr){
//			if(this.relationships == 'bundles'){
//				this.layout.shuffle(tr.getProperty('key'), this.getBundleRelationships(tr.getProperty('key')));
//			} else if(this.relationships == 'services') {	
//				this.layout.shuffle(tr.getProperty('key'), this.getServiceRelationships(tr.getProperty('key')));
//			}
//		});
//		
//		$('side-bar').empty();
//		$('side-bar').append(bundlesTable);
//	};
//	
//	this.formatHeader = function(rawHeaders){
//		var result = {};
//		$.each(rawHeaders, function(index, header){
//			result[header.Key] = header.Value;
//		});
//		return result;
//	};
//	
//	this.processPackages = function(id, imports, exports) {
//		imports.each(function(packageKey) {
//			if(this.packages[packageKey]){
//				this.packages[packageKey].importers.push(id);
//			} else {
//				var nameAndVersion = packageKey.split(';');
//				this.packages[packageKey] = {name : nameAndVersion[0], version : nameAndVersion[1], importers : [id], exporters : []};
//			}
//		}, this);
//		exports.each(function(packageKey) {
//			if(this.packages[packageKey]){
//				this.packages[packageKey].exporters.push(id);
//			} else {
//				var nameAndVersion = packageKey.split(';');
//				this.packages[packageKey] = {name : nameAndVersion[0], version : nameAndVersion[1], importers : [], exporters : [id]};
//			}
//		}, this);
//	};
//	
//	this.bundleClicked = function(bundleId){
//		console.log('Scrolling table to', $('bundle-' + bundleId));
//		$('side-bar').retrieve('scroller').toElementCenter($('bundle-' + bundleId), 'y');
//		$('bundle-table').retrieve('HtmlTable').selectNone();
//		$('bundle-table').retrieve('HtmlTable').selectRow($('bundle-' + bundleId));
//	};
//	
//	// ****** BUNDLES VIEW ****** //
//	
//	this.getBundleRelationships = function(bundleId){
//		var bundle = this.bundles[bundleId];
//		var providers = new Array();
//		var requirers = new Array();
//		bundle.importedPackages.each(function(packageKey){
//			this.packages[packageKey].exporters.each(function(exporter){
//				providers.push({'bundle' : this.bundles[exporter], 'info' : this.getPackageInfo, 'infoKey' : packageKey, 'tooltip' : 'Package: ' + packageKey});
//			}, this);
//		}, this);
//		bundle.exportedPackages.each(function(packageKey){
//			this.packages[packageKey].importers.each(function(importer){
//				requirers.push({'bundle' : this.bundles[importer], 'info' : this.getPackageInfo, 'infoKey' : packageKey, 'tooltip' : 'Package: ' + packageKey});
//			}, this);
//		}, this);
//		return [providers, requirers];
//	};
//	
//	this.getPackageInfo = function(packageKey, callBack) {
//		var nameAndVersion = packageKey.split(';');
//		var packageRequest = [{
//			"mbean" : "osgi.core:type=packageState,version=1.5",
//			"operation" : "isRemovalPending",
//			"arguments" : [nameAndVersion[0], nameAndVersion[1]],
//			"type" : "exec"
//		},{
//			"mbean" : "osgi.core:type=packageState,version=1.5",
//			"operation" : "getImportingBundles",
//			"arguments" : [nameAndVersion[0], nameAndVersion[1]],
//			"type" : "exec"
//		},{
//			"mbean" : "osgi.core:type=packageState,version=1.5",
//			"operation" : "getExportingBundles",
//			"arguments" : [nameAndVersion[0], nameAndVersion[1]],
//			"type" : "exec"
//		}];
//		util.doBulkQuery(packageRequest, function(response) {
//		console.log(this.packages);
//		console.log(response);
//			callBack([response[0].value, response[1].value, response[2].value]);
//		}); 
//	};
//	
//	// ****** SERVICES VIEW ****** //
//	
//	this.getServiceRelationships = function(bundleId) {
//		var bundle = this.bundles[bundleId];
//		var providers = new Array();
//		var requirers = new Array();
//		bundle.consumedServices.each(function(consumedServiceId){
//			$.each(this.bundles, function(index, bundleToCheck){
//				if(bundleToCheck.providedServices.contains(consumedServiceId)){
//					providers.push({'bundle' : bundleToCheck, 'info' : this.getServiceInfo, 'infoKey' : consumedServiceId, 'tooltip' : 'Service Id: ' + consumedServiceId});
//				}
//			});
//		}, this);
//		bundle.providedServices.each(function(providedServiceId){
//			$.each(this.bundles, function(bundleToCheck){
//				if(bundleToCheck.consumedServices.contains(providedServiceId)){
//					requirers.push({'bundle' : bundleToCheck, 'info' : this.getServiceInfo, 'infoKey' : providedServiceId, 'tooltip' : 'Service Id: ' + providedServiceId});
//				}
//			});
//		}, this);
//		return [providers, requirers];
//	};
//	
//	this.getServiceInfo = function(serviceId, callBack) {
//		var serviceRequest = [{
//			"mbean" : "osgi.core:type=serviceState,version=1.5",
//			"operation" : "getProperties",
//			"arguments" : [serviceId],
//			"type" : "exec"
//		},{
//			"mbean" : "osgi.core:type=serviceState,version=1.5",
//			"operation" : "getUsingBundles",
//			"arguments" : [serviceId],
//			"type" : "exec"
//		},{
//			"mbean" : "osgi.core:type=serviceState,version=1.5",
//			"operation" : "getObjectClass",
//			"arguments" : [serviceId],
//			"type" : "exec"
//		},{
//			"mbean" : "osgi.core:type=serviceState,version=1.5",
//			"operation" : "getBundleIdentifier",
//			"arguments" : [serviceId],
//			"type" : "exec"
//		}];
//		util.doBulkQuery(serviceRequest, function(response) {
//			var content = $('ul.infoContent');
//			$('li').inject(content).appendText('Service provided by bundle: ' + this.bundles[response[3].value].summary());
//			$('li').inject(content).appendText('Objectclass: ' + response[2].value);
//			$('li').inject(content).appendText('Properties');
//			var propertiesList = $('ul.infoContent').inject(content);
//			$.each(response[0].value, function(property){
//				$('li').inject(propertiesList).appendText(property.Key + ' - ' + property.Value);
//			});
//			$('li').inject(content).appendText('Consumers');
//			var consumersList = $('ul.infoContent').inject(content);
//			response[1].value.each(function(bundleId){
//				$('li').inject(consumersList).appendText(this.bundles[bundleId].summary());
//			});
//			callBack(content);
//		}); 
//	};

};
