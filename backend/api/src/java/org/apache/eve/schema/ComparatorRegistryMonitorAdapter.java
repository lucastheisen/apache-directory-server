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


import java.util.Comparator;

import javax.naming.NamingException;


/**
 * An adapter for the ComparatorRegistry's monitor.
 *
 * @author <a href="mailto:directory-dev@incubator.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class ComparatorRegistryMonitorAdapter 
    implements ComparatorRegistryMonitor
{
    /* (non-Javadoc)
     * @see org.apache.eve.schema.ComparatorRegistryMonitor#registered(
     * org.apache.eve.schema.Comparator)
     */
    public void registered( Comparator comparator )
    {
    }

    
    /* (non-Javadoc)
     * @see org.apache.eve.schema.ComparatorRegistryMonitor#lookedUp(
     * org.apache.eve.schema.Comparator)
     */
    public void lookedUp( Comparator comparator )
    {
    }

    
    /* (non-Javadoc)
     * @see org.apache.eve.schema.ComparatorRegistryMonitor#lookupFailed(
     * java.lang.String, javax.naming.NamingException)
     */
    public void lookupFailed( String oid, NamingException fault )
    {
        if ( fault != null )
        {
            fault.printStackTrace();
        }
    }

    
    /* (non-Javadoc)
     * @see org.apache.eve.schema.ComparatorRegistryMonitor#registerFailed(
     * org.apache.eve.schema.Comparator, javax.naming.NamingException)
     */
    public void registerFailed( Comparator comparator, NamingException fault )
    {
    }
}
