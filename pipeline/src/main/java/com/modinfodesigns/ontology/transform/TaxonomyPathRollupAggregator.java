package com.modinfodesigns.ontology.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.ontology.ITaxonomyNode;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private transient static final Logger LOG = LoggerFactory.getLogger( TaxonomyPathRollupAggregator.class );
    
  private String pathSeparator = "/";
    
  private String fieldSuffix = "_Type";
    
  private boolean onlyLeafNodes = false;
  private boolean onlyParentNodes = false;
    
  private boolean skipRootNode = true;
    
  private List<String> excludedNodes;
    
    private List<String> includeLeafNodesFor;
    
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
    
  public void addExcludedNode( String excludedNode )
  {
    if (excludedNodes == null) excludedNodes = new ArrayList<String>( );
    excludedNodes.add( excludedNode );
  }
    
  public void setExcludedNode( String excludedNode )
  {
    addExcludedNode( excludedNode );
  }
    
  public void addIncludeLeafNodesFor( String includeLeafNodes )
  {
    System.out.println( "addIncludeLeafNodesFor " + includeLeafNodes );
    if (this.includeLeafNodesFor == null) this.includeLeafNodesFor = new ArrayList<String>( );
    this.includeLeafNodesFor.add( includeLeafNodes );
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
    LOG.debug( "TaxonomyPathRollupAggregator: transformTaxo" );
    List<String> paths = taxoNode.getPaths( );
        
    if (paths != null)
    {
      for (String path : paths )
      {
        String[] segments = path.split( pathSeparator );
        int rootSeg = (skipRootNode) ? 2 : 1;
          
        String pathName = segments[ rootSeg ] + fieldSuffix;
        pathName = pathName.replaceAll( " ", "_" );
        
        // Figure out whether to skip Leaf Nodes for this path
        boolean skipLeafNodes = !( includeLeafNodesFor != null && includeLeafNodesFor.contains( pathName ));
        System.out.println( "skipLeafNodes for " + pathName + " = " + skipLeafNodes );
        int endSeg = (skipLeafNodes) ? segments.length - 1 : segments.length;
        if (endSeg < rootSeg + 1 ) return;
          
        LOG.debug( segments[0] );

        for (int i = rootSeg + 1; i < endSeg; i++ )
        {
          if (excludedNodes == null || !excludedNodes.contains( segments[i] ))
          {
            LOG.debug( " add: " + pathName + " = " + segments[i] );
            taxoNode.addProperty( new StringProperty( pathName, segments[i] ) );
          }
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