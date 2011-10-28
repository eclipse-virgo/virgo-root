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
 * Scripts to be loaded in to the head of the overview view
 */
function pageinit() {
	
	var renderOverviewTable = function (rows){
		
		new HtmlTable({ properties: {'class': 'bordered-table'}, 
			headers: ['Property', 'Value'], 
			rows: rows,
			zebra: true
		}).replaces($('server-overview'));
		
		util.pageReady();
	};
	
	new Server().getServerOverview(renderOverviewTable);
	
}
