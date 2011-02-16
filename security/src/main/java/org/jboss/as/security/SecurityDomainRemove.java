/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.security;

import org.jboss.as.controller.BasicOperationResult;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationResult;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;

import org.jboss.as.controller.ModelAddOperationHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.security.service.JaasConfigurationService;
import org.jboss.as.server.RuntimeOperationContext;
import org.jboss.as.server.RuntimeOperationHandler;
import org.jboss.as.server.RuntimeTask;
import org.jboss.as.server.RuntimeTaskContext;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.security.config.ApplicationPolicyRegistration;

/**
 * Remove a security domain configuration.
 *
 * @author <a href="mailto:mmoyses@redhat.com">Marcus Moyses</a>
 * @author Brian Stansberry
 */
class SecurityDomainRemove implements ModelAddOperationHandler, RuntimeOperationHandler {

    static final String OPERATION_NAME = REMOVE;

    static final SecurityDomainRemove INSTANCE = new SecurityDomainRemove();

    /** Private to ensure a singleton. */
    private SecurityDomainRemove() {
    }

    @Override
    public OperationResult execute(OperationContext context, ModelNode operation, ResultHandler resultHandler) {

        ModelNode opAddr = operation.require(OP_ADDR);
        PathAddress address = PathAddress.pathAddress(opAddr);
        final String securityDomain = address.getLastElement().getValue();

        // Create the compensating operation
        final ModelNode compensatingOperation = SecurityDomainAdd.getRecreateOperation(opAddr, context.getSubModel());

        if (context instanceof RuntimeOperationContext) {
            final RuntimeOperationContext updateContext = (RuntimeOperationContext) context;
            RuntimeOperationContext.class.cast(context).executeRuntimeTask(new RuntimeTask() {
                public void execute(RuntimeTaskContext context, ResultHandler resultHandler) throws OperationFailedException {
                    // remove security domain
                    final ServiceController<?> jaasConfigurationService = context.getServiceRegistry().getService(
                            JaasConfigurationService.SERVICE_NAME);
                    if (jaasConfigurationService != null) {
                        ApplicationPolicyRegistration config = (ApplicationPolicyRegistration) jaasConfigurationService.getValue();
                        config.removeApplicationPolicy(securityDomain);
                    }
                    resultHandler.handleResultComplete();
                }
            }, resultHandler);
        } else {
            resultHandler.handleResultComplete();
        }
        return new BasicOperationResult(compensatingOperation);
    }
}
