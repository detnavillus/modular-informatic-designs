package com.modinfodesigns.search;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses a proxy IFinder to execute the query. Removes duplicate results from the set of the results 
 * returned to the client. Keeps an internal cache of records so that the incoming query page and 
 * page size parameters can be honored. The total number of records in the result set will change 
 * as duplicates are found and eliminated.
 * 
 * @author Ted Sullivan
 */
public class DedupingFinder implements IFinder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DedupingFinder.class );

  private IFinder proxyFinder;
    
  private ArrayList<String> dedupeProps;
    
  private ArrayList<String> queryProps;
    
  private ObjectCache objCache;
    
  private String lastQueryKey;
    
  private String name;
    
  public void setFinder( IFinder finder )
  {
    this.proxyFinder = finder;
  }
    
  public void addDedupeProperty( String dedupeProp )
  {
    if (dedupeProps == null) dedupeProps = new ArrayList<String>( );
    dedupeProps.add( dedupeProp );
  }
    
  public void addQueryKeyProperty( String queryKeyProp )
  {
    if ( queryProps == null ) queryProps = new ArrayList<String>( );
    queryProps.add( queryKeyProp );
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
    if (proxyFinder == null)
    {
      throw new FinderException( "Proxy Finder is NULL!" );
    }
		
    LOG.debug( "executeQuery( )..." );

    // see where we are in the cache. If needed get some more results (may increase page size
    // to avoid repeated calls to executeQuery on the proxy(s).
    if (cacheHasResults( query ))
    {
      return getCachedResults( query );
    }
    else
    {
      LOG.debug( "Updating cache..." );

      // need to loop here until we have all the results OR we have enough to fulfill the request...
      boolean done = false;
      int queryStartRec = query.getStartRecord( );
      int queryPageSize = query.getPageSize( );
	        
      while( !done )
      {
        objCache.lastStartRec = (objCache.lastStartRec < 0) ? queryStartRec : objCache.lastStartRec + queryPageSize;
                
        LOG.debug( "DedupeingFilter getting docs from " + objCache.lastStartRec + " to " + (objCache.lastStartRec + queryPageSize) );

        query.setStartRecord( objCache.lastStartRec );
        IResultList newResults = proxyFinder.executeQuery( query );
        LOG.debug( "Got newResults: " + newResults.getValue() + "\r\n total Results = " + newResults.getTotalResults( ) );
        query.setStartRecord( queryStartRec );
	            
        cacheResults( query, newResults );
        done = cacheIsReady( query );

        if (newResults.size( ) == 0)
        {
          LOG.debug( "No more results!" );
          break;
        }
      }
    }
	    
    LOG.debug( "Returning cached results ..." );
    return getCachedResults( query );
  }


  private void cacheResults( IQuery query, IResultList resList )
  {
    LOG.debug( "cacheResults( )..." );
        
    objCache.setSourceTotalRecords( resList.getTotalResults( ) );
    LOG.debug( "source totals = " + objCache.sourceTotalRecs );

    for (int i = 0; i < resList.size(); i++)
    {
      DataObject dobj = resList.item( i );

      String dedupeKey = "";
      if (dedupeProps != null)
      {
        LOG.debug( "Adding " + dedupeProps.size( ) + " dedupe fields." );

        for (int f = 0; f < dedupeProps.size( ); f++)
        {
          String field = dedupeProps.get( f );
          LOG.debug( "Adding field = '" + field + "'" );

          IProperty value = dobj.getProperty( field );
          if (value != null)
          {
            dedupeKey += field + "=" + value.getValue( );
          }
          else
          {
            LOG.debug( "DataObject has no value for '" + field + "'" );
          }
        }
      }

      if (( objCache.getMatchingDataObject( dedupeKey )) == null )
      {
        LOG.debug( "Adding new data object ..." );
        objCache.addDataObject( dobj, dedupeKey );
      }
      else
      {
        LOG.debug( "Found a duplicate!" );
        objCache.nDeduped++;
      }
    }
  }
    
    
  private String getQueryKey( IQuery query )
  {
    if (queryProps == null) return "";
    	
    StringBuilder strbuilder = new StringBuilder( );
    for (Iterator<String> queryPropIt = queryProps.iterator(); queryPropIt.hasNext(); )
    {
      String queryPropName = queryPropIt.next();
      IProperty queryProp = query.getProperty( queryPropName );
      if (queryProp != null)
      {
        strbuilder.append( queryProp.getValue() );
      }
    }
    	
    return strbuilder.toString();
  }
    
  private boolean cacheHasResults( IQuery query )
  {
    LOG.debug( "cacheHasResults: lastQueryKey = '" + lastQueryKey + "'" );
    String newQueryKey = getQueryKey( query );
    LOG.debug( "newQueryKey = '" + newQueryKey + "'" );
        
    if ( newQueryKey.trim().length() > 0 && (lastQueryKey == null || newQueryKey.equals( lastQueryKey ) == false ) )
    {
      LOG.debug( "New ObjectCache ..." );
      objCache = new ObjectCache( );
      lastQueryKey = newQueryKey;
      return false;
    }
        
    return cacheIsReady( query );
  }
    
  private boolean cacheIsReady( IQuery query )
  {
    LOG.debug( "cacheIsReady: " );
    	
    // check that startRec < objCache.size && startRec + pageSize - 1 < size...
    // if totalRecords is limiting, return true.
    if (objCache == null) return false;
      
    int from = query.getStartRecord( );
    int to = from + query.getPageSize( ) - 1;
    int nCached = objCache.size( );

    boolean isReady =  (( from < nCached && to < nCached ) || (to >= objCache.getTotalRecordsDeduped( ) && objCache.getTotalRecordsDeduped() == objCache.size( )));
        
    LOG.debug( "cacheIsReady from = " + from + " to = " + to + " current result set size = " + nCached  + " total (after deduping) = "
                + objCache.getTotalRecordsDeduped( ) + " isReady = " + isReady );
        
    return isReady;
  }



  private IResultList getCachedResults( IQuery query )
  {
    LOG.debug( "getCachedResults( )" );
    if (objCache == null || objCache.dedupedResults == null)
    {
      LOG.debug( "Returning null!" );
      return null;
    }

    IResultList cached = new BasicResultList( );
    cached.setTotalResults(objCache.getTotalRecordsDeduped());

    for (int i = query.getStartRecord(); i < (query.getStartRecord() + query.getPageSize()); i++)
    {
      if (i < objCache.dedupedResults.size())
      {
        DataObject dobj = objCache.dedupedResults.item( i );
        cached.addDataObject( dobj );
      }
      else
      {
        break;
      }
    }
        
    LOG.debug( "Returning cached records: " + cached.size() + " of total " + cached.getTotalResults( ) );
    return cached;
  }
    
    
	
  public class ObjectCache
  {
    IResultList dedupedResults = null;

    HashMap<String,DataObject> dedupeMap;
        
    int nDeduped;
    int sourceTotalRecs;
        
    int lastStartRec = -1;
      
    public void setTotalResults( int totalResults )
    {
      LOG.debug( "Set totalResults: " + totalResults );
      if (dedupedResults != null) dedupedResults.setTotalResults( totalResults );
    }

    public void addDataObject( DataObject dobj, String objectKey )
    {
      LOG.debug( "addDataObject: " + objectKey );
        	
      if (dedupedResults == null) dedupedResults = new BasicResultList( );
        	
      dedupedResults.addDataObject( dobj );
      if (dedupeMap == null) dedupeMap = new HashMap<String,DataObject>( );
      dedupeMap.put( objectKey, dobj );
    }
        
    private int size( )
    {
      return (dedupedResults != null) ? dedupedResults.size( ) : 0;
    }

    public DataObject getMatchingDataObject( String objectKey )
    {
      if (dedupeMap == null) return null;
      return (DataObject)dedupeMap.get( objectKey );
    }
        
    void setSourceTotalRecords( int sourceTotalRecs )
    {
      LOG.debug( "setSourceTotalRecords( " + sourceTotalRecs + " )" );
      this.sourceTotalRecs = sourceTotalRecs;
    }
        
    int getTotalRecordsDeduped(  )
    {
      return sourceTotalRecs - nDeduped;
    }
  }



  @Override
  public Set<String> getQueryFields()
  {
    return (proxyFinder != null) ? proxyFinder.getQueryFields() : null;
  }

  @Override
  public Set<String> getNavigatorFields()
  {
    return (proxyFinder != null) ? proxyFinder.getNavigatorFields() : null;
  }

  @Override
  public Set<String> getResultFields()
  {
    return (proxyFinder != null) ? proxyFinder.getResultFields() : null;
  }

  @Override
  public Iterator<String> getFinderFields()
  {
    return (proxyFinder != null) ? proxyFinder.getFinderFields() : null;
  }

  @Override
  public FinderField getFinderField(String name)
  {
    return (proxyFinder != null) ? proxyFinder.getFinderField( name ) : null;
  }

  @Override
  public DataObject getSingleResult( String resultID ) throws FinderException
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
    return (proxyFinder != null) ? proxyFinder.getSourceField(  ) : null;
  }


}
