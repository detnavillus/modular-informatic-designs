package com.modinfodesigns.app.search.model;

import java.util.HashMap;
import java.util.Iterator;


import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the list of 'Navigator' or Facet fields provided by the search application or search
 * application page. This is a collection object (DataList) that contains one or more NavigatorField 
 * objects.
 * 
 * @author Ted Sullivan
 */
public class NavigatorSchema extends DataList
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NavigatorSchema.class );
    
	public static final String NAME = "NavigatorSchema";
	
	private HashMap<String,NavigatorFieldDefinition> navigatorFields;
	
	@Override
	public String getName( )
	{
		return (name != null) ? super.getName( ) : NAME;
	}
	
    protected String getListName( )
    {
    	return "Navigators";
    }
    
	public void addNavigatorField( NavigatorFieldDefinition navField )
	{
		LOG.debug( "addNavigatorField( " + navField.getName( ) + " )" );
		
		if (navigatorFields == null) navigatorFields = new HashMap<String,NavigatorFieldDefinition>( );
		navigatorFields.put( navField.getName( ), navField );
		super.addDataObject( navField );
	}
	
	@Override
	public void addDataObject( DataObject dObject )
	{
		if (dObject == null) return;
		
		if (dObject instanceof NavigatorFieldDefinition )
		{
			addNavigatorField( (NavigatorFieldDefinition)dObject );
		}
		else
		{
		    super.addDataObject( dObject );
		}
	}
    
    public NavigatorFieldDefinition getNavigatorField( String fieldName )
    {
		return (navigatorFields != null) ? navigatorFields.get( fieldName ) : null;
    }

	public Iterator<String> getNavigatorFields(  )
	{
		return (navigatorFields != null) ? navigatorFields.keySet().iterator() : null;
	}
	
    public boolean hasField( String fieldName )
    {
		return (navigatorFields != null && navigatorFields.get( fieldName ) != null);
    }
}
