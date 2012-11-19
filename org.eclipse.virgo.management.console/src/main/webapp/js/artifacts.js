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
	$('#upload-target-id').load(uploadManager.deployComplete);
	$('#add-upload-box').click(uploadManager.addUploadBox);
	$('#minus-upload-box').click(uploadManager.minusUploadBox);
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
var Tree = function() {
	
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
			if(-1 == $.inArray(artifactFilterValue, filterMatches)){
				filterMatches.push(artifactFilterValue);
			};
			
		});
		$.each(filterMatches, function(index, filterMatch){
			var node;
			if(filter == 'type'){
				node = self.getNodeContainer(filterMatch, filterMatch, filterMatch, filter);
			} else {
				node = self.getNodeContainer(filterMatch, 'default', filterMatch, filter);
			}
			$('.artifact-label', node).click({'node': node, 'filter': filter}, tree.renderTopLevel);
			$('#artifacts-tree').append(node);
		});
	};

	/**
	 * Called when the user chooses a different value to sort the top level of the tree on
	 * 
	 * @param filter - the artifact property to filter on
	 */
	this.reRenderWithFilter = function (filter){
		$('#artifacts-tree').empty();
		util.doQuery('search/org.eclipse.virgo.kernel:type=ArtifactModel,*', function (response){
			self.setup(response.value, filter);
		});
		if(filter == 'type') {
			$('#type-filter-button').addClass('button-selected');
			$('#region-filter-button').removeClass('button-selected');
		} else {
			$('#region-filter-button').addClass('button-selected');
			$('#type-filter-button').removeClass('button-selected');
		}
		
	};
	
	/**
	 * Called when one of the top level node is clicked
	 * 
	 * @param filter - the artifact property to sort by
	 * @param parent - the unique key of the parent node
	 */
	this.renderTopLevel = function(eventData){
		var node = eventData.data.node;
		self.setIconElement(node.children('.artifact-label').children('.twisty'), 'loader-small.gif');
		if(node.children().length == 1){//It's closed
			util.doQuery('search/org.eclipse.virgo.kernel:type=ArtifactModel,*', function (response){
				self.renderTopLevelRequest(response, node, eventData.data.filter);
				self.setIconElement(node.children('.artifact-label').children('.twisty'), 'tree-icons/minus.png');
			});
		} else {//It's open
			node.children('.fx-container').slideToggle(util.fxTime, function(){
				$(this).remove();
			});
			self.setIconElement(node.children('.artifact-label').children('.twisty'), 'tree-icons/plus.png');
		}
	};

	/**
	 * Called when any artifact in the tree is expanded
	 * 
	 * @param objectName - the unique key of the artifact to render
	 * @param parent - id of the element to render the artifact under
	 */
	this.renderArtifact = function (eventData){
		var node = eventData.data.node;
		self.setIconElement(node.children('.artifact-label').children('.twisty'), 'loader-small.gif');
		if(node.children().length == 1){//It's closed
			util.doQuery('read/' + eventData.data.objectName.toString, function(response){
				self.renderArtifactRequest(response, eventData.data.objectName, node);
				self.setIconElement(node.children('.artifact-label').children('.twisty'), 'tree-icons/minus.png');
			});
		} else {//It's open
			node.children('.fx-container').slideToggle(util.fxTime, function(){
				$(this).remove();
			});
			self.setIconElement(node.children('.artifact-label').children('.twisty'), 'tree-icons/plus.png');
		}
	};
	
	/**
	 * Accepted operations are 'start', 'stop', 'uninstall' and 'refresh'
	 * 
	 */
	this.doArtifactOperation = function(event){
		util.doQuery('exec/' + event.data.objectName.toString + '/' + event.data.action, function(response){
			util.doQuery('read/' + event.data.objectName.toString, function(response){
				self.renderOperationResult(response, event.data.objectName);
			});
		});
	};
	
	this.renderOperationResult = function(responseJSON, objectName){
		var artifact = new Artifact(objectName);
		if(responseJSON.status == 404){
			$('.' + artifact.key).remove();
		} else if(responseJSON.value){
			$.each($('.' + artifact.key), function(index, nodeToUpdate){
				var fxContainer = $(nodeToUpdate).children('.fx-container');
				if(fxContainer){
					fxContainer.remove();
					self.renderArtifactRequest(responseJSON, objectName, $(nodeToUpdate));
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
		var fxContainer = $('<div />', {'class': 'fx-container'});
		var mbeans = json.value.sort();
		$.each(mbeans, function(index, mbean){
			var artifact = new Artifact(util.readObjectName(mbean));
			if(artifact[filter] == parent.attr('id')){
				fxContainer.append(self.getArtifactLabel(artifact, parent.attr('id')));
			}
		});
		parent.append(fxContainer);
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
		var fxContainer = $('<div />', {'class': 'fx-container'});
		var fullArtifact = new FullArtifact(json.value, objectName);
		
		var artifactControlBar = self.getArtifactControlBar(fullArtifact);
		if(fullArtifact.type == 'configuration'){
			var configControl = $('<a />', {'class': 'artifact-control'});
			configControl.attr('href', util.getCurrentHost() + '/content/configurations#' + fullArtifact.name);
			configControl.text('VIEW');
			artifactControlBar.append(configControl);
		}

		fxContainer.append(artifactControlBar);
		fxContainer.append(self.getArtifactAttribute('SymbolicName: ' + fullArtifact.name));
		fxContainer.append(self.getArtifactAttribute('Version: ' + fullArtifact.version));
		fxContainer.append(self.getArtifactAttribute('Region: ' + fullArtifact.region));
		fxContainer.append(self.getArtifactAttribute('Type: ' + fullArtifact.type));
		fxContainer.append(self.getArtifactAttribute(fullArtifact.state, 'state-' + fullArtifact.state));
		
		var spring = false;
		$.each(fullArtifact.properties, function(key, value){
			if(value == 'true' || value == true){
				if(key == 'Spring'){
					spring = true;
					fxContainer.append(self.getArtifactAttribute('Spring Powered', key));
				} else if(key == 'Scoped' || key == 'Atomic' || key == 'Scoped-Atomic'){
					fxContainer.append(self.getArtifactAttribute(key, key));
				} else {
					fxContainer.append(self.getArtifactAttribute(key + ': ' + value));
				}
			} else {
				if(key == 'Bundle Id'){
					fxContainer.append(self.getArtifactAttribute(key + ': ' + value, undefined, util.getCurrentHost() + '/content/wirings#' + value));
				} else {
					fxContainer.append(self.getArtifactAttribute(key + ': ' + value));
				}
			}
		});
		
		if(spring == false && fullArtifact.type == 'bundle'){
			fxContainer.append(self.getArtifactAttribute('No Spring', 'Spring'));
		}
		
		var dependents = fullArtifact.dependents.sort(function(a, b){
			return a.compare(b);
		});
		
		$.each(dependents, function(index, objectName){
			var dependentArtifact = new Artifact(objectName);
			fxContainer.append(self.getArtifactLabel(dependentArtifact, parent.attr('id')));
		});

		parent.append(fxContainer);
		fxContainer.slideToggle(util.fxTime);
	};
	
	/**
	 * Create an artifact label element with text, icons etc for the supplied artifact
	 * 
	 * @param artifact - to construct
	 * @param parent - element to insert the $ in to
	 */
	this.getArtifactLabel = function(artifact, parent){
		var node = self.getNodeContainer(artifact.name + ': ' + artifact.version, artifact.type, parent + artifact.key, artifact.key);
		$('.artifact-label', node).click({'objectName': artifact.objectName, 'node': node}, tree.renderArtifact);
		return node;
	};
	
	/**
	 * Create a label element with text and icons from the supplied details
	 * 
	 * @param text - text for the artifact label
	 * @param icon - the name of the icon to use
	 * @param id - unique id of the label
	 */
	this.getNodeContainer = function(text, icon, id, key){
		var artifactContainer = $('<div />', {'class': 'artifact-container'});
		artifactContainer.addClass(key);
		artifactContainer.prop('id', id);

		var artifactLabel = $('<div />', {'class': 'artifact-label'});
		artifactContainer.append(artifactLabel);

		var plusMinus = self.getIconElement('tree-icons/plus.png');
		plusMinus.addClass('plus twisty');
		artifactLabel.append(plusMinus);
		artifactLabel.append(self.getIconElement('tree-icons/node-' + icon + '.png'));
		var span  = $('<span />', {'class': 'label-text'});
		span.text(text);
		artifactLabel.append(span);
		
		return artifactContainer;
	};
	
	this.getArtifactControlBar = function(artifact) {
		var controlBar = $('<div />', {'class': 'artifact-attribute'});
		controlBar.append(self.getIconElement('tree-icons/attribute-default.png'));
		var span  = $('<span />', {'class': 'label-text'});
		span.text('Actions:');
		
		controlBar.append(span);
		controlBar.append(self.getArtifactControl('start', artifact.objectName));
		controlBar.append(self.getArtifactControl('refresh', artifact.objectName));
		controlBar.append(self.getArtifactControl('stop', artifact.objectName));
		controlBar.append(self.getArtifactControl('uninstall', artifact.objectName));
		return controlBar;
	};
	
	this.getArtifactControl = function(action, objectName) {
		var control = $('<div />', {'class': 'artifact-control'});
		control.text(action.toUpperCase());
		control.click({'action': action, 'objectName': objectName}, tree.doArtifactOperation);
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
		property.append(self.getIconElement('tree-icons/attribute-default.png'));
		if(icon){
			property.append(self.getIconElement('tree-icons/attribute-' + icon + '.png'));
		}
		var label;
		if(link){
			label = $('<a />', {'class': 'label-text'});
			label.attr('href', link);
		} else {
			label  = $('<span />', {'class': 'label-text'});
		}
		label.text(text);
		property.append(label);
		return property;
	};
	
	/**
	 * Create an element with a background image applied
	 * 
	 * @param iconName - for the image
	 */
	this.getIconElement = function(iconName){
		return self.setIconElement($('<div />', {'class': 'tree-icon'}), iconName);
	};
	
	/**
	 * Set an element with a background image applied
	 * 
	 * @param element - element to set the image on
	 * @param iconName - for the image
	 */
	this.setIconElement = function(element, iconName){
		element.css('background', 'url("' + util.getCurrentHost() + '/resources/images/' + iconName.toLowerCase()  + '") no-repeat center center');
		return element;
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
	
	var self = this;
	
	self.name = metaData['Name'];
	self.version = metaData['Version'];
	self.region = metaData['Region'];
	self.type = metaData['Type'];
	self.state = metaData['State'];
	self.objectName = objectName;
	
	self.dependents = [];
	$.each(metaData['Dependents'], function(index, item){
		self.dependents.push(util.readObjectName(item.objectName));
	});
	
	self.properties = {};
	$.each(metaData['Properties'], function(key, value){
		if(!(value == false || value == 'false')){
			self.properties[key] = value;
		}
	});
	
	//Special processing for scoped/atomic artifacts
	if(self.type == 'plan' || self.type == 'par'){
		var scoped = metaData['Scoped'];
		var atomic = metaData['Atomic'];
		if(scoped == true && atomic == true){
			self.properties['Scoped-Atomic'] = 'true';
		} else {
			if(scoped == true){
				self.properties['Scoped'] = 'true';
			}
			if(atomic == true){
				self.properties['Atomic'] = 'true';
			}
		}
	}
	self.key = (self.name + self.version + self.region).replace(new RegExp('\\.', 'g'), '_');//Converts the dots to underscores so the osgi symbolic-name grammar complies to the CSS class identifier grammar 
};
	

var UploadManager = function() {

	var self = this;
	
	this.uploading = false;
	
	this.open = false;
	
	this.toggle = function() {
		$('#upload-manager').slideToggle(util.fxTime);
		if(self.open) {
			$('#upload-toggle-button').removeClass('button-selected');
			self.open = false;
		} else {
			$('#upload-toggle-button').addClass('button-selected');
			self.open = true;
		}
	};
	
	this.addUploadBox = function() {
		$('#upload-list').append(self.getUploadFormElement($('#upload-list').children('li').length));
	};
	
	this.minusUploadBox = function() {
		var uploadBoxes = $('#upload-list').children('li');
		if(uploadBoxes.length > 1){
			uploadBoxes.last().remove();
		}
	};
	
	this.startUpload = function() {
		self.uploading = true;
		//this.spinner.show(true);
		$('#upload-form').submit();
	};
	
	this.resetForm = function() {
		$('#upload-list').empty();
		$('#upload-target-id').empty();
		$('#upload-list').append(self.getUploadFormElement('1'));
	};
	
	this.deployComplete = function(){
		if(self.uploading){
			var iframe = $('#upload-target-id');
			var results = $('#uploadResults', iframe[0].contentDocument).children();
			if(results.length == 0){
				alert('Nothing Deployed');
			}else{
				var resultString = '';
			    $.each(results, function(i, result){
			    	resultString = resultString + '\n' + $(result).text();
			    });
			    alert('Deployment result\n\n' + resultString);
			}
		    self.resetForm();
			self.uploading = false;
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
