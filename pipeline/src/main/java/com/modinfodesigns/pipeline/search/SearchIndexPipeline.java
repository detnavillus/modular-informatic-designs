package com.modinfodesigns.pipeline.search;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.pipeline.source.IDataObjectSource;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.search.IFinderIndexFeeder;

import com.modinfodesigns.pipeline.process.DataTransformProcessor;
import com.modinfodesigns.classify.Classifier;
import com.modinfodesigns.classify.EntityExtractorMatcher;
import com.modinfodesigns.classify.IIndexMatcherFactory;
import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.entity.IEntityExtractor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up and runs a Search Indexing Pipeline for a given IDataObjectSource and IFinder implementations.  Creates the necessary 
 * transformation and classification framework using FinderField properties (names of PropertyHolderTransform, EntityExtractor 
 * or IndexMatcherFactory configurations). 
 * 
 * Contains a field mapping from the DataObject source to the IndexFeeder FinderField schema. This 
 * map includes transformations such as the input field(s) and the outputField for either an auxiliary
 * IPropertyHolderTransform or for a Classifier transform (if the FinderField has an EntityExtractor
 * or IndexMatcherFactory reference ). 
 * 
 * Note: the IndexMatcherFactory will create IndexMatchers with their own property metadata. If these 
 * are to be used "as is" no additional mapping would be required. However, there may need to be 
 * property transforms or mapping operations to copy these classifier fields to the correct place 
 * in the FinderField schema.)
 * 
 * @author Ted Sullivan
 */

