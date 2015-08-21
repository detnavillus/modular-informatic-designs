package com.modinfodesigns.search.lucene;

import com.modinfodesigns.app.ApplicationManager;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.IQuantity;

import com.modinfodesigns.search.IFinder;
import com.modinfodesigns.search.IFinderIndexFeeder;
import com.modinfodesigns.search.FinderField;
import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.QueryField;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.FieldType;

import com.modinfodesigns.app.search.model.ContentSource;

import java.util.Iterator;
import java.util.Map;
import java.util.List;

import java.io.StringReader;
import java.io.IOException;
import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a Lucene Index searchable by a LuceneFinder.  
 * 
 * @author Ted Sullivan
 */

public class LuceneDataObjectIndexer implements IFinderIndexFeeder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( LuceneDataObjectIndexer.class );
    
    // private IndexWriter    
    private String contentSourceName;
    private LuceneFinder luceneFinder;
    private ContentSource contentSource;
    
    private IndexWriterConfig indexWriterConfig;
    
    // The class name of the analyzer Class: must implement Lucene Analyzer interface
    // and have a null constructor
    private String analyzerClass;

    public void setAnalyzerClass( String analyzerClass )
    {
        this.analyzerClass = analyzerClass;
    }
    
    
    @Override
    public IDataList processDataList( IDataList data )
    {
    	LOG.debug( "processDataList" );
    	
        try
        {
            IndexWriter ndxWriter = getIndexWriter( );
            
            if (ndxWriter != null)
            {
                Iterator<DataObject> dobjIt = data.getData( );
                while (dobjIt != null && dobjIt.hasNext() )
                {
                    DataObject dobj = dobjIt.next();
            
                    // Create a Lucene Document
                    if (shouldIndex( dobj ))
                    {
                        Document doc = getLuceneDocument( dobj );
                        ndxWriter.addDocument( doc );
                    }
                }
            
                ndxWriter.commit( );
                ndxWriter.close( );
            }
        }
        catch ( IOException ioe )
        {
            LOG.error( "Got IOException: " + ioe.getMessage( ) );
        }
        
        return data;
    }
    
    
    private Document getLuceneDocument( DataObject dobj )
    {
    	LOG.debug( "getLuceneDocument" );
    	
        // should probably create a document from the FinderFields and re use it before
        // adding to index ...
        
        Document lucDoc = new Document( );
        
        Map<String,FinderField> finderMap = getFinderMap( );
        if (finderMap == null) return null;
        
        String defaultField = getDefaultSearchField( );
        StringBuilder defaultFieldBuf = (defaultField != null) ? new StringBuilder( ) : null;
        
        for (Iterator<IProperty> propIt = dobj.getProperties(); propIt.hasNext(); )
        {
            IProperty dobjProp = propIt.next( );
            FinderField ff = finderMap.get( dobjProp.getName() );
            if (ff != null)
            {
                // check FinderField Properties, determine type of Lucene Field
                // If it is a defaultText field - add its contents to the LuceneFinder
                // default field..
                if (defaultField != null && ff.isDefaultTextField( ) )
                {
                	LOG.debug( " adding to " + defaultField );
                    defaultFieldBuf.append( " " ).append( getPropValue( dobjProp ) );
                }

                if (isTextProperty( dobjProp ))
                {
                    if (ff.isExactMatch( ) )
                    {
                        // make it a non-tokenized field
                        FieldType ft = new FieldType( );
                        ft.setTokenized( false );
                        //ft.setIndexed( true );
                        lucDoc.add( new Field(ff.getSourceFieldName(), new StringReader( getPropValue( dobjProp ) ), ft ));    
                    }
                    else if (ff.isSearchable() && !ff.isDisplayable() )
                    {
                        // make it an Unstored field
                        lucDoc.add( new TextField( ff.getSourceFieldName(), getPropValue( dobjProp ), Field.Store.NO ));
                    }
                    else if (ff.isDisplayable() && !ff.isSearchable() )
                    {
                        // make it an Unindexed field
                        FieldType ft = new FieldType( );
                        ft.setStored( true );
                        lucDoc.add( new Field(ff.getSourceFieldName(), new StringReader( getPropValue( dobjProp ) ), ft ));
                    }
                    else
                    {
                        // make it a Stored field ...
                      	LOG.debug( "    adding Stored field " + ff.getSourceFieldName() );
                        lucDoc.add( new TextField( ff.getSourceFieldName(), getPropValue( dobjProp ), Field.Store.YES ));
                    }
                }
                else
                {
                    if (dobjProp instanceof IntegerProperty )
                    {
                        IntegerProperty intProp = (IntegerProperty)dobjProp;
                        lucDoc.add( new IntField(ff.getSourceFieldName(), intProp.getIntegerValue(), Field.Store.YES ) );              
                    }
                    else if (dobjProp instanceof IQuantity )
                    {
                        IQuantity quantProp = (IQuantity)dobjProp;
                        lucDoc.add( new DoubleField( ff.getSourceFieldName( ), quantProp.getQuantity(), Field.Store.YES ));
                    }
                }
            }
        }
        
        // Add defaultField data to the Document ...
        if (defaultField != null)
        {
        	LOG.debug( "Adding defaultField data " + defaultFieldBuf.toString() );
            lucDoc.add( new TextField( defaultField, defaultFieldBuf.toString(), Field.Store.NO ));
        }
        
        // make sure that if the ContentSource has a ScopeQuery - we make sure that our documents
        // are correctly marked so that they will hit ...
        if (contentSource != null && contentSource.getQuery() != null)
        {
            // get the QueryField, find the Finder Field, set the value in the document etc...
            IQuery scopeQuery = contentSource.getQuery( );
            List<QueryField> qFields = scopeQuery.getQueryFields();
            for (int i = 0; i < qFields.size(); i++)
            {
                QueryField qField = qFields.get( i );
                String fieldName = qField.getFieldName( );
                FinderField ff = finderMap.get( fieldName );
                if (ff != null && dobj.getProperty( fieldName ) == null)
                {
                    // don't have a property, need to set one so that the Scope Query will work
                    String fieldValue = qField.getFieldValue( );
                    lucDoc.add( new TextField( ff.getSourceFieldName(), fieldValue, Field.Store.NO ));
                }
            }    
        }
        
        return lucDoc;
    }
    
    
    private boolean shouldIndex( DataObject dobj )
    {
        // return false if content source has a ScopeQuery and this DataObject
        // has a value that does not match the IQuery ... ( query value is not contained
        // in the object value)
    	
        ContentSource cs = getContentSource( );
        if (cs != null && cs.getQuery() == null) return true;
        
        IQuery scopeQuery = cs.getQuery( );
        List<QueryField> qFields = scopeQuery.getQueryFields();
        for (int i = 0; i < qFields.size(); i++)
        {
            QueryField qField = qFields.get( i );
            String fieldName = qField.getFieldName( );
            if (dobj.getProperty( fieldName ) != null)
            {
                IProperty objProp = dobj.getProperty( fieldName );
                String fieldValue = qField.getFieldValue( );
                
                // may need to get more sophisticated - field value equals
                // one of the property value tokens ...
                if (objProp.getValue().indexOf( fieldValue ) < 0) return false;
            }
        }
        
        return true;
    }
    
    private String getPropValue( IProperty prop )
    {
    	if (prop instanceof PropertyList )
    	{
    		PropertyList pList = (PropertyList)prop;
    		StringBuilder strbuilder = new StringBuilder( );
    		
    		Iterator<IProperty> propIt = pList.getProperties( );
    		while (propIt != null && propIt.hasNext( ) )
    		{
    			IProperty lProp = propIt.next( );
    			strbuilder.append( getPropValue( lProp ) );
    			strbuilder.append( " " );
    		}
    		
    		return strbuilder.toString( );
    	}
    	else
    	{
    	    return prop.getValue( );
    	}
    }
    
    
    private boolean isTextProperty( IProperty prop )
    {
        return (prop != null && !((prop instanceof IntegerProperty || prop instanceof IQuantity)));
    }
    
    
    private IndexWriter getIndexWriter(  ) throws IOException
    {
    	LOG.debug( "getIndexWriter( )..." );
    	
        if ( this.indexWriterConfig == null )
        {
            Analyzer analyzer = null;
            if (analyzerClass != null)
            {
                try
                {
                    analyzer = (Analyzer)Class.forName( analyzerClass ).newInstance( );
                }
                catch ( Exception e )
                {
                    LOG.error( "Could not instantiate analyzer " + analyzerClass );
                }
            }
            else
            {
                analyzer = new StandardAnalyzer(  );
            }
            
            indexWriterConfig = new IndexWriterConfig( analyzer );
        }
        
        return new IndexWriter( getDirectory( ), indexWriterConfig );
    }

    @Override
    public void processComplete( IPropertyHolder result, boolean status )
    {
        
    }

    @Override
    public void setContentSource( String contentSourceName )
    {
        this.contentSourceName = contentSourceName;
    }

    @Override
    public ContentSource getContentSource()
    {	
        if ( contentSourceName == null )
        {
            LOG.error( "ContentSource name is NULL!" );
            return null;
        }
        
        if (this.contentSource == null)
        {
            LOG.debug( "getContentSource( ) " + contentSourceName );
            // Use ApplicationManager to get the finder of our name 
            ApplicationManager appMan = ApplicationManager.getInstance( );
            this.contentSource = (ContentSource)appMan.getApplicationObject( contentSourceName, "ContentSource" );
        }
        
        return this.contentSource;
    }
    
    
    private LuceneFinder getLuceneFinder( )
    {	
        if (luceneFinder == null)
        {   
            ContentSource contentSource = getContentSource( );
            if (contentSource == null)
            {
                return null;
            }
            
            this.contentSource = contentSource;
            
            IFinder myFinder = contentSource.getFinder( );

            if (myFinder == null || !(myFinder instanceof LuceneFinder))
            {
                LOG.error( "Cannot get LuceneFinder!" );
                return null;
            }

            luceneFinder = (LuceneFinder)myFinder;
            LOG.debug( "found LuceneFinder: " + luceneFinder.getName( ) );
        }
        
        return luceneFinder;
    }
    
    
    private String getIndexDirectory(  )
    {
        LuceneFinder luceneFinder = getLuceneFinder( );
        
        return (luceneFinder != null) ? luceneFinder.getIndexDirectory( ) : null;
    }
    
    
    @SuppressWarnings("resource")
	private FSDirectory getDirectory(  ) throws IOException
    {
        String luceneDir = getIndexDirectory( );
        return (luceneDir != null) ? new SimpleFSDirectory( new File( luceneDir ).toPath( ) ) : null;
    }
    
    
    private Map<String,FinderField> getFinderMap( )
    {
        LuceneFinder luceneFinder = getLuceneFinder( );
        
        return (luceneFinder != null) ?  luceneFinder.getFinderMap( ) : null;
    }
    
    
    private String getDefaultSearchField( )
    {
        LuceneFinder luceneFinder = getLuceneFinder( );
        return (luceneFinder != null) ?  luceneFinder.getDefaultField( ) : null;
    }

}
