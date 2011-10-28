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
	new Request.JSON({
		url: util.getCurrentHost() + '/jolokia/search/org.eclipse.virgo.kernel:type=Configuration,*', 
		method: 'get',
		onSuccess: function (responseJSON){
			configurationViewer = new ConfigurationViewer();
			configurationViewer.renderConfigurationMBeans(responseJSON.value);
		}
	}).send();
}

var ConfigurationViewer = function(){

	this.configs = [];
	
	this.renderConfigurationMBeans = function(mbeans){
		
		mbeans.each(function(item, index){
			var objectName = util.readObjectName(item);
			var label = this.getConfigurationLabel(objectName.get('name'), $('config-list'));
			label.firstChild.set('onclick', "configurationViewer.configs[" + index + "].toggle()");
			var config = new Configuration(objectName, label);
			if(util.pageLocation && util.pageLocation == objectName.get('name')){
				config.toggle();
			}
			this.configs[index] = config;
		}.bind(this));
	
		util.pageReady();
	};
	
	// Private methods
	
	this.getConfigurationLabel = function(labelText, parentElement){
		var configContainer = new Element('div');
		configContainer.addClass('config-container');
		
		var configLabel = new Element('div');
		configLabel.addClass('config-label');
		
		var configIcon = getIconElement('tree-icons/plus.png');
		configIcon.addClass('plus');
		configIcon.inject(configLabel);
		
		var text = new Element('span');
		text.appendText(labelText);
		text.inject(configLabel);
	
		configLabel.inject(configContainer);
		configContainer.inject(parentElement);
		return configContainer;
	};
};


var Configuration = function(objectName, label){

	this.objectName = objectName;
	
	this.name = objectName.get('name');
	
	this.label = label;
	
	this.icon = label.firstChild.firstChild;
	
	this.toggle = function(){
		var isClosed = this.icon.hasClass('plus');
		this.setPlusMinusIcon('loader-small.gif', 'spinnerIcon');
		if(isClosed){
			new Request.JSON({
				url: util.getCurrentHost() + '/jolokia/read/' + this.objectName.toString, 
				method: 'get',
				onSuccess: this.createTable.bind(this)
			}).send();
		} else {
			this.label.getChildren()[1].nix(true);
			this.setPlusMinusIcon('tree-icons/plus.png', 'plus');
		}
	};
	
	//Private methods
	
	this.setPlusMinusIcon = function (iconName, className){
		var oldIcon = this.icon;
		var newIcon = getIconElement(iconName);
		newIcon.addClass(className);
		newIcon.replaces(oldIcon);
		this.icon = newIcon;
		oldIcon.destroy();
	};
	
	this.createTable = function(json){
		var tableRows = [];
		Object.each(json.value.Properties, function(value, key){
			tableRows.push([key, value]);
		});
							
		var tableHolder = new Element('div');
		tableHolder.addClass('config-properties');
		tableHolder.inject(this.label);
			
		var propertiesTable = new HtmlTable({ 
			properties: {'class': "config-table"}, 
			headers: ['Key', 'Value'], 
			rows: tableRows,
			zebra: true
		});
		propertiesTable.inject(tableHolder);
		tableHolder.set('reveal', {duration: util.fxTime});
		tableHolder.reveal();	
		this.setPlusMinusIcon('tree-icons/minus.png', 'minus');
	};
	
};

/**
 * Create an element with a background image applied
 * 
 * @param iconName - for the image
 */
function getIconElement(icon){
	var imageElement = new Element('div');
	imageElement.addClass('tree-icon');
	imageElement.set('styles', {'background': 'url("' + util.getCurrentHost() + '/resources/images/' + icon + '") no-repeat center center'});
	return imageElement;
};
