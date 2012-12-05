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
 * Scripts to be loaded in to the head of the logging view
 */
function pageinit(){

	$.ajax({
		url: util.getCurrentHost() + '/jolokia/read/ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator/LoggerList',
		dataType: 'json',
		success: function (response) {
			loggingHandler = new LoggerList(response.value);
			util.pageReady();
		}
	});
	
}


var LoggerList = function(loggerList){
	
	var self = this;
	
	self.loggerList = loggerList;

	self.rows = new Array();
	
	$.each(loggerList, function(index, loggerName){
		var row = new Array(loggerName, '', '');
		
		var td = $('<td />', {'class' : 'logger-update','onClick': 'loggingHandler.update("' + loggerName + '")'});
		row.push(td.text('update'));
		self.rows.push(row);
	});
	
	self.loggerTable = util.makeTable({
		clazz: 'logger-table',
		headers: [{title: 'Name', type: 'alpha'}, {title: 'Level', type: 'alpha'}, {title: 'Effective Level', type: 'alpha'}, {title: 'Update', type: 'alpha'}],
		rows: self.rows,
		hoverable: true,
		sortable: true,
		sortIndex: 0
	});
	
	$('#logging-display').append(self.loggerTable);
	
	self.displayLoggerList = function(){
		
	};
	
	self.displayLoggerListResult = function(loggerList){
		self.loggerList = loggerList;
		
	};
	
	self.update = function(loggerName){
		var name = 'updatelevel' + loggerName;
		var title = 'Update a Logging level';
		var content = $('<div />');
		content.append($('<div />').text('New level for: ' + loggerName));
		content.append($('<input />', {type: 'text'}).text('New level for: ' + loggerName));
		
		var link = $('<div />', {'class': 'button-container'});
		
		var linkButton = $('<div />', {'class': 'button'});
		
		linkButton.append($('<div />', {'class': 'button-cap-left-blue'}));
		linkButton.append($('<div />', {'class': 'button-text'}).text('Submit'));
		linkButton.append($('<div />', {'class': 'button-cap-right-blue'}));
		link.append(linkButton);
		content.append(link);
		
		var infoBox = new InfoBox({name: name, title: title, content: content, closeable: true}).show();
		
		link.click(function(event){
			event.stopPropagation();
			infoBox.hide();
		});
		
	};
	
};
