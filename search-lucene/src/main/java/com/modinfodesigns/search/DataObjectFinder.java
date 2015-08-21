package com.modinfodesigns.search;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IntrinsicPropertyDelegate;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.time.DateProperty;

import com.modinfodesigns.pipeline.source.IDataObjectSource;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;

import com.modinfodesigns.utils.StringMethods;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;


import java.io.IOException;

import com.modinfodesigns.classify.InvertedIndex;

import com.modinfodesigns.search.lucene.LuceneFinder;

import com.modinfodesigns.security.IUserCredentials;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple In-Memory finder for cached DataObjects. Uses Lucene RAMDirectory
 * 
 * @author Ted Sullivan
 */

public class DataObjectFinder implements IFinder, IDataObjectProcessor
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataObjectFinder.class );
    
  public static final String ID_FIELD = "docID";
    
  private int MAX_DOCS = 1000;
    
  private IDataObjectSource dobjSource;
  private boolean initialized = true;
    
  private String name;
    
  private IUserCredentials userCred;
    
  private String sourceName;
    
    
  private HashMap<String,FinderField> finderFields;
    
  private Directory luceneIndex;
  private IndexWriterConfig indexWriterConfig;
    
  private static int idGenerator;
    
  private static Object lock_obj = new String( "Lock me up" );
    
  public void addDataSource( IDataObjectSource dobjSource )
  {
    setDataSource( dobjSource );
  }
    
  public void setDataSource( IDataObjectSource dobjSource )
  {
    this.dobjSource = dobjSource;
    this.initialized = false;
  }
    
  public void setUserCredentials( IUserCredentials userCred )
  {
    this.userCred = userCred;
  }
    
  public DataObjectFinder(  ) {  }

  public DataObjectFinder( String name )
  {
    this.name = name;
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
    
  private void initialize(  )
  {
    LOG.debug( "initialize( )..." );
    if (initialized) return;
    	
    synchronized( this )
    {
      if (initialized) return;
    		
      dobjSource.addDataObjectProcessor( this );
    		
      if (userCred != null)
      {
        dobjSource.run( userCred );
      }
      else
      {
        Thread runThread = new Thread( dobjSource );
        runThread.start( );
    		
        while( !initialized )
        {
          try
          {
            Thread.sleep( 500L );
          }
          catch (InterruptedException e )
          {
    				
          }
        }
      }
    }
  }
    

  @Override
  public IResultList executeQuery( IQuery query )
                                   throws FinderException
  {
    LOG.debug( "executeQuery( ) ..." );
    	
    if (luceneIndex == null)
    {
      if (!initialized)
      {
        initialize( );
      }
      else
      {
        throw new FinderException( "Lucene Index is not initialized!" );
      }
    }
    if (query == null)
    {
      throw new FinderException( "Query is NULL!" );
    }
        
    initialize( );
        
    try
    {
      BasicResultList brl = new BasicResultList( );
        
      DirectoryReader reader = DirectoryReader.open( luceneIndex );
      IndexSearcher iSearcher = new IndexSearcher( reader );
            
      Query lucQuery = getLuceneQuery( query );
      LOG.debug( "LuceneQuery: " + lucQuery.toString() );
      TopDocs tDocs = iSearcher.search( lucQuery, null, MAX_DOCS );
            
      ScoreDoc[] hits = tDocs.scoreDocs;
      int totalDocs = tDocs.totalHits;
      LOG.debug( this + " Got " + totalDocs + " hits." );
      brl.setTotalResults( totalDocs );
            
      int lastDoc = query.getStartRecord() + query.getPageSize() - 1;
      lastDoc = Math.min( lastDoc, totalDocs );
      LOG.debug( "lastDoc = " + lastDoc );
            
      for (int i = query.getStartRecord() - 1; i < lastDoc; i++)
      {
        Document hitDoc = iSearcher.doc(hits[i].doc);
        brl.addDataObject( getDataObject( hitDoc ) );
      }
            
      return brl;
    }
    catch( IOException ioe )
    {
      LOG.error( "Got IOException!" );
      throw new FinderException( "Got IOException: " + ioe.getMessage() );
    }
  }
    
  private DataObject getDataObject( Document luceneDoc )
  {
    LOG.debug( "getDataObject( ) ..." );
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
        if (name.equals( ID_FIELD ))
        {
          dobj.setID( value );
        }
        else
        {
          dobj.addProperty( new StringProperty( name, value ));
        }
      }
    }
        
    return dobj;
  }

  @Override
  public DataObject getSingleResult( String resultID )
                                     throws FinderException
  {
    initialize( );
    	
    com.modinfodesigns.search.Query query = new com.modinfodesigns.search.Query( );
    query.addQueryField( new QueryField( ID_FIELD, resultID ));
    IResultList res = executeQuery( query );
        
    if (res != null && res.size() > 0)
    {
      return res.item( 0 );
    }
        
    return null;
  }

  @Override
  public Set<String> getQueryFields()
  {
    if (finderFields == null) return null;
        
    HashSet<String> queryFields = new HashSet<String>( );
        
    for (Iterator<String> fieldIt = finderFields.keySet().iterator(); fieldIt.hasNext(); )
    {
      String field = fieldIt.next( );
      FinderField sf = finderFields.get( field );
      if (sf.isSearchable( ))
      {
        queryFields.add( sf.getSourceFieldName( ) );
      }
    }
        
    return queryFields;
  }

  @Override
  public Set<String> getNavigatorFields()
  {
    return null;
  }

  @Override
  public Set<String> getResultFields()
  {
    if (finderFields == null) return null;
        
    HashSet<String> displayFields = new HashSet<String>( );
        
    for ( Iterator<String> fieldIt = finderFields.keySet().iterator(); fieldIt.hasNext(); )
    {
      String field = fieldIt.next( );
      FinderField sf = finderFields.get( field );
      if (sf.isDisplayable( ))
      {
        displayFields.add( sf.getSourceFieldName( ) );
      }
    }
        
    return displayFields;
  }
    
    
  public void addDataObjects( List<DataObject> dobjList )
  {
    addDataObjects( dobjList.iterator( ) );
  }
    
  public void addDataObjects( Iterator<DataObject> dobjIt )
  {
    ArrayList<IPropertyHolder> propHolderLst = new ArrayList<IPropertyHolder>( );
    while( dobjIt != null && dobjIt.hasNext())
    {
      propHolderLst.add( dobjIt.next( ) );
    }
    	
    addPropertyHolders( propHolderLst );
  }
    
  public void addPropertyHolders( List<IPropertyHolder> propHolders)
  {
    try
    {
      if (luceneIndex == null)
      {
        luceneIndex = new RAMDirectory( );
        Analyzer analyzer = new StandardAnalyzer( );
        indexWriterConfig = new IndexWriterConfig( analyzer );
      }
        
      IndexWriter iwriter = new IndexWriter( luceneIndex, indexWriterConfig );
        
      for (int i = 0; i < propHolders.size(); i++)
      {
        IPropertyHolder dobj = propHolders.get( i );
        if (dobj.getID() == null)
        {
          dobj.setID( getNextID( ) );
        }
                
        LOG.debug( this + " adding " + dobj.getValue( ) );
                
        // now build some search structures ...
        // first add some finder fields based on the properties in the DataObject ...
        Iterator<String> propertyNames = dobj.getPropertyNames( );
        Document doc = new Document( );
        
        while ( propertyNames != null && propertyNames.hasNext() )
        {
          String propName = propertyNames.next( );
          IProperty prop = dobj.getProperty( propName );
          if (prop != null && prop.getValue() != null && (!(prop instanceof IntrinsicPropertyDelegate)))
          {
            if (getFinderField( propName ) == null)
            {
              FinderField ff = new FinderField(  );
              ff.setName( propName );
              ff.setFieldName( propName );
              ff.setSourceFieldName( propName );
              ff.setDisplayName( propName );
              ff.setSearchable( true );
              ff.setDisplayable( true );
                    
              // To do: set based on property type ...
              ff.setDataType( getFinderType( prop ) );
                
              addFinderField( ff );
            }

            doc.add( new Field(prop.getName(), prop.getValue(), TextField.TYPE_STORED) );
          }
        }
            
        doc.add( new Field( ID_FIELD, dobj.getID(), TextField.TYPE_STORED ));
            
        iwriter.addDocument( doc );
      }
            
      iwriter.close( );
    }
    catch( Exception e )
    {
      LOG.error(  "Got Exception: " + e.getMessage( ) );
    }
  }
    
    
  private String getFinderType( IProperty prop )
  {
    if ( prop instanceof IntegerProperty )
    {
      return FinderField.INTEGER;
    }
    else if ( prop instanceof DateProperty )
    {
      return FinderField.DATE;
    }
    else if ( prop instanceof IQuantity )
    {
      return FinderField.FLOAT;
    }
        
    return FinderField.TEXT;
  }
    
  public void setFinderField( FinderField ff )
  {
    addFinderField( ff );
  }
    
    
  public void addFinderField( FinderField ff )
  {
    if (finderFields == null) finderFields = new HashMap<String,FinderField>( );
    finderFields.put( ff.getName(), ff );
  }

  @Override
  public Iterator<String> getFinderFields()
  {
    return (finderFields != null) ? finderFields.keySet().iterator() : null;
  }

  @Override
  public FinderField getFinderField(String name)
  {
    return (finderFields != null) ? finderFields.get( name ) : null;
  }
    
  private Query getLuceneQuery( IQuery query )
  {
    LOG.debug( "getLuceneQuery( ) ..." );
    	
    if (query == null) return null;
        
    if (query instanceof QueryTree)
    {
      return LuceneFinder.createBooleanQuery( (QueryTree)query, null );
    }
        
    String operator = query.getMultiFieldOperator( );
    if (operator == null) operator = IQuery.AND;
        
    // build a query from FinderFields ...
    // and query field values ...
    BooleanQuery bq = new BooleanQuery( );
    List<QueryField> fieldLst = query.getQueryFields( );
    if (fieldLst != null)
    {
      for (int i = 0; i < fieldLst.size( ); i++)
      {
        QueryField qField = fieldLst.get( i );
        String fieldName = qField.getFieldName( );
        LOG.debug( "processing field: " + fieldName );
                
        FinderField ff = finderFields.get( fieldName );
        if (ff != null)
        {
          String luceneField = ff.getSourceFieldName( );
          String fieldValue = qField.getFieldValue( ).toLowerCase( );
                    
          String[] terms = StringMethods.getStringArray( fieldValue, InvertedIndex.tokenDelimiter );
                    
          // add a Boolean Clause ...
          if (terms.length == 1 )
          {
            // check if PrefixQuery ...
            if (qField.getQueryFieldOperator() != null &&
                qField.getQueryFieldOperator().getFieldOperator().equals( QueryFieldOperator.PREFIX ))
            {
              bq.add( new PrefixQuery(new Term( luceneField, fieldValue )),
                      (operator.equals( IQuery.AND ) ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD) );
            }
            else
            {
              LOG.debug( "adding " + luceneField + " = " + fieldValue );
              bq.add( new TermQuery(new Term( luceneField, fieldValue )),
                    (operator.equals( IQuery.AND ) ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD) );
            }
          }
          else
          {
            Query pq = null;
                        
            QueryFieldOperator qFieldOp = qField.getQueryFieldOperator( );
            if ( qFieldOp != null )
            {
              pq = LuceneFinder.buildCompoundQuery( luceneField, terms, qFieldOp );
            }
            else
            {
              pq = new PhraseQuery( );
              for (int t = 0; t < terms.length; t++ )
              {
                ((PhraseQuery)pq).add( new Term( luceneField, terms[t] ));
              }
            }
            if (pq != null)
            {
              bq.add( pq, (operator.equals( IQuery.AND ) ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD) );
            }
          }
        }
      }
    }
    else
    {
      LOG.debug( "Field List is empty!" );
    }
        
    return bq;
  }
    

    
  private String getNextID( )
  {
    synchronized( lock_obj )
    {
      int myID = idGenerator++;
      return Integer.toString( myID );
    }
  }
    
  @Override
  public void finalize( )
  {
    try
    {
      if (luceneIndex != null)
      {
        luceneIndex.close( );
      }
    }
    catch( Exception e )
    {
            
    }
  }

  @Override
  public IDataList processDataList( IDataList data )
  {
    Iterator<DataObject> dataIt = data.getData( );
		
    addDataObjects( dataIt );
    return data;
  }

  @Override
  public void processComplete( IPropertyHolder result, boolean status)
  {
    initialized = true;
  }

  @Override
  public void setSourceFieldName( String sourceFieldName )
  {
    this.sourceName = sourceFieldName;
  }

  @Override
  public FinderField getSourceField()
  {
    return null;
  }

}
