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
		url: util.getHostAndAdminPath() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/ConfiguredDumpDirectory', 
		dataType: 'json',
		contentType: 'application/json',
		cache: false,
		success: function (response) {
			$('#dumpLocation').text("Location: " + response.value);
			dumpViewer.setDumpLocation(response.value);
		}
	});
}

function DumpViewer(){
	
	var self = this;
	
	self.loadingDump = false;

	self.selectedDump = null;
	
	self.setDumpLocation = function(dumpLocation){
		self.dumpLocation = dumpLocation;
	};
	
	self.displayDumps = function(){
		$('#dumps').empty();
		$.ajax({
			url: util.getHostAndAdminPath() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/Dumps', 
			dataType: 'json',
			contentType: 'application/json',
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
				dumpListItem.append($('<div />', {'class' : 'delete'}).text('Delete').click(dumpListItem, self.deleteDump));
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
				url: util.getHostAndAdminPath() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/getDumpEntries/' + dumpId, 
				dataType: 'json',
				contentType: 'application/json',
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
				url: util.getHostAndAdminPath() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=' + event.data.queryString, 
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
		
		var dataSource = new QuasiDataSource(self.dumpLocation + '!/' + dumpId);
		dataSource.updateData(function(){
			dataSource.getUnresolvedBundleIds(function(bundles){
				
				if($.browser.msie){
					$('#bundle-canvas').css({'height': '574px', 'width': '1000px'});
				}
				
				layoutManager = new LayoutManager('bundle-canvas', 1000, 553, dataSource);
				if(bundles.length < 1){
					controls.append($('<div />').text('There were no unresolved bundles at the time of this state dump.'));
				}else{
					$.each(bundles, function(index, unresolvedBundle){
						var bundleTitle = $('<div />').text('Bundle [' + unresolvedBundle.identifier + '] ' + unresolvedBundle.symbolicName + ': ' + unresolvedBundle.version + ' is unresolved.');
						var cause = $('<div />', {'class': 'unresolved-bundle-cause'}).text(unresolvedBundle.description);
						var unresolvedBundleElement = $('<div />', {'class': 'unresolved-bundle'}).append(bundleTitle).append(cause);
						unresolvedBundleElement.click(unresolvedBundle.identifier, function(eventData){
							layoutManager.displayBundle(eventData.data);
						});
						controls.append(unresolvedBundleElement);
					});
					layoutManager.displayBundle(bundles[0].identifier);
				}
				var tableHolder = $('<div />', {id: 'table-holder'});
				controls.append(tableHolder);
				new TopBar(tableHolder, layoutManager, dataSource).init();
				$('#side-bar').height($('#dump-item-content').height() - 17);
			});
		}, function(){
			controls.append($('<div />').text('Unable to retrieve Bundle data for the state dump, this requires the Virgo Kernel or above.'));
		});
	};
	
	//CREATE AND DELETE DUMPS
	
	self.createDump = function(){
		if(!self.loadingDump){
			self.loadingDump = true;
			$('#create-dump-button').addClass('grey-out');
			$('#dumps').append($('<div />', {'class' : 'spinner-small'}));
			$.ajax({
				url: util.getHostAndAdminPath() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/createDump', 
				dataType: 'json',
				cache: false,
				success: function (response){
					self.loadingDump = false;
					$('#create-dump-button').removeClass('grey-out');
					self.displayDumps();
				}
			});
		}
	};

	self.deleteDump = function(event){
		var dumpListItem = event.data;
		var dumpId = dumpListItem.attr("id");
		$.ajax({
			url: util.getHostAndAdminPath() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/deleteDump/' +  dumpId, 
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
		util.doQuery('exec/org.eclipse.virgo.kernel:type=Medic,name=StateDumpInspector/getUnresolvedBundleFailures/' + self.dumpFolder, function(response){
			callback(response.value);
		});
	};
	
	self.updateData = function(callback, failCallback){
		util.doQuery('exec/org.eclipse.virgo.kernel:type=Medic,name=StateDumpInspector/listBundles/' + self.dumpFolder, function(response){
			if(response.value){
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
			}else{
				failCallback();
			}
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


var TopBar = function(container, layoutManager, dataSource){

	var self = this;
	
	self.dataSource = dataSource;
	
	self.layoutManager = layoutManager;
	
	self.container = container;
	
	self.focused = -1;
	
	self.layoutManager.setFocusListener(function(bundleId){
		if(bundleId != self.focused){
			self.setFocused(bundleId);
		}
	});
	
	self.setFocused = function(bundleId){
		self.focused = bundleId;
		var rowIds = $('tbody td:first-child', self.bundlesTable);
		$.each(rowIds, function(index, rowId){
			if($(rowId).text() == bundleId){
				self.container.scrollTop(self.bundlesTable.getElementOffset($(rowId)));				
				$('.table-tr-selected', self.bundlesTable).removeClass('table-tr-selected');
				$(rowId).parent().addClass('table-tr-selected');
			}
		});
	};
	
	self.init = function(){
		var tRows = new Array();
		$.each(dataSource.bundles, function(id, bundle){		
			tRows.push([id, self.getFormattedBundleName(bundle), bundle.Version]);
		});
		
		self.bundlesTable = util.makeTable({ 
			id: 'bundle-table',
			headers: [{title: 'Id', type: 'numeric'}, {title: 'SymbolicName', type: 'alpha'}, {title: 'Version', type: 'version'}], 
			rows: tRows,
			sortable : true,
			sortIndex: 0,
			selectable : self.clickEvent
		});
		self.container.append(self.bundlesTable);
		if(util.pageLocation && util.pageLocation.length > 0){
			self.setFocused(util.pageLocation);
		}
	};
	
	self.getFormattedBundleName = function(bundle){
		var formatBundleList = function(bundleIdArray){
			var result = bundleIdArray[0];
			for(var i = 1; i < bundleIdArray.length; i++) {
				result = result + ', ' + bundleIdArray[i];
			}
			return result;
		};
		if(bundle.Fragments && bundle.Fragments.length > 0){
			return bundle.SymbolicName + ' - Fragments [' + formatBundleList(bundle.Fragments) + ']';
		}
		if(bundle.Hosts && bundle.Hosts.length > 0){
			return bundle.SymbolicName + ' - Host [' + formatBundleList(bundle.Hosts) + ']';
		}else if(bundle.Fragment == 'true'){
			return bundle.SymbolicName + ' Is Fragments';
		}
		return bundle.SymbolicName;
	};
	
	self.clickEvent = function(row){
		var bundleId = $('td:first-child', row).text();
		self.focused = bundleId;
		self.layoutManager.displayBundle(bundleId);
	};
	
};
		