package com.modinfodesigns.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.modinfodesigns.property.DataObject;

/**
 * Enables pre-processing of a query before submission to a Finder source. Can be chained with a
 * FilteredResultListFinder to provide both pre- and post-processing of Query and Query Results.
 * 
 * @author Ted Sullivan
 */

public class FilteredQueryFinder implements IFinder 
{
  private ArrayList<IQueryFilter> queryFilters;
    
  private IFinder proxyFinder;
    
  private String name;
    
  public void setFinder( IFinder finder )
  {
    this.proxyFinder = finder;
  }
    
  public void addQueryFilter( IQueryFilter queryFilter )
  {
    if (this.queryFilters == null) queryFilters = new ArrayList<IQueryFilter>( );
    queryFilters.add( queryFilter );
  }
    
  public void setName( String name )
  {
    this.name = name;
  }

  @Override
  public String getName()
  {
    return this.name;
  }

    
  @Override
  public IResultList executeQuery( IQuery query ) throws FinderException
  {
    if (proxyFinder == null || queryFilters == null)
    {
      throw new FinderException( "Configuration ERROR: Finder or Filter (or both) are null!" );
    }
		
    if (queryFilters != null)
    {
      for (int i = 0; i < queryFilters.size(); i++)
      {
        IQueryFilter filter = queryFilters.get( i );
        query = filter.filterQuery( query );
      }
    }
		
    return proxyFinder.executeQuery( query );
  }

  @Override
  public Set<String> getQueryFields()
  {
    return (proxyFinder != null) ? proxyFinder.getQueryFields(  ) : null;
  }

  @Override
  public Set<String> getNavigatorFields()
  {
    return (proxyFinder != null) ? proxyFinder.getNavigatorFields(  ) : null;
  }

  @Override
  public Set<String> getResultFields()
  {
    return (proxyFinder != null) ? proxyFinder.getResultFields(  ) : null;
  }

  @Override
  public Iterator<String> getFinderFields()
  {
    return (proxyFinder != null) ? proxyFinder.getFinderFields(  ) : null;
  }

  @Override
  public FinderField getFinderField(String name)
  {
    return (proxyFinder != null) ? proxyFinder.getFinderField( name ) : null;
  }

  @Override
  public DataObject getSingleResult(String resultID) throws FinderException
  {
    return (proxyFinder != null) ? proxyFinder.getSingleResult( resultID ) : null;
  }

  @Override
  public void setSourceFieldName(String sourceFieldName)
  {
		
  }

  @Override
  public FinderField getSourceField()
  {
    return null;
  }

}
