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

	util.loadScript('raphael', function(){
		self.paper = Raphael('bundle-canvas', width, height);	
	});
	
	
	self.setFocusListener = function(listener) {
		self.focusListener = listener;
	};
	
	this.display = function(type, callback){
		if(type == 'bundles') {
			$('view-bundles-button').addClass('button-selected');
			$('view-services-button').removeClass('button-selected');
			this.relationships = type;
			var currentRow = $('bundle-table').retrieve('HtmlTable').getSelected()[0];
			if(currentRow){
				$('bundle-table').retrieve('HtmlTable').selectNone();
				$('bundle-table').retrieve('HtmlTable').selectRow(currentRow);
			};
		} else if(type == 'services'){
			$('view-services-button').addClass('button-selected');
			$('view-bundles-button').removeClass('button-selected');
			this.relationships = type;
			var currentRow = $('bundle-table').retrieve('HtmlTable').getSelected()[0];
			if(currentRow){
				$('bundle-table').retrieve('HtmlTable').selectNone();
				$('bundle-table').retrieve('HtmlTable').selectRow(currentRow);
			};
		}
		if(callback){
			callback();
		};
	};
	
	self.dataSource = dataSource;
	
	this.bundles = {};
	
	this.bundleSpacing = 10; //Pixels to leave between bundles when rendering
	
	this.relationships = {};
	
	this.shuffle = function(bundleId, newRelationships){
		$.each(this.relationships, function(index, oldRelationship){
			oldRelationship.remove();
		});
		this.relationships = {};
		this.hideAll();

		console.log("In", newRelationships[0].length);
		console.log("Out", newRelationships[1].length);
		
		var widthTop = this.renderBundlesRow(this.bundles[bundleId], false, newRelationships[0], -239);
		var widthBottom = this.renderBundlesRow(this.bundles[bundleId], true, newRelationships[1], 239);
		
		var newWidth = widthTop < widthBottom ? widthBottom : widthTop;
		newWidth < 900 ? paper.setSize(900, paper.height) : paper.setSize(newWidth, paper.height);
		this.bundles[bundleId].move((paper.width/2).round(), (paper.height/2).round());
		this.bundles[bundleId].show();
		
		new Fx.Scroll($('bundle-canvas')).set((paper.width/2).round() - 450, (paper.height/2).round());

		$.each(this.relationships, function(index, relationship){
			relationship.display();
		});
		
		$('display').setStyle('visibility', 'visible');
	};
	
	this.addBundle = function(bundle){
		this.bundles[bundle.id] = bundle;
	};
	
	this.removeBundle = function(bundleId){
		this.bundles[bundleId].hide();
		Object.erase(this.bundles, bundleId);
	};
	
	this.empty = function(){
		this.hideAll();
		this.bundles = {};
	};
	
	this.hide = function(bundleId){
		this.bundles[bundleId].hide();
	};
	
	this.hideAll = function(){
		$.each(this.bundles, function(index, value){
			value.hide();
		});
	};

	this.renderBundlesRow = function(focused, isFrom, relations, offSet){
		var yPos = (paper.height/2).round() + offSet;
		var xPos = this.bundleSpacing;
		relations.each(function(relation){
			if(relation.bundle.id == focused.id){
				var relationship = new SelfRelationship(focused, relation.info, relation.infoKey, relation.tooltip);
				this.relationships[relationship.key] = relationship;
				focused.addRelationship(relationship);
			} else {
				if(relation.bundle.isVisible){
					if(isFrom){
						var relationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
					}else {
						var relationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
					}
					var existingRelationship = this.relationships[relationship.key];
					if(existingRelationship){
						existingRelationship.increaseCount(relation.tooltip);
					} else {
						console.log('Extra', isFrom, relation.tooltip);
						
						if(isFrom){
							var backRelationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
						}else{
							var backRelationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
						}
						
						
						this.relationships[backRelationship.key] = backRelationship;
						focused.addRelationship(backRelationship);
						relation.bundle.addRelationship(backRelationship);
					}
				} else {
					xPos = xPos + (relation.bundle.boxWidth/2);
					relation.bundle.move(xPos, yPos);
					relation.bundle.show();
					xPos = xPos + (relation.bundle.boxWidth/2) + this.bundleSpacing;
					if(isFrom){
						var relationship = new Relationship(focused, relation.bundle, relation.info, relation.infoKey, relation.tooltip);
					}else{
						var relationship = new Relationship(relation.bundle, focused, relation.info, relation.infoKey, relation.tooltip);
					}
					this.relationships[relationship.key] = relationship;
					focused.addRelationship(relationship);
					relation.bundle.addRelationship(relationship);
				}
			}
		}, this);
		return xPos;
	};
	
};

