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
 * Used during testing to fake out required parts of MooTools
 */


Function.prototype.bind = function(bind){
	return this;
};

Object.prototype.each = function(object, someFunction, scope){

};

String.prototype.capitalize = function(){
	return this;
};

Array.prototype.contains = function(something){
	return false;
};

Array.prototype.each = function(someFunction, scope){
	for (var i = 0, l = this.length; i < l; i++){
		if (i in this) someFunction.call(scope, this[i], i, this);
	}
};
