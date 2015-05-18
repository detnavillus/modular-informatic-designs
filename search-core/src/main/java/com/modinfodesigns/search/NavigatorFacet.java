package com.modinfodesigns.search;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class NavigatorFacet extends IntegerProperty
{
  public NavigatorFacet(  ) {  }
	
  public NavigatorFacet( String name, int count )
  {
    super( name, count );
  }
	
  @Override
  public String getValue( )
  {
    return Integer.toString( getIntegerValue( ) );
  }
	
  @Override
  public String getValue( String format )
  {
    if (format.equals( IProperty.JSON_FORMAT))
    {
      return "{name:\"" + getName() + "\", count:" + Integer.toString( getIntegerValue() ) + "}";
    }
    else if (format.equals( IProperty.JSON_VALUE))
    {
      return "name:\"" + getName() + "\", count:" + Integer.toString( getIntegerValue() );
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
      return "<Facet name=\"" + getName() + "\" count=\""
			                        + Integer.toString( getIntegerValue() ) + "\"/>";
    }
		
    return super.getValue( format );
  }
	
  public static List<String> sortFacetsByCount( INavigatorField navField )
  {
    ArrayList<NavigatorFacet> facetCounts = new ArrayList<NavigatorFacet>( );
    List<String> values = navField.getFacetValues( );
    if (values != null)
    {
      for( int i = 0; i < values.size(); i++ )
      {
        NavigatorFacet fc = navField.getFacet( values.get( i ) );
        if (fc != null) facetCounts.add( fc );
      }

      Collections.sort( facetCounts );
        
      ArrayList<String> facetList = new ArrayList<String>( );
      for (int i = 0; i < facetCounts.size( ); i++)
      {
        NavigatorFacet fc= (NavigatorFacet)facetCounts.get( i );
        facetList.add( fc.getName() );
      }
            
      return facetList;
    }
        
    return null;
  }
	
  public static List<String> sortFacetsByName( INavigatorField navFacet )
  {
    ArrayList<String> facetNames = new ArrayList<String>( );
    List<String> values = navFacet.getFacetValues( );
    if (values != null)
    {
      facetNames.addAll( values );
      Collections.sort( facetNames );
      return facetNames;
    }
        
    return null;
  }
}
