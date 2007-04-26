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
package org.apache.openejb.core.entity;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.openejb.Container;
import org.apache.openejb.InterfaceType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.core.ivm.EjbObjectProxyHandler;
import org.apache.openejb.util.proxy.ProxyManager;

public class EntityEjbObjectHandler extends EjbObjectProxyHandler {

    /*
    * The registryId is a logical identifier that is used as a key when placing EntityEjbObjectHandler into
    * the BaseEjbProxyHanlder's liveHandleRegistry.  EntityEjbObjectHandlers that represent the same
    * bean identity (keyed by the registry id) will be stored together so that they can be removed together
    * when the BaseEjbProxyHandler.invalidateAllHandlers is invoked. The EntityEjbObjectHandler uses a 
    * compound key composed of the entity bean's primary key, deployment id, and
    * container id.  This uniquely identifies the bean identity that is proxied by this handler allowing it
    * to be removed with other handlers bound to the same registry id.
    */
    private Object registryId;

    public EntityEjbObjectHandler(DeploymentInfo deploymentInfo, Object pk, InterfaceType interfaceType, List<Class> interfaces) {
        super(deploymentInfo, pk, interfaceType, interfaces);
    }

    /*
    * This method generates a logically unique entity bean identifier from the primary key,
    * deployment id, and container id. This registry key is then used as an index for the associated
    * entity bean in the BaseEjbProxyHandler.liveHandleRegistry. The liveHandleRegistry tracks 
    * handler for the same bean identity so that they can removed together when one of the remove() operations
    * is called.
    */
    public static Object getRegistryId(Object primKey, Object deployId, Container contnr) {
        return "" + primKey + deployId + contnr.getContainerID();
    }

    public Object getRegistryId() {
        if (registryId == null)
            registryId = getRegistryId(primaryKey, deploymentID, container);
        return registryId;
    }

    protected Object getPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        return primaryKey;
    }

    protected Object isIdentical(Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);

        Object hndr = ProxyManager.getInvocationHandler(proxy);

        if (hndr instanceof EntityEjbObjectHandler) {

            EntityEjbObjectHandler handler = (EntityEjbObjectHandler) hndr;

            /*
            * The registry id is a compound key composed of the bean's primary key, deployment id, and
            * container id.  It uniquely identifies the entity bean that is proxied by the EntityEjbObjectHandler
            * within the IntraVM.
            */
            if (this.getRegistryId().equals(handler.getRegistryId())) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;

    }

    protected Object remove(Class interfce, Method method, Object[] args, Object proxy) throws Throwable {
        checkAuthorization(method);
        Object value = container.invoke(deploymentID, interfce, method, args, primaryKey);
        /* 
        * This operation takes care of invalidating all the EjbObjectProxyHanders associated with 
        * the same RegistryId. See this.createProxy().
        */
        invalidateAllHandlers(getRegistryId());
        return value;
    }

}
