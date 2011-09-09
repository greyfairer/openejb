/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.arquillian;

import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.spec.servlet.web.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.servlet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServletFilterEjbRemoteInjectionTest {

    public static final String TEST_NAME = ServletFilterEjbRemoteInjectionTest.class.getSimpleName();

    @Test
    public void ejbInjectionShouldSucceed() throws Exception {
        final String expectedOutput = "Remote: OpenEJB is employed at TomEE Software Inc.";
        validateTest(expectedOutput);
    }

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        WebAppDescriptor descriptor = Descriptors.create(WebAppDescriptor.class)
                .version("3.0")
                .filter(RemoteServletFilter.class, "/" + TEST_NAME);

        WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_NAME + ".war")
                .addClass(RemoteServletFilter.class)
                .addClass(CompanyRemote.class)
                .addClass(DefaultCompany.class)
                .setWebXML(new StringAsset(descriptor.exportAsString()))
                .addAsWebResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        System.err.println(descriptor.exportAsString());

        return archive;
    }

    public static enum Code {
        OK,
        ERROR;
    }

    public static class RemoteServletFilter implements Filter {

        @EJB
        private CompanyRemote remoteCompany;

        private FilterConfig config;

        public void init(FilterConfig config) {
            this.config = config;
        }

        public void destroy() {
        }

        @Override
        public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
            String name = req.getParameter("name");
            if (StringUtils.isEmpty(name)) {
                name = "OpenEJB";
            }

            if (remoteCompany != null) {
                resp.getOutputStream().println("Remote: " + remoteCompany.employ(name));
            }
        }

    }

    @Remote
    public static interface CompanyRemote {
        public String employ(String employeeName);
    }

    @Stateless
    public static class DefaultCompany implements CompanyRemote {

        private final String name = "TomEE Software Inc.";

        public String employ(String employeeName) {
            return employeeName + " is employed at " + name;
        }

    }

    private void validateTest(String expectedOutput) throws IOException {
        final InputStream is = new URL("http://localhost:9080/" + TEST_NAME + "/" + TEST_NAME).openStream();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        int bytesRead = -1;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer)) > -1) {
            os.write(buffer, 0, bytesRead);
        }

        is.close();
        os.close();

        String output = new String(os.toByteArray(), "UTF-8");
        assertNotNull("Response shouldn't be null", output);
        assertTrue("Output should contain: " + expectedOutput, output.contains(expectedOutput));
    }

}


