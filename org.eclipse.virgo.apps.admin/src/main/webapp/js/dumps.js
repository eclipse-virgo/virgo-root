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
	new Request.JSON({
		url: Util.getCurrentHost() + '/jolokia/read/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/Dumps', 
		method: 'get',
		onSuccess: function (responseJSON){
			dumpViewer = new DumpViewer();
			dumpViewer.renderDumps(responseJSON.value);
		}
	}).send();
}

var DumpViewer = function(){

	this.dumps = [];
		
	this.renderDumps = function(json){
		if(json && json.length > 0){
			json.each(function(item){
				var dumpListItem = new Element('li.dump');
				dumpListItem.set('id', item );
				dumpListItem.appendText(item);
				dumpListItem.set('onClick', 'dumpViewer.renderDumpItems("' + item + '")');
				dumpListItem.inject($('dumps'));
			}, this);
		} else {
			var dumpListItem = new Element('li');
			dumpListItem.appendText('No dumps found');
			dumpListItem.inject($('dumps'));
		}
		Util.pageReady();
	};
	

	this.renderDumpItems = function(id){
		$('dumps').getChildren().each(function(dump){dump.removeClass('selected-item');});
		$(id).addClass('selected-item');
		new Request.JSON({
			url: Util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/getDumpEntryNames/' + id, 
			method: 'get',
			onSuccess: function (responseJSON){
				this.renderDumpItemsResponse(responseJSON.value, id);
			}.bind(this)
		}).send();
	};
	
	this.renderDumpItemsResponse = function(json, id){
		$('dump-items').empty();
		$('dump-item-content').empty();
		if(json && json.length > 0){
			json.each(function(item){
				var dumpListItem = new Element('li.dump-item');
				dumpListItem.set('id', id + item );
				dumpListItem.appendText(item);
				dumpListItem.set('onClick', 'dumpViewer.displayDumpItem("' + id + '","' + item + '")');
				dumpListItem.inject($('dump-items'));
				if('summary.txt' == item){
					this.displayDumpItem(id, item);
				}
			}, this);
		} else {
			var dumpListItem = new Element('li');
			dumpListItem.appendText('No entries found');
			dumpListItem.inject($('dump-items'));
		}
	};
	
	this.displayDumpItem = function(id, item){
		$('dump-items').getChildren().each(function(dump){dump.removeClass('selected-item');});
		$(id + item).addClass('selected-item');
		new Request.JSON({
			url: Util.getCurrentHost() + '/jolokia/exec/org.eclipse.virgo.kernel:type=Medic,name=DumpInspector/getDumpEntry/' + id + "/" + item, 
			method: 'get',
			onSuccess: function (responseJSON){
				this.displayDumpItemResponse(responseJSON.value);
			}.bind(this)
		}).send();
	};
	
	this.displayDumpItemResponse = function(json){
		$('dump-item-content').empty();
		json.each(function(item){
			var dumpListItem = new Element('div.dump-file-line');
			dumpListItem.appendText(item);
			dumpListItem.inject($('dump-item-content'));
		}, this);
	};
	
};
