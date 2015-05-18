package com.modinfodesigns.app.search.model;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.search.IFinder;
import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.Query;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes an abstract, searchable Content Source. A Content source describes a set of content that can be
 * acquired from some IFinder implementation. It can be the entire content set searchable from that
 * IFinder or some subset as defined by its optional Scope Query filter.
 * 
 *    IFinder Implementation (name)
 *    Source Attributes:
 *    <ul>
 *       <li> Collection name (if applicable - implementation dependent)
 *       <li> Scope filter (IQuery) - defines a filter the can restrict the set of results available
 *            from the source.
 *       <li> Relevance query (IQuery) - defines a dynamic sort setting, formula or function (depending
 *            on the IFinder implementation.
 *       <li> Access control (ISecurityManager name)  - determines if user can "see" it
 *       <li> Admin Credentials (IUserCredentials) - used to provide proxy access to secure sources
 *    </ul>
 * 
 * @author Ted Sullivan
 */

public class ContentSource extends DataObjectBean
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ContentSource.class );
    
	public static final String FINDER_NAME           = "FinderName";
	public static final String DISPLAY_NAME          = "DisplayName";
	public static final String SCOPE_QUERY           = "ScopeQuery";
	public static final String COLLECTION            = "Collection";
	public static final String RELEVANCE_QUERIES     = "RelevanceQueries";
	public static final String SECURITY_MANAGER      = "SecurityManager";
	
    private IFinder finder;
    
    public ContentSource( ) {  }
    
    public ContentSource( String name )
    {
    	setName( name );
    }
    
    public void setFinderName( String finderName )
    {
    	doSetProperty( new StringProperty( FINDER_NAME, finderName ));
    }
    
    public String getFinderName( )
    {
    	IProperty finderProp = getProperty( FINDER_NAME );
    	return (finderProp != null) ? finderProp.getValue( ) : null;
    }
    
    public void setDisplayName( String displayName )
    {
    	doSetProperty( new StringProperty( DISPLAY_NAME, displayName ));
    }
    
    public String getDisplayName( )
    {
    	IProperty dispProp = getProperty( DISPLAY_NAME );
    	return (dispProp != null) ? dispProp.getValue( ) : null;
    }
    
    public IFinder getFinder(  )
    {
    	if (finder != null) return finder;
    	
    	String finderName = getFinderName( );
    	if (finderName == null) return null;
    	
    	Object finderOb = ApplicationManager.getInstance( )
    			                            .getApplicationObject( finderName, "Finder" );
    	if (finderOb != null && finderOb instanceof IFinder )
    	{
    		this.finder = (IFinder)finderOb;
    	}
    	
    	return finder;
    }
    
    public void addQuery( IQuery scopeQuery )
    {
    	setQuery( scopeQuery );
    }
    
    /**
     * Sets the content source scope query.  Sets IsScopeQuery true on the
     * IQuery so that if the IFinder has a filter mechanism that preselects a set of
     * content but does not do relevance ranking, it can be used. This can result in
     * better performing queries.
     * 
     * @param scopeQuery
     */
    public void setQuery( IQuery scopeQuery )
    {
    	LOG.debug( "setQuery: " + scopeQuery );
    	
    	if (scopeQuery == null) return;
    	
    	LOG.debug( scopeQuery.getValue( ) );
    	
    	scopeQuery.setName( SCOPE_QUERY );
    	doAddProperty( scopeQuery );
    }
    
    
    public IQuery getQuery(  )
    {
    	LOG.debug( "getQuery( )..." );
    	
    	IQuery scopeProp = (IQuery)getProperty( SCOPE_QUERY );
    	if (scopeProp != null) return scopeProp;
    	
    	return null;
    }
    
    public void addRelevanceQuery( Query relevanceQuery )
    {
    	DataList relQueryList = (DataList)getProperty( RELEVANCE_QUERIES );
    	if (relQueryList == null)
    	{
    		relQueryList = new DataList( );
    		relQueryList.setName( RELEVANCE_QUERIES );
    		doAddProperty( relQueryList );
    	}
    	
    	relQueryList.addDataObject( relevanceQuery );
    }
    
    public IQuery getRelevanceQuery( String name )
    {
    	DataList relQueryList = (DataList)getProperty( RELEVANCE_QUERIES );
    	
    	if (relQueryList != null)
    	{
    		Iterator<DataObject> dobIt = relQueryList.getData( );
    		while( dobIt != null && dobIt.hasNext() )
    		{
    			DataObject dobj = dobIt.next( );
    			if (dobj instanceof Query && dobj.getName().equals( name ))
    			{
    				return (Query)dobj;
    			}
    		}
    	}
    	
    	return null;
    }

    public void setCollectionName( String collectionName )
    {
    	doSetProperty( new StringProperty( COLLECTION, collectionName ));
    }
    
    public String getCollectionName( )
    {
    	IProperty collProp = getProperty( COLLECTION );
    	return (collProp != null) ? collProp.getValue( ) : null;
    }
    
    public void setSecurityManager( String securityManager )
    {
    	doSetProperty( new StringProperty( SECURITY_MANAGER, securityManager ));
    }
    
    public String getSecurityManager( )
    {
    	IProperty secManProp = getProperty( SECURITY_MANAGER );
    	return (secManProp != null) ? secManProp.getValue( ) : null;
    }

	@Override
	public DataObjectSchema createDataObjectSchema( DataObject context )
	{
		DataObjectSchema dos = new DataObjectSchema( );
		dos.setName( "ContentSource" );
		
		PropertyDescriptor pd = new PropertyDescriptor( );
		pd.setName( FINDER_NAME );
		pd.setPropertyType( "String" );
		ApplicationManager appMan = ApplicationManager.getInstance( );
		List<Object> finders = appMan.getApplicationObjects( "Finder" );
		String[] finderNames = new String[ finders.size( ) ];
		for (int i = 0; i < finders.size( ); i++)
		{
			IFinder finder = (IFinder)finders.get( i );
			finderNames[i] = finder.getName( );
		}
		pd.setPropertyValues( finderNames );
		dos.addPropertyDescriptor( pd );
		
		pd = new PropertyDescriptor( );
		pd.setName( DISPLAY_NAME );
		pd.setPropertyType( "String" );
		dos.addPropertyDescriptor( pd );
		
		pd = new PropertyDescriptor( );
		pd.setName( COLLECTION );
		pd.setPropertyType( "String" );
		dos.addPropertyDescriptor( pd );
		
		pd = new PropertyDescriptor( );
		pd.setName( SECURITY_MANAGER );
		pd.setPropertyType( "String" );
		dos.addPropertyDescriptor( pd );
		
		// problem here - what is the Schema for a Query?
		// must have a method to create the IQuery given an XML String ...
		pd = new PropertyDescriptor( );
		pd.setName( SCOPE_QUERY );
		pd.setPropertyType( "com.modinfodesigns.search.IQuery" );
		pd.setIsInterface( true );
        dos.addPropertyDescriptor( pd );
        
		pd = new PropertyDescriptor( );
		pd.setName( RELEVANCE_QUERIES );
		pd.setPropertyType( "com.modinfodesigns.search.IQuery" );
		pd.setIsInterface( true );
		pd.setMultiValue( true );
		dos.addPropertyDescriptor( pd ); 
		
		return dos;
	}
	
	public static String[] getContentSources( )
	{
		ApplicationManager appMan = ApplicationManager.getInstance( );
		List<Object> contentSources = appMan.getApplicationObjects( "ContentSource" );
		if (contentSources != null)
		{
			ArrayList<String> contentSourceNames = new ArrayList<String>( );
			for (int i = 0; i < contentSources.size( ); i++)
			{
				Object contentSourceObj = contentSources.get( i );
				if (contentSourceObj instanceof ContentSource)
				{
					ContentSource cs = (ContentSource)contentSourceObj;
					contentSourceNames.add( cs.getName( ) );
				}
			}
			
			String[] contentArray = new String[ contentSourceNames.size( ) ];
			contentSourceNames.toArray( contentArray );
			return contentArray;
		}
		
		return null;
	}

}
