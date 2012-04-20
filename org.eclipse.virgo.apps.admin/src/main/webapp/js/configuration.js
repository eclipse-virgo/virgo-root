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
 * Scripts to be loaded in to the head of the configuration view
 */
function pageinit(){
	$.ajax({
		url: util.getCurrentHost() + '/jolokia/search/org.eclipse.virgo.kernel:type=Configuration,*',
		dataType: 'json',
		success: ConfigurationInit.init
	});
}

ConfigurationInit = {

	init : function(json){
		$.each(json.value, function(index, item){
			var objectName = util.readObjectName(item);
			var label = ConfigurationInit.getConfigurationLabel(objectName.get('name'));
			var config = new Configuration(objectName, label);
			if(util.pageLocation && util.pageLocation == objectName.get('name')){
				config.toggle();
			}
			$('.config-label', label).click(config, function(event){
				event.data.toggle();
			});
		});
		util.pageReady();
	},
	
	getConfigurationLabel : function(labelText){
		var configContainer = $('<div />', {'class': 'config-container'});
		var configLabel = $('<div />', {'class': 'config-label'});
		configLabel.append($('<div />', {'class': 'tree-icon plus'}).css('background', 'url("' + util.getCurrentHost() + '/resources/images/tree-icons/plus.png") no-repeat center center'));
		configLabel.append($('<span />').text(labelText));
		configContainer.append(configLabel);
		$('#config-list').append(configContainer);
		return configContainer;
	}
};


var Configuration = function(objectName, label){
	
	this.objectName = objectName;
	
	this.name = objectName.get('name');
	
	this.label = label;
	
	this.icon = $('.tree-icon', label);
	
	this.toggle = function(){
		var isClosed = this.icon.hasClass('plus');
		this.setPlusMinusIcon('loader-small.gif', 'spinnerIcon');
		if(isClosed){
			$.ajax({
				url: util.getCurrentHost() + '/jolokia/read/' + this.objectName.toString,
				dataType: 'json',
				context: this,
				success: this.createTable
			});
			
		} else {
			$('.config-properties', this.label).slideToggle(util.fxTime, function(){
				$(this).remove();
			});
			this.setPlusMinusIcon('tree-icons/plus.png', 'plus');
		}
	};
	
	this.setPlusMinusIcon = function (icon, className){
		this.icon.css('background', 'url("' + util.getCurrentHost() + '/resources/images/' + icon + '") no-repeat center center');
		this.icon.removeClass('plus');
		this.icon.removeClass('minus');
		this.icon.removeClass('spinnerIcon');
		this.icon.addClass(className);
	};
	
	this.createTable = function(json){
		var tableRows = new Array();
		$.each(json.value.Properties, function(index, item){
			tableRows.push([index, item]);
		});
			
		var propertiesTable = util.makeTable({ 
			clazz: "config-table", 
			headers: ['Key', 'Value'], 
			rows: tableRows
		});

		var tableHolder = $('<div />', {'class' : 'config-properties'}).append(propertiesTable);
		this.label.append(tableHolder);
		tableHolder.slideToggle(util.fxTime);
		this.setPlusMinusIcon('tree-icons/minus.png', 'minus');
	};
	
};
