package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyException;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.pipeline.source.IDataObjectSource;
import com.modinfodesigns.pipeline.source.DataListObjectSource;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.schema.DataObjectSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a hierarchical Taxonomy structure from a list of DataObjects. The DataObjects are assumed to have a
 * property that contains the ID of their parent object. If an object does not have a parent ID property, it
 * is assumed to be an "orphan" and will be put as a child of the root of the output Taxonomy. If there
 * is only one orphan object, it will become the root node of the output Taxonomy.
 * 
 * If the DataSource is a TaxonomyDataSource (with a DataMatcher filter), Taxonomy filtering is
 * possible.
 * 
 * @author Ted Sullivan
 */

public class DataListTaxonomyBuilder implements ITaxonomyBuilder, IDataObjectProcessor
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataListTaxonomyBuilder.class );

  private String name;
	
  private String dataSourceName;
	
  private IDataObjectSource dataSource;
	
  private String parentIDProp = "Parent";
	
  private TaxonomyNode taxo;
	
  public void setName( String name )
  {
    this.name = name;
  }
	
  public void setDataSourceRef( String dataSourceName )
  {
    this.dataSourceName = dataSourceName;
  }
	
  public void addDataSource( IDataObjectSource dataSource )
  {
    setDataSource( dataSource );
  }
	
  public void setDataSource( IDataObjectSource dataSource )
  {
    LOG.debug( "setDataSource: " + dataSource );
    this.dataSource = dataSource;
  }
	
  public void setParentIDProperty( String parentIDProp )
  {
    this.parentIDProp = parentIDProp;
  }
	
  private HashMap<String,ArrayList<DataObject>> parentIDMap = new HashMap<String,ArrayList<DataObject>>( );
  private ArrayList<DataObject> orphans = new ArrayList<DataObject>( );
    
    
  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    // get the parameters from the context object ...
    if (context instanceof IDataList)
    {
      IDataList contextList = (IDataList)context;
      this.dataSource = new DataListObjectSource( contextList );
      return buildTaxonomy( );
    }
		
    return null;
  }
	
  @Override
  public ITaxonomyNode buildTaxonomy()
  {
    LOG.debug( "buildTaxonomy( )" );
		
    if ( this.taxo != null ) return this.taxo;
		
    IDataObjectSource mySource = getDataSource( );
    if (mySource == null)
    {
      LOG.error( "Cannot create Taxonomy: DataSource is NULL!" );
      return null;
    }
		
    synchronized( this )
    {
      parentIDMap.clear( );
      orphans.clear( );
			
      mySource.addDataObjectProcessor( this );
      mySource.run( );

      LOG.debug( "have " + parentIDMap.size( ) + " nodes " + orphans.size( ) + " orphans" );
			
      // ===========================================================
      // at this point, we should have the data that we need to build
      // the Taxonomy
      // ===========================================================
      this.taxo = new TaxonomyNode( );
      if (orphans.size() == 0)
      {
        taxo.setName( name );
        addTaxonomyNodes( taxo );
      }
      else if (orphans.size() == 1)
      {
        LOG.debug( "Single Rooted Taxonomy" );
        DataObject root = orphans.get( 0 );
        taxo.setName( root.getName( ) );
        taxo.setID( root.getID( ) );
            	
        addTaxonomyNodes( taxo );
      }
      else if (orphans.size( ) > 0)
      {
        LOG.debug( "got " + orphans.size( ) + " orphans!" );
            	
        taxo.setName( "root" );
        // taxo has no ID now ...
            	
        for (int i = 0; i < orphans.size(); i++)
        {
          DataObject orphan = orphans.get( i );
          try
          {
            TaxonomyNode chNode = new TaxonomyNode( );
            chNode.setName( orphan.getName( ) );
            chNode.setID( orphan.getID( ) );
            chNode.setProperties( orphan.getProperties( ) );
            taxo.addChildNode( chNode );
            		    
            addTaxonomyNodes( chNode );
          }
          catch ( TaxonomyException te )
          {
            LOG.error( "Got TaxonomyException! " + te );
          }
        }
      }
            
      return taxo;
    }
  }
	
  private IDataObjectSource getDataSource(  )
  {
    if (dataSource != null || dataSourceName == null) return dataSource;
		
    synchronized( this )
    {
      if (dataSource != null) return dataSource;
			
      ApplicationManager appMan = ApplicationManager.getInstance( );
      this.dataSource = (IDataObjectSource)appMan.getApplicationObject( dataSourceName, "DataSource" );
    }
		
    return dataSource;
  }
	
  private void addTaxonomyNodes( TaxonomyNode parentNode )
  {
    LOG.debug( "addTaxonomyNodes " + parentNode.getName( ) );
		
    ArrayList<DataObject> children = parentIDMap.get( parentNode.getID( ) );
		
    if (children != null && children.size( ) > 0)
    {
      for (int i = 0; i < children.size( ); i++ )
      {
        DataObject childOb = children.get( i );
        try
        {
          TaxonomyNode childNode = new TaxonomyNode( );
          childNode.setName( childOb.getName( ) );
          childNode.setID( childOb.getID( ) );
          childNode.setProperties( childOb.getProperties( ) );
          parentNode.addChildNode( childNode );
            
          addTaxonomyNodes( childNode );
        }
        catch (TaxonomyException te )
        {
          LOG.error( "Got TaxonomyException! " + te );
        }
      }
    }
  }

  @Override
  public IDataList processDataList( IDataList data )
  {
    LOG.debug( "processDataList( )..." );
		
    Iterator<DataObject> datIt = data.getData( );
    while( datIt != null && datIt.hasNext() )
    {
      DataObject dobj = datIt.next();
      IProperty parentProp = dobj.getProperty( parentIDProp );

      if (parentProp == null || parentProp.getValue( ) == null || parentProp.getValue().trim().length() == 0)
      {
        orphans.add( dobj );
      }
      else
      {
        ArrayList<DataObject> childList = parentIDMap.get( parentProp.getValue( ) );
        if ( childList == null )
        {
          childList = new ArrayList<DataObject>( );
          parentIDMap.put( parentProp.getValue( ), childList );
        }
				
        childList.add( dobj );
      }
    }
		
    return data;
  }

  @Override
  public void processComplete( IPropertyHolder result, boolean status )
  {
    
  }

}
