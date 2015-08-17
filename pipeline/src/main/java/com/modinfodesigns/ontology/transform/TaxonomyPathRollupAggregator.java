package com.modinfodesigns.ontology.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.ontology.ITaxonomyNode;

import java.util.List;
import java.util.HashSet;

/**
 * Creates the set of paths contained by a TaxonomyNode and creates a
 * property [initial_node]_Type: [leaf_node]
 *
 * Example:
 *  Music Ontology - have hierarchical classes like
 *  Composer
 *    Arranger
 *    Lyricist
 *    Songwriter
 *
 *  Musician
 *    Instrumentalist
 *      Guitarist
 *      Pianist
 *      Violinist
 *    Singer
 *      Bass
 *      Tenor
 *      Alto
 *      Soprano
 *    Conductor
 *
 *  Transformer will create properties like
 *  Composer_Type: Songwriter
 *  Composer_type: Composer
 *
 *  And:
 *  Musician_Type: Singer
 *  Musician_Type: Guitarist
 *  Musician_Type: Instrumentalist
 *  Musician_Type: Musician
 */

public class TaxonomyPathRollupAggregator implements IPropertyHolderTransform
{
  private String pathSeparator = "/";
    
  private String fieldSuffix = "_Type";
    
  private boolean onlyLeafNodes = false;
  private boolean onlyParentNodes = false;
    
  private boolean skipRootNode = true;
    
  public void setFieldSuffix( String fieldSuffix )
  {
    this.fieldSuffix = fieldSuffix;
  }
    
  public void setOnlyLeafNodes( boolean onlyLeafNodes )
  {
    this.onlyLeafNodes = onlyLeafNodes;
  }
    
  public void setOnlyParentNodes( boolean onlyParentNodes )
  {
    this.onlyParentNodes = onlyParentNodes;
  }
    
  public void setSkipRootNode( boolean skipRootNode )
  {
    this.skipRootNode = skipRootNode;
  }
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (!(input instanceof ITaxonomyNode))
    {
      throw new PropertyTransformException( "Input must be an instance of ITaxonomyNode!" );
    }
      
    transformTaxo( (ITaxonomyNode)input );
      
    return input;
  }
    
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                         throws PropertyTransformException
  {
    if (!(input instanceof ITaxonomyNode))
    {
      throw new PropertyTransformException( "Input must be an instance of ITaxonomyNode!" );
    }
      
    transformTaxo( (ITaxonomyNode)input );
      
    return input;
  }
    
  private void transformTaxo( ITaxonomyNode taxoNode )
  {
    List<String> paths = taxoNode.getPaths( );
        
    if (paths != null)
    {
      for (String path : paths )
      {
        String[] segments = path.split( pathSeparator );
        int rootSeg = (skipRootNode) ? 1 : 0;
        if (segments.length < rootSeg + 1 ) return;
        String pathName = segments[ rootSeg ] + fieldSuffix;
        for (int i = rootSeg + 1; i < segments.length; i++ )
        {
          taxoNode.addProperty( new StringProperty( pathName, segments[i] ) );
        }
      }
    }
  }
    
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {
        
  }
}