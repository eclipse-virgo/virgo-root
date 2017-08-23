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
	util.doQuery('search/org.eclipse.equinox.region.domain:type=Region,*', function(response){
		$.each(response.value, function(index, region){
			var regionName = util.readObjectName(region).get('name');
			util.doQuery('exec/osgi.compendium:region=' + regionName + ',service=cm,version=1.3/getConfigurations/(service.pid=*)', function(response){
			ConfigurationInit.addConfigurationSection(regionName, response.value);
			});
		});
		util.pageReady();
	});
}


ConfigurationInit = {

	addConfigurationSection : function(regionName, json){
		ConfigurationInit.appendConfigurationHeader(regionName);
		$.each(json, function(index, item){
			var pid = item[0];
			var location = item[1];
			var label = ConfigurationInit.getConfigurationLabel(pid);
			var config = new Configuration(pid, location, regionName, label);
			if(util.pageLocation && util.pageLocation == pid){
				config.toggle();
			}
			$('.config-label', label).click(config, function(event){
				event.data.toggle();
			});
		});
	},
	
	getConfigurationLabel : function(labelText){
		var configContainer = $('<div />', {'class': 'config-container'});
		var configLabel = $('<div />', {'class': 'config-label'});
		configLabel.append($('<div />', {'class': 'tree-icon plus'}).css('background', 'url("' + util.getHostAndAdminPath() + '/resources/images/tree-icons/plus.png") no-repeat center center'));
		configLabel.append($('<span />').text(labelText));
		configContainer.append(configLabel);
		$('#config-list').append(configContainer);
		return configContainer;
	},
	
	appendConfigurationHeader : function(regionName){
		var configLabel = $('<div />', {'class': 'config-header'});
		configLabel.append($('<span />').text('Region: ' + regionName));
		$('#config-list').append(configLabel);
	}
	
};


var Configuration = function(pid, location, regionName, label){
	
	var self = this;
	
	self.location = location;
	
	self.regionName = regionName;
	
	self.pid = pid;
	
	self.label = label;
	
	self.icon = $('.tree-icon', label);
	
	self.toggle = function(){
		if(self.icon.hasClass('plus')){
			self.setPlusMinusIcon('loader-small.gif', 'spinnerIcon');
			util.doQuery('exec/osgi.compendium:region=' + self.regionName + ',service=cm,version=1.3/getProperties/' + pid, self.createTable);
		} else {
			$('.config-properties', self.label).slideToggle(util.fxTime, function(){
				$(this).remove();
			});
			self.setPlusMinusIcon('tree-icons/plus.png', 'plus');
		}
	};
	
	self.setPlusMinusIcon = function (icon, className){
		self.icon.css('background', 'url("' + util.getHostAndAdminPath() + '/resources/images/' + icon + '") no-repeat center center');
		self.icon.removeClass('plus').removeClass('minus').removeClass('spinnerIcon').addClass(className);
	};
	
	self.createTable = function(json){
		var tableRows = new Array();
		$.each(json.value, function(index, item){
			tableRows.push([index, item.Value]);
		});
			
		var propertiesTable = util.makeTable({ 
			clazz: "config-table", 
			headers: [{title: 'Property'}, {title: 'Value'}], 
			rows: tableRows
		});

		var tableHolder = $('<div />', {'class' : 'config-properties'}).append(propertiesTable);
		self.label.append(tableHolder);
		tableHolder.slideToggle(util.fxTime);
		self.setPlusMinusIcon('tree-icons/minus.png', 'minus');
	};
	
};
