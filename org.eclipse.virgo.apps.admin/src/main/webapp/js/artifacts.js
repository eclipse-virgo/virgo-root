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
 * Script to be loaded in to the head of the artifacts view
 */
function pageinit(){
	uploadManager = new UploadManager();
	util.doQuery('search/org.eclipse.virgo.kernel:type=ArtifactModel,*', function (response){
		tree = new Tree();
		tree.setup(response.value, 'type');
		util.pageReady();
	});
}

/**
 * Constructor method for the Artifacts Tree
 * 
 * @param mbeans
 * @returns
 */
var Tree = function(mbeans) {
	
	var self = this;
	
	/**
	 * Do the initial population of the tree
	 * 
	 * @param mbeans - the json data from the server
	 * @param filter - what the top level should be sorted by
	 */
	this.setup = function (mbeans, filter){
		var filterMatches = new Array();
		$.each(mbeans, function(index, mbean){
			var artifact = new Artifact(util.readObjectName(mbean));
			var artifactFilterValue = artifact[filter];
			if(-1 == filterMatches.indexOf(artifactFilterValue)){
				filterMatches.push(artifactFilterValue);
			};
			
		});
		$.each(filterMatches, function(filterMatch){
			var filterContainer;
			if(filter == 'type'){
				filterContainer = self.getNodeContainer(filterMatch, filterMatch, filterMatch, filter);
			} else {
				filterContainer = self.getNodeContainer(filterMatch, 'default', filterMatch, filter);
			}
			filterContainer.firstChild.set('onclick', 'tree.renderTopLevel("' + filter + '", "' + filterMatch + '")');
			$('.artifacts-tree').append(filterContainer);
		});
	};

	/**
	 * Called when the user chooses a different value to sort the top level of the tree on
	 * 
	 * @param filter - the artifact property to filter on
	 */
	this.reRenderWithFilter = function (filter){
		$('artifacts-tree').empty();
		util.doQuery('search/org.eclipse.virgo.kernel:type=ArtifactModel,*', function (response){
			setup(response.value, filter);
		});
		if(filter == 'type') {
			$('type-filter-button').addClass('button-selected');
			$('region-filter-button').removeClass('button-selected');
		} else {
			$('region-filter-button').addClass('button-selected');
			$('type-filter-button').removeClass('button-selected');
		}
		
	};
	
	/**
	 * Called when one of the top level node is clicked
	 * 
	 * @param filter - the artifact property to sort by
	 * @param parent - the unique key of the parent node
	 */
	this.renderTopLevel = function(filter, parent){
		var parentElement = $(parent);
		var children = parentElement.getChildren();
		children[0].firstChild.replaceWith(this.getIconElement('loader-small.gif'));// Set the plus/minus icon
		if(children.length == 1){//It's closed
			util.doQuery('search/org.eclipse.virgo.kernel:type=ArtifactModel,*', function (response){
				renderTopLevelRequest(response.value, parent, filter);
				children[0].firstChild.replaceWith(this.getIconElement('tree-icons/minus.png'));// Set the plus/minus icon
			});
		} else {//It's open
			parentElement.children[1].destroy();
			children[0].firstChild.replaceWith(this.getIconElement('tree-icons/plus.png'));// Set the plus/minus icon
		}	
	};
	
	/**
	 * Called when any artifact in the tree is expanded
	 * 
	 * @param objectName - the unique key of the artifact to render
	 * @param parent - id of the element to render the artifact under
	 */
	this.renderArtifact = function (objectName, parent){		
		var parentElement = $(parent);
		var children = parentElement.getChildren();
		children[0].firstChild.replaceWith(this.getIconElement('loader-small.gif'));// Set the plus/minus icon
		if(children.length == 1){//It's closed
			util.doQuery('read/' + objectName, function(response){
				renderArtifactRequest(response.value, util.readObjectName(objectName), parent);
				children[0].firstChild.replaceWith(this.getIconElement('tree-icons/minus.png'));// Set the plus/minus icon
			});
		} else {//It's open
			parentElement.children[1].destroy();
			children[0].firstChild.replaceWith(this.getIconElement('tree-icons/plus.png'));// Set the plus/minus icon
		}
	};
	
	/**
	 * Accepted operations are 'start', 'stop', 'uninstall' and 'refresh'
	 * 
	 */
	this.doArtifactOperation = function(objectName, operation){
		util.doQuery('exec/' + objectName + '/' + operation, function(response){
			util.doQuery('read/' + objectName, function(response){
				renderOperationResult(response, util.readObjectName(objectName));
			});
		});
	};
	
	this.renderOperationResult = function(responseJSON, objectName){
		var artifact = new Artifact(objectName);
		if(responseJSON.status == 404){
			$('.' + artifact.key).destroy();
		} else if(responseJSON.value){
			$.each($('.' + artifact.key), function(nodeToUpdate){
				if(nodeToUpdate.getChildren().length > 1){
					nodeToUpdate.getChildren()[1].destroy();
					renderArtifactRequest(responseJSON.value, objectName, nodeToUpdate.id);
				}
			});
		} else {
			alert('Unable to retrieve information about the modified Artifact, please refresh the page.');
		}
	};
	
	/* **************** START PRIVATE METHODS **************** */
	
	/**
	 * When the server responds to an artifact type request this method
	 * takes care of rendering the top level results
	 * 
	 * @param json - the raw json returned form the server
	 * @param parent - element to put the artifact under
	 */
	this.renderTopLevelRequest = function(json, parent, filter){
		var parentElement = $(parent);
		var fxContainer = $('<div />', {'class': 'fx-container'});
		json.each(function(mbean){
			var artifact = new Artifact(util.readObjectName(mbean));
			if(artifact[filter] == parent){
				this.getArtifactLabel(artifact, parent).inject(fxContainer);
			}
		}, this);
		parentElement.append(fxContainer);
		fxContainer.slideToggle(util.fxTime);
	};
	
	/**
	 * When the server responds to an artifact request this method takes care 
	 * of rendering the artifact and all its attributes and dependents
	 * 
	 * @param json - the raw json returned form the server
	 * @param objectName - of the artifact
	 * @param parent - element to put the artifact under
	 */
	this.renderArtifactRequest = function(json, objectName, parent){	
		var parentElement = $(parent);
		var fxContainer = $('<div />', {'class': 'fx-container'});
		var fullArtifact = new FullArtifact(json, objectName);
		
		var artifactControlBar = this.getArtifactControlBar(fullArtifact);
		if(fullArtifact.type == 'configuration'){
			var configControl = $('<a />', {'class': 'artifact-control'});
			configControl.set('href', util.getCurrentHost() + '/content/configuration#' + fullArtifact.name);
			configControl.text('View');
			artifactControlBar.append(configControl);
		}

		fxContainer.append(artifactControlBar);
		fxContainer.append(this.getArtifactAttribute('Name: ' + fullArtifact.name));
		fxContainer.append(this.getArtifactAttribute('Version: ' + fullArtifact.version));
		fxContainer.append(this.getArtifactAttribute('Region: ' + fullArtifact.region));
		fxContainer.append(this.getArtifactAttribute('Type: ' + fullArtifact.type.capitalize()));
		fxContainer.append(this.getArtifactAttribute(fullArtifact.state.toLowerCase().capitalize(), 'state-' + fullArtifact.state));
		
		$.each(fullArtifact.properties, function(key, value){
			if(value == 'true' || value == true){
				if(key == 'Spring' || key == 'Scoped' || key == 'Atomic' || key == 'Scoped-Atomic'){
					fxContainer.append(this.getArtifactAttribute(key, key));
				} else {
					fxContainer.append(this.getArtifactAttribute(key));
				}
			} else {
				if(key == 'Bundle Id'){
					fxContainer.append(this.getArtifactAttribute(key + ': ' + value, null, util.getCurrentHost() + '/content/explorer#' + value));
				} else {
					fxContainer.append(this.getArtifactAttribute(key + ': ' + value));
				}
			}
		});
		
		$.each(fullArtifact.dependents, function(objectName){
			var dependentArtifact = new Artifact(objectName);
			fxContainer.append(this.getArtifactLabel(dependentArtifact, parent));
		});

		parentElement.append(fxContainer);
		fxContainer.slideToggle(util.fxTime);
	};
	
	/**
	 * Create an artifact label element with text, icons etc for the supplied artifact
	 * 
	 * @param artifact - to construct
	 * @param parent - element to insert the $ in to
	 */
	this.getArtifactLabel = function(artifact, parent){
		var label = this.getNodeContainer(artifact.name + '_' + artifact.version, artifact.type, parent + artifact.key, artifact.key);
		label.firstChild.set('onclick', 'tree.renderArtifact("' + artifact.objectName.toString + '", "' + parent + artifact.key + '")');
		return label;
	};
	
	/**
	 * Create a label element with text and icons from the supplied details
	 * 
	 * @param text - text for the artifact label
	 * @param icon - the name of the icon to use
	 * @param id - unique id of the label
	 */
	this.getNodeContainer = function(text, icon, id, key){
		var artifactContainer = $('div', {'class': 'artifact-container'});
		artifactContainer.addClass(key);
		artifactContainer.prop('id', id);

		var artifactLabel = $('div', {'class': 'artifact-label'});
		artifactContainer.append(artifactLabel);

		var plusMinus = this.getIconElement('tree-icons/plus.png');
		plusMinus.addClass('plus');
		artifactLabel.append(plusMinus);
		artifactLabel.append(this.getIconElement('tree-icons/node-' + icon + '.png'));
		var span  = $('span.label-text');
		span.text(text);
		artifactLabel.append(span);
		
		return artifactContainer;
	};
	
	this.getArtifactControlBar = function(artifact) {
		var controlBar = $('div', {'class': 'artifact-attribute'});
		this.getIconElement('tree-icons/attribute-default.png').inject(controlBar);
		var span  = $('span.label-text');
		span.text('Actions:');
		
		controlBar.append(span);
		controlBar.append(this.getArtifactControl('start', artifact.objectName));
		controlBar.append(this.getArtifactControl('refresh', artifact.objectName));
		controlBar.append(this.getArtifactControl('stop', artifact.objectName));
		controlBar.append(this.getArtifactControl('uninstall', artifact.objectName));
		return controlBar;
	};
	
	this.getArtifactControl = function(action, objectName) {
		var control = $('<div />', {'class': 'artifact-control'});
		control.text(action.capitalize());
		control.set('onclick', 'tree.doArtifactOperation("' + objectName.toString + '", "' + action + '")');
		return control;
	};
	
	/**
	 * Create an element for an artifacts property with an icon
	 * 
	 * @param text - the text for the attribute
	 * @param icon - the icon name
	 */
	this.getArtifactAttribute = function(text, icon, link) {
		var property = $('<div />', {'class': 'artifact-attribute'});
		property.append(this.getIconElement('tree-icons/attribute-default.png'));
		if(icon){
			property.append(this.getIconElement('tree-icons/attribute-' + icon + '.png'));
		}
		var label;
		if(link){
			label = $('a');
			label.set('href', link);
		} else {
			label  = $('span');
		}
		label.text(text);
		label.addClass('label-text');
		property.append(label);
		return property;
	};
	
	/**
	 * Create an element with a background image applied
	 * 
	 * @param iconName - for the image
	 */
	this.getIconElement = function(iconName){
		var imageElement = $('<div />', {'class': 'tree-icon'});
		imageElement.css('background', 'url("' + util.getCurrentHost() + '/resources/images/' + iconName.toLowerCase()  + '") no-repeat center center');
		return imageElement;
	};
	
};

