package com.modinfodesigns.ontology;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.ontology.builder.ITaxonomyBuilder;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.pipeline.source.IDataObjectSource;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.security.IUserCredentials;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a set of DataObjects from a Taxonomy.  Can be set to return all nodes
 * or just leaf nodes.
 *
 * @author Ted Sullivan
 */
public class TaxonomyDataSource implements IDataObjectSource
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TaxonomyDataSource.class );
    
  private String taxonomyBuilderName;
    
  private ITaxonomyBuilder taxoBuilder;
    
  private boolean leafNodesOnly = false;
    
  private boolean executeAllPerNode = true;   // if true execute all transforms on each node
                                            // if false, executes each transform on all nodes
    
  private ArrayList<IPropertyHolderTransform> dataTransforms;
    
  private ArrayList<IDataObjectProcessor> dataProcs;
    
  private HashSet<String> uniqueNodes;
    
    
  public TaxonomyDataSource(  )
  {
    LOG.info( "Creating TaxonomyDataSource ..." );
  }
    
  public void setTaxonomyBuilder( ITaxonomyBuilder taxoBuilder )
  {
    this.taxoBuilder = taxoBuilder;
  }
    
  public void setTaxonomyBuilderRef( String taxonomyBuilder )
  {
    this.taxonomyBuilderName = taxonomyBuilder;
  }
    
  public void setLeafNodesOnly( boolean leafNodesOnly )
  {
    this.leafNodesOnly = leafNodesOnly;
  }
    
  public void setLeafNodesOnly( String leafNodesOnly )
  {
    this.leafNodesOnly = (leafNodesOnly != null && leafNodesOnly.equalsIgnoreCase( "true" ));
  }
    
  public void setExecuteAllPerNode( boolean executeAllPerNode )
  {
    this.executeAllPerNode = executeAllPerNode;
  }
    
  public void setExecuteAllPerNode( String executeAllPerNode )
  {
    this.executeAllPerNode = (executeAllPerNode != null && executeAllPerNode.equalsIgnoreCase( "true" ));
  }
    
  @Override
  public void run( IUserCredentials withUser )
  {
    ITaxonomyBuilder theBuilder = getTaxonomyBuilder( );
    if (theBuilder == null)
    {
      LOG.error( "Could not get TaxonomyBuilder! " );
      return;
    }
        
    ITaxonomyNode taxo = theBuilder.buildTaxonomy( );
        
    uniqueNodes = new HashSet<String>( );
    DataList nodeList = new DataList( );
      
    if (taxo != null)
    {
      List<ITaxonomyNode> descendants = taxo.getDescendants( );
      if (descendants != null)
      {
        for (int i = 0; i < descendants.size( ); i++)
        {
          ITaxonomyNode descendant = descendants.get( i );
          if (!leafNodesOnly || descendant.isLeafNode( ) )
          {
            if (!uniqueNodes.contains( descendant.getID( ) ) )
            {
              // in this mode execute all transforms on each node
              if (executeAllPerNode && dataTransforms != null)
              {
                for (IPropertyHolderTransform dataTransform : dataTransforms )
                {
                  try
                  {
                    dataTransform.transformPropertyHolder( descendant );
                  }
                  catch (PropertyTransformException pte )
                  {
                    // Oh damn!
                  }
                }
              }
                
              uniqueNodes.add( descendant.getID( ) );
              if (descendant instanceof DataObject)
              {
                nodeList.addDataObject( (DataObject)descendant );
              }
              else
              {
                // not a DataObject - need to clone it!
                DataObject dobj = new DataObject( );
                dobj.setName( descendant.getName( ) );
                dobj.setID( descendant.getID( ) );
                dobj.setProperties( descendant.getProperties( ) );
                nodeList.addDataObject( dobj );
              }
            }
          }
        }

        // in this mode - execute each transform on all nodes
        if ( !executeAllPerNode && dataTransforms != null )
        {
          for (IPropertyHolderTransform dataTransform : dataTransforms )
          {
            System.out.println( "Running Data Transform: " + dataTransform );
            for (Iterator<DataObject> dit = nodeList.getData( ); dit.hasNext(); )
            {
              DataObject dobj = dit.next();
              try
              {
                dataTransform.transformPropertyHolder( dobj );
              }
              catch ( PropertyTransformException pte )
              {
                // Oh damn!
              }
            }
            System.out.println( "Data Transform DONE." );
          }
        }
                
        if ( nodeList.size( ) > 0 )
        {
          for (int p = 0; p < dataProcs.size( ); p++)
          {
            IDataObjectProcessor dataProc = dataProcs.get( p );
            dataProc.processDataList( nodeList );
          }
        }
      }
    }
  }
    
  private ITaxonomyBuilder getTaxonomyBuilder( )
  {
    if (taxoBuilder != null || taxonomyBuilderName == null) return taxoBuilder;
        
    synchronized( this )
    {
      if (taxoBuilder != null || taxonomyBuilderName == null) return taxoBuilder;
            
      ApplicationManager appMan = ApplicationManager.getInstance( );
      taxoBuilder = (ITaxonomyBuilder)appMan.getApplicationObject( taxonomyBuilderName, "TaxonomyBuilder" );
    }
        
    return taxoBuilder;
  }
    
    
  @Override
  public void run( )
  {
    run( (IUserCredentials)null );
  }
    
  public void addDataTransform( IPropertyHolderTransform dataTransform )
  {
    if (dataTransforms == null) dataTransforms = new ArrayList<IPropertyHolderTransform>( );
    dataTransforms.add( dataTransform );
  }
    
  @Override
  public void addDataObjectProcessor( IDataObjectProcessor dataProcessor )
  {
    if (dataProcs == null) dataProcs = new ArrayList<IDataObjectProcessor>( );
    dataProcs.add( dataProcessor );
  }
}
