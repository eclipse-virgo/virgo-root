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

	self.relationships = 'wires';

	self.dataSource = dataSource;

	self.bundles = {};

	self.bundleSpacing = 10; //Pixels to leave between bundles when rendering
	
	self.focused = -1;
	
	util.loadScript('raphael', function(){
		self.paper = Raphael('bundle-canvas', width, height);	
	});
	
	self.setFocusListener = function(listener) {
		self.focusListener = listener;
	};
	
	self.displayType = function(type){
		console.log(type);
		self.hideAll();
		if(self.focused != -1){
			self.focused.show();
		}
		if(type == 'wires') {
			$('#view-bundles-button').addClass('button-selected');
			$('#view-services-button').removeClass('button-selected');
			self.relationships = type;
			self.renderWires(self.focused);
		} else if(type == 'services'){
			$('#view-services-button').addClass('button-selected');
			$('#view-bundles-button').removeClass('button-selected');
			self.relationships = type;
			self.renderServices(self.focused);
		}
	};
	
	self.displayBundle = function(bundleId){
		self.hideAll();
		self.focused = -1;
		var bundle;
		if(self.bundles[bundleId]){
			bundle = self.bundles[bundleId];
		}else{
			bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId]);
			self.bundles[bundleId] = bundle;
		}
		self.focused = bundle;
		self.dataSource.updateBundle(bundleId, function(){
			bundle.show();
			if(self.relationships == 'wires'){
				self.renderWires(bundle);
			}else{
				self.renderServices(bundle);
			}
		});
		bundle.move(Math.round(self.paper.width/2), Math.round(self.paper.height/2));
	};
	
	self.renderWires = function(bundle){
		console.log(bundle);
		var bundleIds = new Array();
		$.each(bundle.rawBundle.RequiredWires, function(index, wire){
			bundleIds.push(wire.ProviderBundleId);
		});
		var topWidth = self.renderBundleRow(bundleIds, -239);
		$.each(bundle.rawBundle.ProvidedWires, function(index, wire){
			bundleIds.push(wire.RequirerBundleId);
		});
		var bottomWidth = self.renderBundleRow(bundleIds, 239);
		var newWidth = topWidth < bottomWidth ? bottomWidth : topWidth;
		newWidth < 900 ? self.paper.setSize(900, self.paper.height) : self.paper.setSize(newWidth, self.paper.height);
	};
	
	self.renderServices = function(bundle){
		var bundleIds = new Array();
		$.each(bundle.rawBundle.ServicesInUse, function(index, service){
			//console.log(service);
		});
		var topWidth = self.renderBundleRow(bundleIds, -239);
		$.each(bundle.rawBundle.RegisteredServices, function(index, service){
			//console.log(service);
		});
		var bottomWidth = self.renderBundleRow(bundleIds, 239);
		var newWidth = topWidth < bottomWidth ? bottomWidth : topWidth;
		newWidth < 900 ? self.paper.setSize(900, self.paper.height) : self.paper.setSize(newWidth, self.paper.height);
	};

	self.renderBundleRow = function(bundleIds, offset){
		var yPos = Math.round(self.paper.height/2) + offset;
		var xPos = this.bundleSpacing;
		var bundle;
		$.each(bundleIds, function(index, bundleId){
			if(self.bundles[bundleId]){
				bundle = self.bundles[bundleId];
			}else{
				bundle = new Bundle(self.paper, self.dataSource.bundles[bundleId]);
				self.bundles[bundleId] = bundle;
			}
			if(!bundle.isVisible){
				xPos = xPos + Math.round(bundle.boxWidth/2);
				bundle.move(xPos, yPos);
				bundle.show();
				xPos = xPos + Math.round(bundle.boxWidth/2) + self.bundleSpacing;
			}
		});
		return xPos;
	};

//	self.empty = function(){
//		self.hideAll();
//		self.bundles = {};
//	};

	self.hideAll = function(){
		$.each(self.bundles, function(index, value){
			value.hide();
		});
	};
	
