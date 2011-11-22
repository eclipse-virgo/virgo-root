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
 * Scripts to be loaded in to the head of the repository view
 */
function pageinit(){
		
	new Request.JSON({
		url: util.getCurrentHost() + '/jolokia/search/org.eclipse.virgo.kernel:type=Repository,*', 
		method: 'get',
		onSuccess: function (responseJSON, responseText){
			TabManager.renderRepositoryMBeans(responseJSON.value);
	}}).send();

}

TabManager = {
	
	renderRepositoryMBeans: function(mbeans){
		mbeans.each(function(item, index){
			var button = new Element('div.button');
			new Element('div.button-cap-left-blue').inject(button);
			new Element('div.button-text').inject(button).appendText(util.readObjectName(item).get('name'));
			new Element('div.button-cap-right-blue').inject(button);
			button.inject($('repository-controls'));
			button.store('mBean', item);
			button.addEvent('click', this.display);
			if(index == 0){
				button.fireEvent('click');
			}
		}, this);
		util.pageReady();
	},
	
	display: function(event){
		$$('.button-selected').each(function(item, index){
			item.removeClass('button-selected');
		});
		this.addClass('button-selected');
		new Request.JSON({
			url: util.getCurrentHost() + '/jolokia/read/' + this.retrieve('mBean'), 
			method: 'get',
			onSuccess: function (responseJSON, responseText){
				var rows = [];
				responseJSON.value.AllArtifactDescriptorSummaries.each(function(item, index){
					rows.push([item.type, 
					           item.name, 
					           item.version, 
					           {content: 'deploy', properties: {'class': 'repository-deploy', 'onClick': 'TabManager.deploy("repository:' + item.type + '/' + item.name + '")'}}]);
				});		
				var propertiesTable = new HtmlTable({
					properties: {'class': 'repository-table'}, 
					headers: ['Type', 'Name', 'Version'], 
					rows: rows,
					selectable: true,
					sortable: true,
					sortIndex: 1,
					zebra: true
				});
				$('repository-pannel').empty();
				propertiesTable.inject($('repository-pannel'));
		}}).send();
	},
	
	deploy: function(artifact){
		
		var request = [{
			"type":"EXEC",
			"mbean":"org.eclipse.virgo.kernel:category=Control,type=Deployer",
			"operation":"deploy(java.lang.String)",
			"arguments":[artifact]
		}];
		
		new Request.JSON({
			url: util.getCurrentHost() + '/jolokia', 
			method: 'post',
			data: JSON.encode(request),
			onSuccess: function(responseJSON){
				alert('Deployed artifact of type:' + responseJSON[0].value.type + ', name: ' + responseJSON[0].value.symbolicName + ', version: ' + responseJSON[0].value.version + '. If the version dosn\'t match then another artifact was found higher up in the configured repository chain');
			},
			onFailure: function (xhr){
				console.log('Deploying ' + xhr);
				alert('Deployment failed: ' + xhr);
			}
		}).send();
		
	}

};