/**
 * Bundle
 */
var Bundle = function(name, version, region, id, state, location, headers, isFragment, hosts, fragments, importedPackages, exportedPackages, requiredBundles, requiringBundles, providedServices, consumedServices, dblClickCallback){

	var self = this;
	
	//Data about the bundle
	this.name = name;
	this.version = version;
	this.region = region;
	this.id = id;
	this.state = state;
	this.location = location;
	this.headers = headers; 
	this.isFragment = isFragment;
	this.hosts = hosts;
	this.fragments = fragments;
	this.importedPackages = importedPackages;
	this.exportedPackages = exportedPackages;
	this.requiredBundles = requiredBundles;
	this.requiringBundles = requiringBundles;
	this.providedServices = providedServices;
	this.consumedServices = consumedServices;
	this.dblClickCallback = dblClickCallback;
	
	this.isVisible = false; 
	
	this.relationships = {};
	
	//Display attributes
	this.bundleMargin = 8;
	this.x = 5;
	this.y = 5;
	
	this.summary = function(){
		return "[" + this.id + "] " + this.name + "\n" + this.version;
	};
	
	this.text = paper.text(this.x, this.y, this.summary()).attr({
		"text-anchor" : "start", 
		"font" : "12px Arial"
	}).hide();
	
	this.boxWidth = this.text.getBBox().width.round() + 2*this.bundleMargin;
	this.boxHeight = this.text.getBBox().height.round() + 2*this.bundleMargin;
	
	this.box = paper.rect(this.x, this.y, this.boxWidth, this.boxHeight, 8).attr({
		"fill" : "#E8F6FF", 
		"stroke" : "#002F5E"
	}).hide();
	
	this.box.toBack();
	
	this.box.dblclick(function(){this.dblClickCallback(this.id);}.bind(this));
	this.text.dblclick(function(){this.dblClickCallback(this.id);}.bind(this));
	
	this.hide = function(){
		this.text.hide();
		this.box.hide();
		this.isVisible = false; 
	};
	
	this.show = function(){
		this.text.show();
		this.box.show();
		this.isVisible = true; 
	};
	
	this.move = function(x, y) {
		this.x = x;
		this.y = y;
		this.box.attr({
			'x' : x - (this.boxWidth/2), 
			'y' : y - (this.boxHeight/2)
		});
		this.text.attr({
			'x' : x - (this.boxWidth/2) + this.bundleMargin, 
			'y' : y
		});
		
	};

	this.addRelationship = function(relationship){
		this.relationships[relationship.key] = relationship;
	};

	this.removeRelationship = function(relationshipKey){
		Object.erase(this.relationships, relationshipKey);
	};

};

var Relationship = function(fromBundle, toBundle, infoCallback, infoKey, tooltip) {

	var self = this;
	
	this.fromBundle = fromBundle;
	this.toBundle = toBundle;
	this.infoCallback = infoCallback;
	this.infoKey = infoKey;
	this.tooltip = tooltip;
	this.count = 1;
	this.key = 'from' + this.fromBundle.id + 'to' + this.toBundle.id;
	this.controlPointOffset = 100;

	this.setCoordinates = function(){
		this.startPoint = {'x' : fromBundle.x, 'y' : fromBundle.y + fromBundle.boxHeight/2};
		this.endPoint = {'x' : toBundle.x, 'y' : toBundle.y - toBundle.boxHeight/2};
		this.startPointControl = {'x' : this.startPoint.x, 'y' : this.startPoint.y + this.controlPointOffset}; 
		this.endPointControl = {'x' : this.endPoint.x, 'y' : this.endPoint.y - this.controlPointOffset};
		this.midPoint = this.calculateMidpoint(this.startPoint.x, this.startPoint.y, this.endPoint.x, this.endPoint.y); 
	};
	
	this.calculateMidpoint = function(startX, startY, endX, endY){
		if(startX < endX){
			var midX = startX + (endX - startX)/2;
		} else {
			var midX = endX + (startX - endX)/2;
		}
		if(startY < endY){
			var midY = startY + (endY - startY)/2;
		} else {
			var midY = endY + (startY - endY)/2;
		}
		return {'x' : midX, 'y' : midY};
	};
	
	this.display = function() {
		if(this.visual){
			this.visual.remove();
		}
		if(this.infoPoint){
			this.infoPoint.remove();
		}
		if(this.infoPointText){
			this.infoPointText.remove();
		}
		this.setCoordinates();
		this.visual = paper.path('M' + this.startPoint.x + ',' + this.startPoint.y + 
									'C' + this.startPointControl.x + ',' + this.startPointControl.y + 
									',' + this.endPointControl.x + ',' + this.endPointControl.y + 
									',' + this.endPoint.x + ',' + this.endPoint.y).attr({
			'arrow-end' : 'block-wide-long',
			'stroke-width' : 3,
			'stroke' : '#002F5E'
		}).toBack();
		this.infoPoint = paper.circle(this.midPoint.x, this.midPoint.y, 10).attr({
			'fill' : '#BAD9EC', 
			'stroke' : 'none',
			'title' : this.tooltip
		});
		this.infoPointText = paper.text(this.midPoint.x, this.midPoint.y, this.count).attr({
			'font' : '13px Arial', 
			'stroke' : '#002F5E',
			'title' : this.tooltip
		});
		
		this.infoPoint.click(function(){this.displayInfoBox();});
		this.infoPointText.click(function(){this.displayInfoBox();});
		
		this.infoPoint.hover(function(){this.glow = this.visual.glow();}, function(){this.glow.remove();}, this, this);
		this.infoPointText.hover(function(){this.glow = this.visual.glow();}, function(){this.glow.remove();}, this, this);
	};
	
	this.displayInfoBox = function() {
		infoBox.go(this.infoCallback, this.infoKey);
	};
	
	this.increaseCount = function(tooltip) {
		this.count = this.count + 1;
		this.tooltip = this.tooltip + ' ' + tooltip;
		if(this.infoPointText){
			this.infoPointText.attr({
				'text' : this.count
			});
		};
	};
	
	this.remove = function() {
		this.visual.remove();
		this.infoPoint.remove();
		this.infoPointText.remove();
		this.fromBundle.removeRelationship(this.key);
		this.toBundle.removeRelationship(this.key);
	};

};

