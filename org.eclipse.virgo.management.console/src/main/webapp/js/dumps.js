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
			dumpViewer.setDumpLocation(response.value);
		}
	});
}

var DumpViewer = function(){
	
	var self = this;

	self.selectedDump = null;
	
	self.setDumpLocation = function(dumpLocation){
		self.dumpLocation = dumpLocation;
	};
	
	self.displayDumps = function(){
		$('#dumps').empty();
		$.ajax({
			url: util.getCurrentHost() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/Dumps', 
			dataType: 'json',
			cache: false,
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
				dumpListItem.append($('<div />', {'class' : 'label'}).text(item).click(dumpListItem, self.displayDumpEntries));
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
				cache: false,
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
			self.displayOSGiStateDumpEntry(event.data.dumpId);
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
	
	self.displayOSGiStateDumpEntry = function(dumpId){
		$('#dump-item-content').empty();
		var controls = $('<div />', {id: 'gui-controls'});
		var bundleCanvas = $('<div />', {id: 'bundle-canvas'});
		$('#dump-item-content').append(controls);
		$('#dump-item-content').append(bundleCanvas);
		var width = 1000;
		var height = 562;
		bundleCanvas.css({'width' : width, 'height' : height + 18});
		
		var dataSource = new QuasiDataSource(self.dumpLocation + '!/' + dumpId);
		dataSource.updateData(function(){
			dataSource.getUnresolvedBundleIds(function(bundles){
				layoutManager = new LayoutManager('bundle-canvas', width, height, dataSource);
				if(bundles.length < 1){
					controls.append($('<div />').text('There were no unresolved bundles at the time of this state dump.'));
					layoutManager.displayBundle(5);
				}else{
					$.each(bundles, function(index, unresolvedBundle){
						var displayLink = $('<div />').text('Bundle ' + unresolvedBundle.identifier + ' unresolved.').click(unresolvedBundle.identifier, layoutManager.displayBundle);
						var cause = $('<div />', {'class': 'unresolved-bundle-cause'}).text(unresolvedBundle.description);
						controls.append($('<div />', {'class': 'unresolved-bundle'}).append(displayLink).append(cause));
					});
					layoutManager.displayBundle(bundles[0].identifier);
				}
			});
		});
	};
	
	//CREATE AND DELETE DUMPS
	
	self.createDump = function(){
		$('#dumps').append($('<div />', {'class' : 'spinner-small'}));
		$.ajax({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/createDump', 
			dataType: 'json',
			cache: false,
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
			cache: false,
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
var QuasiDataSource = function(dumpFolder){

	var self = this;

	self.dumpFolder = dumpFolder.replace('/', '!/');
	
	self.bundles = {};
	
	self.services = {};
	
	self.getUnresolvedBundleIds = function(callback){
		util.doQuery('exec/org.eclipse.virgo.kernel:type=Medic,name=StateDumpInspector/getUnresolvedBundleIds/' + self.dumpFolder, function(response){
			callback(response.value);
		});
	};
	
	self.updateData = function(callback){
		util.doQuery('exec/org.eclipse.virgo.kernel:type=Medic,name=StateDumpInspector/listBundles/' + self.dumpFolder, function(response){
			self.bundles = {};
			$.each(response.value, function(index, item){
				self.bundles[item.identifier] = {	'SymbolicName': item.symbolicName,
													'Version': item.version,
													'Identifier': item.identifier,
													'State': item.state,
													'Region': item.region,
													'Location': item.location,
													'Fragment': item.fragment,
													'ExportedPackages': item.exportedPackages,
													'ImportedPackages': item.importedPackages};
			});
			callback();
		});
	};
	

	self.updateBundle = function(bundleId, callback){
		util.doQuery('exec/org.eclipse.virgo.kernel:type=Medic,name=StateDumpInspector/getBundle/' + self.dumpFolder + '/' + bundleId, function(response){
			self.bundles[bundleId].ProvidedWires = self.processWires(response.value.providedWires);
			self.bundles[bundleId].RequiredWires = self.processWires(response.value.requiredWires);
			callback();
		});
	};
	
	self.processWires = function(badlyFormattedWires){
		var wellFormattedWires = new Array();
		$.each(badlyFormattedWires, function(index, badlyFormattedWire){
			wellFormattedWires.push({ProviderBundleId: badlyFormattedWire.providerBundleId, 
									 RequirerBundleId: badlyFormattedWire.requirerBundleId, 
									 BundleRequirement: {Namespace: badlyFormattedWire.namespace,
										 				 Attributes: self.processProperties(badlyFormattedWire.bundleRequirementAttributes),
										 				 Directives: self.processProperties(badlyFormattedWire.bundleRequirementDirectives)},
									 BundleCapability: {Namespace: badlyFormattedWire.namespace,
										 				 Attributes: self.processProperties(badlyFormattedWire.bundleCapabilityAttributes),
						 				 				 Directives: self.processProperties(badlyFormattedWire.bundleCapabilityDirectives)}});
		});
		return wellFormattedWires;
	};
	
	self.processProperties = function(wellFormattedProperties){
		var specFormattedProperties = {};
		$.each(wellFormattedProperties, function(key, value){
			specFormattedProperties[key] = {'Key': key, 'Value': value};
		});
		return specFormattedProperties;
	};

};
		