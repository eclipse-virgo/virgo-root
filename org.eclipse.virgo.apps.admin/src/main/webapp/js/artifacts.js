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
	new Request.JSON({
		url: Util.getCurrentHost() + '/jolokia/search/org.eclipse.virgo.kernel:type=ArtifactModel,*', 
		method: 'get',
		onSuccess: function (responseJSON, responseText){
			tree = new Tree();
			tree.setup(responseJSON.value, 'type');
			Util.pageReady();
		}
	}).send();
}

/**
 * Constructor method for the Artifacts Tree
 * 
 * @param mbeans
 * @returns
 */
var Tree = function(mbeans) {
	
	/**
	 * Do the initial population of the tree
	 * 
	 * @param mbeans - the json data from the server
	 * @param filter - what the top level should be sorted by
	 */
	this.setup = function (mbeans, filter){
		var filterMatches = new Array();
		mbeans.each(function(mbean){
			var artifact = new Artifact(Util.readObjectName(mbean));
			var artifactFilterValue = artifact[filter];
			if(!filterMatches.contains(artifactFilterValue)){
				filterMatches.push(artifactFilterValue);
			};
			
		}, this);
		filterMatches.each(function(filterMatch){
			var filterContainer;
			if(filter == 'type'){
				filterContainer = this.getNodeContainer(filterMatch.capitalize(), filterMatch, filterMatch, filter);
			} else {
				filterContainer = this.getNodeContainer(filterMatch, 'default', filterMatch, filter);
			}
			filterContainer.firstChild.set('onclick', 'tree.renderTopLevel("' + filter + '", "' + filterMatch + '")');
			filterContainer.inject($('artifacts-tree'));
		}, this);
	};

	/**
	 * Called when the user chooses a different value to sort the top level of the tree on
	 * 
	 * @param filter - the artifact property to filter on
	 */
	this.reRenderWithFilter = function (filter){
		$('artifacts-tree').empty();
		new Request.JSON({
			url: Util.getCurrentHost() + '/jolokia/search/org.eclipse.virgo.kernel:type=ArtifactModel,*', 
			method: 'get',
			onSuccess: function (responseJSON, responseText){
				this.setup(responseJSON.value, filter);
			}.bind(this)
		}).send();
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
		this.getIconElement('loader-small.gif').replaces(children[0].firstChild);// Set the plus/minus icon
		if(children.length == 1){//It's closed	
			new Request.JSON({
				url: Util.getCurrentHost() + '/jolokia/search/org.eclipse.virgo.kernel:type=ArtifactModel,*', 
				method: 'get',
				onSuccess: function (responseJSON, responseText){
					this.renderTopLevelRequest(responseJSON.value, parent, filter);
					this.getIconElement('tree-icons/minus.png').replaces(children[0].firstChild);// Set the plus/minus icon
				}.bind(this)
			}).send();
		} else {//It's open
			parentElement.children[1].nix(true);
			this.getIconElement('tree-icons/plus.png').replaces(children[0].firstChild);// Set the plus/minus icon
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
		this.getIconElement('loader-small.gif').replaces(children[0].firstChild);// Set the plus/minus icon
		if(children.length == 1){//It's closed	
			new Request.JSON({ 
				url: Util.getCurrentHost() + '/jolokia/read/' + objectName,
				method: 'get',
				onSuccess: function(responseJSON, responseText){
					this.renderArtifactRequest(responseJSON.value, Util.readObjectName(objectName), parent);
					this.getIconElement('tree-icons/minus.png').replaces(children[0].firstChild);// Set the plus/minus icon
				}.bind(this)
			}).send();
		} else {//It's open
			parentElement.children[1].nix(true);
			this.getIconElement('tree-icons/plus.png').replaces(children[0].firstChild);// Set the plus/minus icon
		}
	};
	
	/**
	 * Accepted operations are 'start', 'stop', 'uninstall' and 'refresh'
	 * 
	 */
	this.doArtifactOperation = function(objectName, operation){
		new Request({ 
			url: Util.getCurrentHost() + '/jolokia/exec/' + objectName + '/' + operation,
			method: 'get',
			onSuccess: function(response){
				new Request.JSON({ 
					url: Util.getCurrentHost() + '/jolokia/read/' + objectName,
					method: 'get',
					onSuccess: function(responseJSON, responseText){
						this.renderOperationResult(responseJSON, Util.readObjectName(objectName));
					}.bind(this)
				}).send();
			}.bind(this)
		}).send();
	};
	
	this.renderOperationResult = function(responseJSON, objectName){
		var artifact = new Artifact(objectName);
		if(responseJSON.status == 404){
			$$('.' + artifact.key).nix(true);
		} else if(responseJSON.value){
			$$('.' + artifact.key).each(function(nodeToUpdate){
				if(nodeToUpdate.getChildren().length > 1){
					nodeToUpdate.getChildren()[1].nix(true);
					this.renderArtifactRequest(responseJSON.value, objectName, nodeToUpdate.id);
				}
			}, this);
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
		var fxContainer = new Element('div.fx-container');
		json.each(function(mbean){
			var artifact = new Artifact(Util.readObjectName(mbean));
			if(artifact[filter] == parent){
				this.getArtifactLabel(artifact, parent).inject(fxContainer);
			}
		}, this);
		fxContainer.inject(parentElement);
		fxContainer.set('reveal', {duration: Util.fxTime});
		fxContainer.reveal();
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
		var fxContainer = new Element('div.fx-container');
		var fullArtifact = new FullArtifact(json, objectName);
		
		var artifactControlBar = this.getArtifactControlBar(fullArtifact);
		if(fullArtifact.type == 'configuration'){
			var configControl = new Element('a.artifact-control');
			configControl.set('href', Util.getCurrentHost() + '/content/configuration#' + fullArtifact.name);
			configControl.appendText('View');
			configControl.inject(artifactControlBar);
		}

		artifactControlBar.inject(fxContainer);
		this.getArtifactAttribute('Name: ' + fullArtifact.name).inject(fxContainer);
		this.getArtifactAttribute('Version: ' + fullArtifact.version).inject(fxContainer);
		this.getArtifactAttribute('Region: ' + fullArtifact.region).inject(fxContainer);
		this.getArtifactAttribute('Type: ' + fullArtifact.type.capitalize()).inject(fxContainer);
		this.getArtifactAttribute(fullArtifact.state.toLowerCase().capitalize(), 'state-' + fullArtifact.state).inject(fxContainer);
		
		Object.each(fullArtifact.properties, function(value, key){
			if(value == 'true' || value == true){
				if(key == 'Spring' || key == 'Scoped' || key == 'Atomic' || key == 'Scoped-Atomic'){
					this.getArtifactAttribute(key, key).inject(fxContainer);
				} else {
					this.getArtifactAttribute(key).inject(fxContainer);
				}
			} else {
				if(key == 'Bundle Id'){
					this.getArtifactAttribute(key + ': ' + value, null, Util.getCurrentHost() + '/content/explorer#' + value).inject(fxContainer);
				} else {
					this.getArtifactAttribute(key + ': ' + value).inject(fxContainer);
				}
			}
		}, this);
		
		fullArtifact.dependents.each(function(objectName){
			var dependentArtifact = new Artifact(objectName);
			this.getArtifactLabel(dependentArtifact, parent).inject(fxContainer);
		}, this);

		fxContainer.inject(parentElement);
		fxContainer.set('reveal', {duration: Util.fxTime});
		fxContainer.reveal();	
	};
	
	/**
	 * Create an artifact label element with text, icons etc for the supplied artifact
	 * 
	 * @param artifact - to construct
	 * @param parent - element to insert the new element in to
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
		var artifactContainer = new Element('div.artifact-container');
		artifactContainer.addClass(key);
		artifactContainer.setProperty('id', id);
		
		var artifactLabel = new Element('div.artifact-label');
		artifactLabel.inject(artifactContainer);

		var plusMinus = this.getIconElement('tree-icons/plus.png');
		plusMinus.addClass('plus');
		plusMinus.inject(artifactLabel);
		this.getIconElement('tree-icons/node-' + icon + '.png').inject(artifactLabel);
		var span  = new Element('span.label-text');
		span.appendText(text);
		span.inject(artifactLabel);
		
		return artifactContainer;
	};
	
	this.getArtifactControlBar = function(artifact) {
		var controlBar = new Element('div.artifact-attribute');
		this.getIconElement('tree-icons/attribute-default.png').inject(controlBar);
		var span  = new Element('span.label-text');
		span.appendText('Actions:');
		span.inject(controlBar);
		this.getArtifactControl('start', artifact.objectName).inject(controlBar);
		this.getArtifactControl('refresh', artifact.objectName).inject(controlBar);
		this.getArtifactControl('stop', artifact.objectName).inject(controlBar);
		this.getArtifactControl('uninstall', artifact.objectName).inject(controlBar);
		return controlBar;
	};
	
	this.getArtifactControl = function(action, objectName) {
		var control = new Element('div.artifact-control');
		control.appendText(action.capitalize());
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
		var property = new Element('div.artifact-attribute');
		this.getIconElement('tree-icons/attribute-default.png').inject(property);
		if(icon){
			this.getIconElement('tree-icons/attribute-' + icon + '.png').inject(property);
		}
		var label;
		if(link){
			label = new Element('a');
			label.set('href', link);
		} else {
			label  = new Element('span');
		}
		label.appendText(text);
		label.addClass('label-text');
		label.inject(property);
		return property;
	};
	
	/**
	 * Create an element with a background image applied
	 * 
	 * @param iconName - for the image
	 */
	this.getIconElement = function(iconName){
		var imageElement = new Element('div.tree-icon');
		imageElement.set('styles', {'background': 'url("' + Util.getCurrentHost() + '/resources/images/' + iconName.toLowerCase()  + '") no-repeat center center'});
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
	metaData['Dependents'].each(function(item){
		this.dependents.push(Util.readObjectName(item.objectName));
	}, this);
	
	this.properties = {};
	Object.each(metaData['Properties'], function(value, key){
		if(!(value == false || value == 'false')){
			this.properties[key] = value;
		}
	}, this);
	
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

	this.display = new Fx.Reveal($('upload-manager'), {duration: Util.fxTime}).dissolve();
	
	this.spinner = new Spinner('upload-manager');
	
	this.uploading = false;
	
	this.open = false;
	
	this.toggle = function() {
		this.display.toggle();
		if(this.open) {
			$('upload-toggle-button').removeClass('button-selected');
			this.open = false;
		} else {
			$('upload-toggle-button').addClass('button-selected');
			this.open = true;
		}
	};
	
	this.addUploadBox = function() {
		this.getUploadFormElement($('upload-list').getChildren(['li']).length).inject($('upload-list'));
	};
	
	this.minusUploadBox = function() {
		var uploadBoxes = $('upload-list').getChildren(['li']);
		if(uploadBoxes.length > 1){
			uploadBoxes.getLast().destroy();
		}
	};
	
	this.startUpload = function() {
		this.uploading = true;
		this.spinner.position();
		this.spinner.show(true);
		$('upload-form').submit();
	};
	
	this.resetForm = function() {
		$('upload-list').empty();
		this.getUploadFormElement('1').inject($('upload-list'));
	};
	
	this.uploadComplete = function(){
		if(this.uploading){
			this.uploading = false;
			alert("Upload Complete");
			this.spinner.hide();
			this.resetForm();
		}
	};

	/* **************** START PRIVATE METHODS **************** */
	
	this.getUploadFormElement = function(number){
		var uploadBox = new Element('input');
		uploadBox.setProperty('type', 'file');
		uploadBox.setProperty('size', '70');
		uploadBox.setProperty('name', number);
		var listItem = new Element('li');
		uploadBox.inject(listItem);
		return listItem;
	};
	
};
