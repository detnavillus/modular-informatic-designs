package com.modinfodesigns.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.app.ApplicationManager;

/**
 * Finder implementation that enables post-processing of a Result List using
 * one or more IResultListFilter implementations.
 * 
 * @author Ted Sullivan
 *
 */
public class FilteredResultListFinder implements IFinder
{
  private ArrayList<IResultListFilter> resultListFilters;
    
  private IFinder proxyFinder;
    
  private String proxyFinderName;
    
  private String name;
    
  public void setFinder( IFinder proxyFinder )
  {
    this.proxyFinder = proxyFinder;
  }
    
  public void setFinderName( String finderName )
  {
    this.proxyFinderName = finderName;
  }
    
  public void addResultListFilter( IResultListFilter resultListFilter )
  {
    if (this.resultListFilters == null) resultListFilters = new ArrayList<IResultListFilter>( );
    this.resultListFilters.add( resultListFilter );
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
    IFinder theProxy = getProxyFinder( );
		
    if (theProxy == null || resultListFilters == null)
    {
      throw new FinderException( "Configuration ERROR: Finder or Filter (or both) is null!" );
    }
		
    IResultList resList = theProxy.executeQuery( query );
		
    for (int i = 0; i < resultListFilters.size(); i++)
    {
      IResultListFilter resultListFilter = resultListFilters.get( i );
      resList = resultListFilter.processResultList( resList );
    }
		
    return resList;
  }
	
  private IFinder getProxyFinder(  )
  {
    if (this.proxyFinder != null) return proxyFinder;
		
    if (proxyFinderName != null)
    {
      this.proxyFinder = (IFinder)ApplicationManager.getInstance( ).getApplicationObject( proxyFinderName, "Finder" );
    }
		
    return this.proxyFinder;
  }


  @Override
  public Set<String> getQueryFields()
  {
    IFinder theProxy = getProxyFinder( );
		
    return (theProxy != null) ? theProxy.getQueryFields( ) : null;
  }

  @Override
  public Set<String> getNavigatorFields()
  {
    IFinder theProxy = getProxyFinder( );
    return (theProxy != null) ? theProxy.getNavigatorFields( ) : null;
  }

  @Override
  public Set<String> getResultFields()
  {
    IFinder theProxy = getProxyFinder( );
    return (theProxy != null) ? theProxy.getResultFields( ) : null;
  }

  @Override
  public Iterator<String> getFinderFields()
  {
    IFinder theProxy = getProxyFinder( );
    return (theProxy != null) ? theProxy.getFinderFields( ) : null;
  }

  @Override
  public FinderField getFinderField(String name)
  {
    IFinder theProxy = getProxyFinder( );
    return (theProxy != null) ? theProxy.getFinderField( name ) : null;
  }

  @Override
  public DataObject getSingleResult(String resultID) throws FinderException
  {
    IFinder theProxy = getProxyFinder( );
    return (theProxy != null) ? theProxy.getSingleResult( resultID ) : null;
  }

  @Override
  public void setSourceFieldName( String sourceFieldName )
  {
		
  }

  @Override
  public FinderField getSourceField()
  {
    return null;
  }

}
