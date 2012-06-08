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

var LayoutManager = function(width, height, dataSource){

	var self = this;

	self.relationshipType = 'wires';
	
	self.relationships = {};

	self.dataSource = dataSource;

	self.bundles = {};
	
	self.focused = -1;

	self.bundleSpacing = 10; //Pixels to leave between bundles when rendering
	
	util.loadScript('raphael', function(){
		self.paper = Raphael('bundle-canvas', width, height);	
	});
	
	self.setFocusListener = function(listener) {
		self.focusListener = listener;
	};
	
	self.displayType = function(type){
		self.hideAll();
		if(self.focused != -1){
			self.focused.show();
		}
		if(type == 'wires') {
			$('#view-wires-button').addClass('button-selected');
			$('#view-services-button').removeClass('button-selected');
			self.relationshipType = type;
			if(self.focused != -1){
				self.renderWires(self.focused);
			}
		} else if(type == 'services'){
			$('#view-services-button').addClass('button-selected');
			$('#view-wires-button').removeClass('button-selected');
			self.relationshipType = type;
			if(self.focused != -1){
				self.renderServices(self.focused);
			}
		}
	};
	
	self.displayBundle = function(bundleId){
		self.hideAll();
		self.focused = -1;
		var bundle;
		self.dataSource.updateBundle(bundleId, function(){
			if(self.bundles[bundleId]){
				bundle = self.bundles[bundleId];
			}else{
				bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId], self.displayBundle);
				self.bundles[bundleId] = bundle;
			}
			self.focused = bundle;
			bundle.move(Math.round(self.paper.width/2), Math.round(self.paper.height/2));
			bundle.show();
			if(self.relationshipType == 'wires'){
				self.renderWires(bundle);
			}else{
				self.renderServices(bundle);
			}
		});
		if(self.focusListener){
			self.focusListener(bundleId);
		}
	};
	
	self.renderWires = function(bundle){
		var bundleIds = {};
		$.each(bundle.rawBundle.RequiredWires, function(index, wire){
			bundleIds[wire.ProviderBundleId] = new InfoBox().initWithWire(wire);
		});
		var topWidth = self.renderBundleRow(bundleIds, -239, bundle, false);
		$.each(bundle.rawBundle.ProvidedWires, function(index, wire){
			bundleIds[wire.RequirerBundleId] = new InfoBox().initWithWire(wire);
		});
		var bottomWidth = self.renderBundleRow(bundleIds, 239, bundle, true);
		var newWidth = topWidth < bottomWidth ? bottomWidth : topWidth;
		newWidth < 900 ? self.paper.setSize(900, self.paper.height) : self.paper.setSize(newWidth, self.paper.height);
	};
	
	self.renderServices = function(bundle){
		var bundleIds = {};
		$.each(bundle.rawBundle.ServicesInUse, function(index, service){
			bundleIds[service.BundleIdentifier] = new InfoBox().initWithService(service);
		});
		var topWidth = self.renderBundleRow(bundleIds, -239, bundle, false);
		$.each(bundle.rawBundle.RegisteredServices, function(index, service){
			$.each(service.UsingBundles, function(index, bundleId){
				bundleIds[bundleId] = new InfoBox().initWithService(service);
			});
		});
		var bottomWidth = self.renderBundleRow(bundleIds, 239, bundle, true);
		var newWidth = topWidth < bottomWidth ? bottomWidth : topWidth;
		newWidth < 900 ? self.paper.setSize(900, self.paper.height) : self.paper.setSize(newWidth, self.paper.height);
	};

	self.renderBundleRow = function(bundleIds, offset, focused, isFrom){
		var yPos = Math.round(self.paper.height/2) + offset;
		var xPos = this.bundleSpacing;
		var bundle;
		$.each(bundleIds, function(bundleId, relationshipInfoBox){
			if(self.bundles[bundleId]){
				bundle = self.bundles[bundleId];
			}else{
				bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId], self.displayBundle);
				self.bundles[bundleId] = bundle;
			}
			var releationshipKey = self.getRelationshipKey(bundle, focused);
			if(!bundle.isVisible){
				var relationship;
				if(isFrom){
					relationship = new Relationship(self.paper, focused, bundle, relationshipInfoBox);
				}else{
					relationship = new Relationship(self.paper, bundle, focused, relationshipInfoBox);
				}
				self.relationships[releationshipKey] = relationship;
				xPos = xPos + bundle.boxWidth/2;
				bundle.move(xPos, yPos);
				bundle.show();
				relationship.display();
				xPos = xPos + bundle.boxWidth/2 + self.bundleSpacing;
			} else {
				var existingRelationship = self.relationships[releationshipKey];
				if(bundle.rawBundle.Identifier == focused.rawBundle.Identifier){
					console.log('Need a self link');
				} else {
					if(isFrom){
						existingRelationship.increaseBackCount(relationshipInfoBox);
					} else {
						existingRelationship.increaseBackCount(relationshipInfoBox);
					}
					console.log('isFrom' + isFrom + ', already visible' + bundle.summary);
				}
			}
		});
		return xPos;
	};
	
	self.getRelationshipKey = function(bundle1, bundle2){
		if(bundle1.rawBundle.Identifier == bundle2.rawBundle.Identifier){
			return 'self' + bundle1.rawBundle.Identifier;
		}
		if(bundle1.rawBundle.Identifier < bundle2.rawBundle.Identifier){
			return bundle1.rawBundle.Identifier + '-' + bundle2.rawBundle.Identifier;
		}
		return bundle2.rawBundle.Identifier + '-' + bundle1.rawBundle.Identifier;
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
	
};

