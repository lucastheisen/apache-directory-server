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
package org.apache.directory.server.core.interceptor.context;


import org.apache.directory.server.core.CoreSession;
import org.apache.directory.shared.ldap.message.MessageTypeEnum;
import org.apache.directory.shared.ldap.name.LdapDN;


/**
 * A Delete context used for Interceptors. It contains all the informations
 * needed for the delete operation, and used by all the interceptors
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class DeleteOperationContext extends AbstractOperationContext
{
    /**
     * Creates a new instance of DeleteOperationContext.
     */
    public DeleteOperationContext( CoreSession session )
    {
        super( session );
    }
    

    /**
     * Creates a new instance of DeleteOperationContext.
     *
     * @param collateralOperation true if this is a side effect operation
     */
    public DeleteOperationContext( CoreSession session, boolean collateralOperation )
    {
        super( session, collateralOperation );
    }


    /**
     * Creates a new instance of DeleteOperationContext.
     *
     * @param deleteDn The entry DN to delete
     */
    public DeleteOperationContext( CoreSession session, LdapDN deleteDn )
    {
        super( session, deleteDn );
    }


    /**
     * Creates a new instance of DeleteOperationContext.
     *
     * @param deleteDn The entry DN to delete
     * @param collateralOperation true if this is a side effect operation
     */
    public DeleteOperationContext( CoreSession session, LdapDN deleteDn, boolean collateralOperation )
    {
        super( session, deleteDn, collateralOperation );
    }


    /**
     * @return the operation name
     */
    public String getName()
    {
        return MessageTypeEnum.DEL_REQUEST.name();
    }

    
    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return "DeleteContext for DN '" + getDn().getUpName() + "'";
    }
}
