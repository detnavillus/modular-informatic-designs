package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.classify.IIndexMatcherFactory;
import com.modinfodesigns.classify.IIndexMatcher;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.ModInfoObjectFactory;

import com.modinfodesigns.utils.DOMMethods;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Uses an IIndexMatcherFactory to create a set of IIndexMatchers, copies their properties
// to DataObject and sends them to IDataObjectProcessor(s)

// To Do - enable individual IndexMatchers to be added at configuration time (by value or by
// reference.

public class IndexMatcherFactorySource extends ModInfoObjectFactory implements IDataObjectSource
{
  private transient static final Logger LOG = LoggerFactory.getLogger( IndexMatcherFactorySource.class );

  public static String INDEX_MATCHER_NAME = "IndexMatcherName";
    
  private ArrayList<IIndexMatcherFactory> factories;
  private ArrayList<String> factoryNames;
    
  private ArrayList<IDataObjectProcessor> dataProcessors;
  private ArrayList<String> processorNames;
    
  private boolean initialized = false;
    
  private static String baseName = "IndexMatcherFactorySource";
  private String name;
  private String factoryName;
    
  private HashMap<String,IIndexMatcher> matcherMap;

  private static int factoryNum;
    
    
  public IndexMatcherFactorySource( )
  {

  }
    
  public void setName( String name )
  {
    this.name = name;
  }

  @Override
  public void run()
  {
    initFactories( );
    
    if ((matcherMap == null) || dataProcessors == null)
    {
      LOG.error( "ERROR: No IndexMatcherFactory or no Data Processors" );
      return;
    }
        
    DataList data = new DataList( );
    for (Iterator<IIndexMatcher> matchIt = matcherMap.values().iterator(); matchIt.hasNext(); )
    {
      IIndexMatcher matcher = matchIt.next( );
      DataObject dobj = new DataObject( );
      loadProperties( dobj, matcher );
      data.addDataObject( dobj );
    }
        	
    for (int p = 0; p < dataProcessors.size( ); p++)
    {
      IDataObjectProcessor dataProc = dataProcessors.get( p );
      dataProc.processDataList( data );
      dataProc.processComplete( (IPropertyHolder)null, true );
    }
  }
    
  private void loadProperties( DataObject dobj, IIndexMatcher ndxMatcher )
  {
    for (Iterator<IProperty> propIt = ndxMatcher.getProperties( ); propIt.hasNext(); )
    {
      IProperty prop = propIt.next( );
      dobj.addProperty( prop.copy( ) );
    }
        
    // add property for ndxMatcher name
    if (ndxMatcher.getName() != null)
    {
      dobj.setProperty( new StringProperty( INDEX_MATCHER_NAME, ndxMatcher.getName( ) ));
    }
  }

  @Override
  public void run( IUserCredentials withUser )
  {
    run( );
  }

  @Override
  public void addDataObjectProcessor( IDataObjectProcessor dataProcessor )
  {
    if (dataProcessors == null) dataProcessors = new ArrayList<IDataObjectProcessor>( );
    dataProcessors.add( dataProcessor );
  }
    
  public void addDataProcessorRef( String processorName )
  {
    if (processorNames == null) processorNames = new ArrayList<String>( );
    processorNames.add( processorName );
  }
    
  public void addIndexMatcherFactory( IIndexMatcherFactory factory )
  {
    if (factory == null)
    {
      LOG.error( "IIndexMatcherFactory is NULL!" );
      return;
    }
    	
    if (factories == null) factories = new ArrayList<IIndexMatcherFactory>( );
    factories.add( factory );
  }
    
  public void addIndexMatcherFactoryRef( String factoryName )
  {
    if (factoryNames == null) factoryNames = new ArrayList<String>( );
    factoryNames.add( factoryName );
  }
    

