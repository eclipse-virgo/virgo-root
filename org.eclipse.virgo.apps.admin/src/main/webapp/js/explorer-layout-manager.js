/*******************************************************************************
* Copyright (c) 2011 David Normiongton
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   David Normington - initial contribution
*   
*******************************************************************************/

function LayoutManager(bundles, key, arrows) {
	
	/**
	 * Fields
	 */
	this.bundles = bundles;
	this.key = key;
	this.arrows = arrows;

	/**
	 * Sets the service relationships
	 * 
	 * @param bundles (Object) - The main bundles object
	 * @param bundleKey (string) - Key of currently selected bundle
	 */
	this.setServiceRelations = function() {
		
		this.inUseArrReady = false;
		this.inUsePropsReady = false;
		this.regArrReady = false;
		this.regPropsReady = false;

		// Get bundle Ids and properties from service Ids
		this.getServices('ServicesInUse');
		this.getServices('RegisteredServices');

		var future = function() {
			// Check return of bundle Ids and properties
			if (this.inUseArrReady && this.inUsePropsReady && this.regArrReady && this.regPropsReady) {
				clearInterval(future);

				// Merge bundle Id and property Objects
				this.servicesInUseAll = this.mergeIdAndProperties(this.servicesInUseArr, this.servicesInUseProps);
				this.registeredServicesAll = this.mergeIdAndProperties(this.registeredServicesArr, this.registeredServicesProps);

				// Give bundles serviceStates and service properties
				this.orderBundles(this.servicesInUseAll, this.registeredServicesAll, function() {

					// Position bundles on canvas
					this.positionServices(function() {

						// arrows and arrow events
						this.drawRelationships();
					}.bind(this));
				}.bind(this));
			}
		}.bind(this);

		// Similar functionality to future variables in Java
		var future = future.periodical(10);
	};

	/**
	 * Retrieves the bundle Ids and properties that are related by service to the selected bundle
	 * 
	 * @param bundles (Object) - The main bundles object
	 * @param bundleKey (string) - Key of currently selected bundle
	 * @param type (string) - Type of service to query
	 */
	this.getServices = function(type) {

		if (type == 'ServicesInUse') {
			// Get service Ids
			this.servicesInUse = this.bundles[this.key].bundle[type];

			// Prepare for JSON
			var requestBundleIds = this.getRequestArr("EXEC", "osgi.core:type=serviceState,version=1.5", "getBundleIdentifier(long)", this.servicesInUse);
			var requestProperties = this.getRequestArr("EXEC", "osgi.core:type=serviceState,version=1.5", "getProperties(long)", this.servicesInUse);

			// Requests for bundle Ids and properties
			util.doBulkQuery(requestBundleIds, this.getIdsFromServicesInUse.bind(this), null);
			util.doBulkQuery(requestProperties, this.getPropsFromServicesInUse.bind(this), null);

		} else if (type == 'RegisteredServices') {
			// Get service Ids
			this.registeredServices = this.bundles[this.key].bundle[type];

			// Prepare for JSON
			var requestBundleIds = this.getRequestArr("EXEC", "osgi.core:type=serviceState,version=1.5", "getUsingBundles(long)", this.registeredServices);
			var requestProperties = this.getRequestArr("EXEC", "osgi.core:type=serviceState,version=1.5", "getProperties(long)", this.registeredServices);

			// Requests for bundle Ids and properties
			util.doBulkQuery(requestBundleIds, this.getIdsFromRegisteredServices.bind(this), null);
			util.doBulkQuery(requestProperties, this.getPropsFromRegisteredServices.bind(this), null);
		}
	};

	/**
	 * Takes list of service Ids and prepares them for bulk query
	 * 
	 * @param type (string) - EXEC
	 * @param mbean (string) - MBean's ObjectName
	 * @param operation (string) - The operation to execute
	 * @param keys (Array) - Array of service Ids
	 */
	this.getRequestArr = function(type, mbean, operation, keys) {
		var request = [];
		Object.each(keys, function(value, key) {
			var obj = {
				"type" : type,
				"mbean" : mbean,
				"operation" : operation,
				"arguments" : [ value ]
			};
			request.push(obj);
		});
		return request;
	};

	/**
	 * The JSON callback for serviceInUse queries
	 * 
	 * @param JSON (Object) - Object returned from JSON
	 */
	this.getIdsFromServicesInUse = function(JSON) {
		this.servicesInUseArr = {};
		Object.each(JSON, function(objValue, objKey) {
			this.servicesInUseArr[objKey] = objValue.value + "";
		}, this);

		this.inUseArrReady = true;
	};

	/**
	 * @param JSON (Object) - Service Properties
	 */
	this.getPropsFromServicesInUse = function(JSON) {
		this.servicesInUseProps = {};
		Object.each(JSON, function(objValue, objKey) {
			this.servicesInUseProps[objKey] = {};
			Object.each(objValue.value, function(value, key) {
				this.servicesInUseProps[objKey][value['Key']] = value['Value'];
			}, this);
		}, this);
		this.inUsePropsReady = true;
	};

	/**
	 * The JSON callback for registeredService queries
	 * 
	 * @param JSON (Object) - Object returned from JSON
	 */
	this.getIdsFromRegisteredServices = function(JSON) {
		this.registeredServicesArr = {};
		Object.each(JSON, function(objValue, objKey) {
			Object.each(objValue['value'], function(valValue, valKey) {
				this.registeredServicesArr[objKey] = valValue + "";
			}, this);
		}, this);
		this.regArrReady = true;
	};

	/**
	 * @param JSON (Object) - Service Properties
	 */
	this.getPropsFromRegisteredServices = function(JSON) {
		this.registeredServicesProps = {};
		Object.each(JSON, function(objValue, objKey) {
			this.registeredServicesProps[objKey] = {};
			Object.each(objValue.value, function(value, key) {
				this.registeredServicesProps[objKey][value['Key']] = value['Value'];
			}, this);
		}, this);
		this.regPropsReady = true;
	};

	/**
	 * Merge bundle Ids and properties of services
	 * 
	 * @param arr (Object) - Bundle Ids
	 * @param props (Object) - Properties
	 */
	this.mergeIdAndProperties = function(arr, props) {
		var all = {};
		Object.each(arr, function(value, key) {
			all[key] = {};
			all[key]['bundleId'] = value;
			all[key]['properties'] = {};
			all[key]['properties'] = props[key];
		});
		return all;
	};

	/**
	 * Assigns serviceState attribute to all bundles. Either selected, inUse, registered or hide. Assigns properties of
	 * the service. Also gets the width (pixels) of all inUse bundles, width of registered bundles and the count of
	 * bundles for each type.
	 * 
	 * @param servicesInUse (Array) - Bundle Ids and properties of servicesInUse
	 * @param registeredServices (Array) - Bundle Ids and properties of registeredServices
	 * @param callback (Function) - To call when finished
	 */
	this.orderBundles = function(servicesInUse, registeredServices, callback) {
		var inUseCount = 0, inUseWidth = 0, regCount = 0, regWidth = 0, xMargin = 10;
		
		var bundlesOrdered = {};
		bundlesOrdered.bundles = {};
		
		Object.each(this.bundles, function(bundleValue, bundleKey) {
			if(bundleKey != 'widest' && bundleKey != 'lastAdded'){
				
				bundlesOrdered.bundles[bundleKey] = bundleValue;
				
				// if selected bundle
				if (bundleKey + "" == this.key) {
					bundleValue.service.serviceState = "selected";

					// if serviceInUse
				} else if (Object.contains(this.servicesInUseArr, bundleKey)) {
					// Assign serviceState and properties
					bundleValue = this.setServiceAttributes(bundleValue, bundleKey, "inUse", servicesInUse);
					// Add width of bundle plus xMargin
					inUseWidth += bundleValue.visual['set'].getBBox().width + xMargin;
					// Increase count
					inUseCount++;
	
					// if registeredService
				} else if (Object.contains(this.registeredServicesArr, bundleKey)) {
					// Assign serviceState and properties
					bundleValue = this.setServiceAttributes(bundleValue, bundleKey, "registered", registeredServices);
					// Add width of bundle plus xMargin
					regWidth += bundleValue.visual['set'].getBBox().width + xMargin;
					// Increase count
					regCount++;
	
					// if none of the above
				} else {
					bundleValue.service.serviceState = "hide";
				}
			}
		}, this);

		bundlesOrdered.inUseWidth = inUseWidth;// Width of all serviceInUse bundles
		bundlesOrdered.inUseCount = inUseCount;// Number of all servicesInUse
		bundlesOrdered.regWidth = regWidth;// Width of all registeredService bundles
		bundlesOrdered.regCount = regCount;// Number of all registeredServices
		this.bundlesOrdered = bundlesOrdered;
		
		callback();
	};



	/**
	 * Sets the serviceState and properties for that bundle
	 * 
	 * @param bundle (Object) - Current bundle
	 * @param bundleKey (string) - Current bundle Id
	 * @param state (string) - serviceState to add
	 * @param properties (Object) - The properties of the service
	 * @param services (Object) - List of services to compare bundle to
	 */
	this.setServiceAttributes = function(bundle, bundleKey, state, services) {
		bundle.service['serviceState'] = state;

		Object.each(services, function(value, key) {
			if (value['bundleId'] == bundleKey) {
				bundle.service['properties'] = value['properties'];
			}
		}, this);

		return bundle;
	};

	/**
	 * Resizes canvas depending on the width of the service bundles.
	 */
	this.resizeCanvas = function() {
		// Dynamically set canvas
		if (this.bundlesOrdered.inUseWidth >= this.bundlesOrdered.regWidth) {
			if (this.bundlesOrdered.inUseWidth > $('expl_canvas').getSize().x) {
				Explorer.canvas.setSize(this.bundlesOrdered.inUseWidth, Explorer.canvas.height);
			} else {
				Explorer.canvas.setSize($('expl_canvas').getSize().x, Explorer.canvas.height);
			}
		} else if (this.bundlesOrdered.regWidth > this.bundlesOrdered.inUseWidth) {
			if (this.bundlesOrdered.regWidth > $('expl_canvas').getSize().x) {
				Explorer.canvas.setSize(this.bundlesOrdered.regWidth, Explorer.canvas.height);
			} else {
				Explorer.canvas.setSize($('expl_canvas').getSize().x, Explorer.canvas.height);
			}
		}

		// If no services, shrink canvas
		if (this.bundlesOrdered.inUseCount == 0 && this.bundlesOrdered.regCount == 0) {
			Explorer.canvas.setSize($("expl_canvas").getSize().x, Explorer.canvas.height);
		}
	};

	/**
	 * Removes current arrows from canvas
	 */
	this.clearArrows = function() {
		Object.each(this.arrows, function(value, key) {
			if (value instanceof Object) {
				value['underLine'].remove();
				value['overLine'].remove();
				value['head'].remove();
			}
		});
	};

	/**
	 * Sets the starting x position for services. This allows the services to be centred.
	 * 
	 * @returns (Object) - Starting positions for both registeredServices and servicesInUse
	 */
	this.setServiceXStartPos = function() {
		var xRegStart = 0, xInUseStart = 0;
		
		if (this.bundlesOrdered.inUseWidth >= this.bundlesOrdered.regWidth
				&& this.bundlesOrdered.inUseWidth >= $("expl_canvas").getSize().x) {
			xRegStart = (Explorer.canvas.width - this.bundlesOrdered.regWidth) / 2;
			
		} else if (this.bundlesOrdered.inUseWidth >= this.bundlesOrdered.regWidth
				&& this.bundlesOrdered.inUseWidth < $("expl_canvas").getSize().x) {
			xInUseStart = (Explorer.canvas.width - this.bundlesOrdered.inUseWidth) / 2;
			xRegStart = (Explorer.canvas.width - this.bundlesOrdered.regWidth) / 2;
			
		} else if (this.bundlesOrdered.regWidth >= this.bundlesOrdered.inUseWidth
				&& this.bundlesOrdered.regWidth >= $("expl_canvas").getSize().x) {
			xInUseStart = (Explorer.canvas.width - this.bundlesOrdered.inUseWidth) / 2;
			
		} else if (this.bundlesOrdered.regWidth >= this.bundlesOrdered.inUseWidth
				&& this.bundlesOrdered.regWidth < $("expl_canvas").getSize().x) {
			xInUseStart = (Explorer.canvas.width - this.bundlesOrdered.inUseWidth) / 2;
			xRegStart = (Explorer.canvas.width - this.bundlesOrdered.regWidth) / 2;
		}
		
		return {
			"reg" : xRegStart,
			"inUse" : xInUseStart
		};
	};

	/**
	 * Positions the bundles on the canvas, resets the canvas size, removes previous arrows if they exist, Delays
	 * callback to allow for the animating of bundles
	 * 
	 * @param callback (Function) - To be called when finished
	 */
	this.positionServices = function(callback) {

		var bundles = this.bundlesOrdered.bundles;
		this.resizeCanvas();
		var xStart = this.setServiceXStartPos();
		this.clearArrows();
		var centerCoords = this.findCenter();
		
		//Scroll to centre
		$("expl_canvas").scrollTo(centerCoords['x'] - $("expl_canvas").getSize().x / 2, 0);

		var xMargin = 10;
		Object.each(bundles, function(bundleValue, bundleKey) {
			if(bundleKey != 'widest' && bundleKey != 'lastAdded') {
				if (bundleValue.service['serviceState'] == "selected") {
					bundleValue.visual['set'].show();
					var distance = this.distancesTo(bundleValue.visual['set'], centerCoords['x']- (bundleValue.visual['set'].getBBox().width / 2), centerCoords['y']);
					this.moveShape(bundleValue.visual['set'], distance['x'], distance['y']);
	
				} else if (bundleValue.service['serviceState'] == "inUse") {
					bundleValue.visual['set'].show();
					var distance = this.distancesTo(bundleValue.visual['set'], xStart["inUse"], centerCoords['y'] - 100);
					this.moveShape(bundleValue.visual['set'], distance['x'], distance['y']);
					// Sort next x coord
					xStart["inUse"] += bundleValue.visual['set'].getBBox().width + xMargin;
	
				} else if (bundleValue.service['serviceState'] == "registered") {
					bundleValue.visual['set'].show();
					var distance = this.distancesTo(bundleValue.visual['set'], xStart["reg"], centerCoords['y'] + 100);
					this.moveShape(bundleValue.visual['set'], distance['x'], distance['y']);
					// Sort next x coord
					xStart["reg"] += bundleValue.visual['set'].getBBox().width + xMargin;
	
				} else if (bundleValue.service['serviceState'] == "hide") {
					bundleValue.visual['set'].hide();
				}
			}
		}, this);

		// Allow time for animations before drawing relationships
		callback.delay('0');

	};

	/**
	 * Draws arrows from servicesInUse to selected and from selected to registeredServices
	 * 
	 */
	this.drawRelationships = function() {
		var bundles = this.bundlesOrdered.bundles, arrows = this.arrows, centerCoords = this.findCenter();

		var inUseSpaceSelected = bundles[this.key].visual['set'].getBBox().width / this.bundlesOrdered.inUseCount;
		var regSpaceSelected = bundles[this.key].visual['set'].getBBox().width / this.bundlesOrdered.regCount;
		var selectedLeft = centerCoords['x'] - (bundles[this.key].visual['set'].getBBox().width / 2);

		var inUseCount = 0, regCount = 0;

		Object.each(bundles, function(bundleValue, bundleKey) {
			if(bundleKey != 'widest' && bundleKey != 'lastAdded') {
				if (bundleValue.service['serviceState'] == "inUse") {
					var inUseEndX = selectedLeft + (inUseCount * inUseSpaceSelected) + (inUseSpaceSelected / 2);
					var xInUseMid = bundleValue.visual['rect'].getBBox().x + (bundleValue.visual['rect'].getBBox().width / 2);
					var yInUseTop = 150 + bundleValue.visual['set'].getBBox().height;
					arrows[bundleKey] = this.arrow(xInUseMid, yInUseTop, inUseEndX, centerCoords['y']);
					this.addArrowEventHandler(arrows[bundleKey]['head'].node, bundleValue.service['properties']);
					this.addArrowEventHandler(arrows[bundleKey]['overLine'].node, bundleValue.service['properties']);
					this.addArrowEventHandler(arrows[bundleKey]['underLine'].node, bundleValue.service['properties']);
					inUseCount++;
	
				} else if (bundleValue.service['serviceState'] == "registered") {
					var regEndX = selectedLeft + (regCount * regSpaceSelected) + (regSpaceSelected / 2);
					var xRegMid = bundleValue.visual['rect'].attr("x") + (bundleValue.visual['rect'].attr("width") / 2);
					var yRegTop = centerCoords['y'] + 100;
	
					arrows[bundleKey] = this.arrow(regEndX, centerCoords['y'] + bundles[this.key].visual['set'].getBBox().height, xRegMid, yRegTop);
					this.addArrowEventHandler(arrows[bundleKey]['head'].node, bundleValue.service['properties']);
					this.addArrowEventHandler(arrows[bundleKey]['overLine'].node, bundleValue.service['properties']);
					this.addArrowEventHandler(arrows[bundleKey]['underLine'].node, bundleValue.service['properties']);
					regCount++;
				}
			}
		}, this);
	};

	/**
	 * Add an event listener to an arrow
	 * 
	 * @param el - Dom element
	 * @param props - Object to create tooltip text from
	 */
	this.addArrowEventHandler = function(el, props) {
		var text = "";
		Object.each(props, function(value, key) {
			text += "<strong>" + key + ":</strong> " + value + "<br />";
		});

		el.onmouseover = function(e) {
			util.tooltip(el, text);
		};
	};

	/**
	 * Creates a path with an arrowhead
	 * 
	 * @param x1 (number) - x coordinate of start pos.
	 * @param y1 (number) - y coordinate of start pos.
	 * @param x2 (number) - x coordinate of end pos.
	 * @param y2 (number) - y coordinate of end pos.
	 * @returns (Object) - Holding line and arrow Raphael objects
	 */
	this.arrow = function(x1, y1, x2, y2) {
		// Make this work x1 = x2 and y1 = y2
		var width = 5, height = 10;
		var angle = (Math.atan2(y2 - y1, x2 - x1) / Math.PI) * 180;
		var underLine = Explorer.canvas.path("M" + x1 + " " + y1 + "L" + x2 + " " + y2).attr({
			'stroke' : '#fff',
			'stroke-width' : '5'
		});
		var overLine = Explorer.canvas.path("M" + x1 + " " + y1 + "L" + x2 + " " + y2).attr({
			'stroke' : '#333'
		});
		var arrow = Explorer.canvas.path(
				"M" + x2 + " " + y2 + "L" + (x2 - height) + " " + (y2 + width / 2) + "L" + (x2 - height) + " "
						+ (y2 - width / 2) + "L" + x2 + " " + y2).attr({
			'fill' : '#333',
			'stroke' : '#333'
		}).rotate(angle, x2, y2);
		return {
			'underLine' : underLine,
			'overLine' : overLine,
			'head' : arrow
		};
	};

	/**
	 * Gets the distances (pixels) to the coordinates give
	 * 
	 * @param el (Object) - Raphael object
	 * @param x (number) - x coordinate of destination
	 * @param y (number) - y coordinate of destination
	 * @returns (Object) - Distances to position
	 */
	this.distancesTo = function(el, x, y) {
		var xDistance = (x - el.getBBox().x);
		var yDistance = (y - el.getBBox().y);
		return {
			'x' : xDistance,
			'y' : yDistance
		};
	};

	/**
	 * Animate an object to a position
	 * 
	 * @param el (Object) - Raphael object to move
	 * @param x (number) - x offset of destination
	 * @param y (number) - y offset of destination
	 */
	this.moveShape = function(el, x, y) {
		el.transform('t' + x + ',' + y);
	};

	/**
	 * Finds the centre of the canvas
	 * 
	 * @returns (Object) - Coordinates of centre
	 */
	this.findCenter = function() {
		var width = Explorer.canvas.width + "", height = Explorer.canvas.height + "";
		width.indexOf("px") != -1 ? width = width.substr(-1, 2) : this;
		height.indexOf("px") != -1 ? height = height.substr(0, height.length - 2) : this;
		var x = (width / 2);
		var y = (height / 2);
		return {
			'x' : x,
			'y' : y
		};
	};

	/**
	 * Start layout
	 */
	this.setServiceRelations();
	
};
