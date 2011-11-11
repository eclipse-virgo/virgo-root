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
 * Test object for use when testing the Artifact page JavaScript
 */


var Location = function() {
	
	this.value = "Testing";

};

var DataOne = function() {

	window.log('DataOne');
	this.length = 1;
	
	this.each = function(processingFunction){
		processingFunction('sampleItem');
	};	

};

var DataTwo = function() {

	this.length = 1;
	
	this.each = function(processingFunction){
		processingFunction(['sampleItem1', 'sampleItem2']);
	};	

};
