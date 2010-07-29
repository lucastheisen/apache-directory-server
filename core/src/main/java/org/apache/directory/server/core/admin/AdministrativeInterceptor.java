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
package org.apache.directory.server.core.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.directory.SearchControls;

import org.apache.directory.server.constants.ServerDNConstants;
import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultCoreSession;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.LdapPrincipal;
import org.apache.directory.server.core.authn.Authenticator;
import org.apache.directory.server.core.filtering.EntryFilteringCursor;
import org.apache.directory.server.core.interceptor.BaseInterceptor;
import org.apache.directory.server.core.interceptor.NextInterceptor;
import org.apache.directory.server.core.interceptor.context.AddOperationContext;
import org.apache.directory.server.core.interceptor.context.DeleteOperationContext;
import org.apache.directory.server.core.interceptor.context.ModifyOperationContext;
import org.apache.directory.server.core.interceptor.context.MoveAndRenameOperationContext;
import org.apache.directory.server.core.interceptor.context.MoveOperationContext;
import org.apache.directory.server.core.interceptor.context.RenameOperationContext;
import org.apache.directory.server.core.interceptor.context.SearchOperationContext;
import org.apache.directory.server.core.partition.PartitionNexus;
import org.apache.directory.server.i18n.I18n;
import org.apache.directory.shared.ldap.constants.AuthenticationLevel;
import org.apache.directory.shared.ldap.constants.SchemaConstants;
import org.apache.directory.shared.ldap.entry.DefaultEntryAttribute;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Modification;
import org.apache.directory.shared.ldap.entry.StringValue;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.exception.LdapAttributeInUseException;
import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.exception.LdapInvalidAttributeValueException;
import org.apache.directory.shared.ldap.exception.LdapNoSuchAttributeException;
import org.apache.directory.shared.ldap.exception.LdapOperationException;
import org.apache.directory.shared.ldap.exception.LdapUnwillingToPerformException;
import org.apache.directory.shared.ldap.filter.EqualityNode;
import org.apache.directory.shared.ldap.filter.ExprNode;
import org.apache.directory.shared.ldap.message.AliasDerefMode;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.schema.AttributeType;
import org.apache.directory.shared.ldap.schema.SchemaManager;
import org.apache.directory.shared.ldap.util.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An interceptor to manage the Administrative model
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AdministrativeInterceptor extends BaseInterceptor
{
    /** A ogger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( AdministrativeInterceptor.class );

    /**
     * Speedup for logs
     */
    private static final boolean IS_DEBUG = LOG.isDebugEnabled();

    /** A reference to the DirectoryService instance */
    private DirectoryService directoryService;

    /** A reference to the SchemaManager instance */
    private SchemaManager schemaManager;

    /** A reference to the nexus for direct backend operations */
    private PartitionNexus nexus;

    /** A reference to the AdministrativeRole AT */
    private static AttributeType ADMINISTRATIVE_ROLE_AT;

    /** The possible roles */
    private static final Set<String> ROLES = new HashSet<String>();

    static
    {
        ROLES.add( SchemaConstants.AUTONOMOUS_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.AUTONOMOUS_AREA_OID );
        ROLES.add( SchemaConstants.ACCESS_CONTROL_SPECIFIC_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.ACCESS_CONTROL_SPECIFIC_AREA_OID );
        ROLES.add( SchemaConstants.ACCESS_CONTROL_INNER_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.ACCESS_CONTROL_INNER_AREA_OID );
        ROLES.add( SchemaConstants.COLLECTIVE_ATTRIBUTE_SPECIFIC_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.COLLECTIVE_ATTRIBUTE_SPECIFIC_AREA_OID );
        ROLES.add( SchemaConstants.COLLECTIVE_ATTRIBUTE_INNER_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.COLLECTIVE_ATTRIBUTE_INNER_AREA_OID );
        ROLES.add( SchemaConstants.SUB_SCHEMA_ADMIN_SPECIFIC_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.SUB_SCHEMA_ADMIN_SPECIFIC_AREA_OID );
        ROLES.add( SchemaConstants.TRIGGER_EXECUTION_SPECIFIC_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.TRIGGER_EXECUTION_SPECIFIC_AREA_OID );
        ROLES.add( SchemaConstants.TRIGGER_EXECUTION_INNER_AREA.toLowerCase() );
        ROLES.add( SchemaConstants.TRIGGER_EXECUTION_INNER_AREA_OID );
    }

    /**
     * Creates an Administrative service interceptor.
     */
    public AdministrativeInterceptor()
    {
    }

    //-------------------------------------------------------------------------------------------
    // Helper methods
    //-------------------------------------------------------------------------------------------
    private List<Entry> getAdministrativePoints( String adminRoleType ) throws LdapException
    {
        List<Entry> entries = new ArrayList<Entry>();

        DN adminDn = new DN( ServerDNConstants.ADMIN_SYSTEM_DN, schemaManager );

        CoreSession adminSession = new DefaultCoreSession(
            new LdapPrincipal( adminDn, AuthenticationLevel.STRONG ), directoryService );

        SearchControls controls = new SearchControls();
        controls.setSearchScope( SearchControls.SUBTREE_SCOPE );
        controls.setReturningAttributes(
            new String[]
                {
                    SchemaConstants.ADMINISTRATIVE_ROLE_AT,
                    SchemaConstants.ENTRY_UUID_AT
                } );

        ExprNode filter = new EqualityNode<String>( ADMINISTRATIVE_ROLE_AT, new StringValue(
            adminRoleType ) );

        SearchOperationContext searchOperationContext = new SearchOperationContext( adminSession, DN.EMPTY_DN, filter,
            controls );
        searchOperationContext.setAliasDerefMode( AliasDerefMode.NEVER_DEREF_ALIASES );

        EntryFilteringCursor results = nexus.search( searchOperationContext );

        try
        {
            while ( results.next() )
            {
                Entry entry = results.get();

                entries.add( entry );
            }
        }
        catch ( Exception e )
        {
            throw new LdapOperationException( e.getMessage() );
        }

        return entries;
    }


    /**
     * Tells if a given role is a valid administrative role. We check the lower cased
     * and trimmed value, and also the OID value.
     */
    private boolean isValidRole( String role )
    {
        return ROLES.contains( StringTools.toLowerCase( StringTools.trim( role ) ) );
    }


    //-------------------------------------------------------------------------------------------
    // Interceptor initialization
    //-------------------------------------------------------------------------------------------
    /**
     * Registers and initializes all {@link Authenticator}s to this service.
     */
    public void init( DirectoryService directoryService ) throws LdapException
    {
        LOG.debug( "Initializing the AdministrativeInterceptor" );

        this.directoryService = directoryService;

        schemaManager = directoryService.getSchemaManager();
        nexus = directoryService.getPartitionNexus();

        ADMINISTRATIVE_ROLE_AT = schemaManager.getAttributeType( SchemaConstants.ADMINISTRATIVE_ROLE_AT );

        // Load all the AdministratvePoint :
        // Autonomous Administrative Point first, then Specific
        // administrative point, finally the Inner administrative Point
        DN adminDn = new DN( ServerDNConstants.ADMIN_SYSTEM_DN, schemaManager );

        CoreSession adminSession = new DefaultCoreSession(
            new LdapPrincipal( adminDn, AuthenticationLevel.STRONG ), directoryService );

        SearchControls controls = new SearchControls();
        controls.setSearchScope( SearchControls.SUBTREE_SCOPE );
        controls.setReturningAttributes( new String[] { SchemaConstants.ADMINISTRATIVE_ROLE_AT } );

        // get the list of all the AAPs
        List<Entry> autonomousSAPs = getAdministrativePoints( SchemaConstants.AUTONOMOUS_AREA );

        // get the list of all the specific APs
        List<Entry> accessControlSAPs = getAdministrativePoints( SchemaConstants.ACCESS_CONTROL_SPECIFIC_AREA );
        List<Entry> collectiveAttributeSAPs = getAdministrativePoints( SchemaConstants.COLLECTIVE_ATTRIBUTE_SPECIFIC_AREA );
        List<Entry> subSchemaSAPs = getAdministrativePoints( SchemaConstants.SUB_SCHEMA_ADMIN_SPECIFIC_AREA );
        List<Entry> triggerExecutionSAPs = getAdministrativePoints( SchemaConstants.TRIGGER_EXECUTION_SPECIFIC_AREA );

        // get the list of all the inner APs
        List<Entry> accessControlIAPs = getAdministrativePoints( SchemaConstants.ACCESS_CONTROL_INNER_AREA );
        List<Entry> collectiveAttributeIAPs = getAdministrativePoints( SchemaConstants.COLLECTIVE_ATTRIBUTE_INNER_AREA );
        List<Entry> triggerExecutionIAPs = getAdministrativePoints( SchemaConstants.TRIGGER_EXECUTION_INNER_AREA );
    }


    /**
     * {@inheritDoc}
     */
    public void destroy()
    {
    }


    /**
     * {@inheritDoc}
     */
    public void add( NextInterceptor next, AddOperationContext addContext ) throws LdapException
    {
        Entry entry = addContext.getEntry();

        // Check if we are adding an Administrative Point
        EntryAttribute adminPoint = entry.get( ADMINISTRATIVE_ROLE_AT );

        if ( adminPoint == null )
        {
            // Nope, go on.
            next.add( addContext );

            LOG.debug( "Exit from Administrative Interceptor" );

            return;
        }

        LOG.debug( "Addition of an administrative point at {} for the role {}", entry.getDn(), adminPoint );

        // Now we are trying to add an Administrative point. We have to check that the added
        // AP is correct if it's a AAP : it has to have all the required SAP
        // Note :
        if ( adminPoint.contains( SchemaConstants.AUTONOMOUS_AREA ) )
        {
            if ( ( adminPoint.contains( SchemaConstants.ACCESS_CONTROL_SPECIFIC_AREA ) ||
                   adminPoint.contains( SchemaConstants.ACCESS_CONTROL_SPECIFIC_AREA_OID ) ) &&
                 ( adminPoint.contains( SchemaConstants.COLLECTIVE_ATTRIBUTE_SPECIFIC_AREA ) ||
                   adminPoint.contains( SchemaConstants.COLLECTIVE_ATTRIBUTE_SPECIFIC_AREA_OID ) ) &&
                 ( adminPoint.contains( SchemaConstants.SUB_SCHEMA_ADMIN_SPECIFIC_AREA ) ||
                   adminPoint.contains( SchemaConstants.SUB_SCHEMA_ADMIN_SPECIFIC_AREA_OID ) ) &&
                 ( adminPoint.contains( SchemaConstants.TRIGGER_EXECUTION_SPECIFIC_AREA ) ||
                   adminPoint.contains( SchemaConstants.TRIGGER_EXECUTION_SPECIFIC_AREA_OID ) ) )
            {
                // Ok, we have all the four needed specific area, we can add the AAP
                LOG.debug( "Adding an Autonomous Administrative Point at {}", entry.getDn() );

                next.add( addContext );

                LOG.debug( "Added an Autonomous Administrative Point at {}", entry.getDn() );

                return;
            }
            else
            {
                String message = "Cannot add an Autonomous Administratve Point when some of the" +
                    " specific areas are missing : " + adminPoint;
                LOG.error( message );
                throw new LdapUnwillingToPerformException( message );
            }
        }

        // check that we can't mix Inner and Specific areas
        if ( ( ( adminPoint.contains( SchemaConstants.ACCESS_CONTROL_SPECIFIC_AREA ) ||
                 adminPoint.contains( SchemaConstants.ACCESS_CONTROL_SPECIFIC_AREA_OID ) ) &&
               ( adminPoint.contains( SchemaConstants.ACCESS_CONTROL_INNER_AREA ) ||
                 adminPoint.contains( SchemaConstants.ACCESS_CONTROL_INNER_AREA_OID ) ) ) ||
             ( ( adminPoint.contains( SchemaConstants.COLLECTIVE_ATTRIBUTE_SPECIFIC_AREA ) ||
                 adminPoint.contains( SchemaConstants.COLLECTIVE_ATTRIBUTE_SPECIFIC_AREA_OID ) ) &&
               ( adminPoint.contains( SchemaConstants.COLLECTIVE_ATTRIBUTE_INNER_AREA ) ||
                 adminPoint.contains( SchemaConstants.COLLECTIVE_ATTRIBUTE_INNER_AREA_OID ) ) ) ||
             ( ( adminPoint.contains( SchemaConstants.TRIGGER_EXECUTION_SPECIFIC_AREA ) ||
                 adminPoint.contains( SchemaConstants.TRIGGER_EXECUTION_SPECIFIC_AREA_OID ) ) &&
               ( adminPoint.contains( SchemaConstants.TRIGGER_EXECUTION_INNER_AREA ) ||
                 adminPoint.contains( SchemaConstants.TRIGGER_EXECUTION_INNER_AREA_OID ) ) ) )
        {
            // This is inconsistant
            String message = "Cannot add a specific Administratve Point and the same" +
                " inner Administrative point at the same time : " + adminPoint;
            LOG.error( message );
            throw new LdapUnwillingToPerformException( message );
        }

        // Ok, we are golden.
        next.add( addContext );

        LOG.debug( "Added an Autonomous Administrative Point at {}", entry.getDn() );

        return;
    }


    /**
     * {@inheritDoc}
     */
    public void delete( NextInterceptor next, DeleteOperationContext deleteContext ) throws LdapException
    {
        next.delete( deleteContext );
    }


    /**
     * {@inheritDoc}
     */
    public void modify( NextInterceptor next, ModifyOperationContext modifyContext ) throws LdapException
    {
        // We have to check that the modification is acceptable
        List<Modification> modifications = modifyContext.getModItems();

        EntryAttribute modifiedAdminRole = (modifyContext.getEntry()).getOriginalEntry().get( ADMINISTRATIVE_ROLE_AT );

        for ( Modification modification : modifications )
        {
            EntryAttribute attribute = modification.getAttribute();

            if ( attribute.getAttributeType() != ADMINISTRATIVE_ROLE_AT )
            {
                continue;
            }

            // Ok, we have a modification impacting the administrative role
            // Apply it to a virtual AdministrativeRole attribute
            switch ( modification.getOperation() )
            {
                case ADD_ATTRIBUTE :
                    if ( modifiedAdminRole == null )
                    {
                        // Create the attribute
                        modifiedAdminRole = new DefaultEntryAttribute( ADMINISTRATIVE_ROLE_AT, attribute.get() );
                        break;
                    }

                    for ( Value<?> value : attribute )
                    {
                        if ( !isValidRole( value.getString() ) )
                        {
                            // Not a valid role : we will throw an exception
                            String msg = "Invalid role : " + value.getString();
                            LOG.error( msg );
                            throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, msg );
                        }

                        // At this point, we know that the attribute's syntax is correct
                        // We just have to check that the current attribute does not
                        // contains the value already
                        if ( modifiedAdminRole.contains( value ) )
                        {
                            // This is an error.
                            String msg = I18n.err( I18n.ERR_54, value );
                            LOG.error( msg );
                            throw new LdapAttributeInUseException( msg );
                        }

                        // Add the role to the modified attribute
                        modifiedAdminRole.add( value );
                    }

                    break;

                case REMOVE_ATTRIBUTE :
                    if ( modifiedAdminRole == null )
                    {
                        // We can't remove a value when the attribute does not exist.
                        String msg = "Cannot remove the administrative role, it does not exist";
                        LOG.error( msg );
                        throw new LdapNoSuchAttributeException( msg );
                    }

                    // It may be a complete removal
                    if ( attribute.size() == 0 )
                    {
                        // Complete removal
                        modifiedAdminRole = null;
                        break;
                    }

                    // Now deal with the values to remove
                    for ( Value<?> value : attribute )
                    {
                        if ( !isValidRole( value.getString() ) )
                        {
                            // Not a valid role : we will throw an exception
                            String msg = "Invalid role : " + value.getString();
                            LOG.error( msg );
                            throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, msg );
                        }

                        if ( !modifiedAdminRole.contains( value ) )
                        {
                            // We can't remove a value if it does not exist !
                            String msg = "Cannot remove the administrative role value" + value + ", it does not exist";
                            LOG.error( msg );
                            throw new LdapNoSuchAttributeException( msg );
                        }

                        modifiedAdminRole.remove( value );
                    }

                    break;

                case REPLACE_ATTRIBUTE :
                    if ( modifiedAdminRole == null )
                    {
                        // We have to create the attribute
                        // We can't remove a value when the attribute does not exist.
                        String msg = "Cannot remove the administrative role, it does not exist";
                        LOG.error( msg );
                        throw new LdapNoSuchAttributeException( msg );
                    }

                    // It may be a complete removal
                    if ( attribute.size() == 0 )
                    {
                        // Complete removal
                        modifiedAdminRole = null;
                        break;
                    }

                    // Now replace the existing attributes with the new ones if they are valid
                    modifiedAdminRole.clear();

                    for ( Value<?> value : attribute )
                    {
                        if ( !isValidRole( value.getString() ) )
                        {
                            // Not a valid role : we will throw an exception
                            String msg = "Invalid role : " + value.getString();
                            LOG.error( msg );
                            throw new LdapInvalidAttributeValueException( ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX, msg );
                        }

                        if ( modifiedAdminRole.contains( value ) )
                        {
                            // We can't add a value if it already exists !
                            String msg = "Cannot add the administrative role value" + value + ", it already exists";
                            LOG.error( msg );
                            throw new LdapAttributeInUseException( msg );
                        }

                        modifiedAdminRole.add( value );
                    }

                    break;
            }
        }

        next.modify( modifyContext );
    }


    /**
     * {@inheritDoc}
     */
    public void move( NextInterceptor next, MoveOperationContext moveContext ) throws LdapException
    {
        next.move( moveContext );
    }


    /**
     * {@inheritDoc}
     */
    public void moveAndRename( NextInterceptor next, MoveAndRenameOperationContext moveAndRenameContext )
        throws LdapException
    {
        next.moveAndRename( moveAndRenameContext );
    }


    /**
     * {@inheritDoc}
     */
    public void rename( NextInterceptor next, RenameOperationContext renameContext )
        throws LdapException
    {
        next.rename( renameContext );
    }
}