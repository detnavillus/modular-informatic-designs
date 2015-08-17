package com.modinfodesigns.ontology;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.ontology.builder.ITaxonomyBuilder;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.pipeline.source.IDataObjectSource;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.compare.IPropertyHolderMatcher;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.security.IUserCredentials;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a set of DataObjects from a Taxonomy.  Can be set to return all nodes
 * or just leaf nodes, or nodes that match a filter (IPropertyHolderMatcher).
 * 
 * This enables nesting of TaxonomyDataSource in a DataListTaxonomyBuilder to
 * create a filtered (i.e. "pruned" ) taxonomy.
 * 
 * Adds Taxonomy properties such as path.
 * 
 * @author Ted Sullivan
 */
public class SimpleTaxonomyDataSource implements IDataObjectSource
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TaxonomyDataSource.class );

  private String taxonomyBuilderName;
    
  private ITaxonomyBuilder taxoBuilder;
    
  private boolean leafNodesOnly = false;
    
  private String pathProperty = "path";
    
  private String parentIDProp  = "parentID";
    
  private ArrayList<IDataObjectProcessor> dataProcs;
    
  private int batchSize = 50;
    
  private String dataMatcherName;
    
  private IPropertyHolderMatcher dobjMatcher;
    
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
    
  public void setDataMatcher( IPropertyHolderMatcher dataMatcher )
  {
    this.dobjMatcher = dataMatcher;
  }
    
  public void setDataMatcherRef( String dataMatcherName )
  {
    this.dataMatcherName = dataMatcherName;
  }
    
  public void setParentIDProperty( String parentIDProp )
  {
    this.parentIDProp = parentIDProp;
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
    IPropertyHolderMatcher dataMatcher = getDataMatcher( );
        
    if (taxo != null)
    {
      List<ITaxonomyNode> descendants = taxo.getDescendants( );
      if (descendants != null)
      {
        DataList dList = new DataList( );
                
        for (int i = 0; i < descendants.size( ); i++)
        {
          ITaxonomyNode descendant = descendants.get( i );
          if (!leafNodesOnly || descendant.isLeafNode( ) )
          {
            DataObject dobj = new DataObject( );
            dobj.setName( descendant.getName( ) );
            dobj.setID( descendant.getID( ) );
            dobj.setProperties( descendant.getProperties( ) );
                        
            List<String> paths = descendant.getPaths( );
            if (paths != null)
            {
              for (int p = 0; p < paths.size(); p++ )
              {
                String path = paths.get( p );
                dobj.addProperty( new StringProperty( pathProperty, path ));
              }
            }
                        
            if (dataMatcher == null || dataMatcher.equals( withUser, dobj ))
            {
              List<ITaxonomyNode> parents = descendant.getParents( );
              if (parents != null && parents.size() > 0)
              {
                for (int p = 0; p < parents.size(); p++ )
                {
                  ITaxonomyNode parent = parents.get( p );
                  dobj.addProperty( new StringProperty( parentIDProp, parent.getID( ) ));
                }
              }
                            
              dList.addDataObject( dobj );
            }
                        
            if ( dList.size( ) == batchSize )
            {
              for (int p = 0; p < dataProcs.size( ); p++)
              {
                IDataObjectProcessor dataProc = dataProcs.get( p );
                dataProc.processDataList( dList );
              }
                            
              dList.clearDataList( );
            }
          }
        }
                
        if (dList.size( ) > 0 )
        {
          for (int p = 0; p < dataProcs.size( ); p++)
          {
            IDataObjectProcessor dataProc = dataProcs.get( p );
            dataProc.processDataList( dList );
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
    
  private IPropertyHolderMatcher getDataMatcher( )
  {
    if (dobjMatcher != null || dataMatcherName == null) return dobjMatcher;
            
    synchronized( this )
    {
      if (dobjMatcher != null || dataMatcherName == null) return dobjMatcher;
        
      ApplicationManager appMan = ApplicationManager.getInstance( );
      dobjMatcher = (IPropertyHolderMatcher)appMan.getApplicationObject( dataMatcherName, "DataMatcher" );
    }
            
    return dobjMatcher;
  }
    
    
  @Override
  public void run( )
  {
    run( (IUserCredentials)null );
  }

  @Override
  public void addDataObjectProcessor( IDataObjectProcessor dataProcessor )
  {
    if (dataProcs == null) dataProcs = new ArrayList<IDataObjectProcessor>( );
    dataProcs.add( dataProcessor );
  }

}
