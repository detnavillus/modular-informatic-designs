package com.modinfodesigns.search.solr;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.search.model.ContentSource;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringListProperty;

import com.modinfodesigns.search.FinderField;
import com.modinfodesigns.search.IFinder;
import com.modinfodesigns.search.IFinderIndexFeeder;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrServer;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SOLRDataObjectIndexer implements IFinderIndexFeeder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SOLRDataObjectIndexer.class );
    
  private ContentSource contentSource;
  private String contentSourceName;
  private SOLRFinder solrFinder;

  private int batchSize = 50;
    
  private HttpSolrServer solrServer;

  private String serverURL;
    
  public void setServerURL( String serverURL )
  {
    this.serverURL = serverURL;
  }
    
  private ArrayList<SolrInputDocument> solrDocuments = new ArrayList<SolrInputDocument>( );
    
  @Override
  public IDataList processDataList( IDataList data )
  {
    LOG.debug( "processDataList( )..." );
    try
    {
      SolrClient solrServer = getSOLRClient( );
			
      for ( Iterator<DataObject> dobjIt = data.getData(); dobjIt.hasNext( ); )
      {
        DataObject dobj = dobjIt.next();
        SolrInputDocument solrDoc = createSOLRDoc( dobj );
        if (solrDoc != null)
        {
          solrDocuments.add( solrDoc );
          if (solrDocuments.size( ) >= batchSize)
          {
            solrServer.add( solrDocuments );
            // solrServer.commit( );
            solrDocuments.clear( );
          }
        }
      }
    		
      if (solrDocuments.size() > 0)
      {
        LOG.debug( "Indexing " + solrDocuments.size( ) + " docs." );
        solrServer.add( solrDocuments );
        solrServer.commit( );
      }
    }
    catch( Exception e )
    {
      LOG.debug( "Got Exception: " + e );
    }
		
    return data;
  }
	
  private SolrInputDocument createSOLRDoc( DataObject dobj )
  {
    LOG.debug( "createSOLRDoc " );
		
    SolrInputDocument solrDoc = new SolrInputDocument( );
    if (!hasIDField( dobj ))
    {
      LOG.error( "ID Field is NULL not indexing" );
      return null;
    }
		
    for ( Iterator<String> it = dobj.getPropertyNames( ); it.hasNext( ); )
    {
      String fieldName = (String)it.next();
      // LOG.debug( "got object field = " + fieldName );
        
      IProperty fieldProp = dobj.getProperty( fieldName );
      String solrFieldName = getSOLRField( fieldName );
        
      if ( solrFieldName != null )
      {
        ArrayList<String> propValues = getPropertyValues( fieldProp );
          
        for (int i = 0; i < propValues.size( ); i++)
        {
          solrDoc.addField( solrFieldName, propValues.get( i ) );
        }
      }
    }
        
    return solrDoc;
  }
	
  private boolean hasIDField( DataObject dobj )
  {
    SOLRFinder solrFinder = getSOLRFinder( );
    if (solrFinder == null)
    {
      LOG.debug( "Can't get SOLRFinder!" );
      return false;
    }
		
    String IDField = (solrFinder != null) ? solrFinder.getIDField( ) : null;
		
    if (IDField != null)
    {
      LOG.debug( "Checking for " + IDField + " = " + solrFinder.getFieldName( IDField ) );
      IProperty idProp = dobj.getProperty( solrFinder.getFieldName( IDField ) );
      if (idProp != null && idProp.getValue() != null && idProp.getValue().trim().length() > 0)
      {
        return true;
      }
    }
		
    return false;
  }
	
  private SolrClient getSOLRClient( )
  {
    if (this.solrServer != null) return this.solrServer;
		
    this.solrServer = new HttpSolrServer( this.serverURL );
    return solrServer;
  }
	
  ArrayList<String> getPropertyValues( IProperty prop )
  {
    ArrayList<String> propVals = new ArrayList<String>( );
    if (prop instanceof PropertyList)
    {
      PropertyList pl = (PropertyList)prop;
      for (Iterator<IProperty> propIt = pl.getProperties(); propIt.hasNext(); )
      {
        IProperty lp = propIt.next();
        ArrayList<String> lvals = getPropertyValues( lp );
        propVals.addAll( lvals );
      }
    }
    else if (prop instanceof StringListProperty)
    {
      StringListProperty slp = (StringListProperty)prop;
      String[] values = slp.getStringList( );
      for (int i = 0; i < values.length; i++)
      {
        propVals.add( values[i] );
      }
    }
    else
    {
      propVals.add( prop.getValue() );
    }
		
    return propVals;
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
    if (this.contentSource != null) return this.contentSource;
		
    this.contentSource = (ContentSource)ApplicationManager.getInstance()
                                                          .getApplicationObject( contentSourceName, "ContentSource" );
    return this.contentSource;
  }
	
  private String getSOLRField( String fieldName )
  {
    SOLRFinder solrFinder = getSOLRFinder( );
    if (solrFinder != null)
    {
      FinderField solrField = solrFinder.getFinderField( fieldName );
      return (solrField != null) ? solrField.getSourceFieldName( ) : null;
    }
    else
    {
      LOG.error( "Cannot get SOLRFinder " );
    }
		
    return null;
  }
	
  float getFieldBoost( String fieldName )
  {
    SOLRFinder solrFinder = getSOLRFinder( );
    if (solrFinder != null)
    {
      FinderField solrField = solrFinder.getFinderField( fieldName );
      float boost = solrField.getFieldBoost( );
      // LOG.debug( "Got boost = " + boost );
      return boost;
    }
		
    return (float)0.0;
  }

  private SOLRFinder getSOLRFinder( )
  {
    if (solrFinder == null)
    {
      ContentSource contentSource = getContentSource( );
      if (contentSource == null)
      {
        LOG.error( "Cannot find ContentSource" );
        return null;
      }
            
      this.contentSource = contentSource;
        
      IFinder myFinder = contentSource.getFinder( );

      if (myFinder == null || !(myFinder instanceof SOLRFinder))
      {
        LOG.error( "Cannot get SOLRFinder! " + myFinder );
        return null;
      }

      solrFinder = (SOLRFinder)myFinder;
      LOG.debug( "found SOLRFinder: " + solrFinder.getName( ) );
    }
        
    return solrFinder;
  }
}
