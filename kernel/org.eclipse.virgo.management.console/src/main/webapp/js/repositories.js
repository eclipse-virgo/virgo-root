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
		url: util.getHostAndAdminPath() + '/jolokia/search/org.eclipse.virgo.kernel:type=Repository,*',
		dataType: 'json',
		contentType: 'application/json',
		cache: false,
		success: function (response) {
			Repositories.renderRepositoryMBeans(response.value);
			util.pageReady();
		}
	});
	
};

var Repositories = {
	
	renderRepositoryMBeans: function(mbeanNames){
		var getNameAttribute = function(mbeanName){
			return util.readObjectName(mbeanName).get('name');
		};
		
		$.each(mbeanNames, function(i, mbeanName){
			var button = $('<div />', {'class' : 'button'}).append(
											$('<div />', {'class' : 'button-cap-left-blue'}), 
											$('<div />', {'class' : 'button-text'}).text(getNameAttribute(mbeanName)),
											$('<div />', {'class' : 'button-cap-right-blue'})
										);
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
			url: util.getHostAndAdminPath() + '/jolokia/read/' + button.mbeanName,
			dataType: 'json',
			contentType: 'application/json',
			cache: false,
			success: function (response) {
				Repositories.renderArtifactDescriptorSummaries(response.value.AllArtifactDescriptorSummaries);
			}
		});
	
	},
	
	renderArtifactDescriptorSummaries: function(artifactDescriptorSummaries){
		
		var rows = [];
		
		$.each(artifactDescriptorSummaries, function(i, summary){
			var row = [summary.type, summary.name, summary.version];
			if(summary.type != 'library'){
				row.push($('<td />', {'class' : 'repository-deploy', 'onClick': 'Repositories.deploy("repository:' + summary.type + '/' + summary.name + '")'}).text('deploy'));
			}else{
				row.push('');
			}
			rows.push(row);
		});
		
		var descriptorTable = util.makeTable({
			clazz: 'repository-table',
			headers: [{title: 'Type', type: 'alpha'}, {title: 'Name', type: 'alpha'}, {title: 'Version', type: 'version'}],
			rows: rows,
			hoverable: true,
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
			if(response[0].value){
				alert('Deployed artifact of type:' + response[0].value.type + ', name: ' + response[0].value.symbolicName + ', version: ' + response[0].value.version + '. If the version doesn\'t match, then that version was found in the configured repository chain.');
			}else{
				alert('Deployment failed (A Dump may have been generated): ' + response[0].error);
			}
		}, function(xmlHttpRequest, textStatus, errorThrown){
			alert('Deployment failed \'' + textStatus + '\': ' + xmlHttpRequest + ' ' + errorThrown);
		});
	
	}

};