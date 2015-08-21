package com.modinfodesigns.search.lucene;

import com.modinfodesigns.classify.InvertedIndex;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.search.BasicResultList;
import com.modinfodesigns.search.FinderException;
import com.modinfodesigns.search.IFinder;
import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.IResultList;
import com.modinfodesigns.search.FinderField;
import com.modinfodesigns.search.QueryField;
import com.modinfodesigns.search.QueryFieldOperator;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.search.SortProperty;
import com.modinfodesigns.utils.StringMethods;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PhraseQuery;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.NumericRangeQuery;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;


import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;

import org.apache.lucene.index.Term;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finder Implementation using Apache's Lucene search engine.
 * 
 * Freetext searches - use the StandardQueryParser
 * 
 * @author Ted Sullivan
 */

// Detect quoted strings and wildcards..., other types depending on QueryMode
// Add faceted search support ...

public class LuceneFinder implements IFinder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( LuceneFinder.class );
    
    private int MAX_DOCS = 1000;
    private static int DEFAULT_DISTANCE = 5;
    
    private String name;
    
    private HashMap<String,FinderField> searchFieldMap;
    
    private HashSet<String> queryFields;
    private HashSet<String> facetFields;
    private HashSet<String> displayFields;
    
    private String defaultField = "content";   // field to use for free-text queries
    
    // Info on file directory ---
    private String indexDirectory;
    private Directory luceneIndex;
    
    private boolean caseSensitive = false;
    
    private String sourceFieldName;
    
    public void setName( String name )
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }
    
    /**
     * Sets the field used for free text searching.
     * 
     * @param defaultField
     */
    public void setDefaultField( String defaultField )
    {
        this.defaultField = defaultField;
    }
    

    public String getDefaultField(  )
    {
    	return this.defaultField;
    }
    
    public void addFinderField( FinderField ff )
    {
        if (searchFieldMap == null) searchFieldMap = new HashMap<String,FinderField>( );
        searchFieldMap.put( ff.getFieldName(), ff );
    }
    
    Map<String,FinderField> getFinderMap(  )
    {
    	return searchFieldMap;
    }
    

    public void setIndexDirectory( String indexDirectory )
    {
    	this.indexDirectory = indexDirectory;
    }
    
    public String getIndexDirectory( )
    {
    	return this.indexDirectory;
    }
    
    
    @Override
    public DataObject getSingleResult( String resultID ) throws FinderException
    {
    	// get the IDField from the Finder Fields ...
    	String idField = getIDField( );
    	if (idField != null)
    	{
    		Query query = new TermQuery( new Term( idField, resultID ));
            try
            {
            	IndexSearcher searcher = getIndexSearcher( );
            	TopDocs tDocs = searcher.search( query, null, 10 );
                ScoreDoc[] hits = tDocs.scoreDocs;
                if (hits != null && hits.length > 0)
                {
                	Document hitDoc = searcher.doc( hits[0].doc );
                	return getDataObject( hitDoc );
                }
            }
            catch ( IOException ioe )
            {
                throw new FinderException( "Got IOException " + ioe );
            }
    	}
    	
        return null;
    }
    
    @Override
    public IResultList executeQuery( IQuery query ) throws FinderException
    {
    	LOG.debug( "executeQuery: " + query.getValue( ) );
        Query luceneQuery = createLuceneQuery( query );
        
        LOG.debug( "Got luceneQuery " + luceneQuery );
        // Navigator collectors etc...
        try
        {
            IndexSearcher searcher = getIndexSearcher( );
            BasicResultList brl = new BasicResultList( );
            
            if (luceneQuery == null)
            {
            	// This is where we need to do blank Query returns all or nothing...
            	// assume nothing for now.
            	brl.setTotalResults( 0 );
            	return brl;
            }
            
            QueryWrapperFilter scopeFilter = null;
            IQuery scopeQuery = query.getScopeQuery( );
            if (scopeQuery != null)
            {
            	Query luceneScope = createLuceneQuery( scopeQuery );
            	scopeFilter = new QueryWrapperFilter( luceneScope );
            }
            
            Sort lucSort = getLuceneSort( query );
            
            // top hits for startRec + pageSize ...
            TopDocs tDocs = (lucSort !=  null)
            		      ? searcher.search( luceneQuery, scopeFilter, MAX_DOCS, lucSort )
            		      : searcher.search( luceneQuery, scopeFilter, MAX_DOCS );

            ScoreDoc[] hits = tDocs.scoreDocs;
            int totalDocs = tDocs.totalHits;
            
            LOG.debug( this + " Got " + totalDocs + " hits." );
            brl.setTotalResults( totalDocs );
            brl.setQuery( query );
            
            int lastDoc = query.getStartRecord() + query.getPageSize() - 1;
            lastDoc = Math.min( lastDoc, totalDocs );
            LOG.debug( "lastDoc = " + lastDoc );
            
            for (int i = query.getStartRecord() - 1; i < lastDoc; i++)
            {
                Document hitDoc = searcher.doc( hits[i].doc );
                brl.addDataObject( getDataObject( hitDoc ) );
            }
            
            return brl;
        }
        catch ( IOException ioe )
        {
            throw new FinderException( "Got IOException " + ioe );
        }
    }
    
    
    private DataObject getDataObject( Document luceneDoc )
    {
        DataObject dobj = new DataObject( );
        List<IndexableField> luceneFields = luceneDoc.getFields( );
        if (luceneFields != null)
        {
            for (int i = 0; i < luceneFields.size(); i++ )
            {
                IndexableField luceneField = luceneFields.get( i );
                
                String name = luceneField.name();
                String value = luceneField.stringValue();
                LOG.debug( "Got " + name + " = '" + value + "'" );
                String localFieldName = getLocalFieldName( name );
                if ( localFieldName != null )
                {
                    dobj.addProperty( new StringProperty( localFieldName, value ));
                }
            }
        }
        
        return dobj;
    }

    
    private IndexSearcher getIndexSearcher(  ) throws IOException
    {
        if (luceneIndex == null)
        {
            luceneIndex = FSDirectory.open( new File( indexDirectory ).toPath( ) );
        }
        
        DirectoryReader reader = DirectoryReader.open( luceneIndex );
        return new IndexSearcher( reader );
    }
    
    
    private Query createLuceneQuery( IQuery query ) throws FinderException
    {
        Query luceneQuery = null;
        
        IQuery relevanceQuery = query.getRelevanceQuery( );
        
        if (query instanceof QueryTree)
        {
            return createBooleanQuery( (QueryTree)query, this.defaultField );
        }

        // ==============================================================
        // Map the abstract free text field to the default field designated
        // for this Lucene Index. 
        // ==============================================================
        if (defaultField != null && query.getDefaultQueryField() != null)
        {
        	LOG.debug( "Using DefaultQueryField" + query.getDefaultQueryField() );
            QueryField defaultQuery = query.getDefaultQueryField();
            if (defaultQuery != null && defaultQuery.getFieldValue( ) != null)
            {
                try
                {
                	String fieldValue = (caseSensitive) 
                			          ? defaultQuery.getFieldValue( )
                			          : defaultQuery.getFieldValue( ).toLowerCase( );
                			          
                    if (defaultQuery.getQueryFieldOperator() != null &&
                    	defaultQuery.getQueryFieldOperator().getFieldOperator().equals( QueryFieldOperator.EXACT ))
                    {
                    	LOG.debug( "Using EXACT query for: " + fieldValue );
                    	String[] terms = StringMethods.getStringArray( fieldValue, InvertedIndex.tokenDelimiter );
                        return  new SpanNearQuery( getSpans( defaultField, terms ), 0, true );
                    }
                    else if (defaultQuery.getQueryFieldOperator() != null &&
                        	defaultQuery.getQueryFieldOperator().getFieldOperator().equals( QueryFieldOperator.NEAR ))
                    {
                    	String[] terms = StringMethods.getStringArray( fieldValue, InvertedIndex.tokenDelimiter );
                        return new SpanNearQuery( getSpans( defaultField, terms ), getDistance( defaultQuery.getQueryFieldOperator() ), false );
                    }
                    else
                    {
                    	LOG.debug( "Using StandardQueryParser " );

                        StandardQueryParser sqp = new StandardQueryParser( );
                        LOG.debug( "parsing: " + fieldValue );
                        luceneQuery = sqp.parse( fieldValue, defaultField );
                    }
                }
                catch ( QueryNodeException e )
                {
                    throw new FinderException( "Got QueryNodeException " + e );
                }
            }
        }
        
        Set<String> queryFields = getQueryFields( );
        String multiFieldOp = query.getMultiFieldOperator( );
        if (multiFieldOp == null) multiFieldOp = IQuery.AND;
       
        for (Iterator<String> quIt = queryFields.iterator(); quIt.hasNext(); )
        {
            String field = quIt.next();
            QueryField qField = query.getQueryField( field );
            
            if (qField != null)
            {	 
                FinderField sf = searchFieldMap.get( field );
                Query fieldQ = getFieldQuery( sf, qField );
                if (relevanceQuery != null )
                {
                	Float boost = getBoostFactor( relevanceQuery, sf.getName() );
                	if (boost != null)
                	{
                	    fieldQ.setBoost( boost.floatValue() );
                	}
                }
                
                if (luceneQuery == null)
                {
                    luceneQuery = fieldQ;
                }
                else
                {
                    BooleanQuery bq = null;
                    if (luceneQuery instanceof BooleanQuery)
                    {
                        bq = (BooleanQuery)luceneQuery;
                    }
                    else
                    {
                        bq = new BooleanQuery( );
                    }
                  
                    bq.add( fieldQ, (multiFieldOp.equals( IQuery.AND ) ? BooleanClause.Occur.MUST 
                 	      	                                           : BooleanClause.Occur.SHOULD) );
                    luceneQuery = bq;
                }
            }
        }
        
        LOG.debug( "returning " + luceneQuery );
        return luceneQuery;
    }
    
    
    private Query getFieldQuery( FinderField sf, QueryField qField )
    {
    	String queryVal = qField.getFieldValue( );
    	if (!caseSensitive)
    	{
    		queryVal = queryVal.toLowerCase( );
    	}
    	
    	String[] terms = StringMethods.getStringArray( queryVal, InvertedIndex.tokenDelimiter );
    	String luceneField = sf.getSourceFieldName();
    	
        if (terms.length == 1 )
        {
        	// check if PrefixQuery ...
        	if (qField.getQueryFieldOperator() != null)
        	{
        		QueryFieldOperator qFieldOp = qField.getQueryFieldOperator( );

        		if ( qFieldOp.getFieldOperator().equals( QueryFieldOperator.PREFIX ))
        	    {
        	    	return new PrefixQuery(new Term( luceneField, queryVal ) );
        	    }
        		else if ( qFieldOp.isRangeOperator( ) )
        		{
        			return createRangeQuery( sf, qField, qFieldOp );
        		}
        		else if ( qFieldOp.getFieldOperator().equals( QueryFieldOperator.RANGE ) )
            	{
            		IProperty assocRangeProp = qField.getAssociatedRangeField( );
            		if (assocRangeProp != null)
            		{
            			// create bounded range query
            			return null;
            		}
            		else
            		{
            			return createRangeQuery( sf, qField, qFieldOp );
            		}            		
            	}
        		else
        		{
        			return new TermQuery(new Term( luceneField, queryVal ) );
        		}
        	}
        	else
        	{
        		LOG.debug( "adding " + luceneField + " = " + queryVal );
                return new TermQuery(new Term( luceneField, queryVal ) );
        	}
        }
        else
        {
            Query pq = null;
            
            QueryFieldOperator qFieldOp = qField.getQueryFieldOperator( );
            if (qFieldOp != null)
            {
                pq = buildCompoundQuery( luceneField, terms, qFieldOp );
            }
            else
            {
                pq = new PhraseQuery( );
                for (int t = 0; t < terms.length; t++ )
                {
                    ((PhraseQuery)pq).add( new Term( luceneField, terms[t] ));
                }
            }
            
            return pq;
        }
    }
    
    private Float getBoostFactor( IQuery relevanceQuery, String fieldName )
    {
    	QueryField boostField = relevanceQuery.getQueryField( fieldName );
    	if (boostField != null)
    	{
    		String boostVal = boostField.getFieldValue( );
    		try
    		{
    			return new Float( boostVal );
    		}
    		catch( NumberFormatException nfe )
    		{
    			
    		}
    	}
    	
    	return null;
    }
    
    
    public static Query buildCompoundQuery( String luceneField, String[] terms, QueryFieldOperator qFieldOp )
    {
    	LOG.debug( "buildCompoundQuery" );
        String fieldOperator = qFieldOp.getFieldOperator( );
        
        if ( fieldOperator.equalsIgnoreCase( QueryFieldOperator.ANY ))
        {
            BooleanQuery bq = new BooleanQuery( );
            for (int i = 0; i < terms.length; i++)
            {
                bq.add( new TermQuery( new Term( luceneField, terms[i] )), BooleanClause.Occur.SHOULD );
            }
            return bq;
        }
        else if ( fieldOperator.equalsIgnoreCase( QueryFieldOperator.ALL ))
        {
            BooleanQuery bq = new BooleanQuery( );
            for ( int i = 0; i < terms.length; i++ )
            {
                bq.add( new TermQuery( new Term( luceneField, terms[i] )), BooleanClause.Occur.MUST );
            }
            return bq;
        }
        else if ( fieldOperator.equalsIgnoreCase( QueryFieldOperator.NONE ))
        {
            BooleanQuery bq = new BooleanQuery( );
            for ( int i = 0; i < terms.length; i++ )
            {
                bq.add( new TermQuery( new Term( luceneField, terms[i] )), BooleanClause.Occur.MUST_NOT );
            }
            return bq;
        }
        else if ( fieldOperator.equalsIgnoreCase( QueryFieldOperator.EXACT ))
        {
            // use Span Ordered Near Query distance = 0
            return new SpanNearQuery( getSpans( luceneField, terms ), 0, true );
        }
        else if ( fieldOperator.equalsIgnoreCase( QueryFieldOperator.NEAR ))
        {
            // use SpanNearQuery
        	LOG.debug( "Using NEAR Query" );
            return new SpanNearQuery( getSpans( luceneField, terms ), getDistance( qFieldOp ), false );
        }
        else if ( fieldOperator.equalsIgnoreCase( QueryFieldOperator.ONEAR ))
        {
            return new SpanNearQuery( getSpans( luceneField, terms ), getDistance( qFieldOp ), true );
        }
        else if (fieldOperator.equalsIgnoreCase( QueryFieldOperator.PREFIX ))
        {
        	 BooleanQuery bq = new BooleanQuery( );
             for (int i = 0; i < terms.length; i++)
             {
            	 // HAS to be OR because can't start with more than one thing at the same time
                 bq.add( new PrefixQuery( new Term( luceneField, terms[i] )), BooleanClause.Occur.SHOULD );
             }
             return bq;
        }
        else if (fieldOperator.equalsIgnoreCase( QueryFieldOperator.FUZZY ))
        {
            
        }
        
        LOG.error( "Does not support field operator: '" + fieldOperator + "'" );
        // Return an OR query as the default ...
        BooleanQuery bq = new BooleanQuery( );
        for (int i = 0; i < terms.length; i++)
        {
            bq.add( new TermQuery( new Term( luceneField, terms[i] )), BooleanClause.Occur.SHOULD );
        }
        return bq;
    }
    
    private static SpanQuery[] getSpans( String luceneField, String[] terms )
    {
        if (terms == null) return new SpanQuery[0];
        
        SpanQuery[] spQus = new SpanQuery[ terms.length ];
        for ( int i = 0; i < terms.length; i++ )
        {
            spQus[i] = new SpanTermQuery( new Term( luceneField, terms[i] ) );
        }
        
        return spQus;
    }
    
    private static int getDistance( QueryFieldOperator qFieldOp )
    {
        IProperty distanceProp = qFieldOp.getModifier( QueryFieldOperator.DISTANCE );
        if ( distanceProp != null && distanceProp instanceof IntegerProperty )
        {
            IntegerProperty intProp = (IntegerProperty)distanceProp;
            return intProp.getIntegerValue( );
        }
        else if (distanceProp != null)
        {
        	try
        	{
        	    return Integer.parseInt( distanceProp.getValue( ) );
        	}
        	catch ( NumberFormatException nfe )
        	{
        		LOG.error( "Cannot convert: " + distanceProp.getValue( ) );
        	}
        }
        
        LOG.debug( "returning distance = " + DEFAULT_DISTANCE );
        return DEFAULT_DISTANCE;
    }
    
    private Query createRangeQuery( FinderField ff, QueryField qField, QueryFieldOperator queryOp )
    {
    	// Depending on FinderField data Type and queryOp field operator,
    	// NumericRangeQuery
    	// see http://lucene.apache.org/core/4_1_0/core/org/apache/lucene/search/NumericRangeQuery.html
    	
    	return null;
    }
    
    private Sort getLuceneSort( IQuery query )
    {
        SortProperty sortProp = query.getSortProperty( );
        if (sortProp != null)
        {
        	ArrayList<SortField> sortFields = new ArrayList<SortField>( );
        	addSortFields( sortProp, sortFields );
        	
        	SortField[] sortFieldList = new SortField[ sortFields.size( ) ];
        	sortFields.toArray( sortFieldList );
        	return new Sort( sortFieldList );
         }
        
        return null;
    }
    
    private void addSortFields( SortProperty sortProp, ArrayList<SortField> sortFields )
    {
    	String sortField = sortProp.getSortField( );
    	FinderField luceneField = getFinderField( sortField );
    	String sortDirection = sortProp.getSortDirection( );
    	// Check that the order is correct ...
    	boolean reverse = (sortDirection != null && sortDirection.equals( SortProperty.ASCENDING )) ? true : false;
       	SortField sf = new SortField( luceneField.getSourceFieldName( ), getSortFieldType( luceneField ), reverse );
        sortFields.add( sf );
        
        SortProperty secondarySort = sortProp.getSecondarySort( );
        if (secondarySort != null)
        {
        	addSortFields( secondarySort, sortFields );
        }
    }
    
    private SortField.Type getSortFieldType( FinderField ff )
    {
    	if (ff.getDataType().equals( FinderField.STRING ))
    	{
    	    return SortField.Type.STRING;
    	}
    	else if (ff.getDataType().equals( FinderField.INTEGER ))
    	{
    		return SortField.Type.INT;
    	}
    	else if (ff.getDataType().equals( FinderField.FLOAT ))
    	{
    		return SortField.Type.FLOAT;
    	}

    	
    	return SortField.Type.STRING;
    }
    
    private String getIDField(  )
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
    
    // TO DO: Handle other operators such as NEAR operators, etc...
    // 
    public static Query createBooleanQuery( QueryTree queryTree, String defaultField )
    {
    	LOG.debug( "createBooleanQuery " );
    	if (queryTree == null) return null;
    	
    	if (queryTree.getOperator() != null && (queryTree.getOperator().equals( QueryTree.TERM )
    			                             || queryTree.getOperator().equals( QueryTree.PHRASE )))
    	{
    		LOG.debug( "create  query " + queryTree.getQueryField() + " = " + queryTree.getQueryText() );
    		String[] tokens = StringMethods.getStringArray( queryTree.getQueryText(), InvertedIndex.tokenDelimiter );
    		
			String queryField = queryTree.getQueryField( );
			if (defaultField != null && queryTree.getDefaultQueryField() != null && queryField.equals( queryTree.getDefaultQueryField( ).getFieldName( ) ))
			{
				LOG.debug( "queryField = " + queryField + " defaultQueryField = " + queryTree.getDefaultQueryField( ).getFieldName( ) );
				queryField = defaultField;
			}
			
    		if (tokens.length == 1)
    		{
    		    return new TermQuery(new Term( queryField, tokens[0].toLowerCase( ) ) );
    		}
    		else
    		{
    			PhraseQuery pq = new PhraseQuery( );
                for ( int t = 0; t < tokens.length; t++ )
                {
                    ((PhraseQuery)pq).add( new Term( queryField, tokens[t].toLowerCase( ) ));
                }
                 
                return pq;
    		}
    	}
    	
        BooleanQuery boolQ = new BooleanQuery( );
        
        String operator = queryTree.getOperator();
        LOG.debug( "Operator = " + operator );
        BooleanClause.Occur bcl = (operator != null && (operator.equalsIgnoreCase( "AND" ) || operator.equalsIgnoreCase( "ALL" )) ) 
        		                ? BooleanClause.Occur.MUST
                                : BooleanClause.Occur.SHOULD;
        
        List<QueryTree> children = queryTree.getSubTrees( );
        
        for (Iterator<QueryTree> qtreeIt = children.iterator(); qtreeIt.hasNext(); )
        {
            Query chQ = createBooleanQuery( qtreeIt.next( ), defaultField );
            boolQ.add( chQ, bcl );           
        }
        
        // LOG.debug( boolQ.toString() );
        return boolQ;
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

    private String getLocalFieldName( String sourceField )
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

	@Override
	public void setSourceFieldName(String sourceFieldName)
	{
		this.sourceFieldName = sourceFieldName;
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
