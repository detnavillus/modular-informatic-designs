package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes the set of search result display fields that a Search Application supports. 
 * Consists of a set of ResultDisplayField objects.
 * 
 * @author Ted Sullivan
 */

public class ResultDisplaySchema extends DataList
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ResultDisplaySchema.class );
	public static final String NAME = "ResultDisplaySchema";
	
    private HashMap<String,ResultDisplayField> displayFields;
    
    public ResultDisplaySchema(  ) {  }
    
    public ResultDisplaySchema( DataObject dobj )
    {
    	// get the properties, create a display field
    	Iterator<IProperty> propIt = dobj.getProperties( );
    	while( propIt != null && propIt.hasNext() )
    	{
    		IProperty prop = propIt.next( );
    		addDisplayField( new ResultDisplayField( prop.getName( ) ));
    	}
    }
    
    public ResultDisplaySchema( List<String> displayFields )
    {
    	for ( String val : displayFields )
    	{
    		addDisplayField( new ResultDisplayField( val ) );
    	}
    }
    
    public ResultDisplaySchema( String[] displayFields )
    {
    	for ( int i = 0, isz = displayFields.length; i < isz; i++ )
    	{
    		addDisplayField( new ResultDisplayField( displayFields[i] ) );
    	}
    }
    
	@Override
	public String getName( )
	{
		return (name != null) ? super.getName( ) : NAME;
	}
	
    protected String getListName( )
    {
    	return "Fields";
    }
	
    public void addDisplayField( ResultDisplayField dispField )
    {
    	if (dispField == null) return;
    	
    	LOG.debug( "addDisplayField( " + dispField.getName( ) + " )" );
    	
		if (displayFields == null) displayFields = new HashMap<String,ResultDisplayField>( );
		displayFields.put( dispField.getName( ), dispField );
		super.addDataObject( dispField );
    }
    
    public String getIDField( )
    {
    	if ( displayFields == null ) return null;
    	
    	for (Iterator<ResultDisplayField> fieldIt = displayFields.values().iterator(); fieldIt.hasNext(); )
    	{
    		ResultDisplayField resDispField = fieldIt.next();
    		if ( resDispField.isIDField() )
    		{
    			return resDispField.getName( );
    		}
    	}
    	
    	return null;
    }

	@Override
	public void addDataObject( DataObject dObject )
	{
		if (dObject instanceof ResultDisplayField )
		{
			addDisplayField( ( ResultDisplayField )dObject );
		}
		else
		{
		    super.addDataObject( dObject );
		}
	}
	
	public ResultDisplayField getDisplayField( String name )
	{
		return (displayFields != null) ? displayFields.get( name ) : null;
	}
	
	public Iterator<String> getDisplayFields(  )
	{
		return (displayFields != null) ? displayFields.keySet().iterator() : null;
	}
}
