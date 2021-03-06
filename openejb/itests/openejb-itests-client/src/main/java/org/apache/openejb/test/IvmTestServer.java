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
package org.apache.openejb.test;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * 
 * @version $Rev$ $Date$ 
 */
public class IvmTestServer implements TestServer {

    private Properties properties;

    public void init(Properties props){
        
        properties = props;
        
        try{
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.localclient.LocalInitialContextFactory");
            Properties p = new Properties();
            p.putAll(props);
            p.put("openejb.loader", "embed");
            new InitialContext( p );    // initialize openejb via constructing jndi tree
            
        //OpenEJB.init(properties);
        }catch(Exception oe){
            System.out.println("=========================");
            System.out.println(""+oe.getMessage());
            System.out.println("=========================");
            oe.printStackTrace();
            throw new RuntimeException("OpenEJB could not be initiated");
        }
    }

    public void destroy(){
    }

    public void start(){
    }

    public void stop(){

    }

    public Properties getContextEnvironment(){
        return (Properties)properties.clone();
    }

}
