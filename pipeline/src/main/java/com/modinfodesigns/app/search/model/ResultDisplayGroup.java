package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A group of one or more Display Fields.  Used by the IFinder to determine which fields
 * to request from the search engine. Used by the UI to direct the display rendering.
 * 
 * @author Ted Sullivan
 */
public class ResultDisplayGroup extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ResultDisplayGroup.class );
    
	public static final String DISPLAY_FIELDS = "DisplayFields";
	public static final String RESULT_LIST_RENDERER = "ResultListRenderer";
	public static final String RESULT_RENDERER = "ResultRenderer";
	
	private ResultDisplaySchema displaySchema;
	
    private HashMap<String,ResultDisplayFieldProperties> dispPropsMap;
	
	public ResultDisplayGroup( ) { }
	
	public ResultDisplayGroup( DataObject theObject )
	{
	    setResultDisplaySchema( new ResultDisplaySchema( theObject ) );	
	}
	
    public void setResultDisplaySchema( ResultDisplaySchema displaySchema )
    {
    	this.displaySchema = displaySchema;
    }
	
    public void addDisplayField( ResultDisplayFieldProperties dispFieldProps )
    {
    	LOG.debug( "addDisplayField: " + dispFieldProps );
    	
    	StringListProperty dispFieldProp = (StringListProperty)getProperty( DISPLAY_FIELDS );
    	if (dispFieldProp == null)
    	{
    		dispFieldProp = new StringListProperty( DISPLAY_FIELDS );
    		doSetProperty( dispFieldProp );
    		
    		dispPropsMap = new HashMap<String,ResultDisplayFieldProperties>( );
    	}
    	
		dispFieldProp.addString( dispFieldProps.getName( ) );
		dispPropsMap.put( dispFieldProps.getName(), dispFieldProps );
		
		LOG.debug( "now have: " + dispFieldProp.getValue() );
    }
    
    public void setDisplayFields( StringListProperty dispProps )
    {
    	doSetProperty( dispProps );
    }
    
    public void setDisplayFields( String[] dispProps )
    {
    	doSetProperty( new StringListProperty( DISPLAY_FIELDS, dispProps ) );
    	
    }

    
	public ResultDisplayField getDisplayField( String name )
	{
		return (displaySchema != null) ? displaySchema.getDisplayField( name ) : null;
	}
	

	/**
	 * Returns a list of DisplayField objects selected for this group.
	 * 
	 * @return
	 */
	public List<ResultDisplayField> getDisplayFields(  )
	{
		LOG.debug( "getDisplayFields( )" );
		
		ArrayList<ResultDisplayField> dispFields = new ArrayList<ResultDisplayField>( );
		StringListProperty dispFieldProp = (StringListProperty)getProperty( DISPLAY_FIELDS );
		LOG.debug( "got display field property " + dispFieldProp );
		if (displaySchema != null )
		{
			if ( dispFieldProp == null)
			{
				List<ResultDisplayField> allFields = new ArrayList<ResultDisplayField>( );
				Iterator<String> fieldIt = displaySchema.getDisplayFields( );
				while( fieldIt != null && fieldIt.hasNext() )
				{
					allFields.add( displaySchema.getDisplayField( fieldIt.next( ) ) );
				}
				
				return allFields;
			}
			
			LOG.debug( "getting display fields " );
			
			Iterator<String> dispIt = dispFieldProp.iterator();
		    while ( dispIt != null && dispIt.hasNext() )
		    {
		    	// If display field prop == 'IDField' replace its name 
		    	// with the Schema's getIDField( )
		    	String propMapName = dispIt.next( );
		    	LOG.debug( "Got propMapName = " + propMapName );
		    	String propName;
		    	if (propMapName != null && propMapName.equals( ResultDisplayField.IS_ID_FIELD ))
		    	{
		    		propName = displaySchema.getIDField( );
		    	}
		    	else
		    	{
		    		propName = propMapName;
		    	}
		    	
		    	if (propName == null) continue;
		    	
		    	LOG.debug( "Getting display field for " + propName );
		    	
		    	ResultDisplayField df = displaySchema.getDisplayField( propName );
			    if (df != null)
			    {
			    	LOG.debug( "Adding result display field " + df.getValue( ) );
			    	// Copy this if there are any application-specific features that need
			    	// to be set...
			    	ResultDisplayField dfCopy = (ResultDisplayField)df.copy( );
			    	if (dispPropsMap != null && dispPropsMap.get(propMapName) != null)
			    	{
			    	    ResultDisplayFieldProperties dispProps = dispPropsMap.get( propMapName );
			    	    dfCopy.setDisplayFieldProperties( dispProps );
			    	}
			    	
			    	dispFields.add( dfCopy );
			    }
			    else
			    {
			    	ResultDisplayField rdf = new ResultDisplayField( );
			    	rdf.setName( propName );
			    	dispFields.add( rdf );
			    }
		    }
		}
		
		LOG.debug( "returning display fields: " + dispFields );
		return dispFields;
	}
	
	public void setResultListRenderer( String resultRendererName )
	{
		doSetProperty( new StringProperty( RESULT_LIST_RENDERER, resultRendererName ));
	}
	
	/**
	 * Returns the name of a ResultListRenderer implementation.
	 * 
	 * @return
	 */
	public String getResultListRenderer(  )
	{
		IProperty resRendProp = getProperty( RESULT_LIST_RENDERER );
		return (resRendProp != null) ? resRendProp.getValue() : null;	
	}
	
	public void setResultRenderer( String resultRendererName )
	{
		doSetProperty( new StringProperty( RESULT_RENDERER, resultRendererName ));
	}
	
	/**
	 * Returns the name of a ResultRenderer implementation.
	 * 
	 * @return
	 */
	public String getResultRenderer(  )
	{
		IProperty resRendProp = getProperty( RESULT_RENDERER );
		return (resRendProp != null) ? resRendProp.getValue() : null;	
	}

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		DataObjectSchema dos = new DataObjectSchema( );
		
		return dos;
	}
}
