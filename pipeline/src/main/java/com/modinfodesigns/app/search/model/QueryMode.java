package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a specific Mode used in a QueryModeSchema.  A QueryMode object describes a specific
 * query operation to be performed on a set of terms entered by the user, for example "all words", 
 * "any words", "exact phrase", "near", etc. These are display names that describe a specific 
 * query Operator such as ALL, ANY, EXACT, NEAR, etc. that will be used by the controller layer to
 * create a structured query from the user's input.
 * 
 * Some operators require modifiers that refine how the operator works.  For example, proximity
 * operators such as 'NEAR' require a distance or 'Slop' operator that define how near the
 * terms need to be to elicit a match. Modifier Definitions can be added that will describe these fields
 * for the presentation layer and for the controller query formatting layer.
 * 
 * @author Ted Sullivan
 */
public class QueryMode extends DataList 
{
	public static final String QUERY_OPERATOR = "QueryOperator";
	public static final String DISPLAY_NAME = "DisplayName";
	public static final String MODIFIERS    = "Modifiers";
	
   // Fields Operator - 
   // Display Field
	
	public QueryMode( ) {  }
	
	public QueryMode( String queryOperator, String displayName )
	{
		setQueryOperator( queryOperator );
		setDisplayName( displayName );
	}
	
	public QueryMode( String queryOperator, String displayName, List<QueryModeModifier> modifiers )
	{
		setQueryOperator( queryOperator );
		setDisplayName( displayName );
		
		for (int i = 0; i < modifiers.size(); i++)
		{
			addQueryModeModifier( modifiers.get( i ) );
		}
	}
	
	public void setQueryOperator( String queryOperator )
	{
		setProperty( new StringProperty( QUERY_OPERATOR, queryOperator ) );
	}
	
	public String getQueryOperator( )
	{
		IProperty queryProp = getProperty( QUERY_OPERATOR );
		return (queryProp != null) ? queryProp.getValue() : null;
	}
	
	public void setDisplayName( String displayName )
	{
		setProperty( new StringProperty( DISPLAY_NAME, displayName ) );
	}
	
	public String getDisplayName( )
	{
		IProperty displayProp = getProperty( DISPLAY_NAME );
		return (displayProp != null) ? displayProp.getValue() : null;
	}
	
	// add Modifiers ...
	public void addQueryModeModifier( QueryModeModifier modField )
	{
		super.addDataObject( modField );
	}
	
	@Override
	public void addDataObject( DataObject dobj )
	{
		if (dobj instanceof QueryFieldDefinition )
		{
			super.addDataObject( dobj );
		}
	}
	
	public List<QueryModeModifier> getQueryModeModifiers( )
	{
		ArrayList<QueryModeModifier> modFields = new ArrayList<QueryModeModifier>( );
		
		List<DataObject> dobjs = super.getDataList( );
		if (dobjs != null && dobjs.size() > 0)
		{
			for (int i = 0; i < dobjs.size(); i++)
			{
				modFields.add( (QueryModeModifier)dobjs.get( i ));
			}
		}
		
		return modFields;
	}

}
