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
	util.loadScript('bundlesGui', function(){
		util.loadScript('raphael', function(){
	
			var width = 900;
			var height = 551;
			$('#bundle-canvas').css({'width' : width, 'height' : height + 18});
			
			var dataSource = new GeminiDataSource();
			dataSource.updateData(function(){
				layoutManager = new LayoutManager(Raphael('bundle-canvas', width, height), dataSource);
				new SideBar(layoutManager, dataSource).init();
				util.pageReady();
			});
		});
	});
}

var SideBar = function(layoutManager, dataSource){

	var self = this;
	
	self.dataSource = dataSource;
	
	self.layoutManager = layoutManager;
	
	self.focused = -1;
	
	self.layoutManager.setFocusListener(function(bundleId){
		if(bundleId != self.focused){
			self.focused = bundleId;
			var rowIds = $('td:first-child', self.bundlesTable);
			$.each(rowIds, function(index, rowId){
				if($(rowId).text() == bundleId){
					rowId.scrollIntoView(true);
					$('.table-tr-selected', self.bundlesTable).removeClass('table-tr-selected');
					$(rowId).parent().addClass('table-tr-selected');
				}
			});
		}
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
	
	self.clickEvent = function(row){
		var bundleId = $('td:first-child', row).text();
		self.focused = bundleId;
		self.layoutManager.displayBundle(bundleId);
	};
	
};

/**
 * As a datasource to the bundles gui layout manager this object must provide the following methods.
 * 
 * UpdateData
 * UpdateBundle
 * 
 */
var GeminiDataSource = function(){

	var self = this;

	self.bundles = {};
	
	self.services = {};
	
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
			}, function(){alert('Error loading page, please refresh.');});
			
		});
	};
	

	self.updateBundle = function(bundleId, callback){
		var region = self.bundles[bundleId].Region[0];

		var bundleQuery = new Array({
			'mbean' : 'osgi.core:version=1.0,type=wiringState,region=' + region,
			'operation' : "getCurrentWiring(long,java.lang.String)",
			'arguments' : [bundleId, 'osgi.wiring.all'],
			'type' : 'exec'
		},{
			'mbean' : 'osgi.core:version=1.5,type=serviceState,region=' + region,
			'operation' : 'getRegisteredServices(long)',
			'arguments' : [bundleId],
			'type' : 'exec'
		},{
			'mbean' : 'osgi.core:version=1.5,type=serviceState,region=' + region,
			'operation' : 'getServicesInUse(long)',
			'arguments' : [bundleId],
			'type' : 'exec'
		});
		
		util.doBulkQuery(bundleQuery, function(response){
			self.bundles[bundleId].ProvidedWires = response[0].value.ProvidedWires;
			self.bundles[bundleId].RequiredWires = response[0].value.RequiredWires;
			self.bundles[bundleId].Capabilities = response[0].value.Capabilities;
			self.bundles[bundleId].Requirements = response[0].value.Requirements;
			self.bundles[bundleId].RegisteredServices = response[1].value;
			self.bundles[bundleId].ServicesInUse = response[2].value;
			
			callback();
		});
	};

};
