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
	v_servers = new Servers();
	v_servers.loadServers();
	
	var today = new Date();
	if(today.getMonth() == 11 || (today.getMonth() == 0 && today.getDate() <= 5)){
		$('#site-name').css({'background': 'url("' + util.getHostAndAdminPath() + '/resources/images/logo-hat-holly.png") no-repeat scroll center center transparent'});
		$('#site-name').css({'height': '150px'});
		$('#site-name').css({'width': '361px'});
	}
	
	pageinit();
});


function loadMenus(menuNames, viewName){

	var loadMenu = function(menuName, viewName){
		var menuNameCaps = menuName.slice(0,1).toUpperCase() + menuName.slice(1);
		var menuItem = $('<li />');
		var link = $('<a href=\'' + contextPath + '/content/' + menuName + '\' />');
		if(menuName == viewName){
			menuItem.addClass('selected-navigation');
		}
		link.append($('<div />', {'class': 'button-cap-left-white'}));
		link.append($('<div />', {'class': 'navigation-text'}).text(menuNameCaps));
		link.append($('<div />', {'class': 'button-cap-right-white'}));
		menuItem.append(link);
		$('ul', $('#navigation-left')).append(menuItem);
	};

	loadMenu('overview', viewName);
	$.each(menuNames.split(', '), function(index, menuItem){
		loadMenu(menuItem, viewName);
	});
};

// UTILITY FUNCTIONS

