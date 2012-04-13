/*******************************************************************************
* Copyright (c) 2011 VMware Inc.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Chris Frost - initial contribution
*   David Normington - Tooltip code
*   
*******************************************************************************/

// INIT FUNCTIONS

$(document).ready(function() {
	util = new Util();
	util.start();
	servers = new Servers();
	servers.loadServers();
	pageinit();
});

// UTILITY FUNCTIONS

var Util = function(){
	
	this.fxTime = 200;
		
	this.queryHash = undefined; //Global so any page scripts can just grab query vars

	this.pageLocation = undefined;
	
	this.start = function(){
		this.spinner = $('<div class="spinner-img"></div>').dialog({autoOpen: false, modal: true}).dialog('open');
		
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		var hash;
		this.queryHash = {};
	    for(var i = 0; i < hashes.length; i++) {
	        hash = hashes[i].split('=');
	        this.queryHash[hash[0]] = hash[1];
	    }
		
		if(location.hash){
			this.pageLocation = location.hash.replace("#", "");
		}
	};

	/**
	 * 
	 */
	this.pageReady = function(){
		this.spinner.dialog('close');
	};

	/**
	 * 
	 * @returns {String}
	 */
	this.getCurrentHost = function(){
		return location.protocol + '//' + location.host + contextPath;
	};
	
	/**
	 * 
	 * @param objectName
	 * @returns {ObjectName}
	 */
	this.readObjectName = function(objectName){
		marker = objectName.indexOf(':');
		domain = objectName.substring(0, marker);
		propertyParts = objectName.substring(marker + 1).split(',');
		properties = new Object();
		propertyParts.each(function(item){
			marker = item.indexOf('=');
			properties[item.substring(0, marker)] = item.substring(marker + 1);
		});
		return new ObjectName(domain, properties, objectName);
	};
	
	/**
	 * 
	 * @param query
	 * @param callback
	 * @param formatter
	 */
	this.doBulkQuery = function(query, callback){
		$.ajax({
			type: 'POST',
			url: this.getCurrentHost() + '/jolokia',
			dataType: 'json',
			data: JSON.stringify(query),
			success: function (response) {
				callback(response);
			}
		});
	};
	
	/**
	 * Loads a JavaScript file from the current host
	 * 
	 * @param name - the name of the script file to load, .js is not required on the end
	 */
	this.loadScript = function(name, async){
		new Request({
			url: this.getCurrentHost() + '/resources/js/' + name + '.js',
			method: 'get',
			async: async
		}).send();
	};
	
	/**
	 * Adds a tooltip to the target element
	 * 
	 * @param el (Object) - The target elements
	 * @param text (String) - The text of the tooltip
	 */
	this.tooltip = function(el, text) {
		if ($('tooltip') == null) {
			var div = new Element("div");
			div.appendText('');
			document.childNodes[1].appendChild(div);
			div.setProperty('id', 'tooltip');
			div.set('style','display: none;');
		}
		
		el.onmouseover = function(e) {
			$('tooltip').empty();
			$('tooltip').set('html', text);
			$('tooltip').set('styles', {"display": "block", "top": (e.pageY+10) + "px", "left": e.pageX + "px"});
		};
		
		el.onmouseout = function(){
			$('tooltip').set('styles', {"display": "none"});
		};
	};
	
	/**
	 * Create and return a table element populated with the provided rows.
	 */
	this.makeTable = function(properties) {
		var newTable = $('<table></table>');
		if(properties.headers){
			var tHead = $('<thead></thead>');
			newTable.append(tHead);
			var tHeadRow = $('<tr></tr>');
			tHead.append(tHeadRow);
			$.each(properties.headers, function(index, item){
				tHeadRow.append($('<th>' + item + '</th>'));
			});
		}
		if(properties.class){
			newTable.addClass(properties.class);
		}
		var tBody = $('<tbody></tbody>');
		newTable.append(tBody);
		if(properties.rows){
			(function(table, rows) {
				var tBody = table.children().last();
				
				$.each(rows, function(i, row){
					var newRow = $('<tr />');
					$.each(row, function(j, value){
						newRow.append($('<td />').append(value));
					});
					tBody.append(newRow);
				});
			})(newTable, properties.rows);
		}
		return newTable;
	};
	
};

