/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.eve.schema;


import javax.naming.NamingException;

import org.apache.ldap.common.schema.Syntax;


/**
 * Monitor interface for a SyntaxRegistry.
 *
 * @author <a href="mailto:directory-dev@incubator.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public interface SyntaxRegistryMonitor
{
    /**
     * Monitors when a Syntax is registered successfully.
     * 
     * @param syntax the Syntax registered
     */
    void registered( Syntax syntax );

    /**
     * Monitors when a Syntax is successfully looked up.
     * 
     * @param syntax the Syntax looked up
     */
    void lookedUp( Syntax syntax );

    /**
     * Monitors when a lookup attempt fails.
     * 
     * @param oid the OID for the Syntax to lookup
     * @param fault the exception to be thrown for the fault
     */
    void lookupFailed( String oid, NamingException fault );
    
    /**
     * Monitors when a registration attempt fails.
     * 
     * @param syntax the Syntax which failed registration
     * @param fault the exception to be thrown for the fault
     */
    void registerFailed( Syntax syntax, NamingException fault );
}
