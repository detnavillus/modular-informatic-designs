package com.modinfodesigns.app.search.model;


import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.string.StringListProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines a list of Navigator fields that are configured as a group. The configuration layer
 * specifies a list of field names. Uses a NavigatorSchema object to obtain access to individual 
 * NavigatorField objects.
 * 
 * Sets the display properties of the NavigatorField based on the NavigatorProperties configuration
 * object. 
 * 
 * @author Ted Sullivan
 */

public class NavigatorGroup extends DataObject
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NavigatorGroup.class );
    
	public static final String NAVIGATOR_LIST = "NavigatorList";
    private NavigatorSchema navSchema;
    
    private HashMap<String,NavigatorDisplayProperties> navPropsMap;
    
    public NavigatorGroup( ) {  }
    
    public NavigatorGroup( String name )
    {
    	setName( name );
    }
    
    
    public void setNavigatorSchema( NavigatorSchema navSchema )
    {
    	this.navSchema = navSchema;
    }
    
    /**
     * Adds a NavigatorProperties object which specifies the navigator field and any
     * application-specific properties
     * 
     * @param navProps
     */
    public void addNavigatorDisplayProperties( NavigatorDisplayProperties navProps )
    {
    	LOG.debug( "addNavigatorDisplayProperties " + navProps );
    	
    	if (navProps == null)
    	{
    		LOG.error( "NavigatorDisplayProperties is NULL!" );
    		return;
    	}
    	
    	LOG.debug( "Adding Navigator Properties " + navProps.getName( ) );
    	
    	StringListProperty navListProp = (StringListProperty)getProperty( NAVIGATOR_LIST );
    	if (navListProp == null)
    	{
    		navListProp = new StringListProperty( NAVIGATOR_LIST );
    		addProperty( navListProp );
    		
    		navPropsMap = new HashMap<String,NavigatorDisplayProperties>( );
    	}
    	
    	navListProp.addString( navProps.getName( ) );
    	navPropsMap.put( navProps.getName( ), navProps );
    }
    
    
    public List<NavigatorFieldDefinition> getNavigatorFields(  )
    {
    	LOG.debug( "getNavigatorFields( )" );
    	
    	ArrayList<NavigatorFieldDefinition> navFields = new ArrayList<NavigatorFieldDefinition>( );
    	StringListProperty navListProp = (StringListProperty)getProperty( NAVIGATOR_LIST );
    	if (navSchema != null && navListProp != null)
    	{
        	for ( Iterator<String> navIt = navListProp.iterator(); navIt.hasNext(); )
    	    {
        		String navName = navIt.next( );
        		LOG.debug( "Getting NavigatorField for " + navName );
    		    NavigatorFieldDefinition navField = navSchema.getNavigatorField( navName );
    		    if (navField != null)
    		    {
    		    	// Copy it and set the properties based on configuration settings.
    		    	NavigatorFieldDefinition navFieldCopy = (NavigatorFieldDefinition)navField.copy( );
    		    	
    		    	NavigatorDisplayProperties navProps = navPropsMap.get( navName );
    		    	navFieldCopy.setNavigatorProperties( navProps );
    		    	navFields.add( navFieldCopy );
    		    }
    		    else
    		    {
    		    	LOG.error( "NavigatorFieldDefinition is NULL for " + navName );
    		    }
    	    }
    	}
    	
    	return navFields;
    }
    
    public NavigatorFieldDefinition getNavigatorField( String navName )
    {
    	LOG.debug( "getNavigatorField( " + navName + " )" );
    	NavigatorFieldDefinition navDef = (navSchema != null && navPropsMap != null && navPropsMap.containsKey( navName )) 
    			                        ? navSchema.getNavigatorField( navName ) : null;
    	
        if (navDef == null)
        {
        	LOG.debug( "Group doesn't have " + navName );
        	return null;
        }
        
    	NavigatorDisplayProperties navProps = navPropsMap.get( navName );
    	if (navProps != null)
    	{
    		NavigatorFieldDefinition navDefCopy = (NavigatorFieldDefinition)navDef.copy( );
    		navDefCopy.setNavigatorProperties( navProps );
    	    return navDefCopy;
    	}
    	
        return navDef;
    }
}
