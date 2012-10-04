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
	
	self.minimumWidth = width;
	
	self.setFocusListener = function(listener) {
		self.focusListener = listener;
	};
	
	self.displayType = function(type){
		if(type == 'wires') {
			$('#view-wires-button').addClass('button-selected');
			$('#view-services-button').removeClass('button-selected');
			self.relationshipType = type;
			self.displayBundle(self.focused);
		} else if(type == 'services'){
			$('#view-services-button').addClass('button-selected');
			$('#view-wires-button').removeClass('button-selected');
			self.relationshipType = type;
			self.displayBundle(self.focused);
		}
	};
	
	self.displayBundle = function(bundleId){
		if(!isNaN(bundleId)){
			self.hideAll();
			self.bundleCanvas.addClass('spinner-large');
			self.focused = NaN;
			self.dataSource.updateBundle(bundleId, function(){
				var bundle;
				if(self.bundles[bundleId]){
					bundle = self.bundles[bundleId];
				}else{
					bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId], 5, 5, self.displayBundle);
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
				bundle.move(middleX - (bundle.boxWidth/2), middleY);
				bundle.show();
				self.bundleCanvas.scrollLeft(middleX - (self.minimumWidth/2));
				
				$.each(self.relationships, function(key, relationship){
					relationship.display();
				});
				
				self.bundleCanvas.removeClass('spinner-large');
			});
			if(self.focusListener){
				self.focusListener(bundleId);
			}
		}
	};
	
	self.renderWires = function(bundle){
		var topRowBundleIds = {};
		$.each(bundle.rawBundle.RequiredWires, function(index, wire){
			topRowBundleIds[wire.ProviderBundleId] = self.getInfoBoxWithWire(wire);
		});
		var topWidth = self.renderBundleRow(topRowBundleIds, -239, bundle, true);
		var bottomRowBundleIds = {};
		$.each(bundle.rawBundle.ProvidedWires, function(index, wire){
			bottomRowBundleIds[wire.RequirerBundleId] = self.getInfoBoxWithWire(wire);
		});
		var bottomWidth = self.renderBundleRow(bottomRowBundleIds, 239, bundle, false);
		var newWidth = topWidth < bottomWidth ? bottomWidth : topWidth;
		newWidth < self.minimumWidth ? self.paper.setSize(self.minimumWidth, self.paper.height) : self.paper.setSize(newWidth, self.paper.height);
	};
	
	self.renderServices = function(bundle){
		var topRowBundleIds = {};
		$.each(bundle.rawBundle.ServicesInUse, function(index, service){
			topRowBundleIds[service.BundleIdentifier] = self.getInfoBoxWithService(service, service.BundleIdentifier, bundle.rawBundle.Identifier);
		});
		var topWidth = self.renderBundleRow(topRowBundleIds, -239, bundle, true);
		var bottomRowBundleIds = {};
		$.each(bundle.rawBundle.RegisteredServices, function(index, service){
			$.each(service.UsingBundles, function(index, bundleId){
				bottomRowBundleIds[bundleId] = self.getInfoBoxWithService(service, bundleId, bundle.rawBundle.Identifier);
			});
		});
		var bottomWidth = self.renderBundleRow(bottomRowBundleIds, 239, bundle, false);
		var newWidth = topWidth < bottomWidth ? bottomWidth : topWidth;
		newWidth < self.minimumWidth ? self.paper.setSize(self.minimumWidth, self.paper.height) : self.paper.setSize(newWidth, self.paper.height);
	};
	
	self.renderBundleRow = function(bundleIds, offset, focusedBundle, topRow){
		var yPos = (self.paper.height/2) + offset;
		var xPos = this.bundleSpacing;
		var focusedBundleId = focusedBundle.rawBundle.Identifier;
		$.each(bundleIds, function(bundleId, relationshipInfoBox){
			var bundle;
			var existingBundle = false;
			var releationshipKey = self.getRelationshipKey(bundleId, focusedBundleId);
			if(self.bundles[bundleId]){
				bundle = self.bundles[bundleId];
				existingBundle = true;
			}else{ //New Bundle so we will definitely be adding it on this loop. 
				bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId], xPos, yPos, self.displayBundle);
				self.bundles[bundleId] = bundle;
			}
			if(!bundle.isVisible && focusedBundleId != bundleId){// If the bundle is not visible and is not the focused bundle
				var relationship;
				if(topRow){
					relationship = new Relationship(self.paper, bundle, focusedBundle, relationshipInfoBox);
				}else{
					relationship = new Relationship(self.paper, focusedBundle, bundle, relationshipInfoBox);
				}
				self.relationships[releationshipKey] = relationship;
				if(existingBundle){
					bundle.move(xPos, yPos);
				}
				bundle.show();
				xPos = xPos + bundle.boxWidth + self.bundleSpacing;
			} else {
				var existingRelationship = self.relationships[releationshipKey];
				if(bundle.rawBundle.Identifier == focusedBundleId){
					if(existingRelationship){
						existingRelationship.increaseCount(relationshipInfoBox);
					}else{//Handle the first self relationship
						var relationship = new Relationship(self.paper, bundle, bundle, relationshipInfoBox);
						self.relationships[releationshipKey] = relationship;
					}
				} else {
					var existingFromBundle = existingRelationship.fromBundle.rawBundle.Identifier;
					if(!topRow && existingFromBundle == focusedBundleId){// On the bottom row
						existingRelationship.increaseCount(relationshipInfoBox);
					} else if(topRow && bundle.rawBundle.Identifier == existingFromBundle){// On the top row
						existingRelationship.increaseCount(relationshipInfoBox); 
					} else {
						existingRelationship.increaseBackCount(relationshipInfoBox); //On either row and its a back relationship
					}
				}
			}
		});
		return xPos;
	};
	
	self.getRelationshipKey = function(bundleId1, bundleId2){
		if(bundleId1 == bundleId2){
			return 'self' + bundleId1;
		}
		if(bundleId1 < bundleId2){
			return bundleId1 + '-' + bundleId2;
		}
		return bundleId2 + '-' + bundleId1;
	};

	self.hideAll = function(){
		$.each(self.bundles, function(index, value){
			value.hide();
		});
		$.each(self.relationships, function(index, value){
			value.remove();
		});
		self.relationships = {};
	};
	
	self.getInfoBoxWithService = function(service, bundleId1, bundleId2){
		var name = 'service' + service.Identifier;
		var title = 'Service(s) between Bundles ' + bundleId1 + ' and ' + bundleId2;
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>Service [' + service.Identifier + '] ' + service.objectClass[0] + (service.objectClass.length > 1 ? '...' : '') + '</li>').addClass('section-title'));
		infoBox.append($('<li>Published by Bundle ' + service.BundleIdentifier + '</li>'));
		infoBox.append($('<li>Used by</li>'));
		$.each(service.UsingBundles, function(index, item){
			infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
		});
		infoBox.append($('<li>ObjectClass</li>'));
		$.each(service.objectClass, function(index, item){
			infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
		});
		return new InfoBox({name: name, title: title, content: infoBox, closeable: true});
	};
	
	self.getInfoBoxWithWire = function(wire){
		var name = 'wire' + wire.ProviderBundleId + '-' + wire.RequirerBundleId;
		var title = 'Wire(s) between Bundles ' + wire.ProviderBundleId + ' and ' + wire.RequirerBundleId;
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>"' + wire.BundleRequirement.Namespace + '" provided by Bundle ' + wire.ProviderBundleId + ' to Bundle ' + wire.RequirerBundleId + '</li>').addClass('section-title'));
		infoBox.append($('<li>Capability</li>'));
		self.addWireProperties(infoBox, wire.BundleCapability.Attributes, '=', 'Attributes');
		self.addWireProperties(infoBox, wire.BundleCapability.Directives, ':=', 'Directives');
		infoBox.append($('<li>Requirement</li>'));
		self.addWireProperties(infoBox, wire.BundleRequirement.Attributes, '=', 'Attributes');
		self.addWireProperties(infoBox, wire.BundleRequirement.Directives, ':=', 'Directives');
		return new InfoBox({name: name, title: title, content: infoBox, closeable: true});
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

/**
 * Bundle
 */
var Bundle = function(paper, rawBundle, x, y, dblClickCallback){

	var self = this;
	
	self.paper = paper;
	self.rawBundle = rawBundle;
	self.dblClickCallback = dblClickCallback;
	self.relationships = {};
	self.isVisible = false;
	//Display attributes
	self.bundleMargin = 8;
	self.boxHeight = 28 + (2*self.bundleMargin); // Starter value to get text in the right place
	self.x = Math.round(x); //Left edge of the box
	self.y = Math.round(y); //Middle of the box
	
	self.summary = '[' + self.rawBundle.Identifier + '] ' + self.rawBundle.SymbolicName + '\n' + self.rawBundle.Version;
	
	self.text = self.paper.text(self.x + self.bundleMargin, self.y, self.summary).attr({
		'text-anchor' : 'start', 
		'font' : '12px Arial'
	}).hide();
	
	self.boxWidth = Math.round(self.text.getBBox().width) + (3*self.bundleMargin);
	self.boxHeight = Math.round(self.text.getBBox().height) + (2*self.bundleMargin);
	
	self.box = self.paper.rect(self.x, self.y - (self.boxHeight/2), self.boxWidth, self.boxHeight, self.bundleMargin).attr({
		'fill' : '#E8F6FF', 
		'stroke' : '#002F5E'
	}).hide();
	
	self.info = self.paper.text(self.x + (self.boxWidth - 1.5*self.bundleMargin), self.y, 'i').attr({
		'text-anchor' : 'start', 
		'font-size' : '17px',
		'font-family' : 'serif',
		'font-weight' : 'bold',
		'font-style' : 'italic',
		'fill' : '#002F5E',
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
		if(!self.isVisible){
			self.text.show();
			self.box.show();
			self.info.show();
			self.isVisible = true;
		}
	};
	
	self.hide = function(){
		if(self.isVisible){
			self.text.hide();
			self.box.hide();
			self.info.hide();
			self.infoBox = undefined;//Force it to be reloaded as the underlying data may have changed
			self.isVisible = false;
		}
	};
	
	self.move = function(x, y) {
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
		
	};
	
	self.getInfoBoxWithBundle = function(rawBundle){
		var name = 'bundle' + rawBundle.Identifier;
		var title = 'Bundle [' + rawBundle.Identifier + '] ' + rawBundle.SymbolicName + ': ' + rawBundle.Version;
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>State - ' + rawBundle.State + '</li>'));
		if(rawBundle.StartLevel){
			infoBox.append($('<li>StartLevel - ' + rawBundle.StartLevel + '</li>'));
		}
		infoBox.append($('<li>Region - ' + rawBundle.Region + '</li>'));
		infoBox.append($('<li>Location - ' + rawBundle.Location + '</li>'));
		if(rawBundle.LastModified){
			infoBox.append($('<li>LastModified - ' + rawBundle.LastModified + '</li>'));
		}
		infoBox.append($('<li>Is a fragment - ' + rawBundle.Fragment + '</li>'));
		if(rawBundle.PersistentlyStarted){
			infoBox.append($('<li>PersistentlyStarted - ' + rawBundle.PersistentlyStarted + '</li>'));
		}
		if(rawBundle.ActivationPolicyUsed){
			infoBox.append($('<li>ActivationPolicyUsed - ' + rawBundle.ActivationPolicyUsed + '</li>'));
		}
		infoBox.append($('<li>Required - ' + rawBundle.Required + '</li>'));
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
		return new InfoBox({name: name, title: title, content: infoBox, closeable: true});
	};

};

var Relationship = function(paper, fromBundle, toBundle, infoBox) {
	
	var self = this;
	
	self.paper = paper;
	self.fromBundle = fromBundle;
	self.toBundle = toBundle;
	self.infoBox = infoBox;
	self.doubleEnded = 'none';

	self.count = 1;
	self.controlPointOffset = 90;
	
	self.setCoordinates = function(){
		if(self.fromBundle.rawBundle.Identifier == self.toBundle.rawBundle.Identifier){
			var xOffSet = self.fromBundle.x + self.toBundle.boxWidth;
			self.startPoint = {'x' : xOffSet - 20, 'y' : self.fromBundle.y - (self.fromBundle.boxHeight/2)};
			self.endPoint = {'x' : xOffSet - 20, 'y' : Math.round(self.toBundle.y + (self.toBundle.boxHeight/2))};
			self.startPointControl = {'x' : self.startPoint.x + self.controlPointOffset, 'y' : self.startPoint.y - self.controlPointOffset}; 
			self.endPointControl = {'x' : self.endPoint.x + self.controlPointOffset, 'y' : self.endPoint.y + self.controlPointOffset};
			self.midPoint = {'x' : xOffSet + 47, 'y' : self.fromBundle.y};
		}else{
			self.startPoint = {'x' : Math.round(self.fromBundle.x + self.fromBundle.boxWidth/2), 'y' : self.fromBundle.y + (self.fromBundle.boxHeight/2)};
			self.endPoint = {'x' : Math.round(self.toBundle.x + self.toBundle.boxWidth/2), 'y' : self.toBundle.y - (self.fromBundle.boxHeight/2)};
			self.startPointControl = {'x' : self.startPoint.x, 'y' : self.startPoint.y + self.controlPointOffset}; 
			self.endPointControl = {'x' : self.endPoint.x, 'y' : self.endPoint.y - self.controlPointOffset};
			self.midPoint = self.calculateMidpoint(self.startPoint.x, self.startPoint.y, self.endPoint.x, self.endPoint.y);
		}
	};
	
	self.calculateMidpoint = function(startX, startY, endX, endY){
		var midX, midY;
		if(startX < endX){
			midX = startX + (endX - startX)/2;
		} else {
			midX = endX + (startX - endX)/2;
		}
		if(startY < endY){
			midY = startY + (endY - startY)/2;
		} else {
			midY = endY + (startY - endY)/2;
		}
		return {'x' : midX, 'y' : midY};
	};
	
	self.display = function() {
		self.remove();
		self.setCoordinates();
		self.tooltip = 'From bundle ' + self.fromBundle.summary + '\n To bundle ' + self.toBundle.summary;
		self.visual = self.paper.path('M' + self.startPoint.x + ',' + self.startPoint.y + 
									'C' + self.startPointControl.x + ',' + self.startPointControl.y + 
									',' + self.endPointControl.x + ',' + self.endPointControl.y + 
									',' + self.endPoint.x + ',' + self.endPoint.y).attr({
			'arrow-end' : 'block-wide-long',
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
		self.infoPointText = self.paper.text(self.midPoint.x, self.midPoint.y + 1, self.count).attr({
			'font' : '13px serif', 
			'stroke' : '#002F5E',
			'cursor' : 'pointer',
			'title' : self.tooltip
		});
		
		self.infoPoint.click(function(){self.displayInfoBox();});
		self.infoPointText.click(function(){self.displayInfoBox();});
		
		self.infoPoint.hover(function(){self.glow = self.visual.glow();}, function(){self.glow.remove();}, self, self);
		self.infoPointText.hover(function(){self.glow = self.visual.glow();}, function(){self.glow.remove();}, self, self);
	};
	
	self.displayInfoBox = function() {
		self.infoBox.show();
	};
	
	self.increaseCount = function(relationshipInfoBox) {
		self.count = self.count + 1;
		if(self.infoPointText){
			self.infoPointText.attr({'text': self.count});
		}
		self.infoBox.addContent(relationshipInfoBox.content);
	};
	
	self.increaseBackCount = function(relationshipInfoBox) {
		self.increaseCount(relationshipInfoBox);
		if('none' == self.doubleEnded){
			self.doubleEnded = 'block-wide-long';
			if(self.visual){
				self.visual.attr({'arrow-start': 'block-wide-long'});
			}
		}
	};
	
	self.remove = function() {
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

};