public class SearchIndexPipeline
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SearchIndexPipeline.class );

  private String name;
    
  // The Data Source from which the raw data will be acquired from
  private String dataSourceRef;
  private IDataObjectSource dataSource;
    
  // Pre-Classification Processors
  private ArrayList<String> preClassifyProcessorRefs;
    
  private HashMap<String,IDataObjectProcessor> dataProcessors;
    
  // Classification
  // map of IndexMatcher or EntityExtractor used to generate
  // Output fields
  //  InputField(s) for Classifier
  //  Output Field
  //  Name of IndexMatcher(Factory?) or EntityExtractor
  private ArrayList<ClassifierSource> classifierSources;
    
    
  // Post-classify Processors
  private ArrayList<String> postClassifyProcessorRefs;
    
  // Finder Index Feeder
  private String indexFeederRef;
  private IFinderIndexFeeder indexFeeder;
    
  public void setName( String name )
  {
    this.name = name;
  }
    
  public String getName( )
  {
    return this.name;
  }
    
  public void setDataSourceRef( String dataSourceRef )
  {
    this.dataSourceRef = dataSourceRef;
  }

  public void addDataSource( IDataObjectSource dataSource )
  {
    setDataSource( dataSource );
  }
    
  public void setDataSource( IDataObjectSource dataSource )
  {
    this.dataSource = dataSource;
  }
    
  public void addProcessorRef( String processorRef )
  {
    addPreClassifyProcessorRef( processorRef );
  }
    
  public void addDataProcessor( IDataObjectProcessor dataProcessor )
  {
    if (dataProcessors == null) dataProcessors = new HashMap<String,IDataObjectProcessor>( );
    String processorName = "Internal_" + Integer.toString( dataProcessors.size( ) );
    dataProcessors.put( processorName, dataProcessor );
    if (classifierSources == null)
    {
      addPreClassifyProcessorRef( processorName );
    }
    else
    {
      addPostClassifyProcessorRef( processorName );
    }
  }
    
  public void addPreClassifyProcessorRef( String processorRef )
  {
    if (preClassifyProcessorRefs == null) preClassifyProcessorRefs = new ArrayList<String>( );
    preClassifyProcessorRefs.add( processorRef );
  }
    
  public void addClassifierSource( ClassifierSource classifierSource )
  {
    if (classifierSources == null) classifierSources = new ArrayList<ClassifierSource>( );
    classifierSources.add( classifierSource );
  }

  public void addPostClassifyProcessorRef( String processorRef )
  {
    if (postClassifyProcessorRefs == null) postClassifyProcessorRefs = new ArrayList<String>( );
    postClassifyProcessorRefs.add( processorRef );
  }
    
  public void setIndexFeederRef( String indexFeederRef )
  {
    this.indexFeederRef = indexFeederRef;
  }

  public void addFinderIndexFeeder( IFinderIndexFeeder indexFeeder )
  {
    setFinderIndexFeeder( indexFeeder );
  }
    
  public void setFinderIndexFeeder( IFinderIndexFeeder indexFeeder )
  {
    this.indexFeeder = indexFeeder;
  }
    
  List<String> getClassificationFields( )
  {
    ArrayList<String> classificationFields = new ArrayList<String>( );
        
    if ( classifierSources != null )
    {
      for (int i = 0; i < classifierSources.size( ); i++)
      {
        ClassifierSource classSource = classifierSources.get( i );
        Set<String> outputFields = classSource.getOutputFields( );
        if (outputFields != null)
        {
          for (Iterator<String> fIt = outputFields.iterator(); fIt.hasNext(); )
          {
            classificationFields.add( fIt.next( ) );
          }
        }
      }
    }
        
    return classificationFields;
  }
    
  public String getClassificationTagger( String fieldName )
  {
    if ( classifierSources != null )
    {
      for (int i = 0; i < classifierSources.size( ); i++)
      {
        ClassifierSource classSource = classifierSources.get( i );
        Set<String> outputFields = classSource.getOutputFields( );
          
        if ( outputFields != null && outputFields.contains( fieldName ))
        {
          return classSource.taggerName;
        }
      }
    }
        
    return null;
  }

  public String getTaggerType( String fieldName )
  {
    if (classifierSources != null)
    {
      for (int i = 0; i < classifierSources.size( ); i++)
      {
        ClassifierSource classSource = classifierSources.get( i );
        Set<String> outputFields = classSource.getOutputFields( );
                
        if (outputFields != null && outputFields.contains( fieldName ))
        {
          return classSource.taggerType;
        }
      }
    }
        
    return null;
  }

  public void run()
  {
    LOG.debug( "run( )..." );
    // get the DataSource
    IDataObjectSource dataSource = getDataSource( );
        
    if (dataSource == null)
    {
      LOG.error( "Cannot get DataSource!" );
      return;
    }
        
    // add the DataProcessors
    ArrayList<IDataObjectProcessor> dataProcessors = getDataProcessors( );
    if (dataProcessors == null || dataProcessors.size( ) == 0)
    {
      LOG.error( "No DataProcessors!" );
      return;
    }
        
    for (int i = 0; i < dataProcessors.size( ); i++)
    {
      dataSource.addDataObjectProcessor( dataProcessors.get( i ) );
    }
        
    // start the DataSource
    LOG.debug( "Starting the DataSource: " + dataSource );
    dataSource.run( );
  }
    
  private IDataObjectSource getDataSource( )
  {
    if (this.dataSource != null) return dataSource;
        
    if (dataSourceRef == null) return null;
        
    ApplicationManager appMan = ApplicationManager.getInstance( );
        
    IDataObjectSource dataSource = (IDataObjectSource)appMan.getApplicationObject( dataSourceRef, "DataSource" );
    return dataSource;
  }
    
  private ArrayList<IDataObjectProcessor> getDataProcessors(  )
  {
    LOG.debug( "addDataProcessors( ) " );
        
    ArrayList<IDataObjectProcessor> dataProcessors = new ArrayList<IDataObjectProcessor>( );
        
    // add pre-classify processors
    addPreClassifyProcessors( dataProcessors );

    // set up the classifier / entity extractors
    addClassifierProcessors( dataProcessors );
        
    // get post-classify processors
    addPostClassifyProcessors( dataProcessors );
        
    addFinderIndexer( dataProcessors );
    
    return dataProcessors;
  }
    
    
  private void addPreClassifyProcessors( ArrayList<IDataObjectProcessor> dataProcList )
  {
    LOG.debug( "addPreClassifyProcessors( )" );
    if ( preClassifyProcessorRefs != null && preClassifyProcessorRefs.size( ) > 0 )
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
        
      for (int i = 0; i < preClassifyProcessorRefs.size(); i++)
      {
        String preClassifierRef = preClassifyProcessorRefs.get( i );
        IDataObjectProcessor proc = null;
        if (preClassifierRef.startsWith( "Internal_" ))
        {
          proc = dataProcessors.get( preClassifierRef );
        }
        else
        {
          proc = (IDataObjectProcessor)appMan.getApplicationObject( preClassifierRef, "DataProcessor" );
        }
                
                
        if (proc != null)
        {
          dataProcList.add( proc );
        }
        else
        {
          LOG.error( "Cannot create IDataObjectProcessor '" + preClassifierRef + "'" );
        }
      }
    }
    else
    {
      LOG.debug( "No Pre-Classify Processors." );
    }
  }
    
  // Create Classifier DataTransformProcessor(s)
  private void addClassifierProcessors( ArrayList<IDataObjectProcessor> dataProcList )
  {
    LOG.debug( "addClassifierProcessors( )" );
    if (classifierSources == null) return;
        
    ApplicationManager appMan = ApplicationManager.getInstance( );

    for (int i = 0; i < classifierSources.size( ); i++)
    {
      ClassifierSource classSource = classifierSources.get( i );
      Classifier classifier = createClassifier( classSource, dataProcList );
      if (classSource.taggerType.equals( "IndexMatcher" ))
      {
        IIndexMatcher indexMatcher = (IIndexMatcher)appMan.getApplicationObject( classSource.taggerName, "IndexMatcher" );
        classifier.addIndexMatcher( indexMatcher );
      }
      else if (classSource.taggerType.equals( "IndexMatcherFactory" ))
      {
        IIndexMatcherFactory indexMatcherFac = (IIndexMatcherFactory)appMan.getApplicationObject( classSource.taggerName, "IndexMatcherFactory" );
        classifier.addIndexMatcherFactory( indexMatcherFac );
      }
      else if (classSource.taggerType.equals( "EntityExtractor" ))
      {
        IEntityExtractor entityExtractor = (IEntityExtractor)appMan.getApplicationObject( classSource.taggerName, "EntityExtractor" );
        EntityExtractorMatcher eem = new EntityExtractorMatcher( );
        eem.setEntityExtractor( entityExtractor );
        eem.setAddMatchPhrasesProperty( classSource.entityTaggerField );
        eem.setAddMatchPhrasesMode( IIndexMatcher.MATCHED );
        classifier.addIndexMatcher( eem );
      }
    }
        
    // Get "Adhoc" Classifiers - these are classifiers that have been published and will be used to
    // add nodes to a composite "category" field (if supported by the Finder)
    // This would be any IndexMatcherFactory or EntityExtractor that have not already been added
    // directly (native or static classifiers) and have not been excluded for optimization reasons.
  }
    
  private Classifier createClassifier( ClassifierSource  classSource, ArrayList<IDataObjectProcessor> dataProcList )
  {
    DataTransformProcessor dtp = new DataTransformProcessor( );
    Classifier classifier = new Classifier( );
    String[] inputFields = classSource.getInputFields( );
    for (int c = 0; c < inputFields.length; c++)
    {
      classifier.addClassifyField( inputFields[c] );
    }
        
    dtp.addDataTransform( classifier );
    dataProcList.add( dtp );
    
    return classifier;
  }
    
  private void addPostClassifyProcessors( ArrayList<IDataObjectProcessor> dataProcList )
  {
    LOG.debug( "addPostClassifyProcessors( )" );
        
    if ( postClassifyProcessorRefs != null && postClassifyProcessorRefs.size( ) > 0  )
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
            
      for (int i = 0; i < postClassifyProcessorRefs.size(); i++)
      {
        String postClassifierRef = postClassifyProcessorRefs.get( i );
                
        IDataObjectProcessor proc = null;
        if (postClassifierRef.startsWith( "Internal_" ))
        {
          proc = dataProcessors.get( postClassifierRef );
        }
        else
        {
          proc = (IDataObjectProcessor)appMan.getApplicationObject( postClassifierRef, "DataProcessor" );
        }
                
        if (proc != null)
        {
          dataProcList.add( proc );
        }
        else
        {
          LOG.error( "Cannot create IDataObjectProcessor '" + postClassifierRef + "'" );
        }
      }
    }
    else
    {
      LOG.debug( "No Post-Classify Processors." );
    }
        
    // If any "Adhoc" classifiers have been added, need to merge their output into a category
    // field (defined by the Finder) using the following structure
    //
    // Category=[AdhocField]/[AdhocFieldValue]
        
    // Create a Source tagger using any ContentSource implementations that use this pipeline's
    // Finder and have a Scope Query. The Scope Queries should be used to build a Classifier
    // that will tag the Finder's 'Source' Field.
    
  }
    
  private void addFinderIndexer( ArrayList<IDataObjectProcessor> dataProcList )
  {
    LOG.debug( "addFinderIndexer( )" );
        
    if (indexFeeder != null)
    {
      dataProcList.add( indexFeeder );
    }
    else
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
      IFinderIndexFeeder indexFeeder = (IFinderIndexFeeder)appMan.getApplicationObject( indexFeederRef, "FinderIndexFeeder" );
      if (indexFeeder != null)
      {
        dataProcList.add( indexFeeder );
      }
      else
      {
        LOG.error( "Cannot create IFinderIndexFeeder '" + indexFeederRef + "'" );
      }
    }
  }
}
