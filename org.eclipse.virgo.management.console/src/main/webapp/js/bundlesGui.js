/*******************************************************************************
* Copyright (c) 2011 David Normiongton
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Chris Frost - initial contribution
*   
*******************************************************************************/

var LayoutManager = function(bundleCanvas, width, height, dataSource){

	var self = this;

	self.relationshipType = 'wires';
	
	self.relationships = {};

	self.dataSource = dataSource;

	self.bundles = {};
	
	self.focused = NaN;

	self.bundleSpacing = 10; //Pixels to leave between bundles when rendering

	self.bundleCanvas = $('#' + bundleCanvas);
	
	self.paper = Raphael(bundleCanvas, width, height);
		
	self.setFocusListener = function(listener) {
		self.focusListener = listener;
	};
	
	self.displayType = function(type){
		if(type == 'wires') {
			$('#view-wires-button').addClass('button-selected');
			$('#view-services-button').removeClass('button-selected');
			self.relationshipType = type;
			if(!isNaN(self.focused)){
				self.displayBundle(self.focused);
			}
		} else if(type == 'services'){
			$('#view-services-button').addClass('button-selected');
			$('#view-wires-button').removeClass('button-selected');
			self.relationshipType = type;
			if(!isNaN(self.focused)){
				self.displayBundle(self.focused);
			}
		}
	};
	
	self.displayBundle = function(bundleId){
		if(!isNaN(bundleId) && bundleId >= 0){
			self.hideAll();
			self.bundleCanvas.addClass('spinner-large');
			self.focused = NaN;
			self.dataSource.updateBundle(bundleId, function(){
				var bundle;
				if(self.bundles[bundleId]){
					bundle = self.bundles[bundleId];
					bundle.relationship.infoBoxData = new Array();
				}else{
					bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId], 5, 5, self.displayBundle, 'middle', self.relationshipType);
					self.bundles[bundleId] = bundle;
				}
				self.focused = bundleId;
				
				if(self.relationshipType == 'wires'){
					self.renderWires(bundle);
				}else{
					self.renderServices(bundle);
				}
				
				var middleX = Math.round(self.paper.width/2);
				var middleY = Math.round(self.paper.height/2);
				var selfInfoBoxData = bundle.relationship.infoBoxData;
				bundle.reset(middleX - (bundle.boxWidth/2), middleY, 'middle', self.relationshipType);
				bundle.relationship.infoBoxData = selfInfoBoxData;
				self.bundleCanvas.scrollLeft(middleX - (self.bundleCanvas.width()/2));
				
				$.each(self.bundles, function(key, localBundle){
					if(localBundle.state == 'waiting'){
						localBundle.show();
					}
				});
				
				self.bundleCanvas.removeClass('spinner-large');
			});
			if(self.focusListener){
				self.focusListener(bundleId);
			}
		} else {
			alert('No such Bundle: ' + bundleId);
		}
	};
	
	self.renderWires = function(bundle){
		var topRowBundleIds = {};
		$.each(bundle.rawBundle.RequiredWires, function(index, wire){
			self.addRelationshipToMap(topRowBundleIds, wire.ProviderBundleId, wire);
		});
		var topRowRenderResult = self.renderBundleRow(topRowBundleIds, -239, 'top', bundle);
		var bottomRowBundleIds = {};
		$.each(bundle.rawBundle.ProvidedWires, function(index, wire){
			self.addRelationshipToMap(bottomRowBundleIds, wire.RequirerBundleId, wire); 
		});
		var bottomRowRenderResult = self.renderBundleRow(bottomRowBundleIds, 239, 'bottom', bundle);
		self.centerBundles(topRowRenderResult, bottomRowRenderResult);
	};
	
	self.renderServices = function(bundle){
		var topRowBundleIds = {};
		$.each(bundle.rawBundle.ServicesInUse, function(index, service){
			self.addRelationshipToMap(topRowBundleIds, service.BundleIdentifier, {'service': service, 'consumerId': bundle.rawBundle.Identifier});
		});
		var topRowRenderResult = self.renderBundleRow(topRowBundleIds, -239, 'top', bundle);
		var bottomRowBundleIds = {};
		$.each(bundle.rawBundle.RegisteredServices, function(index, service){
			$.each(service.UsingBundles, function(index, bundleId){
				if(bundleId != bundle.rawBundle.Identifier){
					self.addRelationshipToMap(bottomRowBundleIds, bundleId, {'service': service, 'consumerId': bundleId});
				}
			});
		});
		var bottomRowRenderResult = self.renderBundleRow(bottomRowBundleIds, 239, 'bottom', bundle);
		self.centerBundles(topRowRenderResult, bottomRowRenderResult);
	};
	
	self.addRelationshipToMap = function(map, bundleId, relationship){
		if(!map[bundleId]){
			map[bundleId] = new Array();
		}
		map[bundleId].push(relationship);
	};
	
	self.renderBundleRow = function(bundleIds, offset, position, focusedBundle){
		var yPos = (self.paper.height/2) + offset;
		var xPos = this.bundleSpacing;
		var focusedBundleId = focusedBundle.rawBundle.Identifier;
		var renderedBundleIds = new Array();
		$.each(bundleIds, function(bundleId, relationshipInfoDatas){
			$.each(relationshipInfoDatas, function(index, relationshipInfoData){
				if(!self.bundles[bundleId]){
					var rawBundle;
					if(bundleId >= 0){
						rawBundle = self.dataSource.bundles[bundleId];
					}else{
						rawBundle = {'SymbolicName': 'unknown', 'Version': 'unknown', 'Identifier': -1};
					}
					var bundle = new Bundle(self.paper, rawBundle, xPos, yPos, self.displayBundle, position, self.relationshipType, focusedBundle);
					bundle.increaseCount(relationshipInfoData);
					renderedBundleIds.push(bundleId);
					self.bundles[bundleId] = bundle;
					xPos = xPos + bundle.boxWidth + self.bundleSpacing;
				}else if(focusedBundleId == bundleId){
					focusedBundle.increaseCount(relationshipInfoData);
				}else {
					var bundle = self.bundles[bundleId];
					if(bundle.state == 'waiting'){
						if(position != bundle.position){
							bundle.increaseBackCount(relationshipInfoData);
						}else{
							bundle.increaseCount(relationshipInfoData);
						}
					}else{//bundle.state == 'hidden'
						bundle.reset(xPos, yPos, position, self.relationshipType, focusedBundle);
						bundle.increaseCount(relationshipInfoData);
						renderedBundleIds.push(bundleId);
						xPos = xPos + bundle.boxWidth + self.bundleSpacing;
					}
				}
			});
		});
		return {bundleIds: renderedBundleIds, width: xPos};
	};
	
	self.centerBundles = function(topRowRenderResult, bottomRowRenderResult){
		var newWidth = topRowRenderResult.width < bottomRowRenderResult.width ? bottomRowRenderResult.width : topRowRenderResult.width;
		if(newWidth < self.bundleCanvas.width()){
			self.paper.setSize(self.bundleCanvas.width(), self.paper.height);
			newWidth = self.bundleCanvas.width();
		}else{
			self.paper.setSize(newWidth, self.paper.height);
		}
		if(topRowRenderResult.width < newWidth){
			self.moveRow(topRowRenderResult.bundleIds, Math.round((newWidth-topRowRenderResult.width)/2));
		}
		if(bottomRowRenderResult.width < newWidth){
			self.moveRow(bottomRowRenderResult.bundleIds, Math.round((newWidth-bottomRowRenderResult.width)/2));
		}
	};
	
	self.moveRow = function(bundleIds, horizontalAdjustment){
		$.each(bundleIds, function(index, bundleId){
			self.bundles[bundleId].moveHorizontaly(horizontalAdjustment);
		});
	};

	self.hideAll = function(){
		$.each(self.bundles, function(index, bundle){
			bundle.hide();
		});
	};
	
};

