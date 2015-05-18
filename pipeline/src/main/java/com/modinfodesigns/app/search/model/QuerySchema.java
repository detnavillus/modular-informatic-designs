package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerListProperty;

import com.modinfodesigns.property.schema.ICreateDataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchema;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains a search request data model - fields and field characteristics.
 * 
 * Each field has a Name and a Type. Valid types include string, number (integer or floating),
 * range, date, date range, geocode, choice list (multi-select or single-select).
 * 
 * Fields are represented by the QueryField object.  
 * 
 * Schema Properties
 *     Query Mode
 *     Sort Schema
 *     Query Language Support
 *     Page Size Parameter
 *     Page Size Choices
 *     Page Number Parameter
 *     Http Request Method
 * 
 * @author Ted Sullivan
 */

// QuerySchema extends DataList in order to benefit from the serialization options with 
// DataObjects

public class QuerySchema extends DataList implements ICreateDataObjectSchema
{
  private transient static final Logger LOG = LoggerFactory.getLogger( QuerySchema.class );
	public static final String NAME = "QuerySchema";
	
	public static final String PAGE_SIZE  = "PageSize";
	public static final String PAGE_SIZES = "PageSizes";
	public static final String PAGE_NUM   = "PageNum";
	
	public static final String DefaultPageNumParam = "pg";
	public static final String DefaultPageSizeParam = "psz";
	
	private HashMap<String,QueryFieldDefinition> queryFields;
	
	public QuerySchema(  )
	{
		setName( NAME );
	}
	
	@Override
	public String getName( )
	{
		return (name != null) ? super.getName( ) : NAME;
	}
	
	@Override
    protected String getListName( )
    {
    	return "Fields";
    }
	
	public void addQueryField( QueryFieldDefinition qField )
	{
		LOG.debug( "addQueryField " + qField.getName() );
		if (queryFields == null) queryFields = new HashMap<String,QueryFieldDefinition>( );

		queryFields.put( qField.getName( ), qField );
		super.addDataObject( qField );
	}
	
	@Override
	public void addDataObject( DataObject dObject )
	{
		if (dObject == null) return;
		
		// LOG.debug( "addDataObject( )..." );
		if (dObject instanceof QueryFieldDefinition )
		{
			addQueryField( (QueryFieldDefinition)dObject );
		}
		else
		{
		    super.addDataObject( dObject );
		}
	}
	
	/**
	 * Returns a QueryFieldDefinition for the field named, null if this QuerySchema does not
	 * contain the a field with the requested name.
	 * 
	 * @param name
	 * @return
	 */
	public QueryFieldDefinition getQueryField( String name )
	{
		return (queryFields != null) ? queryFields.get( name ) : null;
	}
	
	public Iterator<String> getQueryFields(  )
	{
		return (queryFields != null) ? queryFields.keySet().iterator() : null;
	}
	
	/**
	 * Returns true if this QuerySchema contains the named field, false otherwise 
	 * 
	 * @param queryField
	 * @return
	 */
	public boolean hasField( String queryField )
	{
		return (queryFields != null && queryFields.get( queryField ) != null);
	}
	
	public void setQueryLanguageSupport( QueryLanguageSupport queryLanguageSupport )
	{
		if (queryLanguageSupport == null) return;
		
		addProperty( queryLanguageSupport );
	}

	public QueryLanguageSupport getQueryLanguageSupport( )
	{
		IProperty qlSupportProp = getProperty( QueryLanguageSupport.NAME );
		if (qlSupportProp != null && qlSupportProp instanceof QueryLanguageSupport )
		{
			return (QueryLanguageSupport)qlSupportProp;
		}
		
		return null;
	}
	
	/**
	 * Sets the request parameter used to set the result page size.
	 * 
	 * @param pageSizeParam
	 */
	public void setPageSizeParameter( String pageSizeParam )
	{
		LOG.debug( "setPageSizeParameter '" + pageSizeParam + "'" );
		StringProperty pageSizeProp = new StringProperty( PAGE_SIZE, pageSizeParam );
		super.setProperty( pageSizeProp );
	}
	
	/**
	 * Returns the request parameter used to set the paging size.
	 * 
	 * @return
	 */
	public String getPageSizeParameter(  )
	{
		IProperty pageSizeProp = getProperty( PAGE_SIZE );
		String pageSizeParam = (pageSizeProp != null) ? pageSizeProp.getValue() : DefaultPageSizeParam;
		return pageSizeParam;
	}
	
	/**
	 * Sets available page sizes
	 * 
	 * @param pageSizeChoices
	 */
	public void setPageSizeChoices( int[] pageSizeChoices )
	{
		IntegerListProperty pList = new IntegerListProperty( PAGE_SIZES, pageSizeChoices );
		super.setProperty( pList );
	}
	
	public int[] getPageSizeChoices(  )
	{
		IntegerListProperty pList =  (IntegerListProperty)getProperty( PAGE_SIZES );
		
		return (pList != null) ? pList.getIntegers( ) : null;
	}
	
	/**
	 * Sets the request parameter used to set the result page number
	 * 
	 * @param pageNumParam
	 */
	public void setPageNumParameter( String pageNumParam )
	{
		StringProperty pageNumProp = new StringProperty( PAGE_NUM, pageNumParam );
		super.setProperty( pageNumProp );
	}
	
	/**
	 * Returns the request parameter used to set the result page number
	 * @return
	 */
	public String getPageNumParameter(  )
	{
		IProperty pageNumProp = getProperty( PAGE_NUM );
		return (pageNumProp != null) ? pageNumProp.getValue() : DefaultPageNumParam;
	}

	public void setSortSchema( SortSchema sortSchema )
	{
		setProperty( sortSchema );
	}
	
	public SortSchema getSortSchema(  )
	{
		return (SortSchema)getProperty( SortSchema.NAME );
	}
	
	@Override
	public void setProperty( IProperty prop )
	{
		if (prop.getName().equals( PAGE_SIZE ) && prop instanceof StringProperty )
		{
			StringProperty pageSizeProp = (StringProperty)prop;
			setPageSizeParameter( pageSizeProp.getValue( ) );
		}
		else if (prop.getName().equals( PAGE_NUM ) && prop instanceof StringProperty )
		{
			StringProperty pageNumProp = (StringProperty)prop;
			setPageNumParameter( pageNumProp.getValue( ) );
		}
		else if (prop.getName().equals( PAGE_SIZES ) && prop instanceof IntegerListProperty )
		{
			IntegerListProperty pageSizes = (IntegerListProperty)prop;
			int[] pageSizeChoices = pageSizes.getIntegers( );
			setPageSizeChoices( pageSizeChoices );
		}
		else
		{
			super.setProperty( prop );
		}
	}
	
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		DataObjectSchema dos = new DataObjectSchema( );
		dos.setName( "QuerySchema" );
		
		return dos;
	}
}
