/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.server.operations;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.server.RuntimeOperationContext;
import org.jboss.as.server.RuntimeOperationHandler;
import org.jboss.as.server.RuntimeTask;
import org.jboss.as.server.RuntimeTaskContext;
import org.jboss.dmr.ModelNode;

/**
 * Handler for the server remove-system-property operation.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class SystemPropertyRemoveHandler extends org.jboss.as.controller.operations.common.SystemPropertyRemoveHandler implements RuntimeOperationHandler {

    public static final SystemPropertyRemoveHandler INSTANCE = new SystemPropertyRemoveHandler();

    private SystemPropertyRemoveHandler() {
    }

    @Override
    protected OperationResult removeSystemProperty(final String name, OperationContext context, ResultHandler resultHandler, ModelNode compensating) {
        if (context instanceof RuntimeOperationContext) {
            RuntimeOperationContext.class.cast(context).executeRuntimeTask(new RuntimeTask() {
                public void execute(RuntimeTaskContext context, final ResultHandler resultHandler) throws OperationFailedException {
                    System.clearProperty(name);
                    resultHandler.handleResultComplete();
                }
            }, resultHandler);
        } else {
            resultHandler.handleResultComplete();
        }
        return new BasicOperationResult(compensating);
    }
}