/**
 * Bundle
 */
var Bundle = function(paper, rawBundle, x, y, dblClickCallback, position, type, otherBundle){

	var self = this;
	
	self.paper = paper;
	self.rawBundle = rawBundle;
	self.dblClickCallback = dblClickCallback;
	self.position = position;
	if(position == 'top'){
		self.relationship = new Relationship(self.paper, type, self, otherBundle);
	}else if(position == 'bottom'){
		self.relationship = new Relationship(self.paper, type, otherBundle, self);
	}else{
		self.relationship = new Relationship(self.paper, type, self, self);
	};	
	self.state = 'waiting'; //One of 'hidden', 'waiting' or 'visible'
	//Display attributes
	self.bundleMargin = 8;
	self.boxHeight = 28 + (2*self.bundleMargin); // Starter value to get text in the right place
	self.x = Math.round(x); //Left edge of the box
	self.y = Math.round(y); //Middle of the box
	
	self.stroke = '#002F5E';
	if(self.rawBundle.Identifier == -1){
		self.stroke = '#FF0000';
	}
	
	self.summary = '[' + self.rawBundle.Identifier + '] ' + self.rawBundle.SymbolicName + '\n' + self.rawBundle.Version;
	
	self.text = self.paper.text(self.x + self.bundleMargin, self.y, self.summary).attr({
		'text-anchor' : 'start', 
		'font' : '12px Arial'
	}).hide();
	
	self.boxWidth = Math.round(self.text.getBBox().width) + (3*self.bundleMargin);
	self.boxHeight = Math.round(self.text.getBBox().height) + (2*self.bundleMargin);
	
	self.box = self.paper.rect(self.x, self.y - (self.boxHeight/2), self.boxWidth, self.boxHeight, self.bundleMargin).attr({
		'fill' : '#E8F6FF', 
		'stroke' : self.stroke
	}).hide();
	
	self.info = self.paper.text(self.x + (self.boxWidth - 1.5*self.bundleMargin), self.y, 'i').attr({
		'text-anchor' : 'start', 
		'font-size' : '17px',
		'font-family' : 'serif',
		'font-weight' : 'bold',
		'font-style' : 'italic',
		'fill' : self.stroke,
		'cursor' : 'pointer'
	}).hide();
	
	self.box.toBack();
	
	self.box.dblclick(function(){
		self.dblClickCallback(self.rawBundle.Identifier);
	});
	
	self.text.dblclick(function(){
		self.dblClickCallback(self.rawBundle.Identifier);
	});

	self.info.click(function(){
		if(!self.infoBox){
			self.infoBox = self.getInfoBoxWithBundle(self.rawBundle);
		}
		self.infoBox.show();
	});
	
	self.show = function(){
		if(self.state == 'waiting'){
			self.text.show();
			self.box.show();
			self.info.show();
			self.relationship.show();
			self.state = 'visible';
		}
	};
	
	self.hide = function(){
		if(self.state != 'hidden'){
			self.text.hide();
			self.box.hide();
			self.info.hide();
			self.infoBox = undefined;//Force it to be reloaded as the underlying data may have changed
			self.relationship.hide();
			self.state = 'hidden';
		}
	};
	
	self.reset = function(x, y, position, type, otherBundle) {
		if(self.state == 'visible'){
			self.hide();
		}
		self.x = Math.round(x);
		self.y = Math.round(y);
		self.text.attr({
			'x' : self.x + self.bundleMargin, 
			'y' : self.y
		});
		self.box.attr({
			'x' : self.x,
			'y' : self.y - (self.boxHeight/2)
		});
		self.info.attr({
			'x' : self.x + (self.boxWidth - 1.5*self.bundleMargin), 
			'y' : self.y
		});
		self.position = position;
		if(position == 'top'){
			self.relationship.reset(type, self, otherBundle);
		}else if(position == 'bottom'){
			self.relationship.reset(type, otherBundle, self);
		}else{
			self.relationship.reset(type, self, self);
		};
		self.state = 'waiting';
	};
	
	self.moveHorizontaly = function(horizontalAdjustment){
		self.x = self.x + horizontalAdjustment;
		self.text.attr({
			'x' : self.x + self.bundleMargin, 
		});
		self.box.attr({
			'x' : self.x,
		});
		self.info.attr({
			'x' : self.x + (self.boxWidth - 1.5*self.bundleMargin), 
		});
	};
	
	self.increaseCount = function(relationshipInfoData) {
		self.relationship.increaseCount(relationshipInfoData);
	};
	
	self.increaseBackCount = function(relationshipInfoData) {
		self.relationship.increaseBackCount(relationshipInfoData);
	};
	
	self.getInfoBoxWithBundle = function(rawBundle){
		var name = 'bundle' + rawBundle.Identifier;
		var title = 'Bundle [' + rawBundle.Identifier + '] ' + rawBundle.SymbolicName + ': ' + rawBundle.Version;
		var infoBox = $('<ul></ul>');
		var error = false;
		if(rawBundle.Identifier >= 0){
			infoBox.append($('<li>Region - ' + rawBundle.Region + '</li>'));
			infoBox.append($('<li>Location - ' + rawBundle.Location + '</li>'));
	
			self.appendIfPresent(infoBox, 'State', rawBundle.State);
			self.appendIfPresent(infoBox, 'LastModified', rawBundle.LastModified);
			self.appendIfPresent(infoBox, 'Fragment', rawBundle.Fragment);
			self.appendIfPresent(infoBox, 'StartLevel', rawBundle.StartLevel);
			self.appendIfPresent(infoBox, 'PersistentlyStarted', rawBundle.PersistentlyStarted);
			self.appendIfPresent(infoBox, 'ActivationPolicyUsed', rawBundle.ActivationPolicyUsed);
			self.appendIfPresent(infoBox, 'Required', rawBundle.Required);
	
			if(!rawBundle.ExportedPackages || rawBundle.ExportedPackages.length == 0){
				infoBox.append($('<li>No exported packages</li>'));
			} else {
				infoBox.append($('<li>Exported packages</li>'));
				$.each(rawBundle.ExportedPackages, function(index, item){
					infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
				});
			}
			if(!rawBundle.ImportedPackages || rawBundle.ImportedPackages.length == 0){
				infoBox.append($('<li>No imported packages</li>'));
			} else {
				infoBox.append($('<li>Imported packages</li>'));
				$.each(rawBundle.ImportedPackages, function(index, item){
					infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
				});
			}
		} else {
			error = true;
			infoBox.append($('<li>This is a placeholder for the provider of missing Wires or Services that another Bundle want.</li>'));
		}
		return new InfoBox({name: name, title: title, content: infoBox, closeable: true, error: error});
	};
	
	self.appendIfPresent = function(element, name, field){
		if(field){
			element.append($('<li>' + name + ' - ' + field + '</li>'));	
		}
	};

};