/**
 * Bundle
 */
var Bundle = function(paper, rawBundle, dblClickCallback){

	var self = this;
	
	self.paper = paper;
	self.rawBundle = rawBundle;
	self.infoBox = new InfoBox().initWithBundle(rawBundle);
	self.dblClickCallback = dblClickCallback;
	self.relationships = {};
	self.isVisible = false;
	
	//Display attributes
	self.bundleMargin = 8;
	self.x = 5;
	self.y = 5;
	
	self.summary = '[' + self.rawBundle.Identifier + '] ' + self.rawBundle.SymbolicName + '\n' + self.rawBundle.Version;
	
	self.text = self.paper.text(self.x, self.y, self.summary).attr({
		'text-anchor' : 'start', 
		'font' : '12px Arial'
	}).hide();
	
	self.boxWidth = Math.round(self.text.getBBox().width) + (3*self.bundleMargin);
	self.boxHeight = Math.round(self.text.getBBox().height) + (2*self.bundleMargin);
	
	self.box = self.paper.rect(self.x, self.y, self.boxWidth, self.boxHeight, self.bundleMargin).attr({
		'fill' : '#E8F6FF', 
		'stroke' : '#002F5E'
	}).hide();
	
	self.info = self.paper.text(self.x, self.y, 'i').attr({
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
		self.infoBox.go();
	});
	
	self.hide = function(){
		self.text.hide();
		self.box.hide();
		self.info.hide();
		self.isVisible = false;
	};
	
	self.show = function(){
		self.text.show();
		self.box.show();
		self.info.show();
		self.isVisible = true;
	};
	
	self.move = function(x, y) {
		self.x = x;
		self.y = y;
		self.box.attr({
			'x' : Math.round(x - self.boxWidth/2), 
			'y' : Math.round(y - self.boxHeight/2)
		});
		self.text.attr({
			'x' : Math.round(x - self.boxWidth/2) + self.bundleMargin, 
			'y' : Math.round(y)
		});
		self.info.attr({
			'x' : Math.round(x + self.boxWidth/2 - 1.5*self.bundleMargin), 
			'y' : Math.round(y)
		});
		
	};

};

