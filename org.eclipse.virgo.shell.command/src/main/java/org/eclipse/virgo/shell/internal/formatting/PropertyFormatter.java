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

package org.eclipse.virgo.shell.internal.formatting;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for formatting properties.
 * 
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread-safe
 * 
 */
public class PropertyFormatter {

    /**
     * Formats the supplied value into one or more Strings. Each String in the <code>List</code> represents a line of
     * output.
     * 
     * <p />
     * If <code>value</code> is a single object, i.e. it is not an array, the returned list will contain a single line -
     * the <code>toString</code> of <code>value</code>. If <code>value</code> is an array, lines in the list are formed
     * by concatenating the <code>toString</code> of each entry in the array, wrapping when <code>maxLineLength</code>
     * is reached.
     * 
     * <p />
     * If an individual <code>toString</code> is longer that the given <code>maxLineLength</code> the full string will
     * be included as a line in the returned <code>List</code>, i.e. individual Strings are not truncated and the
     * <code>maxLineLength</code> will be exceeded.
     * 
     * @param value the value to format
     * @param maxLineLength the desired maximum line length
     * @return the formatted value, one list entry per line
     */
    public static List<String> formatPropertyValue(Object value, int maxLineLength) {

        String[] strings;
        if (value.getClass().isArray()) {
            strings = arrayToStrings(value);
        } else {
            strings = new String[] { value.toString() };
        }

        int lineLength = 0;

        List<String> formatted = new ArrayList<String>();

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < strings.length; i++) {
            String item;
            if (i < strings.length - 1) {
                item = strings[i] + ", ";
            } else {
                item = strings[i];
            }

            if (!isSufficientRoomForNextItem(item, lineLength, maxLineLength) && addEntryIfNecessary(builder, formatted)) {
                builder = new StringBuilder();
                lineLength = 0;
            }

            builder.append(item);
            lineLength += item.length();
        }

        addEntryIfNecessary(builder, formatted);

        return formatted;
    }

    private static boolean addEntryIfNecessary(StringBuilder builder, List<String> entries) {
        String string = builder.toString();
        if (!string.isEmpty()) {
            entries.add(string);
            return true;
        }
        return false;
    }

    private static boolean isSufficientRoomForNextItem(String item, int currentLineLength, int maxLineLength) {
        return (currentLineLength + item.length()) <= maxLineLength;
    }

    private static String[] arrayToStrings(Object array) {

        int length = Array.getLength(array);
        String[] strings = new String[length];

        for (int i = 0; i < length; i++) {
            Object object = Array.get(array, i);
            strings[i] = object == null ? "null" : object.toString();
        }

        return strings;
    }
}
