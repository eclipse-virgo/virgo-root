/*******************************************************************************
 * Copyright (c) 2012 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

/**
 * Scripts to be loaded in to the head of the repositories view
 */

function pageinit() {
	
	$.ajax({
		url: util.getCurrentHost() + '/jolokia/search/org.eclipse.virgo.kernel:type=Repository,*',
		dataType: 'json',
		success: function (response) {
			Repositories.renderRepositoryMBeans(response.value);
			util.pageReady();
		}
	});
	
};

Repositories = {

renderRepositoryMBeans: function(mbeanNames){
	var getNameAttribute = function(mbeanName){
		return util.readObjectName(mbeanName).get('name');
	};
		
	$.each(mbeanNames, function(i, mbeanName){
		var button = util.makeDiv('button').append(util.makeDiv('button-cap-left-blue'), util.makeDiv('button-text').text(getNameAttribute(mbeanName)),
				                                   util.makeDiv('button-cap-right-blue'));
		$('#repository-controls').append(button);
		button.mbeanName = mbeanName;
		button.click(button, Repositories.display);
		if(i == 0){
			button.click();
		}

	});
	
},

display: function(clickEvent){
	var button = clickEvent.data;
	
	$('.button-selected').removeClass('button-selected');
	button.addClass('button-selected');
	
	$.ajax({
		url: util.getCurrentHost() + '/jolokia/read/' + button.mbeanName,
		dataType: 'json',
		success: function (response) {
			Repositories.renderArtifactDescriptorSummaries(response.value.AllArtifactDescriptorSummaries);
		}
	});

},

renderArtifactDescriptorSummaries: function(artifactDescriptorSummaries){
	
	var rows = [];
	
	$.each(artifactDescriptorSummaries, function(i, summary){
		rows.push([summary.type, 
		           summary.name, 
		           summary.version,
		           $('<td />', {'class' : 'repository-deploy', 'onClick': 'Repositories.deploy("repository:' + summary.type + '/' + summary.name + '")'}).text('deploy')]);
	});
	
	var descriptorTable = util.makeTable({
		clazz: 'repository-table table-selectable',
		headers: [{title: 'Type'}, {title: 'Name'}, {title: 'Version'}],
		rows: rows,
		selectable: true,
		sortable: true,
		sortIndex: 1
	});
	
	
	var panel = $('#repository-panel');
	panel.empty();
	panel.append(descriptorTable);

},

deploy: function(artifact){

	var request = [{
		"type":"EXEC",
		"mbean":"org.eclipse.virgo.kernel:category=Control,type=Deployer",
		"operation":"deploy(java.lang.String)",
		"arguments":[artifact]
	}];
	
	util.doBulkQuery(request, function(response){
		alert('Deployed artifact of type:' + response[0].value.type + ', name: ' + response[0].value.symbolicName + ', version: ' + response[0].value.version + '. If the version doesn\'t match, then that version was found in the configured repository chain.');
	}, function(xmlHttpRequest, textStatus, errorThrown){
		console.log(xmlHttpRequest, textStatus, errorThrown);
		alert('Deployment failed \'' + textStatus + '\': ' + xmlHttpRequest + ' ' + errorThrown);
	});

}

};