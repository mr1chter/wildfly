/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2022, Red Hat, Inc., and individual contributors
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
package org.wildfly.test.integration.elytron.ejb.propagation.remote;

import static org.jboss.as.test.shared.integration.ejb.security.Util.switchIdentity;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.Callable;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:fjuma@redhat.com">Farah Juma</a>
 */
@WebServlet(urlPatterns = "/whoAmI", loadOnStartup = 1)
@ServletSecurity(@HttpConstraint(rolesAllowed = { "Users" }))
@DeclareRoles("Users")
public class SimpleServletRemote extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private EntryRemote bean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        Writer writer = resp.getWriter();
        String method = req.getParameter("method");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String role = req.getParameter("role");

        if ("whoAmI".equals(method)) {
            try {
                writer.write(bean.whoAmI());
            } catch (Exception e) {
                throw new ServletException("Unexpected Failure", e);
            }
        } else if ("switchWhoAmI".equals(method)) {
            final Callable<String[]> callable = () -> {
                String remoteWho = bean.whoAmI();
                boolean hasRole = bean.doIHaveRole(role);
                return new String[]{remoteWho, String.valueOf(hasRole)};
            };
            try {
                String[] result = switchIdentity(username, password, callable);
                writer.write(result[0] + "," + result[1]);
            } catch (Exception e) {
                throw new ServletException("Unexpected Failure", e);
            }
        } else if ("doIHaveRole".equals(method)) {
            try {
            writer.write(String.valueOf(bean.doIHaveRole(role)));
            } catch (Exception e) {
                throw new ServletException("Unexpected Failure", e);
            }
        } else {
            throw new IllegalArgumentException("Parameter 'method' either missing or invalid method='" + method + "'");
        }
    }
}
