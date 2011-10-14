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


var Data = function() {
	
	this.value = {};

	this.value.each = function(mbeansProcessingFunction){
		var props = {
				'name': 'Fake', 
				'version': 'Foo', 
				'region': 'Somewhere',
				'artifact-type': 'something'
			};
		mbeansProcessingFunction(new SampleArtifact('aDomain', props, 'anObjectName'));
	};		
	
	this.value.Properties = {};
	
};

var SampleArtifact = function(domain, properties, objectName){
	this.domain = domain;
	this.properties = properties;
	this.get = function(key){
		return this.properties[key];
	};
	this.toString = objectName;
};
