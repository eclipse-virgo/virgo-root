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
 * Test object for use when testing the Configuration page JavaScript
 */


var Data = function() {
	
	this.value = {};

	this.value.each = function(mbeansProcessingFunction){
		
	};		
	
	this.value.Properties = {};
	
};

var Label = function() {
	
	this.firstChild = {};
	
	this.firstChild.firstChild = new Element('div');
	
	this.firstChild.firstChild.addClass('plus');
	
	this.getChildren = function(){
		return [new LabelChild(), new LabelChild()];
	};
	
};

var LabelChild = function() {
	
	this.nix = function(destroy){
		
	};
	
};