//	
//	this.shuffle = function(bundleId, newRelationships){
//		$.each(this.relationships, function(index, oldRelationship){
//			oldRelationship.remove();
//		});
//		this.relationships = {};
//		this.hideAll();
//
//		console.log("In", newRelationships[0].length);
//		console.log("Out", newRelationships[1].length);
//		
//		var widthTop = this.renderBundlesRow(this.bundles[bundleId], false, newRelationships[0], -239);
//		var widthBottom = this.renderBundlesRow(this.bundles[bundleId], true, newRelationships[1], 239);
//		
//		var newWidth = widthTop < widthBottom ? widthBottom : widthTop;
//		newWidth < 900 ? paper.setSize(900, paper.height) : paper.setSize(newWidth, paper.height);
//		this.bundles[bundleId].move((paper.width/2).round(), (paper.height/2).round());
//		this.bundles[bundleId].show();
//		
//		new Fx.Scroll($('bundle-canvas')).set((paper.width/2).round() - 450, (paper.height/2).round());
//
//		$.each(this.relationships, function(index, relationship){
//			relationship.display();
//		});
//		
//		$('display').setStyle('visibility', 'visible');
//	};
//	
//	
//
//	this.renderBundlesRow = function(focused, isFrom, relations, offSet){
//		var yPos = (paper.height/2).round() + offSet;
//		var xPos = this.bundleSpacing;
//		relations.each(function(relation){
//			if(relation.bundle.id == focused.id){
//				var relationship = new SelfRelationship(focused, relation.info, relation.infoKey, relation.tooltip);
//				this.relationships[relationship.key] = relationship;
//				focused.addRelationship(relationship);
//			} else {
//				if(relation.bundle.isVisible){
//					if(isFrom){
//						var relationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
//					}else {
//						var relationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
//					}
//					var existingRelationship = this.relationships[relationship.key];
//					if(existingRelationship){
//						existingRelationship.increaseCount(relation.tooltip);
//					} else {
//						console.log('Extra', isFrom, relation.tooltip);
//						
//						if(isFrom){
//							var backRelationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
//						}else{
//							var backRelationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
//						}
//						
//						
//						this.relationships[backRelationship.key] = backRelationship;
//						focused.addRelationship(backRelationship);
//						relation.bundle.addRelationship(backRelationship);
//					}
//				} else {
//					xPos = xPos + (relation.bundle.boxWidth/2);
//					relation.bundle.move(xPos, yPos);
//					relation.bundle.show();
//					xPos = xPos + (relation.bundle.boxWidth/2) + this.bundleSpacing;
//					if(isFrom){
//						var relationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
//					}else{
//						var relationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
//					}
//					this.relationships[relationship.key] = relationship;
//					focused.addRelationship(relationship);
//					relation.bundle.addRelationship(relationship);
//				}
//			}
//		}, this);
//		return xPos;
//	};
	
};

/**
 * Bundle
 */
var Bundle = function(paper, rawBundle, dblClickCallback){

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
	
	self.boxWidth = self.text.getBBox().width + 2*self.bundleMargin + 2;
	self.boxHeight = self.text.getBBox().height + 2*self.bundleMargin + 2;
	
	self.box = self.paper.rect(self.x, self.y, self.boxWidth, self.boxHeight, 8).attr({
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
			'x' : x - (self.boxWidth/2), 
			'y' : y - (self.boxHeight/2)
		});
		self.text.attr({
			'x' : x - (self.boxWidth/2) + self.bundleMargin, 
			'y' : y
		});
		
	};

	self.addRelationship = function(relationship){
		self.relationships[relationship.key] = relationship;
	};

	self.removeRelationship = function(relationshipKey){
		Object.erase(self.relationships, relationshipKey);
	};

};

//var Relationship = function(fromBundle, toBundle, infoCallback, infoKey, tooltip) {
//
//	var self = this;
//	
//	this.fromBundle = fromBundle;
//	this.toBundle = toBundle;
//	this.infoCallback = infoCallback;
//	this.infoKey = infoKey;
//	this.tooltip = tooltip;
//	this.count = 1;
//	this.key = 'from' + this.fromBundle.id + 'to' + this.toBundle.id;
//	this.controlPointOffset = 100;
//
//	this.setCoordinates = function(){
//		this.startPoint = {'x' : fromBundle.x, 'y' : fromBundle.y + fromBundle.boxHeight/2};
//		this.endPoint = {'x' : toBundle.x, 'y' : toBundle.y - toBundle.boxHeight/2};
//		this.startPointControl = {'x' : this.startPoint.x, 'y' : this.startPoint.y + this.controlPointOffset}; 
//		this.endPointControl = {'x' : this.endPoint.x, 'y' : this.endPoint.y - this.controlPointOffset};
//		this.midPoint = this.calculateMidpoint(this.startPoint.x, this.startPoint.y, this.endPoint.x, this.endPoint.y); 
//	};
//	
//	this.calculateMidpoint = function(startX, startY, endX, endY){
//		if(startX < endX){
//			var midX = startX + (endX - startX)/2;
//		} else {
//			var midX = endX + (startX - endX)/2;
//		}
//		if(startY < endY){
//			var midY = startY + (endY - startY)/2;
//		} else {
//			var midY = endY + (startY - endY)/2;
//		}
//		return {'x' : midX, 'y' : midY};
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
//	};
//	
//	this.displayInfoBox = function() {
//		infoBox.go(this.infoCallback, this.infoKey);
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
//		this.fromBundle.removeRelationship(this.key);
//		this.toBundle.removeRelationship(this.key);
//	};
//
//};
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
