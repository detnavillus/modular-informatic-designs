package com.modinfodesigns.search;

import com.modinfodesigns.property.IProperty;
import java.util.List;

public interface INavigatorField extends IProperty
{
  public static final String SORT_BY_NAME  = "Name";
  public static final String SORT_BY_COUNT = "Count";
	
  /**
   * Returns a list of displayable Values
   * @return
   */
  public List<String> getFacetValues( );
    
  /**
   * Returns a list of facet values sorted by NAME or COUNT
   * @param sortBy
   * @return
   */
  public List<String> getFacetValues( String sortBy );

  public NavigatorFacet getFacet( String facetValue );
    
}