  private static synchronized String getFactoryName( )
  {
    return baseName + "_" + Integer.toString( factoryNum++ );
  }
    
    
  private void initFactories(  )
  {
    if (initialized) return;
        
    synchronized( this )
    {
      if (initialized) return;
        
      ApplicationManager appMan = ApplicationManager.getInstance( );
            
      if (factoryNames != null)
      {
        for (int i = 0; i < factoryNames.size(); i++)
        {
          String factoryName = factoryNames.get( i );
          IIndexMatcherFactory ndxFactory = (IIndexMatcherFactory)appMan.getApplicationObject( factoryName, "IndexMatcherFactory" );
          addIndexMatcherFactory( ndxFactory );
        }
      }
            
      for (int i = 0; i < factories.size( ); i++)
      {
        IIndexMatcherFactory factory = factories.get( i );
        IIndexMatcher[] matchers = factory.createIndexMatchers( );
        for (int m = 0; m < matchers.length; m++)
        {
          if (matcherMap == null) matcherMap = new HashMap<String,IIndexMatcher>( );
          matcherMap.put( matchers[m].getName( ), matchers[m] );
        }
      }
            
      if (this.factoryName == null)
      {
        this.factoryName = getFactoryName( );
        appMan.addObjectFactory( this.factoryName, this );
      }
            
      if (processorNames != null)
      {
        for (int i = 0; i < processorNames.size(); i++)
        {
          String procName = processorNames.get( i );
          IDataObjectProcessor dataProc = (IDataObjectProcessor)appMan.getApplicationObject( procName, "DataProcessor" );
          addDataObjectProcessor( dataProc );
        }
      }
            
      initialized = true;
    }
  }

  @Override
  public void initialize( String configXML )
  {
    // Implement this to look for IndexMatcherFactory, IndexMatcherFactoryRef,
    //
    this.factoryName = getFactoryName( );
		
    Document doc = DOMMethods.getDocument( new StringReader( configXML ) );
    
    if (doc == null)
    {
      LOG.error( "Could not create Document from " + configXML );
      return;
    }
        
    Element docElem = doc.getDocumentElement( );
        
    initializeClassNameMappings( docElem );
        
    NodeList dataProcList = docElem.getElementsByTagName( "DataProcessor" );
    if (dataProcList != null && dataProcList.getLength() > 0)
    {
      for (int i = 0; i < dataProcList.getLength(); i++)
      {
        Element dataProcEl = (Element)dataProcList.item( i );
        IDataObjectProcessor dataProcessor = (IDataObjectProcessor)super.createObject( dataProcEl );
        addDataObjectProcessor( dataProcessor );
      }
    }
        
    NodeList dataProcRefList = docElem.getElementsByTagName( "DataProcessorRef" );
    if (dataProcRefList != null && dataProcRefList.getLength() > 0)
    {
      for (int i = 0; i < dataProcRefList.getLength(); i++)
      {
        Element dataProcRefEl = (Element)dataProcRefList.item( i );
        String procRef = DOMMethods.getText( dataProcRefEl );
        addDataProcessorRef( procRef );
      }
    }
        
    NodeList indexFacList = docElem.getElementsByTagName( "IndexMatcherFactory" );
    if (indexFacList != null && indexFacList.getLength() > 0)
    {
      for (int i = 0; i < indexFacList.getLength(); i++)
      {
        Element indexFacEl = (Element)indexFacList.item( i );
        IIndexMatcherFactory indexFac = (IIndexMatcherFactory)super.createObject( indexFacEl );
        addIndexMatcherFactory( indexFac );
      }
    }
        
    NodeList indexFacRefList = docElem.getElementsByTagName( "IndexMatcherFactoryRef" );
    if (indexFacRefList != null && indexFacRefList.getLength() > 0)
    {
      for (int i = 0; i < indexFacRefList.getLength(); i++)
      {
        Element indexFacRefEl = (Element)indexFacRefList.item( i );
        String indexRef = DOMMethods.getText( indexFacRefEl );
        addIndexMatcherFactoryRef( indexRef );
      }
    }
  }

  @Override
  public Object getApplicationObject( String name, String type )
  {
    initFactories( );
		
    if (type.equals( "IndexMatcher" ))
    {
      return (matcherMap != null) ? matcherMap.get( name ) : null;
    }
    else if (type.equals( "DataSource" ) && name.equals( this.name ))
    {
      return this;
    }
    else if (type.equals( "DataProcessor" ))
    {
      // iterate through the dataProcessors list ...
    }
		
    return null;
  }

  @Override
  public List<Object> getApplicationObjects( String type )
  {
    initFactories( );
		
    ArrayList<Object> objLst = new ArrayList<Object>( );
    if (type.equals( "DataSource" ))
    {
      objLst.add( this );
    }
    else if (type.equals( "IndexMatcher" ))
    {
      for (Iterator<IIndexMatcher> matchIt = matcherMap.values().iterator(); matchIt.hasNext(); )
      {
        objLst.add( matchIt.next() );
      }
    }
    else if (type.equals( "DataProcessor" ))
    {
      // convert dataProcessors to a List<Object>
    }
		
    return objLst;
  }

}
