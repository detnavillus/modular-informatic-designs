package com.modinfodesigns.search;

import com.modinfodesigns.property.DataList;

/**
 * Basic implementation of an IResultList. 
 *
 * @author Ted Sullivan
 */

public class BasicResultList extends DataList implements IResultList
{
  private int totalResults = 0;
	
  private IQuery query;
	
  private INavigator navigator;

  @Override
  public INavigator getNavigator()
  {
    return this.navigator;
  }

  @Override
  public void setNavigator( INavigator navigator )
  {
    this.navigator = navigator;
  }
	
	
  @Override
  public int getTotalResults()
  {
    return totalResults;
  }

  @Override
  public void setTotalResults( int totalResults)
  {
    this.totalResults = totalResults;
  }

  @Override
  public int getPageSize()
  {
    return (query != null) ? query.getPageSize() : 0;
  }

  @Override
  public int getStartRecord()
  {
    return (query != null) ? query.getStartRecord( ) : 0;
  }

  @Override
  public void setQuery( IQuery query )
  {
    this.query = query;
  }
	
  @Override
  public IQuery getQuery()
  {
    return this.query;
  }

}
