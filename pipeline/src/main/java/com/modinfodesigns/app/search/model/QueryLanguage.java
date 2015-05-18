package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;

public class QueryLanguage extends DataObjectBean
{
    
    // query language
	public static final String QUERY_LANGUAGE = "QueryLanguage";
	public static final String QUERY_PARSER_CLASS = "QueryParserClass";
	public static final String DISPLAY_NAME = "DisplayName";
	
	/**
	 * Sets the Query Language code - e.g. 'SQL', 'FQL' etc.
	 * @param queryLanguage
	 */
	public void setQueryLanguage( String queryLanguage )
	{
		StringProperty qlProp = new StringProperty( QUERY_LANGUAGE, queryLanguage );
		addProperty( qlProp );
	}
	
	public void setDisplayName( String displayName )
	{
		StringProperty dispProp = new StringProperty( DISPLAY_NAME, displayName );
		addProperty( dispProp );	
	}
	
	public void setQueryParserClass( String queryParserClass )
	{
		StringProperty qParserProp = new StringProperty( QUERY_PARSER_CLASS, queryParserClass );
		addProperty( qParserProp );
	}

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		return null;
	}

}
