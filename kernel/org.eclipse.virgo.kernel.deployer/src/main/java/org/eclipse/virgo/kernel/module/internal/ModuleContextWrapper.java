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

package org.eclipse.virgo.kernel.module.internal;

import org.springframework.beans.BeansException;
import org.springframework.context.ConfigurableApplicationContext;

import org.eclipse.virgo.kernel.module.Component;
import org.eclipse.virgo.kernel.module.ModuleContext;
import org.eclipse.virgo.kernel.module.NoSuchComponentException;

/**
 * {@link ModuleContextWrapper} wraps a {@link ConfigurableApplicationContext} to provide an equivalent {@link ModuleContext}.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Thread safe.
 * 
 */
final class ModuleContextWrapper implements ModuleContext {

    private final ConfigurableApplicationContext appCtx;

    ModuleContextWrapper(ConfigurableApplicationContext appCtx) {
        this.appCtx = appCtx;
    }

    /**
     * {@inheritDoc}
     */
    public Object getApplicationContext() {
        return this.appCtx;
    }

    /**
     * {@inheritDoc}
     */
    public Component getComponent(final String componentName) throws NoSuchComponentException {
        try {
            final Object component = this.appCtx.getBean(componentName);
            return new Component() {

                /**
                 * {@inheritDoc}
                 */
                public String getName() {
                    return componentName;
                }

                /**
                 * {@inheritDoc}
                 */
                public String getType() {
                    return component.getClass().getName();
                }

                /**
                 * {@inheritDoc}
                 */
                public boolean isPrototype() {
                    return ModuleContextWrapper.this.appCtx.getBeanFactory().isPrototype(componentName);
                }

                /**
                 * {@inheritDoc}
                 */
                public boolean isSingleton() {
                    return ModuleContextWrapper.this.appCtx.getBeanFactory().isSingleton(componentName);
                }

            };
        } catch (BeansException e) {
            throw new NoSuchComponentException(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] getComponentNames() {
        return this.appCtx.getBeanDefinitionNames();
    }

    /**
     * {@inheritDoc}
     */
    public String getDisplayName() {
        return this.appCtx.getDisplayName();
    }

}
