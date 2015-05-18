package com.modinfodesigns.search;

import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.security.IUserCredentials;

import java.util.List;

/**
 * Base interface for search query objects. A Query is a basic meta-data object (IPropertyHolder)
 * that contains several search-related properties: start record, page size and sort property.
 * 
 * @author Ted Sullivan
 */

// To do:
// Add method to set/get the "Collection(s)" that the query should go against. These will be
// picked up by the Finder implementation to generate scope queries or to
// select a collection view - or nothing if the "collection" concept is not
// relevant to the Finder implementation. 

public interface IQuery extends IPropertyHolder
{
  public static final String OR = "OR";
  public static final String AND = "AND";
	
  // need properties for pageSize, start record
  public void setPageSize( int pageSize );
  public int getPageSize( );
	
  public void setStartRecord( int startRecord );
  public int getStartRecord( );
	
  // Sort Order
  public void setSortProperty( SortProperty sortProp );
  public SortProperty getSortProperty( );
	
  public IUserCredentials getUserCredentials( );
	
  public String getCollectionName( );
  public void setCollectionName( String collectionName );
	
  public void setDefaultQueryField( QueryField defaultQuery );
  public QueryField getDefaultQueryField( );
    
  public List<QueryField> getQueryFields( );
  public QueryField getQueryField( String fieldName );
	
  public void setMultiFieldOperator( String fieldOperator );
  public String getMultiFieldOperator( );
	
  public void setMultiTermOperator( String multiTermOperator );
  public String getMultiTermOperator( );
	
  public void setNavigatorFields( List<String> navFields );
  public List<String> getNavigatorFields( );
	
  public void setDisplayFields( List<String> displayFields );
  public List<String> getDisplayFields( );
	
  public void setScopeQuery( IQuery scopeQuery );
  public IQuery getScopeQuery( );
	
  public void setRelevanceQuery( IQuery relevanceQuery );
  public IQuery getRelevanceQuery( );
	
  public QueryTree convertToQueryTree( );
}