var SelfRelationship = function(bundle, infoCallback, infoKey, tooltip) {

	var self = this;
	
	this.bundle = bundle;
	this.infoCallback = infoCallback;
	this.infoKey = infoKey;
	this.tooltip = tooltip;
	this.count = 1;
	this.key = 'self' + this.bundle.id;
	this.offset = 10;
	this.offset2 = 100;
	
	this.setCoordinates = function(){
		this.startPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2, 'y' : this.bundle.y - this.offset};
		this.endPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2, 'y' : this.bundle.y + this.offset};
		
		this.startPointControl = {'x' : this.startPoint.x + this.offset2, 'y' : this.startPoint.y - this.offset2}; 
		this.endPointControl = {'x' : this.endPoint.x + this.offset2, 'y' : this.endPoint.y + this.offset2};
		
		this.midPoint = {'x' : this.bundle.x + this.bundle.boxWidth/2 + this.offset2*0.74, 'y' : this.bundle.y}; 
	};
	
	this.display = function() {
		if(this.visual){
			this.visual.remove();
		}
		if(this.infoPoint){
			this.infoPoint.remove();
		}
		if(this.infoPointText){
			this.infoPointText.remove();
		}
		this.setCoordinates();
		this.visual = paper.path('M' + this.startPoint.x + ',' + this.startPoint.y + 
									'C' + this.startPointControl.x + ',' + this.startPointControl.y + 
									',' + this.endPointControl.x + ',' + this.endPointControl.y + 
									',' + this.endPoint.x + ',' + this.endPoint.y).attr({
			'arrow-end' : 'block-wide-long',
			'stroke-width' : 3,
			'stroke' : '#002F5E'
		}).toBack();
		this.infoPoint = paper.circle(this.midPoint.x, this.midPoint.y, 10).attr({
			'fill' : '#BAD9EC', 
			'stroke' : 'none',
			'title' : this.tooltip
		});
		this.infoPointText = paper.text(this.midPoint.x, this.midPoint.y, this.count).attr({
			'font' : '13px Arial', 
			'stroke' : '#002F5E',
			'title' : this.tooltip
		});
		
		this.infoPoint.click(function(){this.displayInfoBox();});
		this.infoPointText.click(function(){this.displayInfoBox();});
		
		this.infoPoint.hover(function(){this.glow = this.visual.glow();}, function(){this.glow.remove();}, this, this);
		this.infoPointText.hover(function(){this.glow = this.visual.glow();}, function(){this.glow.remove();}, this, this);
		
	};
	
	this.increaseCount = function(tooltip) {
		this.count = this.count + 1;
		this.tooltip = this.tooltip + ' ' + tooltip;
		if(this.infoPointText){
			this.infoPointText.attr({
				'text' : this.count
			});
		};
	};
	
	this.remove = function() {
		this.visual.remove();
		this.infoPoint.remove();
		this.infoPointText.remove();
	};
	
};
