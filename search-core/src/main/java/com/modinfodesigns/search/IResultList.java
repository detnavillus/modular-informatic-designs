package com.modinfodesigns.search;

import com.modinfodesigns.property.IDataList;

/**
 * Base interface for data sets returned by an IFinder implementation.  A Result List is 
 * a generic data list with a few search-related attributes for paging, total results
 * available and faceted navigation.
 * 
 * @author Ted Sullivan
 */

public interface IResultList extends IDataList
{
  public static final String FINDER_NAME = "FinderName";
  public static final String QUERY_FIELD = "Query";
	
  public IQuery getQuery( );
    
  public void setQuery( IQuery query );
    
  /**
   * Returns an INavigator to enable Faceted Navigation of the global
   * Result list available from an IFinder
   * @return
  */
  public INavigator getNavigator( );
    
  public void setNavigator( INavigator navigator );
    
  public int getPageSize( );
  public int getStartRecord( );
    
  public int getTotalResults( );
  public void setTotalResults( int totalResults );
}
