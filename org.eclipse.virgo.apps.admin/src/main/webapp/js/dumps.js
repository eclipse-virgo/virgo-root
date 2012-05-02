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
	dumpViewer = new DumpViewer();
	dumpViewer.displayDumps();
	$.ajax({
		url: util.getCurrentHost() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/ConfiguredDumpDirectory', 
		dataType: 'json',
		success: function (response) {
			setDumpDirectory(response.value);
		}
	});
}

function setDumpDirectory(json) {
	$('#dumpLocation').text("Location: " + json);
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
				self.displaySelectedDumpEntries();
			}
		});
	};
	
	self.displayDumpsResponse = function(json){
		if(json && json.length > 0){
			$.each(json, function(index, item){
				var dumpListItem = $('<li />', {'class' : 'dump'});
				dumpListItem.attr("id", item);
				var label = $('<div />', {'class' : 'label'});
				label.text(item);
				label.click(dumpListItem, dumpViewer.displayDumpEntries);
				dumpListItem.append(label);
				dumpListItem.append($('<div />', {'class' : 'delete'}).text("Delete").click(dumpListItem, dumpViewer.deleteDump));
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
		self.displaySelectedDumpEntries();
	};
	
	self.displaySelectedDumpEntries = function(){
		if(self.selectedDump){			
			var dumpId = self.selectedDump.attr("id");
			$.ajax({
				url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/getDumpEntries/' + dumpId, 
				dataType: 'json',
				success: function (response){
					self.displayDumpEntriesResponse(response.value, self.selectedDump);
				}
			});
		}else{
			$('#dump-items').empty();
			$('#dump-item-content').empty();
		}
	};
	
	self.displayDumpEntriesResponse = function(json, dumpListItem){
		var dumpId = dumpListItem.attr("id");
		$('#dump-items').empty();
		$('#dump-item-content').empty();
		if(json && json.length > 0){
			$.each(json, function(index, item){
				// Replace periods in ids to make them easy to use as JQuery selectors.
				var dumpEntryId = (dumpId + item[0]).replace(new RegExp('\\.', 'g'), '_');
				var dumpEntryListItem = $('<li />', {'class' : 'dump-item'});
				dumpEntryListItem.attr('id', dumpEntryId);
				var dumpEntryItem = $('<div />', {'class' : 'label'}).text(item[0]);
				dumpEntryItem.attr('onClick', 'dumpViewer.displayDumpEntry("' + dumpEntryId + '","' + item[1] + '")');
				dumpEntryListItem.append(dumpEntryItem);
				$('#dump-items').append(dumpEntryListItem);
				if('summary.txt' == item[0]){
					self.displayDumpEntry(dumpEntryId, item[1]);
				}
			});
		}
	};
	
	self.displayDumpEntry = function(id, queryString){
		$.each($('#dump-items').children(), function(index, dump){
			$(dump).removeClass('selected-item');
		});
		var dumpEntryListItem = $('#' + id);
		dumpEntryListItem.addClass('selected-item');
		$.ajax({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=' + queryString, 
			dataType: 'json',
			success: function (response){
				self.displayDumpEntryResponse(response.value);
			}
		});
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