var Util = function(){
	
	var self = this;
	
	self.fxTime = 200;
		
	self.queryHash = undefined; 

	self.pageLocation = undefined;
	
	self.starting = false;
	
	self.started = false;
	
	self.start = function(){
		if(!self.starting && !self.started){
			self.starting = true;
			self.spinnerElement = $('<div />');
			self.spinner = self.spinnerElement.dialog({
				modal: true,
				dialogClass: 'spinner-large',
				closeText: '',
				draggable: false,
				resizable: false,
				closeOnEscape: false,
				width: '48px',
				close : function(){
					self.spinnerElement.remove();
				}
			});
			var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
			var hash;
			self.queryHash = {};
		    for(var i = 0; i < hashes.length; i++) {
		        hash = hashes[i].split('=');
		        self.queryHash[hash[0]] = hash[1];
		    }
			if(location.hash){
				self.pageLocation = location.hash.replace("#", "");
			}
		}
	};
	
	/**
	 * 
	 */
	self.pageReady = function(){
		if(self.starting && !this.started){
			self.started = true;
			self.spinner.dialog('close');
			self.spinner = undefined;
			self.spinnerElement.remove();
		}
	};

	
	self.getHost = function(){
		return location.protocol + '//' + location.host;
	};
	
	/**
	 * 
	 * @returns {String}
	 */
	self.getHostAndAdminPath = function(){
		return self.getHost() + contextPath;
	};
	
	/**
	 * 
	 * @param objectName
	 * @returns {ObjectName}
	 */
	self.readObjectName = function(objectName){
		marker = objectName.indexOf(':');
		domain = objectName.substring(0, marker);
		propertyParts = objectName.substring(marker + 1).split(',');
		properties = new Object();
		$.each(propertyParts, function(index, item){
			marker = item.indexOf('=');
			properties[item.substring(0, marker)] = item.substring(marker + 1);
		});
		return new ObjectName(domain, properties, objectName);
	};
	
	/**
	 * 
	 * @param query string for jolokia
	 */
	self.doQuery = function(query, successCallback, errorCallback){
		$.ajax({
			url: util.getHostAndAdminPath() + '/jolokia/' + query,
			dataType: 'json',
			contentType: 'application/json',
			cache: false,
			success: function (response) {
				successCallback(response);
			},
			error: function(xmlHttpRequest, textStatus, errorThrown) {
				if(errorCallback){
					errorCallback(xmlHttpRequest, textStatus, errorThrown);
				}
			}
		});
	};
	
	/**
	 * 
	 * @param query object of keys and values
	 * @param callback
	 */
	self.doBulkQuery = function(query, successCallback, errorCallback){
		$.ajax({
			type: 'POST',
			url: self.getHostAndAdminPath() + '/jolokia',
			dataType: 'json',
			contentType: 'application/json',
			cache: false,
			data: JSON.stringify(query),
			success: function (response) {
				successCallback(response);
			},
			error: function(xmlHttpRequest, textStatus, errorThrown) {
				if(errorCallback){
					errorCallback(xmlHttpRequest, textStatus, errorThrown);
				}
			}
		});
	};
	
	/**
	 * Loads a JavaScript file from the current host
	 * 
	 * @param name - the name of the script file to load, .js is not required on the end
	 */
	self.loadScript = function(name, callback){
		$.getScript(self.getHostAndAdminPath() + '/resources/js/' + name + '.js', callback);
	};
	
	/**
	 * Adds a tooltip to the target element
	 * 
	 * @param el (Object) - The target elements
	 * @param text (String) - The text of the tooltip
	 */
	self.tooltip = function(el, text) {
		if ($('tooltip') == null) {
			var div = $("<div />");
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
	self.makeTable = function(properties) {
		
		var decorate = function(table){
			var tBody = $('tbody', table);
			var bodyRows = $('tr', tBody);
			bodyRows.removeClass('table-tr-odd');
			bodyRows.filter(':odd').addClass('table-tr-odd');
			bodyRows.each(function(index, row){
				$(row).mouseenter(function(){
					$(this).addClass('table-tr-hovered');
				});
				$(row).mouseleave(function(){
					$(this).removeClass('table-tr-hovered');
				});
				if(properties.selectable){
					$(row).click(function(evenData){
						$('.table-tr-selected', newTable).removeClass('table-tr-selected');
						$(this).addClass('table-tr-selected');
						properties.selectable($(this));
					});
				}
			});
		};
		
		var doSort = function(table, th, type){
			
			var upArrow = '\u2191';
			var downArrow = '\u2193';
			
			var stripArrow = function(text){
				var lastChar = text[text.length - 1] ;
				if(lastChar === upArrow || lastChar == downArrow){
					return stripArrow(text.slice(0, text.length - 1));
				}else{
					return text;
				};
			};
			
			var index = th.col;

			var compareAlpha = function(tr1, tr2){
				var getText = function(tr){
					var cell = $('*:nth-child(' + (index+1) + ')', tr);
					return $(cell).text();
				};
				var text1 = getText(tr1);
				var text2 = getText(tr2);
				return ((text1 < text2) ? -1 : ((text1 > text2) ? 1 : 0));
			};
			
			var compareNumeric = function(tr1, tr2){
				var getText = function(tr){
					var cell = $('*:nth-child(' + (index+1) + ')', tr);
					return $(cell).text();
				};
				return getText(tr1) - getText(tr2);
			};
			
			var compareVersion = function(tr1, tr2){
				var getText = function(tr){
					var cell = $('*:nth-child(' + (index+1) + ')', tr);
					return $(cell).text();
				};
				var ver1 = getText(tr1).split('.');
				var ver2 = getText(tr2).split('.');
				var result = ver1[0] - ver2[0];
				if(result == 0){
					result = ver1[1] - ver2[1];
				}
				if(result == 0){
					result = ver1[2] - ver2[2];
				}
				if(result == 0){
					result = ((ver1[3] < ver2[3]) ? -1 : ((ver1[3] > ver2[3]) ? 1 : 0));
				}
				return result;
			};
			
			var revCompareAlpha = function(tr1, tr2) {
				return -compareAlpha(tr1, tr2);
			};
			
			var revCompareNumeric = function(tr1, tr2) {
				return -compareNumeric(tr1, tr2);
			};
			
			var revCompareVersion = function(tr1, tr2){
				return -compareVersion(tr1, tr2);
			};
			
			var ths = $(th).siblings();
			ths.removeClass('table-th-sort');
			ths.removeClass('table-th-sort-rev');
			ths.each(function(i,th){
				var thx = $(th);
				thx.text(stripArrow(thx.text()));
			});
			
			var isSorted = th.hasClass('table-th-sort');
			if(isSorted){
				th.removeClass('table-th-sort');
				th.addClass('table-th-sort-rev');
				th.text(stripArrow(th.text()) + ' ' + upArrow);
			}else{
				th.removeClass('table-th-sort-rev');
				th.addClass('table-th-sort');
				th.text(stripArrow(th.text()) + ' ' + downArrow);
			}
			
			var tBody = $('tbody', table);
			var tRows = tBody.children();
			tRows.remove();
			if(type == 'numeric'){
				tRows.sort(isSorted ? revCompareNumeric : compareNumeric);
			}else if(type == 'alpha'){
				tRows.sort(isSorted ? revCompareAlpha : compareAlpha);
			}else{
				tRows.sort(isSorted ? revCompareVersion : compareVersion);
			}
			tBody.append(tRows);
		};
		
		var sortTable = function(clickEvent){
			var th = clickEvent.data.header;
			var table = th.parents('table');
			var sortType = clickEvent.data.type;
			doSort(table, th, sortType);
			decorate(table);
		};
		
		var newTable = $('<table />');
		var sortTh = null;
		if(properties.headers){
			var tHeadRow = $('<tr />');
			$.each(properties.headers, function(index, item){
				var th = $('<th>' + item.title + '</th>');
				th.col = index;
				if (properties.sortable) {
					th.click({header: th, type: item.type}, sortTable);
				}
				tHeadRow.append(th);
				if (properties.sortable && index == properties.sortIndex) {
					sortTh = th;
				}
			});
			newTable.append($('<thead />', {'class': 'table-head'}).append(tHeadRow));
		}
		if(properties.clazz){
			newTable.addClass(properties.clazz);
		}
		if(properties.id){
			newTable.attr(properties.id);
		}
		if(properties.sortable){
			newTable.addClass('table-sortable');
		}
		if(properties.selectable){
			newTable.addClass('table-selectable');
			newTable.addClass('table-hoverable');
		}
		if(properties.hoverable){
			newTable.addClass('table-hoverable');
		}
		
		var tBody = $('<tbody />', {'class': 'table-body'});
		if(properties.rows){
			$.each(properties.rows, function(i, row){
				var newRow = $('<tr />');
				$.each(row, function(j, value){
					if(value instanceof Object){
						newRow.append(value);
					}else{
						newRow.append($('<td>' + value + '</td>'));
					}
				});
				tBody.append(newRow);
			});
		}
		newTable.append(tBody);
		
		if(properties.sortable && sortTh != null){
			doSort(newTable, sortTh, properties.headers[0].type);
		};
		decorate(newTable);
		
		newTable.getElementOffset = function(elementInTable){
			return elementInTable.position().top - $('thead', this).position().top - 25;
		};
		
		return newTable;
	};
	
};

/**
 * 
 */
var InfoBox = function(properties){
	
	var self = this;
	
	self.isVisible = false;
	self.dialogBox = $('<div />').addClass(properties.name).addClass('info-box');
	self.title = $('<div />', {'class': 'box-title'}).text(properties.title);
	self.content = $('<div />', {'class': 'box-content'}).append(properties.content);

	self.dialogBox.append(self.title);
	self.dialogBox.append(self.content);

	if(properties.closeable){
		self.title.append($('<div />', {'class': 'box-title-close'}).append('x').click(function(event){
			event.stopPropagation();
			self.hide();
		}));
	}

	if(properties.error){
		self.dialogBox.addClass('error-info-box');
	}
	
	self.dialogBox.draggable({  scroll: false, stack: '.info-box', handle: self.title});
	
	self.addContent = function(newContent){
		$.each(newContent.children(), function(index, item){
			self.content.append(item);
		});
	};
	
	self.show = function(relativeTo){
		if(!self.isVisible){
			$("li", self.dialogBox).removeClass('li-odd');
			$("li:odd", self.dialogBox).addClass('li-odd');
			
			var displayPosition = {};
			if(relativeTo){
				var position = relativeTo.position();
				displayPosition.x = position.left + 15;
				displayPosition.y = position.top + 25;
			} else {
				var position = $('#content').position();
				var infoBoxCount = $('.info-box').length;
				var floorCount = Math.floor(infoBoxCount/10);
				var xOffSet = floorCount * 250;
				infoBoxCount = infoBoxCount - (floorCount * 10);
				displayPosition.x = position.left + 40 + xOffSet + (infoBoxCount*25);
				displayPosition.y = position.top + 50 + (infoBoxCount*25);
			}
			self.dialogBox.css({position: 'absolute', 
								left: displayPosition.x, 
								top: displayPosition.y, 
								'z-index': self.getHighestZIndex() + 1});
			self.dialogBox.show();	
			$('body').append(self.dialogBox);
			self.isVisible = true;
		}else{
			self.dialogBox.css({'z-index': self.getHighestZIndex() + 1});
		}
		return self;
	};
	
	self.dialogBox.click(self.show);
	
	self.getHighestZIndex = function(){
		var zIndex = 0;
		$.each($('.info-box'), function(index, otherInfoBox){
			var checkZIndex = new Number($(otherInfoBox).css('z-index')).valueOf();
			if(checkZIndex > zIndex){
				zIndex = checkZIndex;
			}
		});
		return zIndex;
	};
	
	self.hide = function(){
		if(self.isVisible){
			self.dialogBox.detach();
			self.dialogBox.hide();
			self.isVisible = false;
		}
	};
	
};

/**
 * 
 */
var ObjectName = function(domain, properties, objectName){
	
	var self = this;
	
	self.domain = domain;
	self.properties = properties;
	self.toString = objectName;
	
	self.get = function(key){
		return self.properties[key];
	};
	
	self.compare = function(other){
		 if (self.toString < other.toString) {
			 return -1;
		 }
		 if (self.toString > other.toString) {
			 return 1;
		 }
		 return 0; 
	};
	
};

// SERVERS GUI SECTION

var Servers = function(){

	this.open = false;
	
	/*
	 * This takes the server string in the URL and sets the servers ServersDisplay.servers to an array of ip address.
	 */
	this.loadServers = function() {
		var rows;
		if(util.queryHash.s){
			rows = util.queryHash.s.split(',');
			if(!rows.contains(util.getHostAndAdminPath())){
				rows.push(util.getHostAndAdminPath());
			}
		} else {
			rows = [util.getHostAndAdminPath()];
		}
		
		var serversTable = util.makeTable({
			headers: ['Configured Servers'],
			clazz: 'servers-table'
		});

		var tBody = $('tbody', serversTable);
		
		$.each(rows, function(index, item){
			var newRow = $('<tr />');
			newRow.append($('<td>' + item + '</td>\
							 <td class="server-status">Status</td>\
							 <td class="server-link">Overview</td>\
							 <td class="server-link">Artifacts</td>\
							 <td class="server-link">Repositories</td>\
							 <td class="server-link">Configuration</td>\
							 <td class="server-link">Dumps</td>'));
			tBody.append(newRow);
		});
		
		$('#servers-list').replaceWith(serversTable);
	};

	this.toggle = function() {
		$('#servers').slideToggle(util.fxTime);
		if(this.open) {
			$('#servers-button').removeClass('selected-navigation');
			this.open = false;
		} else {
			$('#servers-button').addClass('selected-navigation');
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
			var virgo;
			if(response[0].value.info.extraInfo){
				virgo = ['OSGi Container', response[0].value.info.product + ' ' + response[0].value.info.version + ' (' + response[0].value.info.extraInfo.type + ')'];
			}else{				
				virgo = ['OSGi Container', response[0].value.info.product + ' ' + response[0].value.info.version];
			}
			var runtime = ['Virtual Machine', response[5].value + ' version ' + response[6].value + ' (' + response[4].value + ')'];
			var os = ['Operating System', response[1].value + ' ' + response[2].value + ' (' + response[3].value + ')'];
			callback([virgo, runtime, os]);
		}); 
	};

};
