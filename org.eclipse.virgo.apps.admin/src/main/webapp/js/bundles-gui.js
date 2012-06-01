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
			bundleIds[wire.ProviderBundleId] = 'tooltip';
		});
		var topWidth = self.renderBundleRow(bundleIds, -239, bundle, false);
		$.each(bundle.rawBundle.ProvidedWires, function(index, wire){
			bundleIds[wire.RequirerBundleId] = 'tooltip';
		});
		var bottomWidth = self.renderBundleRow(bundleIds, 239, bundle, true);
		var newWidth = topWidth < bottomWidth ? bottomWidth : topWidth;
		newWidth < 900 ? self.paper.setSize(900, self.paper.height) : self.paper.setSize(newWidth, self.paper.height);
	};
	
	self.renderServices = function(bundle){
		var bundleIds = {};
		$.each(bundle.rawBundle.ServicesInUse, function(index, service){
			bundleIds[service.BundleIdentifier] = 'tooltip';
		});
		var topWidth = self.renderBundleRow(bundleIds, -239, bundle, false);
		$.each(bundle.rawBundle.RegisteredServices, function(index, service){
			$.each(service.UsingBundles, function(index, bundleId){
				bundleIds[bundleId] = 'tooltip';
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
		$.each(bundleIds, function(bundleId, tooltip){
			if(self.bundles[bundleId]){
				bundle = self.bundles[bundleId];
			}else{
				bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId], tooltip, self.displayBundle);
				self.bundles[bundleId] = bundle;
			}
			if(!bundle.isVisible){
				var relationship;
				if(isFrom){
					relationship = new Relationship(self.paper, focused, bundle, self.getRelationshipKey(focused, bundle));//, relation.info, relation.infoKey, relation.tooltip);
				}else{
					relationship = new Relationship(self.paper, bundle, focused, self.getRelationshipKey(bundle, focused));//, relation.info, relation.infoKey, relation.tooltip);
				}
				//bundle.relationships[relationship.key] = relationship;
				//focused.relationships[relationship.key] = relationship;
				self.relationships[relationship.key] = relationship;
				xPos = xPos + bundle.boxWidth/2;
				bundle.move(xPos, yPos);
				bundle.show();
				relationship.display();
				xPos = xPos + bundle.boxWidth/2 + self.bundleSpacing;
			}else{
//				var relationshipKey;
//				if(isFrom){
//					relationshipKey = self.getRelationshipKey(focused, bundle);
//				}else{
//					relationshipKey = self.getRelationshipKey(bundle, focused);
//				}
//				console.log('already visible' + relationshipKey);
			}
		});
		return xPos;
	};
	
	self.getRelationshipKey = function(from, to){
		return 'from' + from.rawBundle.Identifier + 'to' + to.rawBundle.Identifier;
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
var Bundle = function(paper, rawBundle, tooltip, dblClickCallback){

	var self = this;
	
	self.paper = paper;
	self.rawBundle = rawBundle;
	self.dblClickCallback = dblClickCallback;
	self.isVisible = false; 
	self.relationships = {};
	
	//Display attributes
	self.bundleMargin = 8;
	self.x = 5;
	self.y = 5;
	
	self.summary = "[" + self.rawBundle.Identifier + "] " + self.rawBundle.SymbolicName + "\n" + self.rawBundle.Version;
	
	self.text = self.paper.text(self.x, self.y, self.summary).attr({
		"text-anchor" : "start", 
		"font" : "12px Arial"
	}).hide();
	
	self.boxWidth = Math.round(self.text.getBBox().width) + (2*self.bundleMargin);// + 2;
	self.boxHeight = Math.round(self.text.getBBox().height) + (2*self.bundleMargin);// + 2;
	
	self.box = self.paper.rect(self.x, self.y, self.boxWidth, self.boxHeight, self.bundleMargin).attr({
		"fill" : "#E8F6FF", 
		"stroke" : "#002F5E"
	}).hide();
	
	self.box.toBack();
	
	self.box.dblclick(function(){
		self.dblClickCallback(self.rawBundle.Identifier);
	});
	
	self.text.dblclick(function(){
		self.dblClickCallback(self.rawBundle.Identifier);
	});
	
	self.hide = function(){
		self.text.hide();
		self.box.hide();
		self.isVisible = false; 
	};
	
	self.show = function(){
		self.text.show();
		self.box.show();
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
		
	};

};

var Relationship = function(paper, fromBundle, toBundle, key){ //}, infoCallback, infoKey, tooltip) {

	var self = this;
	
	self.paper = paper;
	self.fromBundle = fromBundle;
	self.toBundle = toBundle;
	self.infoCallback = infoCallback;
	self.infoKey = infoKey;
	self.tooltip = tooltip;
	self.count = 1;
	self.key = key;
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
			'title' : self.tooltip
		});
		self.infoPointText = self.paper.text(self.midPoint.x, self.midPoint.y, self.count).attr({
			'font' : '13px Arial', 
			'stroke' : '#002F5E',
			'title' : self.tooltip
		});
		
		self.infoPoint.click(function(){self.displayInfoBox();});
		self.infoPointText.click(function(){self.displayInfoBox();});
		
		self.infoPoint.hover(function(){self.glow = self.visual.glow();}, function(){self.glow.remove();}, self, self);
		self.infoPointText.hover(function(){self.glow = self.visual.glow();}, function(){self.glow.remove();}, self, self);
	};
	
	self.displayInfoBox = function() {
		infoBox.go(self.infoCallback, self.infoKey);
	};
	
	self.increaseCount = function(tooltip) {
		self.count = self.count + 1;
		self.tooltip = self.tooltip + ' ' + tooltip;
		if(self.infoPointText){
			self.infoPointText.attr({
				'text' : self.count
			});
		};
	};
	
	self.remove = function() {
		self.visual.remove();
		self.infoPoint.remove();
		self.infoPointText.remove();
		//self.fromBundle.removeRelationship(self.key);
		//self.toBundle.removeRelationship(self.key);
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
