package com.modinfodesigns.ontology.transform;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.ontology.ITaxonomyNode;
import com.modinfodesigns.ontology.TaxonomyException;
import com.modinfodesigns.ontology.TaxonomyNode;
import com.modinfodesigns.ontology.builder.ITaxonomyBuilder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.string.StringProperty;

// Used for ITaxonomyBuilder functionality
import com.modinfodesigns.pipeline.source.IDataObjectSource;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms an IPropertyHolder by converting a set of path strings into a hierarchical 
 * DataObject (an ITaxonomyNode ). Path Strings can come from other properties in the
 * IPropertyHolder.
 * 
 * @author Ted Sullivan
 */

// Problem: strips any other properties that the input may have - should keep them if possible
// ITaxonomyBuilder too ...

public class TaxonomyPathTransform implements IPropertyHolderTransform, ITaxonomyBuilder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TaxonomyPathTransform.class );

  private String pathProperty;     // property from which paths are to be extracted
    
  private String separator = "/";
    
  private String taxonomyProperty; // property where finished taxonomy should be placed.
                                     // if not set, return the taxonomy itself as the output
  private String countProperty;
    
  private String nodePathProperty;  // if set, will put the node's path as a property
    
  private String pathDataSourceName;  // name of DataSource that will get paths for building a Taxonomy
    
  public void setPathProperty( String pathProperty )
  {
    this.pathProperty = pathProperty;
  }
    
  public void setTaxonomyProperty( String taxonomyProperty )
  {
    this.taxonomyProperty = taxonomyProperty;
  }
    
  public void setCountProperty( String countProperty )
  {
    this.countProperty = countProperty;
  }

  public void setNodePathProperty( String nodePathProperty )
  {
    this.nodePathProperty = nodePathProperty;
  }
    
  @Override
  public IProperty transform( IProperty input )
                              throws PropertyTransformException
  {
    // convert this to an ITaxonomyNode
    return createTaxonomy( getPathStrings( input ) );
  }


  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    if (input == null) return null;
    IProperty pathProp = input.getProperty( pathProperty );
    ITaxonomyNode taxo = (ITaxonomyNode)transform( pathProp );
        
    if (taxonomyProperty != null)
    {
      taxo.setName( taxonomyProperty );
      input.addProperty( taxo );
      return input;
    }
    else
    {
      return taxo;
    }
  }
    
  private ArrayList<String> getPathStrings( IProperty inputProp )
  {
    ArrayList<String> pathStrings = new ArrayList<String>( );
    if (inputProp instanceof StringListProperty)
    {
      StringListProperty slp = (StringListProperty)inputProp;
      String[] strings = slp.getStringList( );
      for (int i = 0, isz = strings.length; i < isz; i++)
      {
        pathStrings.add( strings[i] );
      }
    }
    else if (inputProp instanceof PropertyList)
    {
      PropertyList pl = (PropertyList)inputProp;
      Iterator<IProperty> propIt = pl.getProperties( );
      while( propIt != null && propIt.hasNext() )
      {
        pathStrings.add( propIt.next().getValue() );
      }
    }
    else
    {
      pathStrings.add( inputProp.getValue( ) );
    }
        
    return pathStrings;
  }
    
  @Override
  public ITaxonomyNode buildTaxonomy()
  {
    PathCollector pc = new PathCollector( this.pathDataSourceName );
    return createTaxonomy( pc.getPaths( ) );
  }
	
  /**
   * Creates an Taxonomy from a set of path strings.
   *
   * @param pathStrings    List of Strings used to construct the Taxonomy
   *
   * @return an ITaxonomyNode implementation
  */
  public ITaxonomyNode createTaxonomy( List<String> pathStrings )
  {
    return createTaxonomy( "", pathStrings );
  }
    
  public ITaxonomyNode createTaxonomy( String taxoName, List<String>pathStrings )
  {
    TaxonomyNode taxo = createTaxonomy( taxoName );
    addNodes( taxo, pathStrings );
    return taxo;
  }
    
  private static TaxonomyNode createTaxonomy( String taxoName )
  {
    TaxonomyNode taxo = new TaxonomyNode( );
    taxo.setName( taxoName );
    return taxo;
  }
    
  private void addNodes( ITaxonomyNode taxo, List<String> pathStrings )
  {
    if (pathStrings == null) return;
    	
    for ( int i = 0, isz = pathStrings.size(); i < isz; i++)
    {
      try
      {
        addNode( taxo, pathStrings.get( i ) );
      }
      catch ( TaxonomyException te )
      {
        LOG.error( "Got TaxonomyException! " + te );
      }
    }
  }
    
  private void addNode( ITaxonomyNode parentNode, String path ) throws TaxonomyException
  {
    String completePath = (path != null) ? path.trim() : "";
    if (path == null || path.trim().length() == 0) return;
        
    if (completePath.indexOf( separator ) == 0)
    {
      completePath = completePath.substring( 1 );
    }
    if (completePath.endsWith( separator ))
    {
      completePath = completePath.substring( 0, completePath.lastIndexOf( separator ) );
    }
        
    String name    = (completePath.indexOf( separator ) > 0) ? completePath.substring( 0, completePath.indexOf( separator )) : completePath;
    String subPath = (completePath.indexOf( separator ) > 0) ? completePath.substring( completePath.indexOf( separator ) + 1) : null;
        
    ITaxonomyNode childNode = parentNode.getChildNode( name );
    if (childNode == null)
    {
      childNode = createNode( name );
      parentNode.addChildNode( childNode );
    }

    if (countProperty != null)
    {
      IntegerProperty countProp = (IntegerProperty)childNode.getProperty( countProperty );
      if (countProp != null) countProp.increment( );
      else childNode.addProperty( new IntegerProperty( countProperty, 1 ));
    }
        
    if (nodePathProperty != null)
    {
      childNode.setProperty( new StringProperty( nodePathProperty, path ));
    }

    if (subPath != null && subPath.trim().length() > 0)
    {
      addNode( childNode, subPath );
    }
  }
    
  private ITaxonomyNode createNode( String name )
  {
    TaxonomyNode taxoNode = new TaxonomyNode( );
    taxoNode.setName( name );
    return taxoNode;
  }

  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }

  // Inner class to Collect Path strings from a DataSource
  class PathCollector implements IDataObjectProcessor
  {
    IDataObjectSource doSource;
        
    ArrayList<String> paths = new ArrayList<String>( );
        
    PathCollector( String pathDataSourceName )
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
      doSource = (IDataObjectSource)appMan.getApplicationObject( pathDataSourceName, "DataSource" );
    }
        
    List<String> getPaths(  )
    {
      if (doSource == null) return paths;
        
      doSource.addDataObjectProcessor( this );
      doSource.run( );
        
      return paths;
    }
        
    @Override
    public IDataList processDataList( IDataList data )
    {
      Iterator<DataObject> datIt = data.getData( );
      while( datIt != null && datIt.hasNext() )
      {
        DataObject dobj = datIt.next( );
        IProperty nodePath = dobj.getProperty( nodePathProperty );
        if (nodePath != null)
        {
          // check if its a PropertyList or StringListProperty
          if (nodePath instanceof PropertyList )
          {
            PropertyList pList = (PropertyList)nodePath;
            Iterator<IProperty> propIt = pList.getProperties( );
            while( propIt != null && propIt.hasNext() )
            {
              IProperty nP = propIt.next( );
              paths.add( nP.getValue( ) );
            }
          }
          else if (nodePath instanceof StringListProperty)
          {
            StringListProperty slp = (StringListProperty)nodePath;
            String[] pathLst = slp.getStringList( );
            for (int p = 0, psz = pathLst.length; p < psz; p++)
            {
              paths.add( pathLst[p] );
            }
          }
          else
          {
            paths.add( nodePath.getValue( ) );
          }
        }
      }
        
      return null;
    }

    @Override
    public void processComplete( IPropertyHolder result, boolean status)
    {
			
    }
  }

  @Override
  public ITaxonomyNode buildTaxonomy( DataObject context )
  {
    IProperty pathProp = context.getProperty( pathProperty );
    if (pathProp != null)
    {
      try
      {
        ITaxonomyNode taxo = (ITaxonomyNode)transform( pathProp );
    
        if (taxonomyProperty != null)
        {
          taxo.setName( taxonomyProperty );
        }
                
        return taxo;
      }
      catch ( PropertyTransformException pte )
      {
            
      }
    }
    else
    {
      // report error - no path property to use ...
    }
        
    return null;
  }
}
