package com.modinfodesigns.pipeline.process;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.property.compare.IPropertyHolderMatcher;
import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.search.IResultListFilter;
import com.modinfodesigns.search.IResultList;
import com.modinfodesigns.search.BasicResultList;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filters a DataList using one or more IPropertyHolderMatcher(s). Removes items
 * from the DataList that do not match the criteria represented by the set of
 * Matchers. Can be set to use AND or OR logic.
 * 
 * Passthrough modes:
 * FILTER   remove data objects from the main processing chain that do not
 *          meet the criteria.
 * PROXY  - Pass filtered data to proxyObjectProcessor do not
 *                remove data from the main processing chain.
 *          This mode is set of one or more proxy IDataObjectProcessor(s) are added 
 *          
 * @author Ted Sullivan
 */

public class DataListFilter implements IDataObjectProcessor, IResultListFilter
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataListFilter.class );

  private ArrayList<IPropertyHolderMatcher> propertyHolderMatchers;
    
  private ArrayList<IDataObjectProcessor> proxyDataProcessors;
    
  private boolean isAnd = true;  // for an OR filter - set isAnd == false
    
  // mode - FILTER - remove data from the main processing chain
  //        PROXY  - Pass filtered data to proxyObjectProcessor do not
  //                 remove data from the main processing chain.
  private boolean filterMode = true;
    
  public void addDataMatcher( IPropertyHolderMatcher propHolderMatcher )
  {
    if (propertyHolderMatchers == null) propertyHolderMatchers = new ArrayList<IPropertyHolderMatcher>( );
    propertyHolderMatchers.add( propHolderMatcher );
  }
    
  public void addDataProcessor( IDataObjectProcessor proxyDataProcessor )
  {
    if (proxyDataProcessors == null) proxyDataProcessors = new ArrayList<IDataObjectProcessor>( );
    proxyDataProcessors.add( proxyDataProcessor );
    filterMode = false;
  }
    
  public void setIsAnd( boolean isAnd )
  {
    this.isAnd = isAnd;
  }
    
  public void setMode( String mode )
  {
    if (mode != null && mode.equalsIgnoreCase( "AND" ))
    {
      isAnd = true;
    }
    else if (mode != null && mode.equalsIgnoreCase( "OR" ))
    {
      isAnd = false;
    }
    else
    {
      LOG.error( "Mode must be either 'AND' or 'OR'! - not changing mode" );
    }
  }
    
  @Override
  public IDataList processDataList( IDataList data )
  {
    LOG.debug( "processDataList " + ((data != null) ? data.size() : 0) );
    IDataList processList = (filterMode) ? data : ((data instanceof IResultList) ? new BasicResultList( ) : new DataList( ));
    if (!filterMode)
    {
      for (Iterator<String> propIt = data.getPropertyNames( ); propIt.hasNext(); )
      {
        String listProp = propIt.next();
                
        IProperty prop = data.getProperty( listProp );
        LOG.debug( "Copying property: " + listProp + " = " + prop.getValue( ) );
        processList.addProperty( prop.copy( ) );
      }
    }
		
    ArrayList<Integer> toRemove = (filterMode) ? new ArrayList<Integer>( ) : null;
		
    for (int i = 0; i < data.size(); i++)
    {
      DataObject dobj = data.item( i );
			
      if (matches( dobj ))
      {
        if (!filterMode)
        {
          processList.addDataObject( dobj );
        }
      }
      else
      {
        if (filterMode)
        {
          toRemove.add( new Integer( i ));
        }
      }
    }
		
    if (toRemove != null)
    {
      for (int i = 0; i < toRemove.size(); i++)
      {
        Integer removeAt = toRemove.get( i );
        data.removeDataObject( removeAt.intValue( ) );
      }
    }
		
    if (proxyDataProcessors != null)
    {
      for (int i = 0; i < proxyDataProcessors.size(); i++)
      {
        IDataObjectProcessor dobjProc = proxyDataProcessors.get( i );
        dobjProc.processDataList( processList );
      }
			
      // copy properties from the DataList that are not DataObjects!!!
    }
		
    LOG.debug( "Returning data with " + data.size() + " objects." );
    return data;
  }

  private boolean matches( DataObject dobj )
  {
    if (propertyHolderMatchers == null) return true;
		
    IUserCredentials user = getUserCredentials( );
		
    for (int i = 0; i < propertyHolderMatchers.size(); i++)
    {
      IPropertyHolderMatcher propMatcher = propertyHolderMatchers.get( i );
      boolean matches = propMatcher.equals( user, dobj );
      if (matches != isAnd)
      {
        return matches;
      }
    }
		
    return isAnd;
  }
	
  private IUserCredentials getUserCredentials(  )
  {
    return null;
  }
	
	
  @Override
  public void processComplete( IPropertyHolder result, boolean status)
  {

  }

  @Override
  public IResultList processResultList( IResultList data )
  {
    int dataSize = data.size( );
    IResultList newList = (IResultList)processDataList( data );
    int newSize = newList.size();
		
    int removed = dataSize - newSize;
		
    newList.setQuery( data.getQuery( ) );
    newList.setTotalResults( data.getTotalResults( ) - removed );
    newList.setNavigator( data.getNavigator( ) );
		
    return newList;
  }
}
