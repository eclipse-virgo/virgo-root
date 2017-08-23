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

package org.eclipse.virgo.nano.serviceability;

import java.util.Collection;
import java.util.Map;

/**
 * A set of useful assertions based on those provided by the Spring Framework's Assert class.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * This class is thread safe.
 * 
 * @see org.springframework.util.Assert
 */
public final class Assert {

    /**
     * A <code>FatalAssertionException</code> is thrown when an assertion failure occurs and will result in a dump being
     * generated.
     * 
     * <strong>Concurrent Semantics</strong><br />
     * 
     * This class is <strong>thread-safe</strong>.
     * 
     */
    final public static class FatalAssertionException extends RuntimeException {

        private static final long serialVersionUID = -4633344457818398425L;

        /**
         * Creates a new FatalAssertionException with the supplied message
         * 
         * @param message The exception message
         */
        public FatalAssertionException(String message) {
            super(message);
        }
    }

    /**
     * Assert a boolean expression, throwing a <code>FatalAssertionException</code> if the test result is
     * <code>false</code>.
     * 
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, &quot;The value must be greater than zero&quot;);
     * </pre>
     * 
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if expression is <code>false</code>
     */
    public static void isTrue(boolean expression, String message, Object... inserts) {
        if (!expression) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert a boolean expression, throwing a <code>FatalAssertionException</code> if the test result is
     * <code>true</code>.
     * 
     * <pre class="code">
     * Assert.isFalse(state.isBroken(), &quot;The state is broken&quot;);
     * </pre>
     * 
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if expression is <code>false</code>
     */
    public static void isFalse(boolean expression, String message, Object... inserts) {
        if (expression) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert that an object is <code>null</code>.
     * 
     * <pre class="code">
     * Assert.isNull(value, &quot;The value must be null&quot;);
     * </pre>
     * 
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if the object is not <code>null</code>
     */
    public static void isNull(Object object, String message, Object... inserts) {
        if (object != null) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert that an object is not <code>null</code>.
     * 
     * <pre class="code">
     * Assert.notNull(clazz, &quot;The class must not be null&quot;);
     * </pre>
     * 
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if the object is <code>null</code>
     */
    public static void notNull(Object object, String message, Object... inserts) {
        if (object == null) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert that the given String is not empty; that is, it must not be <code>null</code> and not the empty String.
     * 
     * <pre class="code">
     * Assert.hasLength(name, &quot;Name must not be empty&quot;);
     * </pre>
     * 
     * @param text the String to check
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @see org.eclipse.virgo.util.common.StringUtils#hasLength(String)
     */
    public static void hasLength(String text, String message, Object... inserts) {
        if (text == null || text.length() == 0) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert that an array has elements; that is, it must not be <code>null</code> and must have at least one element.
     * 
     * <pre class="code">
     * Assert.notEmpty(array, &quot;The array must have elements&quot;);
     * </pre>
     * 
     * @param array the array to check
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @throws IllegalArgumentException if the object array is <code>null</code> or has no elements
     */
    public static void notEmpty(Object[] array, String message, Object... inserts) {
        if (array == null || array.length == 0) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert that a collection has elements; that is, it must not be <code>null</code> and must have at least one
     * element.
     * 
     * <pre class="code">
     * Assert.notEmpty(collection, &quot;Collection must have elements&quot;);
     * </pre>
     * @param <T> Element type of collection
     * 
     * @param collection the collection to check
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if the collection is <code>null</code> or has no elements
     */
    public static <T> void notEmpty(Collection<T> collection, String message, Object... inserts) {
        if (collection == null || collection.isEmpty()) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert that a Map has entries; that is, it must not be <code>null</code> and must have at least one entry.
     * 
     * <pre class="code">
     * Assert.notEmpty(map, &quot;Map must have entries&quot;);
     * </pre>
     * @param <K> Key type of map
     * @param <V> Value type of map
     * 
     * @param map the map to check
     * @param message the exception message to use if the assertion fails
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if the map is <code>null</code> or has no entries
     */
    public static <K, V> void notEmpty(Map<K, V> map, String message, Object... inserts) {
        if (map == null || map.isEmpty()) {
            throw new FatalAssertionException(String.format(message, inserts));
        }
    }

    /**
     * Assert that the provided object is a non-null instance of the provided class.
     * 
     * <pre class="code">
     * Assert.instanceOf(Foo.class, foo);
     * </pre>
     * @param <T> Type generic
     * 
     * @param type the type to check against
     * @param obj the object to check
     * @param message a message which will be prepended to the message produced by the function itself, and which may be
     *        used to provide context. It should normally end in a ": " or ". " so that the function generate message
     *        looks ok when prepended to it.
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if the object is not an instance of clazz
     * @see Class#isInstance
     */
    public static <T> void isInstanceOf(Class<T> type, Object obj, String message, Object... inserts) {
        notNull(type, "The type to check against must not be null");
        if (!type.isInstance(obj)) {
            throw new FatalAssertionException(String.format(message, inserts) + "Object of class [" + (obj != null ? obj.getClass().getName() : "null")
                + "] must be an instance of " + type);
        }
    }

    /**
     * Assert that <code>superType.isAssignableFrom(subType)</code> is <code>true</code>.
     * 
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * </pre>
     * @param <T> SuperType
     * @param <U> SubType
     * 
     * @param superType the super type to check against
     * @param subType the sub type to check
     * @param message a message which will be prepended to the message produced by the function itself, and which may be
     *        used to provide context. It should normally end in a ": " or ". " so that the function generate message
     *        looks ok when prepended to it.
     * @param inserts any inserts to include if the message is a format string.
     * @throws FatalAssertionException if the classes are not assignable
     */
    public static <T, U> void isAssignable(Class<T> superType, Class<U> subType, String message, Object... inserts) {
        notNull(superType, "Type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            throw new FatalAssertionException(String.format(message, inserts) + subType + " is not assignable to " + superType);
        }
    }

    /*
     * Prevent instantiation - Java does not allow final abstract classes.
     */
    private Assert() {
    }
}