var Relationship = function(paper, fromBundle, toBundle, infoBox) {
	
	var self = this;
	
	self.paper = paper;
	self.fromBundle = fromBundle;
	self.toBundle = toBundle;
	self.infoBox = infoBox;
	self.tooltip = 'From bundle ' + fromBundle.summary + '\n To bundle ' + toBundle.summary;
	self.doubleEnded = false;

	self.count = 1;
	self.controlPointOffset = 100;

	self.setCoordinates = function(){
		self.startPoint = {'x' : fromBundle.x, 'y' : fromBundle.y + fromBundle.boxHeight/2};
		self.endPoint = {'x' : toBundle.x, 'y' : toBundle.y - toBundle.boxHeight/2};
		self.startPointControl = {'x' : self.startPoint.x, 'y' : self.startPoint.y + self.controlPointOffset}; 
		self.endPointControl = {'x' : self.endPoint.x, 'y' : self.endPoint.y - self.controlPointOffset};
		self.midPoint = self.calculateMidpoint(this.startPoint.x, self.startPoint.y, self.endPoint.x, self.endPoint.y); 
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
		if(self.visual){
			self.visual.remove();
		}
		if(self.infoPoint){
			self.infoPoint.remove();
		}
		if(self.infoPointText){
			self.infoPointText.remove();
		}
		self.setCoordinates();
		self.visual = self.paper.path('M' + self.startPoint.x + ',' + self.startPoint.y + 
									'C' + self.startPointControl.x + ',' + self.startPointControl.y + 
									',' + self.endPointControl.x + ',' + self.endPointControl.y + 
									',' + self.endPoint.x + ',' + self.endPoint.y).attr({
			'arrow-end' : 'block-wide-long',
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
		infoBox.go();
	};
	
	self.increaseCount = function(relationshipInfoBox) {
		self.count = self.count + 1;
		self.infoPointText.attr({'text': self.count});
		self.infoBox.addInfoBox(relationshipInfoBox);
	};
	
	self.increaseBackCount = function(relationshipInfoBox) {
		self.increaseCount(relationshipInfoBox);
		if(!self.doubleEnded){
			self.doubleEnded = true;
			self.visual.attr({'arrow-start' : 'block-wide-long'});
		}
	};
	
	self.remove = function() {
		self.visual.remove();
		self.infoPoint.remove();
		self.infoPointText.remove();
	};

};

var InfoBox = function(){
	
	var self = this;
	
	self.title = "Not Defined";
	self.dialogBox = $('<div />');
	
	self.initWithBundle = function(rawBundle){
		//console.log('rawBundle', rawBundle);
		self.title = 'Bundle [' + rawBundle.Identifier + '] ' + rawBundle.SymbolicName + '_' + rawBundle.Version;
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>State - ' + rawBundle.State + '</li>'));
		infoBox.append($('<li>StartLevel - ' + rawBundle.StartLevel + '</li>'));
		infoBox.append($('<li>Region - ' + rawBundle.Region + '</li>'));
		infoBox.append($('<li>Location - ' + rawBundle.Location + '</li>'));
		infoBox.append($('<li>LastModified - ' + rawBundle.LastModified + '</li>'));
		infoBox.append($('<li>Is a fragment - ' + rawBundle.Fragment + '</li>'));
		infoBox.append($('<li>PersistentlyStarted - ' + rawBundle.PersistentlyStarted + '</li>'));
		infoBox.append($('<li>ActivationPolicyUsed - ' + rawBundle.ActivationPolicyUsed + '</li>'));
		infoBox.append($('<li>Required - ' + rawBundle.Required + '</li>'));
		if(rawBundle.ExportedPackages.length == 0){
			infoBox.append($('<li>No exported packages</li>').addClass('indent1'));
		} else {
			infoBox.append($('<li>Exported packages</li>').addClass('indent1'));
			$.each(rawBundle.ExportedPackages, function(index, item){
				infoBox.append($('<li>' + item + '</li>').addClass('indent2'));
			});
		}
		if(rawBundle.ImportedPackages.length == 0){
			infoBox.append($('<li>No imported packages</li>').addClass('indent1'));
		} else {
			infoBox.append($('<li>Imported packages</li>').addClass('indent1'));
			$.each(rawBundle.ImportedPackages, function(index, item){
				infoBox.append($('<li>' + item + '</li>').addClass('indent2'));
			});
		}
		//infoBox.append($('<li>' + rawBundle + '</li>'));
		self.dialogBox.append(infoBox);
		return self;
	};
	
	self.initWithService = function(service){
		//console.log('service', service);
		self.title = 'Service(s) between between Bundle ' + service.Identifier + ' and Bundle ' ;
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>Service [' + service.Identifier + '] ' + service.objectClass[0] + (service.objectClass.length > 1 ? '...' : '') + '</li>').addClass('section-title'));
		infoBox.append($('<li>Used By</li>'));
		$.each(service.UsingBundles, function(index, item){
			infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
		});
		infoBox.append($('<li>ObjectClass</li>'));
		$.each(service.objectClass, function(index, item){
			infoBox.append($('<li>' + item + '</li>').addClass('indent1'));
		});
		self.dialogBox.append(infoBox);
		return self;
	};
	
	self.initWithWire = function(wire){
		//console.log('wire', wire);
		self.title = 'Wire between Bundle ' + wire.ProviderBundleId + ' and Bundle ' + wire.RequirerBundleId;
		var infoBox = $('<ul></ul>');
		infoBox.append($('<li>"' + wire.BundleRequirement.Namespace + '" from Bundle ' + wire.ProviderBundleId + ' to Bundle ' + wire.RequirerBundleId + '</li>').addClass('section-title'));
		infoBox.append($('<li>Capability</li>'));
		self.addWireProperties(infoBox, wire.BundleCapability.Attributes, '=', 'Attributes');
		self.addWireProperties(infoBox, wire.BundleCapability.Directives, ':=', 'Directives');
		infoBox.append($('<li>Requirement</li>'));
		self.addWireProperties(infoBox, wire.BundleRequirement.Attributes, '=', 'Attributes');
		self.addWireProperties(infoBox, wire.BundleRequirement.Directives, ':=', 'Directives');
		self.dialogBox.append(infoBox);
		return self;
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
	
	self.addInfoBox = function(newInfoBox){
		console.log('adding to ', self.dialogBox);
		$.each(newInfoBox.dialogBox.children(), function(index, item){
			self.dialogBox.append(item);
			console.log('adding', item);
		});
	};
	
	self.go = function(){
		var newDialogBox = self.dialogBox.clone();
		$("li", newDialogBox).removeClass('li-odd');
		$("li:odd", newDialogBox).addClass('li-odd');
		newDialogBox.dialog({ 
			modal : true,
			dialogClass: 'info-box',
			resizable: false,
			width : 600,
			title : self.title,
			close : function(a,b){
				$(this).parent().remove();
				return false;
			}
		});
	};
	
};

//
//var SelfRelationship = function(bundle, infoCallback, infoKey, tooltip) {
//
//	var self = this;
//	
//	this.bundle = bundle;
//	this.infoCallback = infoCallback;
//	this.infoKey = infoKey;
//	this.tooltip = tooltip;
//	this.count = 1;
//	this.key = 'self' + this.bundle.id;
//	this.offset = 10;
//	this.offset2 = 100;
//	
//	this.setCoordinates = function(){
//		this.startPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2, 'y' : this.bundle.y - this.offset};
//		this.endPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2, 'y' : this.bundle.y + this.offset};
//		
//		this.startPointControl = {'x' : this.startPoint.x + this.offset2, 'y' : this.startPoint.y - this.offset2}; 
//		this.endPointControl = {'x' : this.endPoint.x + this.offset2, 'y' : this.endPoint.y + this.offset2};
//		
//		this.midPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2 + this.offset2*0.74, 'y' : this.bundle.y}; 
//	};
//	
//	this.display = function() {
//		if(this.visual){
//			this.visual.remove();
//		}
//		if(this.infoPoint){
//			this.infoPoint.remove();
//		}
//		if(this.infoPointText){
//			this.infoPointText.remove();
//		}
//		this.setCoordinates();
//		this.visual = paper.path('M' + this.startPoint.x + ',' + this.startPoint.y + 
//									'C' + this.startPointControl.x + ',' + this.startPointControl.y + 
//									',' + this.endPointControl.x + ',' + this.endPointControl.y + 
//									',' + this.endPoint.x + ',' + this.endPoint.y).attr({
//			'arrow-end' : 'block-wide-long',
//			'stroke-width' : 3,
//			'stroke' : '#002F5E'
//		}).toBack();
//		this.infoPoint = paper.circle(this.midPoint.x, this.midPoint.y, 10).attr({
//			'fill' : '#BAD9EC', 
//			'stroke' : 'none',
//			'title' : this.tooltip
//		});
//		this.infoPointText = paper.text(this.midPoint.x, this.midPoint.y, this.count).attr({
//			'font' : '13px Arial', 
//			'stroke' : '#002F5E',
//			'title' : this.tooltip
//		});
//		
//		this.infoPoint.click(function(){this.displayInfoBox();});
//		this.infoPointText.click(function(){this.displayInfoBox();});
//		
//		this.infoPoint.hover(function(){this.glow = this.visual.glow();}, function(){this.glow.remove();}, this, this);
//		this.infoPointText.hover(function(){this.glow = this.visual.glow();}, function(){this.glow.remove();}, this, this);
//		
//	};
//	
//	this.increaseCount = function(tooltip) {
//		this.count = this.count + 1;
//		this.tooltip = this.tooltip + ' ' + tooltip;
//		if(this.infoPointText){
//			this.infoPointText.attr({
//				'text' : this.count
//			});
//		};
//	};
//	
//	this.remove = function() {
//		this.visual.remove();
//		this.infoPoint.remove();
//		this.infoPointText.remove();
//	};
//	
//};
