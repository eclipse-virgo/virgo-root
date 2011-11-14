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
	new Request.JSON({
		url: util.getCurrentHost() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/ConfiguredDumpDirectory', 
		method: 'get',
		onSuccess: function (responseJSON){
			setDumpDirectory(responseJSON.value);
		}
	}).send();
}

function setDumpDirectory(json) {
	$('dumpLocation').appendText("Location: " + json);
}

var DumpViewer = function(){

	this.selectedDump = null;
	
	this.displayDumps = function(){
		$('dumps').empty();
		new Request.JSON({
			url: util.getCurrentHost() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/Dumps', 
			method: 'get',
			onSuccess: function (responseJSON){
				this.displayDumpsResponse(responseJSON.value);
				if(this.selectedDump){
					$(this.selectedDump).addClass('selected-item');
				}else{
					this.selectedDump = null;
					$('dump-items').empty();
					$('dump-item-content').empty();
				}
			}.bind(this)
		}).send();
	};
	
	this.displayDumpsResponse = function(json){
		if(json && json.length > 0){
			json.each(function(item){
				var dumpListItem = new Element('li.dump');
				dumpListItem.set('id', item );
				new Element('div.label').appendText(item).set('onClick', 'dumpViewer.displayDumpEntries("' + item + '")').inject(dumpListItem);
				new Element('div.delete').appendText("Delete").set('onClick', 'dumpViewer.deleteDump("' + item + '")').inject(dumpListItem);
				dumpListItem.inject($('dumps'));
			}, this);
		} else {
			var dumpListItem = new Element('li');
			dumpListItem.appendText('No dumps found.');
			dumpListItem.inject($('dumps'));
		}
		util.pageReady();
	};

	this.displayDumpEntries = function(id){
		$('dumps').getChildren().each(function(dump){dump.removeClass('selected-item');});
		$(id).addClass('selected-item');
		this.selectedDump = id;
		new Request.JSON({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/getDumpEntries/' + id, 
			method: 'get',
			onSuccess: function (responseJSON){
				this.displayDumpEntriesResponse(responseJSON.value, id);
			}.bind(this)
		}).send();
	};
	
	this.displayDumpEntriesResponse = function(json, id){
		$('dump-items').empty();
		$('dump-item-content').empty();
		if(json && json.length > 0){
			json.each(function(item){
				var dumpEntryListItem = new Element('li.dump-item');
				dumpEntryListItem.set('id', id + item[0]);
				new Element('div.label').appendText(item[0]).set('onClick', 'dumpViewer.displayDumpEntry("' + id + item[0] + '","' + item[1] + '")').inject(dumpEntryListItem);
				dumpEntryListItem.inject($('dump-items'));
				if('summary.txt' == item[0]){
					this.displayDumpEntry(id + item[0], item[1]);
				}
			}, this);
		} else {
			var dumpEntryListItem = new Element('li');
			dumpEntryListItem.appendText('No dump entries found.');
			dumpEntryListItem.inject($('dump-items'));
		}
	};
	
	this.displayDumpEntry = function(id, queryString){
		$('dump-items').getChildren().each(function(dump){dump.removeClass('selected-item');});
		$(id).addClass('selected-item');
		new Request.JSON({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=' + queryString, 
			method: 'get',
			onSuccess: function (responseJSON){
				this.displayDumpEntryResponse(responseJSON.value);
			}.bind(this)
		}).send();
	};
	
	this.displayDumpEntryResponse = function(json){
		$('dump-item-content').empty();
		if(json && json.length > 0){
			json.each(function(item){
				var dumpListItem = new Element('div.dump-file-line');
				dumpListItem.appendText(item);
				dumpListItem.inject($('dump-item-content'));
			}, this);
		}
	};
	
	this.createDump = function(){
		new Request.JSON({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/createDump', 
			method: 'get',
			onSuccess: function (responseJSON){
				this.displayDumps();
			}.bind(this)
		}).send();
	};

	this.deleteDump = function(dumpId){
		new Request.JSON({
			url: util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/deleteDump/' + dumpId, 
			method: 'get',
			onSuccess: function (responseJSON){
				if(dumpId == this.selectedDump){
					this.selectedDump = null;
				}
				this.displayDumps();
				alert("Dump with id " + dumpId + " has been deleted.");
			}.bind(this)
		}).send();
	};
	
};
