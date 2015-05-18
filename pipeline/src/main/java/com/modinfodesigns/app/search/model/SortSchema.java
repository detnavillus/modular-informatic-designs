package com.modinfodesigns.app.search.model;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.search.SortProperty;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the set of sort choices for a search application.
 * 
 * Contains a list of SortProperty(s) which in turn specify a sort order.
 * 
 * @author Ted Sullivan
 */
public class SortSchema extends DataObject
{
	public static final String NAME = "SortSchema";
	public static final String SORT_PROPERTY_LIST = "SortPropertyList";
	
	@Override
	public String getName( )
	{
		return NAME;
	}
	
	@Override
	public void setName( String name )
	{
        // makes name Read Only
	}
	
	public void setFieldName( String fieldName )
	{
		setProperty( new StringProperty( "SortField", fieldName ) );
	}
	
	public String getFieldName( )
	{
		IProperty fieldProp = getProperty( "SortField" );
		return (fieldProp != null) ? fieldProp.getValue( ) : null;
	}
	
	public void addSortProperty( SortProperty sortProp)
	{
		PropertyList spList = (PropertyList)getProperty( SORT_PROPERTY_LIST );
		if (spList == null)
		{
			spList = new PropertyList( );
			spList.setName( SORT_PROPERTY_LIST );
			addProperty( spList );
		}
		
		spList.addProperty( sortProp );
	}
	
	
	public SortProperty getSortProperty( String propName )
	{
		PropertyList spList = (PropertyList)getProperty( SORT_PROPERTY_LIST );
		if (spList == null) return null;
		for (Iterator<IProperty> propIt = spList.getProperties(); propIt.hasNext(); )
		{
			SortProperty sortProp = (SortProperty)propIt.next();
			if (sortProp.getName() != null && sortProp.getName().equals( propName ))
			{
				return sortProp;
			}
		}
		
		return null;
	}
	
	public List<String> getSortPropertyNames( )
	{
		ArrayList<String> propNames = new ArrayList<String>( );
		PropertyList spList = (PropertyList)getProperty( SORT_PROPERTY_LIST );
		Iterator<IProperty> propIt = spList.getProperties( );
		while (propIt != null && propIt.hasNext( ) )
		{
			IProperty prop = propIt.next( );
			propNames.add( prop.getName( ) );
		}
		
		return propNames;
	}
	
}