/**
 * A representation of a Artifact based on it's objectName
 * 
 * @param objectName
 */
var Artifact = function(objectName) {
	this.name = objectName.get('name');
	this.version = objectName.get('version');
	this.region = objectName.get('region');
	this.type = objectName.get('artifact-type');
	this.objectName = objectName;
	this.key = (this.name + this.version + this.region).replace(new RegExp('\\.', 'g'), '_');//Converts the dots to underscores so the osgi symbolic-name grammar complies to the CSS class identifier grammar 
};

/**
 * A representation of a Artifact based on a full query
 * 
 * @param metaData
 * @param objectName
 */
var FullArtifact = function(metaData, objectName) {
	this.name = metaData['Name'];
	this.version = metaData['Version'];
	this.region = metaData['Region'];
	this.type = metaData['Type'];
	this.state = metaData['State'];
	this.objectName = objectName;
	
	this.dependents = [];
	$each(metaData['Dependents'], function(item){
		dependents.push(util.readObjectName(item.objectName));
	});
	
	this.properties = {};
	$.each(metaData['Properties'], function(value, key){
		if(!(value == false || value == 'false')){
			properties[key] = value;
		}
	});
	
	//Special processing for scoped/atomic artifacts
	if(this.type == 'plan' || this.type == 'par'){
		var scoped = metaData['Scoped'];
		var atomic = metaData['Atomic'];
		if(scoped == true && atomic == true){
			this.properties['Scoped-Atomic'] = 'true';
		} else {
			if(scoped == true){
				this.properties['Scoped'] = 'true';
			}
			if(atomic == true){
				this.properties['Atomic'] = 'true';
			}
		}
	}
	this.key = (this.name + this.version + this.region).replace(new RegExp('\\.', 'g'), '_');//Converts the dots to underscores so the osgi symbolic-name grammar complies to the CSS class identifier grammar 
};
	

