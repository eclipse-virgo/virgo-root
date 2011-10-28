/*******************************************************************************
* Copyright (c) 2011 David Normiongton
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

window.addEvent('domready', function() {
	util = new Util();
	util.start();
	servers = new Servers();
	servers.loadServers();
	pageinit();
});

// UTILITY FUNCTIONS

var Util = function(){
	
	fxTime: 200;
		
	queryHash: undefined; //Global so any page scripts can just grab query vars

	pageLocation: undefined;
	
	this.start = function(){
		this.spinner = new Spinner('content', {destroyOnHide: false, maskMargins: true});
		this.spinner.addEvent('hide', function(){this.destroy();});
		this.spinner.show(true);
		var queryString = window.location.href.slice(window.location.href.indexOf('?') + 1);	
		this.queryHash = queryString.parseQueryString();
		if(location.hash){
			this.pageLocation = location.hash.replace("#", "");
		}
	};

	/**
	 * 
	 */
	this.pageReady = function(){
		this.spinner.hide();
	};

	/**
	 * 
	 * @returns {String}
	 */
	this.getCurrentHost = function(){
		var me = new URI();
		return me.get('scheme') + '://' + me.get('host') + ':' + me.get('port') + contextPath;
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
	this.doBulkQuery = function(query, callback, formatter){
		new Request.JSON({
			url: this.getCurrentHost() + '/jolokia',
			data: JSON.encode(query),
			urlEncoded: false,
			onSuccess: function (response) {
				if(formatter){
					callback(formatter(response));
				} else {
					callback(response);
				}
			}
		}).send();
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
			div.set("style","display: none;");
		}
		
		el.onmouseover = function(e) {
			$("tooltip").empty();
			$("tooltip").set('html',text);
			$("tooltip").set("styles",{"display": "block", "top": (e.pageY+10) + "px", "left": e.pageX + "px"});
		};
		
		el.onmouseout = function(){
			$("tooltip").set("styles", {"display": "none"});
		};
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
		
	this.servers = [];

	this.open = false;
	
	/*
	 * This takes the server string in the URL and sets the servers ServersDisplay.servers to an array of ip address.
	 */
	this.loadServers = function() {
		this.display = new Fx.Reveal($('servers'), {duration: 300}).dissolve();
		var rows;
		if(util.queryHash.s){
			rows = util.queryHash.s.split(',');
			if(!rows.contains(getCurrentHost())){
				rows.push(getCurrentHost());
			}
		} else {
			rows = [util.getCurrentHost()];
		}
		rows.each(function(item, index){
			rows[index] = [item, 	{content: 'Status', properties: {'class': "server-status"}}, 
			               			{content: 'Overview', properties: {'class': "server-link"}},
			               			{content: 'Artifacts', properties: {'class': "server-link"}},
			               			{content: 'Repositories', properties: {'class': "server-link"}},
			               			{content: 'Configuration', properties: {'class': "server-link"}},
			               			{content: 'Logs', properties: {'class': "server-link"}},
			               			{content: 'Dumps', properties: {'class': "server-link"}}];
		});
		
		new HtmlTable({ 
			properties: {'class': "servers-table"}, 
			headers: ['Configured Servers'], 
			rows: rows,
			zebra: true,
			selectable: true,
			allowMultiSelect: false
		}).replaces($('servers-list'));
	};

	this.toggle = function() {
		this.display.toggle();
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

Server = {
		
	/**
	 * Request an overview of the current server.
	 * 
	 * @param callback - a function to call with the retrieved server overview data
	 */
	getServerOverview: function(callback){
		var request = [{
				"type": "version"
			},{
				"mbean":"java.lang:type=OperatingSystem",
				"attribute":"Name",
				"type":"READ"
			},{
				"mbean":"java.lang:type=OperatingSystem",
				"attribute":"Version",
				"type":"READ"
			},{
				"mbean":"java.lang:type=Runtime",
				"attribute":"VmVendor",
				"type":"READ"
			},{
				"mbean":"java.lang:type=Runtime",
				"attribute":"VmName",
				"type":"READ"
			},{
				"mbean":"java.lang:type=Runtime",
				"attribute":"VmVersion",
				"type":"READ"
			}];
		
		util.doBulkQuery(request, callback, this.formatter); 
	},
	
	/* **************** START PRIVATE METHODS **************** */

	/**
 	 * Format the server info request in to table rows.
 	 * 
 	 * @param data - the raw JSON to build the table rows from
 	 */
	formatter: function(data){
		var virgo = ["Virgo", data[0].value.info.version];
		var web = ["Web Container", data[0].value.info.extraInfo.type.capitalize()];
		var runtime = ["Runtime", data[3].value + ' - ' +  data[4].value + ' (' + data[5].value + ')'];
		var os = ["Operating System", data[1].value + ' (' + data[2].value + ')'];
		return [virgo, web, os, runtime];
	}

};
