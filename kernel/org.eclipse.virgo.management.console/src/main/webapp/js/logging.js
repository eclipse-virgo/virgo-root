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

	util.doQuery('read/ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator/LoggerList', function (response){
		loggingHandler = new LoggerList(response.value);
		loggingHandler.renderLoggerList();
		util.pageReady();
	});
	
}


var LoggerList = function(loggerList){
	
	var self = this;
	
	self.loggerList = loggerList;

	self.renderLoggerList = function(){
		$('#logging-display').empty();

		var rows = new Array();
		$.each(loggerList, function(index, loggerName){
			var row = new Array(loggerName);
			var td = $('<td />', {'id': 'click-' + loggerName.replace(new RegExp('\\.', 'g'), '_'), 'class': 'logger-update','onClick': 'loggingHandler.displayLoggerInfo("' + loggerName + '")'});
			row.push(td.text('view/edit'));
			rows.push(row);
		});
		
		self.loggerTable = util.makeTable({
			clazz: 'logger-table',
			headers: [{title: 'Logger Name', type: 'alpha'}, {title: '', type: 'alpha'}],
			rows: rows,
			hoverable: true,
			sortable: true,
			sortIndex: 0
		});
		
		$('#logging-display').append(self.loggerTable);
	};
	
	self.displayLoggerInfo = function(loggerName){
		var request = new Array({
			"type": "EXEC",
			"mbean": "ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator",
			"operation": "getLoggerLevel",
			"arguments": [loggerName]
		},{
			"type": "EXEC",
			"mbean": "ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator",
			"operation": "getLoggerEffectiveLevel",
			"arguments": [loggerName]
		});
		
		var cssLoggerName = loggerName.replace(new RegExp('\\.', 'g'), '_');
		
		util.doBulkQuery(request, function(response) {
			var name = 'updatelevel-' + cssLoggerName;			
			var title = 'Update a Logging level';
			var content = $('<div />');
			
			var tableRows = new Array();

			tableRows.push(['Logger: ', loggerName]);
			tableRows.push(['Logger Level: ', response[0].value]);
			tableRows.push(['Effective Logger Level: ', response[1].value]);
			
			var levelSelector = $('<select />', {id: 'level-selector-' + cssLoggerName, type: 'select'});
			levelSelector.append($('<option />', {value: 'ERROR'}).text('Error'));
			levelSelector.append($('<option />', {value: 'WARN'}).text('Warn'));
			levelSelector.append($('<option />', {value: 'INFO'}).text('Info'));
			levelSelector.append($('<option />', {value: 'DEBUG'}).text('Debug'));
			levelSelector.append($('<option />', {value: 'TRACE'}).text('Trace'));
			tableRows.push(['New Level', levelSelector]);
			
			var propertiesTable = util.makeTable({ 
				clazz: "logger-details-table", 
				headers: [], 
				rows: tableRows
			});
			
			content.append(propertiesTable);
			
			var link = $('<div />', {'class': 'button-container'});
			var linkButton = $('<div />', {'class': 'button'});
			linkButton.append($('<div />', {'class': 'button-cap-left-blue'}));
			linkButton.append($('<div />', {'class': 'button-text'}).text('Submit'));
			linkButton.append($('<div />', {'class': 'button-cap-right-blue'}));
			link.append(linkButton);
			content.append(link);
			
			var infoBox = new InfoBox({name: name, title: title, content: content, closeable: true}).show($('#click-' + cssLoggerName).parent());
			
			link.click({loggerName: loggerName, cssLoggerName: cssLoggerName}, function(event){
				event.stopPropagation();
				var newLevel = $('#level-selector-' + event.data.cssLoggerName).val();				
				infoBox.hide();
				util.doQuery('exec/ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator/setLoggerLevel/' + event.data.loggerName + '/' + newLevel, function (response){
					self.displayLoggerInfo(event.data.loggerName);
				});
			});
			
		});
		

		
	};
	
};