var UploadManager = function() {
	
	this.uploading = false;
	
	this.open = false;
	
	this.toggle = function() {
		$('.upload-manager').slideToggle(util.fxTime);
		if(this.open) {
			$('.upload-toggle-button').removeClass('button-selected');
			this.open = false;
		} else {
			$('.upload-toggle-button').addClass('button-selected');
			this.open = true;
		}
	};
	
	this.addUploadBox = function() {
		$('.upload-list').append(this.getUploadFormElement($('.upload-list').getChildren('li').length));
	};
	
	this.minusUploadBox = function() {
		var uploadBoxes = $('upload-list').getChildren(['li']);
		if(uploadBoxes.length > 1){
			uploadBoxes.getLast().destroy();
		}
	};
	
	this.startUpload = function() {
		this.uploading = true;
		//this.spinner.show(true);
		$('upload-form').submit();
	};
	
	this.resetForm = function() {
		$('.upload-list').empty();
		$('.upload-list').append(this.getUploadFormElement('1'));
	};
	
	this.uploadComplete = function(){
		if(this.uploading){
			this.uploading = false;
			alert("Upload Complete");
			//this.spinner.hide();
			this.resetForm();
		}
	};
	
	this.getUploadFormElement = function(number){
		var uploadBox = $('<input />');
		uploadBox.prop('type', 'file');
		uploadBox.prop('size', '70');
		uploadBox.prop('name', number);
		var listItem = $('<li />');
		listItem.append(uploadBox);
		return listItem;
	};
	
};