/**
 * 
 * 
 */
var ObjectName = function(domain, properties, objectName){
	this.domain = domain;
	this.properties = properties;
	this.get = function(key){
		return this.properties[key];
	};
	this.toString = objectName;
};


// SERVERS GUI SECTION

var Servers = function(){

	this.open = false;
	
	/*
	 * This takes the server string in the URL and sets the servers ServersDisplay.servers to an array of ip address.
	 */
	this.loadServers = function() {
		$('servers').hide("slide", { direction: "up" }, util.fxTime);
		var rows;
		if(util.queryHash.s){
			rows = util.queryHash.s.split(',');
			if(!rows.contains(util.getCurrentHost())){
				rows.push(util.getCurrentHost());
			}
		} else {
			rows = [util.getCurrentHost()];
		}
		
		var serversTable = util.makeTable({
			headers: ['Configured Servers'],
			'class': 'servers-table'
		});

		var tBody = serversTable.children().last();
		
		$.each(rows, function(index, item){
			var newRow = $('<tr></tr>');
			newRow.append($('<td>' + item + '</td>\
							 <td class="server-status">Status</td>\
							 <td class="server-link">Overview</td>\
							 <td class="server-link">Artifacts</td>\
							 <td class="server-link">Repositories</td>\
							 <td class="server-link">Configuration</td>\
							 <td class="server-link">Dumps</td>'));
			tBody.append(newRow);
		});
		
		$('servers-list').replaceWith(serversTable);
	};

	this.toggle = function() {
		this.display.toggle("slide", { direction: "up" }, util.fxTime);
		if(this.open) {
			$('servers-button').removeClass('selected-navigation');
			this.open = false;
		} else {
			$('servers-button').addClass('selected-navigation');
			this.open = true;
		}
	};

};

// SERVERS

var Server = function(){
		
	/**
	 * Request an overview of the current server.
	 * 
	 * @param callback - a function to call with the retrieved server overview data
	 */
	this.getServerOverview = function(callback){
		var request = [{
			"type" : "version"
		},{
			"mbean" : "java.lang:type=OperatingSystem",
			"attribute" : "Name",
			"type" : "READ"
		},{
			"mbean" : "java.lang:type=OperatingSystem",
			"attribute" : "Version",
			"type" : "READ"
		},{
			"mbean" : "java.lang:type=OperatingSystem",
			"attribute" : "Arch",
			"type" : "READ"
		},{
			"mbean" : "java.lang:type=Runtime",
			"attribute" : "VmVendor",
			"type" : "READ"
		},{
			"mbean" : "java.lang:type=Runtime",
			"attribute" : "VmName",
			"type" : "READ"
		},{
			"mbean" : "java.lang:type=Runtime",
			"attribute" : "VmVersion",
			"type" : "READ"
		}];
		
		util.doBulkQuery(request, function(response) {
			callback(this.formatter(response));
		}.bind(this)); 
	};
	
	/* **************** START PRIVATE METHODS **************** */

	/**
 	 * Format the server info request in to table rows.
 	 * 
 	 * @param data - the raw JSON to build the table rows from
 	 */
	this.formatter = function(data){
		var virgo = ["OSGi Container", data[0].value.info.product + ' ' + data[0].value.info.version + ' (' + data[0].value.info.extraInfo.type + ')'];
		var runtime = ["Virtual Machine", data[5].value + ' version ' + data[6].value + ' (' + data[4].value + ')'];
		var os = ["Operating System", data[1].value + ' ' + data[2].value + ' (' + data[3].value + ')'];
		return [virgo, runtime, os];
	};

};
