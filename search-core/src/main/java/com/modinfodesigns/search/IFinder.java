package com.modinfodesigns.search;

import java.util.Set;
import java.util.Iterator;

import com.modinfodesigns.property.DataObject;

/**
 * Basic Searcher interface. Executes a query against some source (back-end or otherwise) and returns a 
 * result list.
 * 
 * @author Ted Sullivan
 */

// TO Do:
// Add special fields for:
//    Source
//    Category (catch all taxonomy for Adhoc Classifier implementations)


public interface IFinder
{
  public static final String FINDER_NAME = "FinderName";
	
  public static final String SOURCE_FIELD = "Source";
	
  public String getName( );
	
  /**
   * Executes a query against some data source, returns an IDataList as a result
   */
  public IResultList executeQuery( IQuery query ) throws FinderException;
    
    
  public DataObject getSingleResult( String resultID ) throws FinderException;
    
    
  /**
   * Returns a Set of all of the abstract field names that can be used as
   * field queries for this finder.
   * @return
   */
  public Set<String> getQueryFields( );
    
  /**
   * Returns the set of Navigator fields that can be used to refine the
   * search results.
   *
   * @return
   */
  public Set<String> getNavigatorFields( );
    
    
  /**
   * Returns the set of fields that can be returned with a search result.
   *
   * @return
   */
  public Set<String> getResultFields( );
    
    
  /**
   * Returns the list of Finder Fields defined for this finder implementation.
   *
   * @return
   */
  public Iterator<String> getFinderFields( );
    
    
  /**
   * Returns a specific Finder Field by application name  (abstract field name )
   *
   * @param name   Normalized or application name.
   * @return
   */
  public FinderField getFinderField( String name );
    
  public void setSourceFieldName( String sourceFieldName );
  public FinderField getSourceField( );
    

}
