package com.modinfodesigns.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigatorField extends DataObject implements INavigatorField
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NavigatorField.class );
    
  private HashMap<String,NavigatorFacet> facets;
	
  public NavigatorField( ) {  }
	
  public NavigatorField( String name )
  {
    setName( name );
  }

  @Override
  public List<String> getFacetValues()
  {
    if (facets == null) return null;
		
    ArrayList<String> facetValues = new ArrayList<String>( );
    for (Iterator<String> facIt = facets.keySet().iterator(); facIt.hasNext(); )
    {
      facetValues.add( facIt.next() );
    }
        
    return facetValues;
  }
	
  @Override
  public List<String> getFacetValues( String sortBy )
  {
    LOG.debug( "getFacetValues( " + sortBy + " )" );
    if (sortBy != null && sortBy.equals( INavigatorField.SORT_BY_COUNT ))
    {
      return NavigatorFacet.sortFacetsByCount( this );
    }
    else if (sortBy != null && sortBy.equals( INavigatorField.SORT_BY_NAME ))
    {
      return NavigatorFacet.sortFacetsByName( this );
    }
		
    LOG.error( "SortBy " + sortBy + " is not recognized." );
    return getFacetValues( );
  }

  @Override
  public NavigatorFacet getFacet( String facetValue )
  {
    return (facets != null) ? facets.get( facetValue ) : null;
  }
	
  public void addNavigatorFacet( NavigatorFacet navFacet )
  {
    if (facets == null) facets = new HashMap<String,NavigatorFacet>( );
    facets.put( navFacet.getName( ), navFacet );
  }
	
  @Override
  public String getValue( )
  {
    return getValue( IProperty.JSON_FORMAT );
  }
	
  @Override
  public String getValue( String format )
  {
    if (format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "{ facets:[" );
      if (facets != null )
      {
        for (Iterator<NavigatorFacet> facIter = facets.values().iterator(); facIter.hasNext(); )
        {
          NavigatorFacet facet = facIter.next( );
          strbuilder.append( facet.getValue( IProperty.JSON_FORMAT ));
          if (facIter.hasNext()) strbuilder.append( "," );
        }
      }
			
      strbuilder.append( "]}" );
      return strbuilder.toString();
    }
    else if (format.equals( IProperty.XML_FORMAT))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "<NavigatorField name=\"" ).append( getName() ).append( "\">" );
      if (facets != null )
      {
        for (Iterator<NavigatorFacet> facIter = facets.values().iterator(); facIter.hasNext(); )
        {
          NavigatorFacet facet = facIter.next( );
          strbuilder.append( facet.getValue( IProperty.XML_FORMAT ));
        }
      }
        
      strbuilder.append( "</NavigatorField>" );
      return strbuilder.toString();
    }

    return super.getValue( format );
  }
}
