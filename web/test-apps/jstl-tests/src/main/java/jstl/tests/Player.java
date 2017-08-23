/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package jstl.tests;

public class Player {
	String name;
	String place;
	String game;

	public Player() {
		name = "  ";
		place = " ";
		game = " ";
	}

	public void setName(String a) {
		name = a;
	}

	public void setPlace(String b) {
		place = b;
	}

	public void setGame(String c) {
		game = c;
	}

	public String getName() {
		return name;
	}

	public String getPlace() {
		return place;
	}

	public String getGame() {
		return game;
	}
}
