package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;

import java.util.List;
import java.util.ArrayList;

/**
 * Defines the free text query mode. This enables users to select from a set of 'modes' to apply
 * advanced query operators without having to write a complex Query language ('xQL') string (although
 * this can be accommodated if required - see below).
 * 
 * A Query Mode Schema contains a set of QueryMode objects that describe the mode choices ("all words",
 * "any words", "exact phrase", "near", etc). A given query mode may have additional modifiers
 * (distance, order, edit distance) etc. that the user interface may need to manage.
 * 
 *  A query mode may define a QL that the user can choose to enter advanced syntax. The controller
 *  layer can provide syntax checkers so that the user can get more informative feedback than "Your query
 *  returned 0 results".
 * 
 * Typically ALL | ANY | PHRASE | FUZZY | ONEAR | NEAR | PREFIX
 * 
 * @author Ted Sullivan
 */

public class QueryModeSchema extends DataList
{
	public static final String NAME = "QueryModeSchema";
	
	public static final String ALL          = "ALL";
	public static final String ANY          = "ANY";
	public static final String EXACT        = "EXACT";
	public static final String NEAR         = "NEAR";
	public static final String ORDERED_NEAR = "ONEAR";
	public static final String PREFIX       = "PREFIX";
	public static final String BOOST        = "BOOST"; // used for Query Field Boosting
	
	public static final String BOOLEAN_SCHEMA = "BooleanSchema";
	public static final String RANGE_SCHEMA   = "RangeSchema";
	public static final String BOOLEAN_PROX_SCHEMA = "BooleanProximitySchema";
	public static final String INITIALIZE_FROM = "InitializeFrom";
	
	public static final String LT = "LT";
	public static final String LTE = "LTE";
	public static final String EQ = "EQ";
	public static final String GT = "GT";
	public static final String GTE = "GTE";
	
	private String queryModeField = "queryMode";
	private String initializeFrom;
	
	public QueryModeSchema( ) {  }
	
	public QueryModeSchema( List<QueryMode> queryModes )
	{
		for (int i = 0; i < queryModes.size(); i++)
		{
			addQueryMode( queryModes.get( i ) );
		}
	}
	
	
	public void addQueryMode( QueryMode queryMode )
	{
		super.addDataObject( queryMode );
	}
	
	public List<QueryMode> getQueryModes(  )
	{
		ArrayList<QueryMode> qModes = new ArrayList<QueryMode>( );
		
		List<DataObject> dobjs = super.getDataList( );
		if (dobjs != null && dobjs.size() > 0)
		{
			for (int i = 0; i < dobjs.size(); i++)
			{
				qModes.add( (QueryMode)dobjs.get( i ));
			}
		}
		
		return qModes;
	}
	
	/**
	 * Ensure that only QueryMode objects get added as List objects
	 * @param dobj
	 */
	@Override
	public void addDataObject( DataObject dobj )
	{
		if (dobj instanceof QueryMode )
		{
			addQueryMode( (QueryMode)dobj );
		}
		else
		{
			super.addProperty( dobj );
		}
	}
	
	
	@Override
	public String getName( )
	{
		return NAME;
	}
	
	@Override
	public void setName( String name )
	{

	}
	
	/**
	 * Defines the request field that will contain the users Query Mode selection
	 * 
	 * @param queryModeField
	 */
	public void setQueryModeField( String queryModeField )
	{
		this.queryModeField = queryModeField;
	}
	
	public String getQueryModeField(  )
	{
		return this.queryModeField;
	}
	
	public void setInitializeFrom( String initializeFrom )
	{
		this.initializeFrom = initializeFrom;
	}
	
	public String getInitializeFrom(  )
	{
		return this.initializeFrom;
	}
	

	public QueryMode getSelectedMode( String queryOperator )
	{
		List<QueryMode> qModes = getQueryModes(  );
		
		if (qModes == null) return null;
		
		for (int i = 0; i < qModes.size(); i++)
		{
			QueryMode qMode = qModes.get( i );
			String qModeOperator = qMode.getQueryOperator();
			
			if (qModeOperator != null && qModeOperator.equals( queryOperator ))
			{
				return qMode;
			}
		}
		
		return null;
	}
	
	public static QueryModeSchema createQueryModeSchema( String type )
	{
		if (type.equals( BOOLEAN_SCHEMA ))
		{
			return createBooleanSchema( );
		}
		else if (type.equals( RANGE_SCHEMA ))
		{
			return createRangeSchema( );
		}
		else if (type.equals( BOOLEAN_PROX_SCHEMA ))
		{
			return createBooleanSchemaWithProximity( );
		}
		
		return null;
	}
	
	public static QueryModeSchema createBooleanSchema( )
	{
		ArrayList<QueryMode> booleanModes = new ArrayList<QueryMode>( );
		booleanModes.add( new QueryMode( ANY,   "Any of these words" ));
		booleanModes.add( new QueryMode( ALL,   "All of these words" ));
		booleanModes.add( new QueryMode( EXACT, "Exact match" ));
		
		return new QueryModeSchema( booleanModes );
	}
	
	public static QueryModeSchema createBooleanSchemaWithProximity( )
	{
		return new QueryModeSchema( );
	}
	
	public static QueryModeSchema createRangeSchema( )
	{
		ArrayList<QueryMode> rangeModes = new ArrayList<QueryMode>( );
		rangeModes.add( new QueryMode( LT,   "Less Than" ));
		rangeModes.add( new QueryMode( LTE,   "Less Than or Equal" ));
		rangeModes.add( new QueryMode( EQ,   "Equal To" ));
		rangeModes.add( new QueryMode( GTE,   "Greater Than or Equal" ));
		rangeModes.add( new QueryMode( GT,   "Greater Than" ));
		
		return new QueryModeSchema( rangeModes );
	}
}
