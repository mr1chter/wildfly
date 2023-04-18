/*
 * Copyright 2023 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.test.integration.mail.providers;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.mail.Session;

@ApplicationScoped
public class MailSessionProducer {

    @Produces
    @Resource(mappedName = "java:jboss/mail/Default")
    private Session sessionFieldProducer;

    @Resource(mappedName = "java:jboss/mail/Default")
    private Session sessionMethodProducer;

    @Produces
    @MethodInjectQualifier
    public Session methodProducer() {
        return sessionMethodProducer;
    }
}