var Relationship = function(paper, type, fromBundle, toBundle) {
	
	var self = this;
	
	self.paper = paper;
	self.fromBundle = fromBundle;
	self.toBundle = toBundle;
	self.type = type;
	self.infoBoxData = new Array();
	self.doubleEnded = 'none';
	self.controlPointOffset = 90;
	
	self.setCoordinates = function(){
		var startPoint;
		var endPoint;
		var startPointControl;
		var endPointControl;
			if(self.fromBundle.rawBundle.Identifier == self.toBundle.rawBundle.Identifier){
				var xOffSet = self.fromBundle.x + self.toBundle.boxWidth;
				startPoint = {'x' : xOffSet - 20, 'y' : self.fromBundle.y - (self.fromBundle.boxHeight/2)};
				endPoint = {'x' : xOffSet - 20, 'y' : Math.round(self.toBundle.y + (self.toBundle.boxHeight/2))};
				startPointControl = {'x' : startPoint.x + self.controlPointOffset, 'y' : startPoint.y - self.controlPointOffset}; 
				endPointControl = {'x' : endPoint.x + self.controlPointOffset, 'y' : endPoint.y + self.controlPointOffset};
				self.midPoint = {'x' : xOffSet + 47, 'y' : self.fromBundle.y};
			}else{
				startPoint = {'x' : Math.round(self.fromBundle.x + self.fromBundle.boxWidth/2), 'y' : self.fromBundle.y + (self.fromBundle.boxHeight/2)};
				endPoint = {'x' : Math.round(self.toBundle.x + self.toBundle.boxWidth/2), 'y' : self.toBundle.y - (self.fromBundle.boxHeight/2)};
				startPointControl = {'x' : startPoint.x, 'y' : startPoint.y + self.controlPointOffset}; 
				endPointControl = {'x' : endPoint.x, 'y' : endPoint.y - self.controlPointOffset};
				self.midPoint = self.calculateMidpoint(startPoint, endPoint);
			}
			self.pathString = 'M' + startPoint.x + ',' + startPoint.y + 
				'C' + startPointControl.x + ',' + startPointControl.y + 
				',' + endPointControl.x + ',' + endPointControl.y + 
				',' + endPoint.x + ',' + endPoint.y;
	};
	
	self.calculateMidpoint = function(startPoint, endPoint){
		var midX, midY;
		if(startPoint.x < endPoint.x){
			midX = startPoint.x + (endPoint.x - startPoint.x)/2;
		} else {
			midX = endPoint.x + (startPoint.x - endPoint.x)/2;
		}
		if(startPoint.y < endPoint.y){
			midY = startPoint.y + (endPoint.y - startPoint.y)/2;
		} else {
			midY = endPoint.y + (startPoint.y - endPoint.y)/2;
		}
		return {'x' : midX, 'y' : midY};
	};
	
	self.show = function() {
		if(0 < self.infoBoxData.length){
			self.setCoordinates();
			self.tooltip = 'From bundle ' + self.fromBundle.summary + '\n To bundle ' + self.toBundle.summary;
			self.visual = self.paper.path(self.pathString).attr({
				'arrow-start': self.doubleEnded,
				'stroke-width' : 3,
				'stroke' : '#002F5E'
			}).toBack();
			self.infoPoint = self.paper.circle(self.midPoint.x, self.midPoint.y, 10).attr({
				'fill' : '#BAD9EC', 
				'stroke' : 'none',
				'cursor' : 'pointer',
				'title' : self.tooltip
			});
			self.infoPointText = self.paper.text(self.midPoint.x, self.midPoint.y + 1, self.infoBoxData.length).attr({
				'font' : '13px serif', 
				'stroke' : '#002F5E',
				'cursor' : 'pointer',
				'title' : self.tooltip
			});

			self.infoPoint.click(function(){self.displayInfoBox();});
			self.infoPointText.click(function(){self.displayInfoBox();});
			
			self.infoPoint.hover(function(){self.glow = self.visual.glow();}, function(){self.glow.remove();}, self, self);
			self.infoPointText.hover(function(){self.glow = self.visual.glow();}, function(){self.glow.remove();}, self, self);
		}
	};
	
	self.displayInfoBox = function() {
		if(!self.infoBox){
			if(self.type == 'wires'){
				self.infoBox = self.getInfoBoxWithWire(self.infoBoxData[0]);
				for ( var i = 1; i < self.infoBoxData.length; i++) {
					self.infoBox.addContent(self.getInfoBoxWithWireContent(self.infoBoxData[i]));
				}
			} else if(self.type == 'services'){
				self.infoBox = self.getInfoBoxWithService(self.infoBoxData[0]);
				for ( var i = 1; i < self.infoBoxData.length; i++) {
					self.infoBox.addContent(self.getInfoBoxWithServiceContent(self.infoBoxData[i].service, self.infoBoxData[i].consumerId));
				}
			}
		}
		self.infoBox.show();
	};
	
	self.increaseCount = function(relationshipInfoData) {		
		if(self.infoPointText){
			self.infoPointText.attr({'text': self.count});
		}
		self.infoBoxData.push(relationshipInfoData);
	};
	
	self.increaseBackCount = function(relationshipInfoData) {
		self.increaseCount(relationshipInfoData);
		if('none' == self.doubleEnded){
			self.doubleEnded = 'block-wide-long';
			if(self.visual){
				self.visual.attr({'arrow-start': self.doubleEnded});
			}
		}
	};
	
	self.hide = function() {
		if(self.visual){
			self.visual.remove();
		}
		if(self.infoPoint){
			self.infoPoint.remove();
		}
		if(self.infoPointText){
			self.infoPointText.remove();
		}
	};
	
	self.reset = function(type, fromBundle, toBundle){
		self.fromBundle = fromBundle;
		self.toBundle = toBundle;
		self.type = type;
		self.infoBoxData = new Array();
		self.doubleEnded = 'none';
		if(self.visual){
			self.visual.attr({'arrow-start': self.doubleEnded});
		}
		if(self.infoBox){
			self.infoBox = undefined;
		}
	};
	
	self.getInfoBoxWithService = function(serviceData){
		var name = 'service' + serviceData.service.Identifier;
		var title = 'Service(s) between Bundles ' + serviceData.service.BundleIdentifier + ' and ' + serviceData.consumerId;
		var content = self.getInfoBoxWithServiceContent(serviceData.service, serviceData.consumerId);
		return new InfoBox({name: name, title: title, content: content, closeable: true});
	};
	
	self.getInfoBoxWithServiceContent = function(service, consumerId){
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>Service [' + service.Identifier + '] ' + service.objectClass[0] + (service.objectClass.length > 1 ? '...' : '') + '</li>').addClass('section-title'));
		infoBox.append($('<li>Published by Bundle ' + service.BundleIdentifier + '</li>'));
		infoBox.append($('<li>Used by Bundle ' + consumerId + '</li>'));
		if(service.UsingBundles.length > 1){
			infoBox.append($('<li>Also used by Bundles</li>'));
			$.each(service.UsingBundles, function(index, item){
				if(item != consumerId){
					infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
				}
			});
		}
		infoBox.append($('<li>ObjectClass</li>'));
		$.each(service.objectClass, function(index, item){
			infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
		});
		return infoBox;
	};
	
	self.getInfoBoxWithWire = function(wire){
		var name = 'wire' + wire.ProviderBundleId + '-' + wire.RequirerBundleId;
		var title = 'Wire(s) between Bundles ' + wire.ProviderBundleId + ' and ' + wire.RequirerBundleId;
		var content = self.getInfoBoxWithWireContent(wire);
		return new InfoBox({name: name, title: title, content: content, closeable: true});
	};

	self.getInfoBoxWithWireContent = function(wire){
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>"' + wire.BundleRequirement.Namespace + '" provided by Bundle ' + wire.ProviderBundleId + ' to Bundle ' + wire.RequirerBundleId + '</li>').addClass('section-title'));
		infoBox.append($('<li>Capability</li>'));
		self.addWireProperties(infoBox, wire.BundleCapability.Attributes, '=', 'Attributes');
		self.addWireProperties(infoBox, wire.BundleCapability.Directives, ':=', 'Directives');
		infoBox.append($('<li>Requirement</li>'));
		self.addWireProperties(infoBox, wire.BundleRequirement.Attributes, '=', 'Attributes');
		self.addWireProperties(infoBox, wire.BundleRequirement.Directives, ':=', 'Directives');
		return infoBox;
	};
	
	self.addWireProperties = function(list, attributes, delimiter, type){
		var firstItem = $('<li></li>').addClass('indent1');
		list.append(firstItem);
		var itemsAdded = false;
		$.each(attributes, function(key, value){
			if(!itemsAdded){
				itemsAdded = true;
				firstItem.text(type);
			}
			list.append($('<li>' + value.Key + delimiter + value.Value + '</li>').addClass('indent2'));
		});
		if(!itemsAdded){
			firstItem.text('No ' + type);
		}
	};
	
};
