package com.modinfodesigns.search.solr;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.search.BasicResultList;
import com.modinfodesigns.search.FinderException;
import com.modinfodesigns.search.IFinder;
import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.Query;
import com.modinfodesigns.search.QueryField;
import com.modinfodesigns.search.QueryFieldOperator;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.search.SortProperty;

import com.modinfodesigns.classify.InvertedIndex;

import com.modinfodesigns.search.INavigator;
import com.modinfodesigns.search.INavigatorField;
import com.modinfodesigns.search.Navigator;
import com.modinfodesigns.search.NavigatorField;
import com.modinfodesigns.search.NavigatorFacet;
import com.modinfodesigns.search.IResultList;
import com.modinfodesigns.search.FinderField;
import com.modinfodesigns.utils.StringMethods;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.FacetField;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrDocument;

import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOLRFinder implements IFinder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SOLRFinder.class );
    
	private String name;
	
    private String serverURL;
    
    private HashMap<String,FinderField> searchFieldMap;
    
    private HashSet<String> queryFields;
    private HashSet<String> facetFields;
    private HashSet<String> displayFields;
    
    private String solrParser = "DisMax";
    
    private String defaultDistance = "5";
    
    private String sourceFieldName;
    
    public void addFinderField( FinderField searchField )
    {
    	LOG.debug( "addFinderField " + ((searchField != null) ? searchField.getFieldName( ) : "" ));
    	if (searchField == null) return;
    	
    	if (searchFieldMap == null) searchFieldMap = new HashMap<String,FinderField>( );
    	searchFieldMap.put( searchField.getFieldName( ), searchField );
    }
    
    public void setServerURL( String serverURL )
    {
    	this.serverURL = serverURL;
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
	public void setSourceFieldName( String sourceFieldName )
	{
		this.sourceFieldName = sourceFieldName;
	}
    
	
	@Override
	public DataObject getSingleResult( String resultID ) throws FinderException 
	{
		String idField = getIDField( );
		
		return null;
	}
	
	
	@Override
	public IResultList executeQuery( IQuery query ) throws FinderException
	{
		LOG.debug( "executeQuery( ) " + query.getValue( ) );
		
		SolrParams sParams = createSolrQuery( query );

        if ( sParams.get( CommonParams.Q ) != null || sParams.get( CommonParams.FQ ) != null )
        {
        	try
        	{
        		SolrClient sServer = getSolrClient( );
                QueryResponse qResponse = sServer.query( sParams );
                BasicResultList resp = createResponse( qResponse );
                if (resp != null)
                {
                	resp.setQuery( query );
                }
                
                LOG.debug( "Got Results " + ((resp != null) ? resp.getValue( ) : "") );
                return resp;
        	}
        	catch ( Exception e )
        	{
        		throw new FinderException( "Got Exception: " + e.getMessage( ) );
        	}
        }
		
        // throw new FinderException( "No 'q' or 'fq' parameters in SolrParams!" );
        return new BasicResultList( );
	}
	
	
	private BasicResultList createResponse( QueryResponse qResponse )
	{
		BasicResultList brl = new BasicResultList( );
        SolrDocumentList solrDocs = qResponse.getResults( );
        
        for (int i = 0, isz = solrDocs.size(); i < isz; i++)
        {
            SolrDocument solrDoc = (SolrDocument)solrDocs.get( i );
            brl.addDataObject( createDataObject( solrDoc ));
        }
        
        brl.setTotalResults( (int)solrDocs.getNumFound( ) );
        LOG.debug( "got " + solrDocs.getNumFound( ) );
        brl.setNavigator( createNavigator( qResponse ));
		return brl;
	}
	
	private DataObject createDataObject( SolrDocument solrDoc )
	{
		DataObject dobj = new DataObject( );
		
		Collection<String> fieldNames = solrDoc.getFieldNames( );
        if (fieldNames != null)
        {
            Iterator<String> fieldIt = fieldNames.iterator( );
            while ( fieldIt.hasNext( ) )
            {
            	String solrField = fieldIt.next( );
            	String fieldName = getFieldName( solrField );
            	
            	if (fieldName != null)
            	{
                    Collection<Object> fieldValues = solrDoc.getFieldValues( solrField );
                    if (fieldValues != null && fieldValues.size( ) == 1)
                    {
                        String value = solrDoc.getFirstValue( solrField ).toString( );
                        if (value.indexOf ( "={" ) > 0)
                        {
                        	value = new String( value.substring( value.indexOf( "={" ) + 2, value.lastIndexOf( "}" )));
                        }
                        dobj.setProperty( new StringProperty( fieldName, value ) );
                    }
                    else if (fieldValues != null)
                    {
                        Iterator<Object> valIt = fieldValues.iterator( );
                        while (valIt.hasNext( ) )
                        {
                            Object value = valIt.next( );
                            dobj.addProperty( new StringProperty( fieldName, value.toString() ) );
                        }
                    }
            	}
            }
        }
		
		return dobj;
	}
	
	
	private INavigator createNavigator( QueryResponse qResponse )
	{
		LOG.debug( "createNavigator( )..." );
		
		Navigator navigator = new Navigator( );
        List<FacetField> facetFields = qResponse.getFacetFields();
        if (facetFields != null)
        {
            for (int i = 0, isz = facetFields.size( ); i < isz; i++)
            {
                INavigatorField navField = createNavigatorField( facetFields.get( i ) );
                navigator.addNavigatorField( navField );
            }
        }
        
		return navigator;
	}
	
	
	private INavigatorField createNavigatorField( FacetField facetField )
	{		
    	String fieldName = getFieldName( facetField.getName( ) );

		LOG.debug( "createNavigatorField: " + fieldName );
		NavigatorField navField = new NavigatorField( fieldName );
		
		HashMap<String,NavigatorFacet> facetMap = new HashMap<String,NavigatorFacet>( );
		
		List<FacetField.Count> facetCounts = facetField.getValues();
        if (facetCounts != null)
        {
            for (int i = 0, isz = facetCounts.size( ); i < isz; i++)
            {
                FacetField.Count facet = facetCounts.get( i );
                
                if (facet.getCount() > 0)
                {
                	String facetName = facet.getName( );
                    if (facetName.indexOf( "={" ) > 0)
                    {
                    	facetName = new String( facetName.substring( facetName.indexOf( "={" ) + 2, facetName.lastIndexOf( "}" )));
                    }
                    
                    String[] facetNames = null;
                    
                    if (facetName.startsWith( "[" ) )
                    {
                    	facetName = new String( facetName.substring( 1, facetName.indexOf( "]" )));
                    	facetNames = StringMethods.getStringArray( facetName, "," );
                    }
                    else
                    {
                    	facetNames = new String[ 1 ];
                    	facetNames[0] = facetName;
                    }
                    
                    for (int f = 0, fsz = facetNames.length; f < fsz; f++)
                    {
                    	String fName = facetNames[f].trim( );
                        LOG.debug( "adding facet " + fName + " " + facet.getCount( ) );

                        NavigatorFacet navFacet = facetMap.get( fName );
                        if (navFacet == null)
                        {
                            navFacet = new NavigatorFacet( fName, (int)facet.getCount() );
                            navField.addNavigatorFacet( navFacet );
                            facetMap.put( fName, navFacet );
                        }
                        else
                        {
                        	navFacet.add( (int)facet.getCount( ) );
                        }
                    }
                }
            }
        }
        
		return navField;
	}

	
	private SolrParams createSolrQuery( IQuery query )
	{
		ModifiableSolrParams solrParams = new ModifiableSolrParams( );
		
        addSolrQuery( query, solrParams );
        
        IQuery scopeQuery = query.getScopeQuery( );
        if (scopeQuery != null)
        {
        	addSolrQuery( scopeQuery, solrParams );
        }
		
        addPageParams( query, solrParams );
		
		// add default field if it exists df=[ default field name ]
        
		addSolrFacets( query, solrParams );
		addSortParams( query, solrParams );
		
		return solrParams;
	}
	
	
	private void addSolrQuery( IQuery query, ModifiableSolrParams solrParams )
	{
		LOG.debug( "getSolrQuery" );
		
		if (query instanceof QueryTree )
		{
			createBooleanQuery( (QueryTree)query, solrParams );
		}
		else if (query instanceof Query )
		{
			createFieldedQuery( (Query)query, solrParams );
		}
	}
	
	/**
	 * Returns a fielded query given the set QueryParser
	 * 
	 * @param query
	 * @return
	 */
	protected void createFieldedQuery( Query query, ModifiableSolrParams solrParams )
	{
		LOG.debug( "getFieldedQuery" );
		
		String operator = query.getMultiFieldOperator( );
		List<QueryField> queryFields = query.getQueryFields( );

		for (int i = 0, isz = queryFields.size(); i < isz; i++)
		{
			QueryField qf = queryFields.get( i );
			LOG.debug( "Got qf " + qf.getFieldName( ) );
			
			solrParams.add( CommonParams.Q, getQueryValue( qf, false ));
		}
		
		QueryField defaultField = query.getDefaultQueryField( );
		if (defaultField != null)
		{
			solrParams.add( CommonParams.Q, getQueryValue( defaultField, true ));
		}
		
	}
	
	private String getQueryValue( QueryField qf, boolean defaultField )
	{
		String queryField = qf.getFieldName( );
		
		String solrField = (!defaultField) ? getSolrField( queryField ) : "";
		if (!defaultField) solrField += ":";
		
		String fieldValue = qf.getFieldValue( );
		
		String[] terms = StringMethods.getStringArray( fieldValue, InvertedIndex.tokenDelimiter );
		QueryFieldOperator qFieldOp = qf.getQueryFieldOperator( );
		
		if (terms.length > 1 )
		{
	    	if (qFieldOp != null)
		    {
			    String operator = qFieldOp.getFieldOperator( );
			    if (operator.equals( QueryFieldOperator.ALL ))
			    {
			    	StringBuilder strbuilder = new StringBuilder( );
			    	for (int i = 0, isz = terms.length; i < isz; i++)
			    	{
			    		strbuilder.append( solrField ).append( terms[i] );
			    		if (i < (terms.length - 1)) strbuilder.append( " +" );
			    	}
			    	return strbuilder.toString( );
			    }
			    else if (operator.equals( QueryFieldOperator.ANY ))
			    {
			    	StringBuilder strbuilder = new StringBuilder( );
			    	for (int i = 0, isz = terms.length; i < isz; i++)
			    	{
			    		strbuilder.append( solrField ).append( terms[i] );
			    		if (i < (terms.length - 1)) strbuilder.append( " " );
			    	}
			    	
			    	return strbuilder.toString( );	
			    }
			    else if (operator.equals( QueryFieldOperator.NEAR ))
			    {
			    	// add ~ and Distance 
			    	IProperty dist = qFieldOp.getModifier( QueryFieldOperator.DISTANCE );
			    	String distVal = (dist != null) ? dist.getValue( ) : defaultDistance;
			    	return solrField + "\"" + fieldValue + "\"~" + distVal;
			    }
		    }
	    	else
	    	{
	    		return solrField + "\"" + fieldValue + "\"";
	    	}
		}
		
		return solrField + fieldValue;
	}
	

	protected void createBooleanQuery( QueryTree qTree, ModifiableSolrParams solrParams )
	{
		String boolQ = getBooleanQuery( qTree, qTree.getDefaultQueryField( ) );
		solrParams.add( CommonParams.Q, boolQ );
	}
	
	
	private String getBooleanQuery( QueryTree qTree, QueryField defaultQueryField )
	{
		LOG.debug( "getBooleanQuery " );
		
		StringBuilder strbuilder = new StringBuilder( );
		
		if ( qTree.getOperator().equals( QueryTree.TERM ) || qTree.getOperator().equals( QueryTree.PHRASE ))
		{
			boolean useDefaultFields = false;
			String fieldName = qTree.getQueryField( );
			if ( defaultQueryField != null && defaultQueryField.getFieldName().equals( fieldName ))
			{
			    LOG.debug( "Have Default Query..." );
			    useDefaultFields = true;
			}
			else
			{
				fieldName = getSolrField( fieldName );
			}

			if (fieldName == null)
			{
				useDefaultFields = true;
			}
			
			if (useDefaultFields)
			{
				strbuilder.append( ( qTree.getOperator().equals( QueryTree.PHRASE )) ? "\"" : "" )
                          .append( qTree.getQueryText( ) )
		                  .append( ( qTree.getOperator().equals( QueryTree.PHRASE )) ? "\"" : "" );
			}
			else
			{
			    strbuilder.append( fieldName ).append( ":" )
			              .append( ( qTree.getOperator().equals( QueryTree.PHRASE )) ? "\"" : "" )
                          .append( qTree.getQueryText( ) )
			              .append( ( qTree.getOperator().equals( QueryTree.PHRASE )) ? "\"" : "" );
			}
		}
		else
		{
			List<QueryTree> subTrees = qTree.getSubTrees( );
			if (subTrees != null)
			{
				String operator = qTree.getOperator( );
				for ( int i = 0, isz = subTrees.size(); i < isz; i++ )
				{
					QueryTree subTree = subTrees.get( i );
					String subQ = getBooleanQuery( subTree, defaultQueryField);
					String lucOp = (operator.equals( "AND" )) ? " AND " : " OR ";
			        // if (operator.equals( "NOT" )) lucOp = "-";
					    	
				    strbuilder.append( subQ );
				    if (i < subTrees.size() - 1) strbuilder.append( lucOp );
				}
			}
		}
		
		LOG.debug( "returning query: " + strbuilder.toString( ) );
		return strbuilder.toString();
	}
	
	private void addPageParams( IQuery query, ModifiableSolrParams solrParams )
	{
		int start = query.getStartRecord( );
		int rows = query.getPageSize( );
		
		solrParams.set( "start", Integer.toString( Math.max( 0, start - 1 ) ) );
		solrParams.set( "rows", Integer.toString( rows ) );
		
		LOG.debug( "start = " + start + " rows = " + rows );
	}
	
	private void addSolrFacets( IQuery query, ModifiableSolrParams solrParams )
	{
		LOG.debug( "addSolrFacets ... " );
		
		List<String> navigators = query.getNavigatorFields( );
		if ( navigators != null && navigators.size() > 0)
		{
			solrParams.set( "facet",  "true" );
			
			Iterator<String> navIt = navigators.iterator();
			while( navIt != null && navIt.hasNext() )
			{
				String fieldName = navIt.next( );
				solrParams.add( "facet.field", getSolrField( fieldName )  );
			}
		}
		else
		{
			LOG.debug( "No Navigator Fields to Add!" );
		}
	}
	
	private void addSortParams( IQuery query, ModifiableSolrParams solrParams )
	{
		SortProperty sortProp = query.getSortProperty( );
        if (sortProp != null)
        {
        	SortProperty[] allProps = sortProp.getSortProperties( );
        	StringBuilder strbuilder = new StringBuilder( );
        	for (int i = 0, isz = allProps.length; i < isz; i++)
        	{
        		SortProperty sP = allProps[i];
        		strbuilder.append( (sP.getName().equals( SortProperty.RELEVANCE )) ? "score" : getSolrField( sP.getName( ) ) );
                strbuilder.append( " " );
                strbuilder.append( (sP.getSortDirection().equals(SortProperty.DESCENDING ) 
                		         || sP.getName().equals( SortProperty.RELEVANCE)) ? "desc" : "asc" );
                if (i < allProps.length - 1 ) strbuilder.append( ", " );
        	}

        	solrParams.set( "sort", strbuilder.toString( ) );
        }
	}
	
	private SolrClient getSolrClient(  ) throws Exception
	{
        HttpSolrServer solrServer = new HttpSolrServer( this.serverURL );
        return solrServer;
	}
	
	@Override
	public Set<String> getQueryFields()
	{
		if (searchFieldMap == null) return null;
		
		if (queryFields != null) return queryFields;
		
		synchronized( this )
		{
			queryFields = new HashSet<String>( );
			for (Iterator<String> fieldIt = searchFieldMap.keySet().iterator(); fieldIt.hasNext(); )
			{
				String field = fieldIt.next( );
				FinderField sf = searchFieldMap.get( field );
				if (sf.isSearchable( ))
				{
					queryFields.add( sf.getFieldName( ) );
				}
			}
		}
		
		return queryFields;
	}

	@Override
	public Set<String> getNavigatorFields()
	{
		if (searchFieldMap == null) return null;
		
		if (facetFields != null) return facetFields;
		
		synchronized( this )
		{
			facetFields = new HashSet<String>( );
			for (Iterator<String> fieldIt = searchFieldMap.keySet().iterator(); fieldIt.hasNext(); )
			{
				String field = fieldIt.next( );
				FinderField sf = searchFieldMap.get( field );
				if (sf.isFacet( ))
				{
					facetFields.add( sf.getFieldName( ) );
				}
			}
		}
		
		return facetFields;
	}

	@Override
	public Set<String> getResultFields()
	{
		if (searchFieldMap == null) return null;
		
		if (displayFields != null) return displayFields;
		
		synchronized( this )
		{
			displayFields = new HashSet<String>( );
			for (Iterator<String> fieldIt = searchFieldMap.keySet().iterator(); fieldIt.hasNext(); )
			{
				String field = fieldIt.next( );
				FinderField sf = searchFieldMap.get( field );
				if (sf.isDisplayable( ))
				{
					displayFields.add( sf.getFieldName( ) );
				}
			}
		}
		
		return displayFields;
	}

	@Override
	public Iterator<String> getFinderFields()
	{
	    return (searchFieldMap != null) ? searchFieldMap.keySet().iterator() : null;
	}
	
	@Override
	public FinderField getFinderField( String name )
	{
		return (searchFieldMap != null) ? searchFieldMap.get( name ) : null;
	}

    public String getFieldName( String sourceField )
    {
        Iterator<FinderField> findIt = searchFieldMap.values().iterator();
        while( findIt != null && findIt.hasNext() )
        {
            FinderField ff = findIt.next();
            if (ff.getSourceFieldName().equals( sourceField ))
            {
                return ff.getFieldName();
            }
        }
        
        return "";
    }
    
    private String getSolrField( String fieldName )
    {
    	FinderField ff = getFinderField( fieldName );
    	return (ff != null) ? ff.getSourceFieldName() : null;
    }
    
    public String getIDField(  )
    {
    	Iterator<String> findIt = getFinderFields();
    	
    	while (findIt != null && findIt.hasNext() )
    	{
    		String field = findIt.next();
    		FinderField ff = getFinderField( field );
    		if (ff.isIDField() )
    		{
    			return ff.getSourceFieldName();
    		}
    	}
    	
    	return null;
    }


	@Override
	public FinderField getSourceField()
	{
		if (this.sourceFieldName == null) return null;
		
        FinderField sourceField = new FinderField( );
        sourceField.setName( IFinder.SOURCE_FIELD );
        sourceField.setSearchable( true );
        sourceField.setDisplayable( true );
        sourceField.setFieldName( IFinder.SOURCE_FIELD );
        sourceField.setSourceFieldName( this.sourceFieldName );
        
		return sourceField;
	}

}
