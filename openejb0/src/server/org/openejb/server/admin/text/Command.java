/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.server.admin.text;

import java.io.*;
import java.net.*;
import java.util.*;
import org.openejb.util.Logger;
import org.openejb.server.EjbDaemon;
import org.openejb.server.admin.text.*;
import org.apache.regexp.RE;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class Command {


    //protected static HashMap commands = new HashMap();
    protected static HashMap commands = null;
    
    static{
        loadCommandList();
    }

    protected static Command unknownCommand = new Command();

    protected static void register(String name, Command cmd){
        commands.put(name, cmd);
    }
    
    protected static void register(String name, Class cmd){
        commands.put(name, cmd);
    }

    public static Command getCommand(String name){
        Object cmd = commands.get(name);
        
        if ( cmd instanceof Class ) {
            cmd = loadCommand((Class)cmd);
            register(name, (Command)cmd);
        }

        return (Command) cmd;
    }

    // - Public methods - //
    
    public void exec(String[] args, DataInputStream in, PrintStream out) throws IOException{
        out.println("not implemented");
    }
    

    // - Protected methods - //
    protected static Command loadCommand(Class commandClass) {
        Command cmd = null;
        try{
            cmd = (Command)commandClass.newInstance();
        } catch(Exception e){
            //throw new IOException("Cannot instantiate command class "+commandClass+"\n"+e.getClass().getName()+":\n"+e.getMessage());
        }

        return cmd;
    }
  

    /*
    TODO: 
    - Create the basic list in ant
    - Add the regexp package to the ant scripts
    - update the loadCommandList to read the list
      made in the ant script
    
    */
    protected static void loadCommandList() {
        try{
            Command.commands = new HashMap();
            URL dir = new URL("resource:/openejb/server/commandlist.txt");
            DataInputStream in = new DataInputStream(dir.openStream());


            RE r = new RE("href=.([A-Za-z]*).java");

            Class[]  params = new Class[0];
            String line = in.readLine();
            while (line != null) {
                if ( r.match(line) ){
                    String cmdName = r.getParen(1);
                    try{
                        Class clazz = Class.forName("org.openejb.server.admin.text."+cmdName);
                        java.lang.reflect.Method register = clazz.getMethod("register", params);
                        register.invoke(null, null);
                    }catch(Throwable e){
                        //e.printStackTrace();
                    }
                }
                line = in.readLine();
            }
        } catch (Throwable t){
            t.printStackTrace();
        }
    }
}

