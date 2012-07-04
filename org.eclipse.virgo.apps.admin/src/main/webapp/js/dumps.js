/*******************************************************************************
 * Copyright (c) 2008, 2011 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

/**
 * Scripts to be loaded in to the head of the dumps view
 */
function pageinit(){
	util.loadScript('bundlesGui', function(){});
	util.loadScript('raphael', function(){});
	dumpViewer = new DumpViewer().displayDumps();
	$.ajax({
		url: util.getCurrentHost() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/ConfiguredDumpDirectory', 
		dataType: 'json',
		success: function (response) {
			$('#dumpLocation').text("Location: " + response.value);
		}
	});
}

var DumpViewer = function(){
	
	var self = this;

	self.selectedDump = null;
	
	self.displayDumps = function(){
		$('#dumps').empty();
		$.ajax({
			url: util.getCurrentHost() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/Dumps', 
			dataType: 'json',
			success: function (response){
				self.displayDumpsResponse(response.value);
				if(self.selectedDump){
					// Look up the id of the selected dump again.
					var dumpId = self.selectedDump.attr("id");
					self.selectedDump = $('#' + dumpId);
					
					if (self.selectedDump){
						$(self.selectedDump).addClass('selected-item');
					}
				
				}
				self.displaySelectedDump();
			}
		});
		return self;
	};
	
	self.displayDumpsResponse = function(json){
		if(json && json.length > 0){
			$.each(json, function(index, item){
				var dumpListItem = $('<li />', {'class' : 'dump'});
				dumpListItem.attr("id", item);
				var label = $('<div />', {'class' : 'label'});
				label.text(item);
				label.click(dumpListItem, self.displayDumpEntries);
				dumpListItem.append(label);
				dumpListItem.append($('<div />', {'class' : 'delete'}).text("Delete").click(dumpListItem, self.deleteDump));
				$('#dumps').append(dumpListItem);
			});
		} else {
			var dumpListItem = $('<li />');
			dumpListItem.text('None');
			$('#dumps').append(dumpListItem);
		}
		util.pageReady();
	};

	self.displayDumpEntries = function(event){
		var dumpListItem = event.data;
		var dumpParent = $('#dumps');
		var dumps = dumpParent.children();
		$.each(dumps, function(index, dump){
			$(dump).removeClass('selected-item');
		});
		dumpListItem.addClass('selected-item');
		self.selectedDump = dumpListItem;
		self.displaySelectedDump();
	};
	
	self.displaySelectedDump = function(){
		if(self.selectedDump){
			var dumpId = self.selectedDump.attr("id");
			$.ajax({
				url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/getDumpEntries/' + dumpId, 
				dataType: 'json',
				success: function (response){
					self.displaySelectedDumpResponse(response.value, self.selectedDump);
				}
			});
		}else{
			$('#dump-items').empty();
			$('#dump-item-content').empty();
		}
	};
	
	self.displaySelectedDumpResponse = function(json, dumpListItem){
		var dumpId = dumpListItem.attr("id");
		$('#dump-items').empty();
		$('#dump-item-content').empty();
		if(json && json.length > 0){
			$.each(json, function(index, item){
				// Replace periods in ids to make them easy to use as JQuery selectors.
				var dumpEntryId = (dumpId + item[0]).replace(new RegExp('\\.', 'g'), '_');
				var dumpEntryListItem = $('<li />', {'class' : 'dump-item'});
				dumpEntryListItem.attr('id', dumpEntryId);
				var dumpEntryLabel = $('<div />', {'class' : 'label'}).text(item[0]);
				var dumpEntryClickData = {'dumpEntryId': dumpEntryId, 'queryString': item[1], 'dumpId': dumpId};
				dumpEntryLabel.click(dumpEntryClickData, self.displayDumpEntry);
				dumpEntryListItem.append(dumpEntryLabel);
				$('#dump-items').append(dumpEntryListItem);
				if('summary.txt' == item[0]){
					dumpEntryLabel.click();
				}
			});
		}
	};
	
	self.displayDumpEntry = function(event){
		$.each($('#dump-items').children(), function(index, dump){
			$(dump).removeClass('selected-item');
		});
		$('#' + event.data.dumpEntryId).addClass('selected-item');
		if(-1 < event.data.queryString.indexOf('StateDumpInspector')){
			self.displayOSGiStateDump(event.data.dumpId);
		} else {
			$.ajax({
				url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=' + event.data.queryString, 
				dataType: 'json',
				success: function (response){
					self.displayDumpEntryResponse(response.value);
				}
			});
		}
	};
	
	self.displayDumpEntryResponse = function(json){
		$('#dump-item-content').empty();
		if(json && json.length > 0){
			$.each(json, function(index, item){
				var dumpListItem = $('<div />', {'class' : 'dump-file-line'});
				dumpListItem.text(item);
				$('#dump-item-content').append(dumpListItem);
			});
		}
	};
	
	self.displayOSGiStateDump = function(dumpId){
		$('#dump-item-content').empty();
		$('#dump-item-content').append($('<div />', {id: 'bundle-canvas'}));
		var width = 1000;
		var height = 562;
		$('#bundle-canvas').css({'width' : width, 'height' : height + 18});
		
		var dataSource = new QuasiDataSource();
		dataSource.updateData(function(){
			new LayoutManager(Raphael('bundle-canvas', width, height),  dataSource).displayBundle(5);
		});
	};
	
	//CREATE AND DELETE DUMPS
	
	self.createDump = function(){
		$.ajax({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/createDump', 
			dataType: 'json',
			success: function (response){
				self.displayDumps();
			}
		});
	};

	self.deleteDump = function(event){
		var dumpListItem = event.data;
		var dumpId = dumpListItem.attr("id");
		$.ajax({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/deleteDump/' +  dumpId, 
			dataType: 'json',
			success: function (response){
				if(dumpListItem == self.selectedDump){
					self.selectedDump = null;
				}
				self.displayDumps();
			}
		});
	};
	
};


/**
 * As a datasource to the bundles gui layout manager this object must provide the following methods.
 * 
 * UpdateData
 * UpdateBundle
 * 
 */
var QuasiDataSource = function(){

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
		