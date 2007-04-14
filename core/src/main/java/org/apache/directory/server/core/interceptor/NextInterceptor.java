/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.server.core.interceptor;


import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.directory.server.core.configuration.PartitionConfiguration;
import org.apache.directory.server.core.interceptor.context.ServiceContext;
import org.apache.directory.server.core.partition.PartitionNexus;
import org.apache.directory.shared.ldap.filter.ExprNode;
import org.apache.directory.shared.ldap.message.ModificationItemImpl;
import org.apache.directory.shared.ldap.name.LdapDN;

/**
 * Represents the next {@link Interceptor} in the interceptor chain.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 * @see Interceptor
 * @see InterceptorChain
 */
public interface NextInterceptor
{
    /**
     * Calls the next interceptor's {@link Interceptor#compare( NextInterceptor, ServiceContext )}.
     */
    boolean compare( ServiceContext compareContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#getRootDSE( NextInterceptor, ServiceContext )}.
     */
    Attributes getRootDSE( ServiceContext getRootDSEContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#getMatchedName( NextInterceptor, ServiceContext )}.
     */
    LdapDN getMatchedName( ServiceContext getMatchedNameContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#getSuffix( NextInterceptor, ServiceContext getSuffixContext )}.
     */
    LdapDN getSuffix( ServiceContext getSuffixContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#listSuffixes( NextInterceptor), ServiceContext )}.
     */
    Iterator listSuffixes( ServiceContext emptyContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link PartitionNexus#addContextPartition(PartitionConfiguration)}.
     */
    void addContextPartition( PartitionConfiguration cfg ) throws NamingException;


    /**
     * Calls the next interceptor's {@link PartitionNexus#removeContextPartition(org.apache.directory.shared.ldap.name.LdapDN)}.
     */
    void removeContextPartition( LdapDN suffix ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#delete(NextInterceptor, org.apache.directory.shared.ldap.name.LdapDN)}.
     */
    void delete( ServiceContext deleteContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#add( NextInterceptor, ServiceContext )}.
     */
    void add( ServiceContext addContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#modify( NextInterceptor, ServiceContext )}.
     */
    void modify( ServiceContext modifyContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#modify(NextInterceptor,org.apache.directory.shared.ldap.name.LdapDN,javax.naming.directory.ModificationItem[])}.
     */
    void modify( LdapDN name, ModificationItemImpl[] items ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#list(NextInterceptor,org.apache.directory.shared.ldap.name.LdapDN)}.
     */
    NamingEnumeration list( LdapDN baseName ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#search(NextInterceptor,org.apache.directory.shared.ldap.name.LdapDN,java.util.Map,org.apache.directory.shared.ldap.filter.ExprNode,javax.naming.directory.SearchControls)}.
     */
    NamingEnumeration search( LdapDN baseName, Map environment, ExprNode filter, SearchControls searchControls )
        throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#lookup( NextInterceptor, ServiceContext )}.
     */
    Attributes lookup( ServiceContext lookupContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#hasEntry( NextInterceptor, ServiceContext )}.
     */
    boolean hasEntry( ServiceContext entryContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#rename( NextInterceptor, ServiceContext )}.
     */
    void rename( ServiceContext renameContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#move( NextInterceptor, ServiceContext )}.
     */
    void move( ServiceContext moveContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#moveAndRename( NextInterceptor, ServiceContext )}.
     */
    void moveAndRename( ServiceContext moveAndRenameContext ) throws NamingException;


    /**
     * Calls the next interceptor's {@link Interceptor#bind( NextInterceptor, ServiceContext )}
     */
    void bind( ServiceContext bindContext ) throws NamingException;

    /**
     * Calls the next interceptor's {@link Interceptor#unbind( NextInterceptor, ServiceContext ))}
     */
    void unbind( ServiceContext unbindContext ) throws NamingException;
}
