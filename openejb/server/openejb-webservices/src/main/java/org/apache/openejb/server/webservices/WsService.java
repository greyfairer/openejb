/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.server.webservices;

import org.apache.openejb.BeanContext;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpListenerRegistry;
import org.apache.openejb.assembler.classic.DeploymentListener;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.PortInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.StatelessBeanInfo;
import org.apache.openejb.assembler.classic.WsBuilder;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.ServletInfo;
import org.apache.openejb.assembler.classic.SingletonBeanInfo;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.core.webservices.PortAddressRegistryImpl;
import org.apache.openejb.core.webservices.PortAddressRegistry;
import org.apache.openejb.core.webservices.PortData;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.server.httpd.util.HttpUtil;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.Injection;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.StringTemplate;

import javax.naming.Context;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.net.Socket;
import java.net.URL;
import java.net.MalformedURLException;

public abstract class WsService implements ServerService, SelfManaging, DeploymentListener {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_WS, WsService.class);
    public static final String WS_ADDRESS_FORMAT = "openejb.wsAddress.format";
    private StringTemplate wsAddressTemplate;

    private PortAddressRegistry portAddressRegistry;
    private CoreContainerSystem containerSystem;
    private Assembler assembler;
    private WsRegistry wsRegistry;
    private String realmName;
    private String transportGuarantee;
    private String authMethod;
    private String virtualHost;
    private final Set<AppInfo> deployedApplications = new HashSet<AppInfo>();
    private final Set<WebAppInfo> deployedWebApps = new HashSet<WebAppInfo>();
    private final Map<String,String> ejbLocations = new TreeMap<String,String>();
    private final Map<String,String> ejbAddresses = new TreeMap<String,String>();
    private final Map<String,String> servletAddresses = new TreeMap<String,String>();

    public WsService() {
        String format = SystemInstance.get().getProperty(WS_ADDRESS_FORMAT, "/{ejbDeploymentId}");
        this.wsAddressTemplate = new StringTemplate(format);
    }

    public StringTemplate getWsAddressTemplate() {
        return wsAddressTemplate;
    }

    public void setWsAddressTemplate(StringTemplate wsAddressTemplate) {
        this.wsAddressTemplate = wsAddressTemplate;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getTransportGuarantee() {
        return transportGuarantee;
    }

    public void setTransportGuarantee(String transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getIP() {
        return "n/a";
    }

    public int getPort() {
        return -1;
    }

    public void init(Properties props) throws Exception {
        if (props == null) return;

        String format = props.getProperty(WS_ADDRESS_FORMAT);
        if (format != null) {
            this.wsAddressTemplate = new StringTemplate(format);
        }

        realmName = props.getProperty("realmName");
        transportGuarantee = props.getProperty("transportGuarantee");
        authMethod = props.getProperty("authMethod");
        virtualHost = props.getProperty("virtualHost");
    }

    public void start() throws ServiceException {
        wsRegistry = SystemInstance.get().getComponent(WsRegistry.class);
        if (wsRegistry == null && SystemInstance.get().getComponent(HttpListenerRegistry.class) != null) {
            wsRegistry = new OpenEJBHttpWsRegistry();
        }

        if (portAddressRegistry == null) {
            portAddressRegistry = new PortAddressRegistryImpl();
            SystemInstance.get().setComponent(PortAddressRegistry.class, portAddressRegistry);
        }
        containerSystem = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        portAddressRegistry = SystemInstance.get().getComponent(PortAddressRegistry.class);
        assembler = SystemInstance.get().getComponent(Assembler.class);
        SystemInstance.get().setComponent(WsService.class, this);
        if (assembler != null) {
            assembler.addDeploymentListener(this);
            for (AppInfo appInfo : assembler.getDeployedApplications()) {
                afterApplicationCreated(appInfo);
            }
        }
    }

    public void stop() throws ServiceException {
        if (assembler != null) {
            assembler.removeDeploymentListener(this);
            for (AppInfo appInfo : new ArrayList<AppInfo>(deployedApplications)) {
                beforeApplicationDestroyed(appInfo);
            }
            assembler = null;
            if (SystemInstance.get().getComponent(WsService.class) == this) {
                SystemInstance.get().removeComponent(WsService.class);
            }
        }
    }

    protected abstract HttpListener createEjbWsContainer(URL moduleBaseUrl, PortData port, BeanContext beanContext) throws Exception;

    protected abstract void destroyEjbWsContainer(String deploymentId);

    protected abstract HttpListener createPojoWsContainer(URL moduleBaseUrl, PortData port, String serviceId, Class target, Context context, String contextRoot) throws Exception;

    protected abstract void destroyPojoWsContainer(String serviceId);

    public void afterApplicationCreated(AppInfo appInfo) {
        if (deployedApplications.add(appInfo)) {
            Map<String, String> webContextByEjb = new HashMap<String, String>();
            for (WebAppInfo webApp : appInfo.webApps) {
                for (String ejb : webApp.ejbWebServices) {
                    webContextByEjb.put(ejb, webApp.contextRoot);
                }
            }

            Map<String,String> contextData = new HashMap<String,String>();
            contextData.put("appId", appInfo.path);
            for (EjbJarInfo ejbJar : appInfo.ejbJars) {
                Map<String, PortInfo> ports = new TreeMap<String,PortInfo>();
                for (PortInfo port : ejbJar.portInfos) {
                    ports.put(port.serviceLink, port);
                }

                URL moduleBaseUrl = null;
                if (ejbJar.path != null) {
                    try {
                        moduleBaseUrl = new File(ejbJar.path).toURI().toURL();
                    } catch (MalformedURLException e) {
                        logger.error("Invalid ejb jar location " + ejbJar.path, e);
                    }
                }

                StringTemplate deploymentIdTemplate = this.wsAddressTemplate;
                if (ejbJar.properties.containsKey(WS_ADDRESS_FORMAT)){
                    String format = ejbJar.properties.getProperty(WS_ADDRESS_FORMAT);
                    logger.info("Using "+ WS_ADDRESS_FORMAT +" '"+format+"'");
                    deploymentIdTemplate = new StringTemplate(format);
                }
                contextData.put("ejbJarId", ejbJar.moduleName);

                for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                    if (bean instanceof StatelessBeanInfo || bean instanceof SingletonBeanInfo) {

                        BeanContext beanContext = containerSystem.getBeanContext(bean.ejbDeploymentId);
                        if (beanContext == null) continue;

                        PortInfo portInfo = ports.get(bean.ejbName);
                        if (portInfo == null) continue;

                        try {
                            PortData port = WsBuilder.toPortData(portInfo, beanContext.getInjections(), moduleBaseUrl, beanContext.getClassLoader());

                            HttpListener container = createEjbWsContainer(moduleBaseUrl, port, beanContext);

                            // generate a location if one was not assigned
                            String location = port.getLocation();
                            if (location == null) {
                                location = autoAssignWsLocation(bean, port, contextData, deploymentIdTemplate);
                            }
                            if (!location.startsWith("/")) location = "/" + location;
                            ejbLocations.put(bean.ejbDeploymentId, location);

                            ClassLoader classLoader = beanContext.getClassLoader();
                            if (wsRegistry != null) {
                                String auth = authMethod;
                                String realm = realmName;
                                String transport = transportGuarantee;

                                if ("BASIC".equals(portInfo.authMethod) || "DIGEST".equals(portInfo.authMethod) || "CLIENT-CERT".equals(portInfo.authMethod)) {
                                    auth = portInfo.authMethod;
                                    realm = portInfo.realmName;
                                    transport = portInfo.transportGuarantee;
                                }

                                List<String> addresses = wsRegistry.addWsContainer(webContextByEjb.get(bean.ejbClass), location, container, virtualHost, realm, transport, auth, classLoader);

                                // one of the registered addresses to be the cannonical address
                                String address = HttpUtil.selectSingleAddress(addresses);

                                if (address != null) {
                                    // register wsdl location
                                    portAddressRegistry.addPort(portInfo.serviceId, portInfo.wsdlService, portInfo.portId, portInfo.wsdlPort, portInfo.seiInterfaceName, address);
                                    logger.info("Webservice(wsdl=" + address + ", qname=" + port.getWsdlService() + ") --> Ejb(id=" + portInfo.portId + ")");
                                    ejbAddresses.put(bean.ejbDeploymentId, address);
                                }
                            }
                        } catch (Throwable e) {
                            logger.error("Error deploying JAX-WS Web Service for EJB " + beanContext.getDeploymentID(), e);
                        }
                    }
                }
            }
            for (WebAppInfo webApp : appInfo.webApps) {
                afterApplicationCreated(webApp);
            }
        }
    }

    public void afterApplicationCreated(WebAppInfo webApp) {
        WebContext webContext = containerSystem.getWebContext(webApp.moduleId);
        if (webContext == null) return;

        // if already deployed skip this webapp
        if (!deployedWebApps.add(webApp)) return;

        Map<String,PortInfo> ports = new TreeMap<String,PortInfo>();
        for (PortInfo port : webApp.portInfos) {
            ports.put(port.serviceLink, port);
        }

        URL moduleBaseUrl = null;
        try {
            moduleBaseUrl = new File(webApp.path).toURI().toURL();
        } catch (MalformedURLException e) {
            logger.error("Invalid ejb jar location " + webApp.path, e);
        }

        for (ServletInfo servlet : webApp.servlets) {
            PortInfo portInfo = ports.get(servlet.servletName);
            if (portInfo == null) continue;

            try {
                ClassLoader classLoader = webContext.getClassLoader();
                Collection<Injection> injections = webContext.getInjections();
                Context context = webContext.getJndiEnc();
                Class target = classLoader.loadClass(servlet.servletClass);

                PortData port = WsBuilder.toPortData(portInfo, injections, moduleBaseUrl, classLoader);

                HttpListener container = createPojoWsContainer(moduleBaseUrl, port, portInfo.serviceLink, target, context, webApp.contextRoot);

                if (wsRegistry != null) {
                    // give servlet a reference to the webservice container
                    List<String> addresses = wsRegistry.setWsContainer(virtualHost, webApp.contextRoot, servlet.servletName, container);

                    // one of the registered addresses to be the connonical address
                    String address = HttpUtil.selectSingleAddress(addresses);

                    // add address to global registry
                    portAddressRegistry.addPort(portInfo.serviceId, portInfo.wsdlService, portInfo.portId, portInfo.wsdlPort, portInfo.seiInterfaceName, address);
                    logger.info("Webservice(wsdl=" + address + ", qname=" + port.getWsdlService() + ") --> Pojo(id=" + portInfo.portId + ")");
                    servletAddresses.put(webApp.moduleId + "." + servlet.servletName, address);
                }
            } catch (Throwable e) {
                logger.error("Error deploying CXF webservice for servlet " + portInfo.serviceLink, e);
            }
        }
    }

    public void beforeApplicationDestroyed(AppInfo appInfo) {
        if (deployedApplications.remove(appInfo)) {
            for (EjbJarInfo ejbJar : appInfo.ejbJars) {
                Map<String,PortInfo> ports = new TreeMap<String,PortInfo>();
                for (PortInfo port : ejbJar.portInfos) {
                    ports.put(port.serviceLink, port);
                }

                for (EnterpriseBeanInfo enterpriseBean : ejbJar.enterpriseBeans) {
                    if (enterpriseBean instanceof StatelessBeanInfo || enterpriseBean instanceof SingletonBeanInfo) {

                        PortInfo portInfo = ports.get(enterpriseBean.ejbName);
                        if (portInfo == null) continue;

                        // remove wsdl addresses from global registry
                        String address = ejbAddresses.remove(enterpriseBean.ejbDeploymentId);
                        if (address != null) {
                            portAddressRegistry.removePort(portInfo.serviceId, portInfo.wsdlService, portInfo.portId);
                        }

                        // remove container from web server
                        String location = ejbLocations.get(enterpriseBean.ejbDeploymentId);
                        if (this.wsRegistry != null && location != null) {
                            this.wsRegistry.removeWsContainer(location);
                        }

                        // destroy webservice container
                        destroyEjbWsContainer(enterpriseBean.ejbDeploymentId);
                    }
                }
            }
            for (WebAppInfo webApp : appInfo.webApps) {
                deployedWebApps.remove(webApp);

                Map<String,PortInfo> ports = new TreeMap<String,PortInfo>();
                for (PortInfo port : webApp.portInfos) {
                    ports.put(port.serviceLink, port);
                }

                for (ServletInfo servlet : webApp.servlets) {
                    PortInfo portInfo = ports.get(servlet.servletClass);
                    if (portInfo == null) continue;

                    // remove wsdl addresses from global registry
                    String address = servletAddresses.remove(webApp.moduleId + "." + servlet.servletName);
                    if (address != null) {
                        portAddressRegistry.removePort(portInfo.serviceId, portInfo.wsdlService, portInfo.portId);
                    }

                    // clear servlet's reference to the webservice container
                    if (this.wsRegistry != null) {
                        this.wsRegistry.clearWsContainer(virtualHost, webApp.contextRoot, servlet.servletName);
                    }

                    // destroy webservice container
                    destroyPojoWsContainer(portInfo.serviceLink);
                }
            }
        }
    }

    private String autoAssignWsLocation(EnterpriseBeanInfo bean, PortData port, Map<String, String> contextData, StringTemplate template) {
        contextData.put("ejbDeploymentId", bean.ejbDeploymentId);
        contextData.put("ejbType", getEjbType(bean.type));
        contextData.put("ejbClass", bean.ejbClass);
        contextData.put("ejbClass.simpleName", bean.ejbClass.substring(bean.ejbClass.lastIndexOf('.') + 1));
        contextData.put("ejbName", bean.ejbName);
        contextData.put("portComponentName", port.getPortName().getLocalPart());
        contextData.put("wsdlPort", port.getWsdlPort().getLocalPart());
        contextData.put("wsdlService", port.getWsdlService().getLocalPart());
        return template.apply(contextData);
    }

    public static String getEjbType(int type) {
        if (type == EnterpriseBeanInfo.STATEFUL) {
            return "StatefulBean";
        } else if (type == EnterpriseBeanInfo.STATELESS) {
            return "StatelessBean";
        } else if (type == EnterpriseBeanInfo.SINGLETON) {
            return "SingletonBean";
        } else if (type == EnterpriseBeanInfo.MANAGED) {
            return "ManagedBean";
        } else if (type == EnterpriseBeanInfo.MESSAGE) {
            return "MessageDrivenBean";
        } else if (type == EnterpriseBeanInfo.ENTITY) {
            return "StatefulBean";
        } else {
            return "UnknownBean";
        }
    }

    public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException("CxfService cannot be invoked directly");
    }

    public void service(Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException("CxfService cannot be invoked directly");
    }
}
