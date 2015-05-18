package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.app.ApplicationManager;

import com.modinfodesigns.search.IFinder;
import com.modinfodesigns.search.IResultList;
import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.IQueryFilter;
import com.modinfodesigns.search.Query;
import com.modinfodesigns.search.QueryField;
import com.modinfodesigns.search.FinderException;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ==================================================================================
// Need to add attributes to control page size, etc.
// first response, all results, etc...

public class LookupTransform implements IPropertyHolderTransform 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( LookupTransform.class );

  private String finderName;
  private IFinder finder;
    
  private String queryFilterName;
  private IQueryFilter queryFilter;
    
  private IQuery query;
    
  // IPropertyTransform that is used to convert input property
  // to query
  private IPropertyTransform queryTransform;
    
  private String queryProperty;
    
  private String resultListProperty;    // property that gets the result list ...
    
  private int pageSize = -1;
  private int startRec = -1;
       
  public static final String ADD_LIST = "ADD-LIST";
  public static final String USE_FIRST = "USE-FIRST";
    
  private String transformMode = ADD_LIST;  // ADD_LIST, USE_FIRST, USE_FIRST_PROPERTIES
    
    
  public void setFinderRef( String finderName )
  {
    this.finderName = finderName;
  }
    
  public void setFinder( IFinder finder )
  {
    this.finder = finder;
  }
    
  public void setQueryFilterRef( String queryFilterName )
  {
    this.queryFilterName = queryFilterName;
  }
    
  public void setQueryFilter( IQueryFilter queryFilter )
  {
    this.queryFilter = queryFilter;
  }
    
  public void setQueryProperty( String queryProperty )
  {
    this.queryProperty = queryProperty;
  }
    
  public void setQuery( IQuery query )
  {
    this.query = query;
  }
    
  public void setResultProperty( String resultProperty )
  {
    this.resultListProperty = resultProperty;
  }
    
  public void setResultListProperty( String resultProperty )
  {
    this.resultListProperty = resultProperty;
  }
    
    
  public void setQueryTransform( IPropertyTransform queryTransform )
  {
    this.queryTransform = queryTransform;
  }
    
  public void setPageSize( int pageSize )
  {
    this.pageSize = pageSize;
  }
    
  public void setPageSize( String pageSize )
  {
    this.pageSize = Integer.parseInt( pageSize );
  }
    
  public void setStartRecord( int startRec )
  {
    this.startRec = startRec;
  }
    
  public void setTransformMode( String mode )
  {
    this.transformMode = mode;
  }
    

  @Override
  public IProperty transform(IProperty input) throws PropertyTransformException
  {
    try
    {
      return executeQuery( input );
    }
    catch (FinderException fe )
    {
      throw new PropertyTransformException( "Got FinderException: " + fe );
    }
  }
	

  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    try
    {
      IResultList resList = null;
			
      if (queryProperty != null)
      {
        IProperty qProp = input.getProperty( queryProperty );
        if (qProp != null)
        {
          resList = executeQuery( qProp );
        }
      }
      else
      {
        resList = executeQuery( input );
      }
            
      if (resList != null && resList.size() > 0 && transformMode.equals( USE_FIRST ) )
      {
        LOG.debug( "Got results - adding the first one." );
        DataObject dobj = resList.item( 0 );
        if (dobj != null)
        {
          for (Iterator<IProperty> propIt = dobj.getProperties(); propIt.hasNext(); )
          {
            IProperty prop = propIt.next();
            input.addProperty( prop );
          }
        }
      }
      else if (resList != null && resultListProperty != null )
      {
        LOG.debug( "Setting " + resultListProperty + " = result list of " + resList.size() );
            	
        resList.setName( resultListProperty );
        input.addProperty( resList );
        return input;
      }
		    
      return resList;
    }
    catch ( FinderException fe )
    {
      throw new PropertyTransformException( "Got FinderException: " + fe );
    }
  }
	
	
  private IResultList executeQuery( IProperty inputProp ) throws FinderException, PropertyTransformException
  {
    IQuery useQuery = this.query;
		
    if (useQuery == null)
    {
      IQueryFilter qFilter = getQueryFilterImplementation( );
		
      if (qFilter != null)
      {
        useQuery = qFilter.createQuery( inputProp );
      }
      else if (inputProp instanceof IQuery)
      {
        useQuery = (IQuery)inputProp;
      }
      else if (inputProp instanceof IPropertyHolder)
      {
        useQuery = new Query( );
			
        for (Iterator<IProperty> propIt = ((IPropertyHolder)inputProp).getProperties( ); propIt.hasNext(); )
        {
          IProperty aProp = propIt.next( );
          useQuery.addProperty( aProp.copy( ) );
        }
      }
      else if (queryTransform != null)
      {
        IProperty queryProp = queryTransform.transform( inputProp );
        if (queryProp instanceof IQuery)
        {
          useQuery = (IQuery)queryProp;
        }
        else
        {
          useQuery = new Query( );
          ((Query)useQuery).addQueryField( new QueryField( queryProp.getName(), queryProp.getValue( ) ));
        }
      }
    }
		
    if (useQuery == null)
    {
      throw new FinderException( "Could not create a valid Query object!" );
    }
		
    if (pageSize > 0)
    {
      useQuery.setPageSize( this.pageSize );
    }
    if (startRec > 0)
    {
      useQuery.setStartRecord( startRec );
    }
		
    IFinder useFinder = getFinderImplementation( );
    if (useFinder == null)
    {
      return null;
    }
    IResultList resList = useFinder.executeQuery( useQuery );
    if (resList != null)
    {
      LOG.debug( transformMode +  " Got : " + resList.size( ) + " out of " + resList.getTotalResults( ) );
		
      if (transformMode.equals( ADD_LIST ))
      {
        if (resList.size() < resList.getTotalResults( ))
        {
          resList = getAllResults( query, resList );
        }
      }
    }
		
    return resList;
  }
	
  private IResultList getAllResults( IQuery query, IResultList inputList ) throws FinderException
  {
    LOG.debug( "getAllResults( )..." );
    int pageSize = query.getPageSize( );
    int startRec = query.getStartRecord( );
    query.setStartRecord( startRec + pageSize );
		
    IResultList moreResults = finder.executeQuery( query );
    if (moreResults != null && moreResults.size() > 0)
    {
      copyData( moreResults, inputList );
      inputList.setTotalResults( moreResults.getTotalResults( ) );
    }
    else
    {
      return inputList;
    }
		
    if ( inputList.size( ) < inputList.getTotalResults( ) )
    {
      return getAllResults( query, inputList );
    }
		
    return inputList;
  }
	
  private void copyData( IResultList from, IResultList to )
  {
    int i = 0;
    for (Iterator<DataObject> dit = from.getData(); dit.hasNext(); )
    {
      DataObject dobj = dit.next();
      to.addDataObject( dobj );
      i++;
    }
		
    LOG.debug( "Copied " + i + " records" );
  }
	
	
  @Override
  public void startTransform(IProperty input, IPropertyTransformListener transformListener) throws PropertyTransformException
  {

  }
	
  private IQueryFilter getQueryFilterImplementation( )
  {
    if (this.queryFilter != null) return this.queryFilter;
		
    if (this.queryFilterName != null)
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
      return (IQueryFilter)appMan.getApplicationObject( queryFilterName,  "QueryFilter" );
    }
		
    return null;
  }
	
  private IFinder getFinderImplementation( )
  {
    if ( this.finder != null) return this.finder;
		
    if (this.finderName != null)
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
      return (IFinder)appMan.getApplicationObject( finderName,  "Finder" );
    }
		
    return null;
  }

}
