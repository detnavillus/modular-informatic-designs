package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

/**
 * Provides a list of supported query languages.  UI can provide advanced users a choice of QL 
 * syntax that they can use.  The Query Transform layer can then choose the appropriate Query Parser
 * for the user's selected Query Language.
 * 
 * @author Ted Sullivan
 */

public class QueryLanguageSupport extends DataList
{
	public static final String QUERY_LANGUAGE  = "QueryLanguageParameter";
	public static final String NAME = "QueryLanguages";
	
	@Override
    protected String getListName( )
    {
    	return NAME;
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

    public void setQueryLanguageParameter( String queryLanguageParameter )
    {
    	StringProperty qlProp = new StringProperty( QUERY_LANGUAGE, queryLanguageParameter );
    	addProperty( qlProp );
    }

	public String getQueryLanguageParameter(  )
	{
		IProperty qlProp = getProperty( QUERY_LANGUAGE );
		return (qlProp != null) ? qlProp.getValue() : null;
	}
	
	public void addQueryLanguage( QueryLanguage queryLang )
	{
		super.addDataObject( queryLang );
	}
	
	@Override
	public void addDataObject( DataObject dobj )
	{
		if (dobj instanceof QueryLanguage )
		{
			super.addDataObject( dobj );
		}
	}
}
