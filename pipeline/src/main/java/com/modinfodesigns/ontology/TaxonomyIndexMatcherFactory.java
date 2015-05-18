package com.modinfodesigns.ontology;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.IIndexMatcherFactory;
import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.MinCountIndexMatcher;

import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.search.Query;
import com.modinfodesigns.utils.StringMethods;

import com.modinfodesigns.ontology.builder.ITaxonomyBuilder;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a set of IIndexMatchers from a Taxonomy.
 * 
 * Taxonomy Nodes can be marked with 'Category', 'Value', 'Evidence' or 'None'
 * Value nodes are found to create the set of IIndexMatchers. 
 * 
 * Properties of the nodes can be descriptive, synonymous (i.e. used for evidence), or categorical
 * This is determined by a TaxonomySchema.
 * 
 * All categories (either from ancestor nodes that are marked as 'Category' nodes or categorical node
 * properties are added to the IIndexMatcher as a 'Category' PropertyList.
 * 
 * TaxonomyNodes may contain IQuery instances as sub properties. These will be added to the
 * matcher QueryTree
 * 
 * @author Ted Sullivan
 */

public class TaxonomyIndexMatcherFactory implements IIndexMatcherFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ApplicationManager.class );

  private String name;
    
  private String taxonomyName;
    
  private String taxonomyBuilderName;
    
  // Tag name used if no CategoryNodes are defined
  // in the Taxonomy
  private String categoryPropertyName = "Category";
    
  private String pathPropertyName = "Path";
    
  private String replaceCategoryNameSpacesWith = "_";
    
  private ITaxonomyBuilder taxoBuilder;
    
  private boolean ancestorsAreCategories = false;
  private boolean descendantsAreEvidence = false;
    
  private boolean includeRootCategory = false;
    
  // Properties of the taxonomy node that should be used to mark or tag
  // matching documents.
  private List<String> categoryProperties;
    
  // Properties of the Taxonomy node (such as synonyms or used-for terms) that
  // should be used as part of the QueryTree used to create the IndexMatcher
  private List<String> evidenceProperties;
    
  // Any IQuery properties that have been added to the TaxonomyNode
  private List<String> queryProperties;
    
  // Properties that contain exclude terms - causing an AND-NOT structure
  // to be created
  private List<String> excludeProperties;
    
    
  private Integer minHitCount = null;
    
    
  public void setName( String name )
  {
    this.name = name;
  }
    
  public String getName( )
  {
    return this.name;
  }
    
  public void setTaxonomyName( String taxonomyName )
  {
    this.taxonomyName = taxonomyName;
  }
    
  public void addTaxonomyBuilderRef( String taxonomyBuilderName )
  {
    setTaxonomyBuilderRef( taxonomyBuilderName );
  }
    
  public void setTaxonomyBuilderRef( String taxonomyBuilderName )
  {
    this.taxonomyBuilderName = taxonomyBuilderName;
  }
    
  public void addTaxonomyBuilder( ITaxonomyBuilder taxoBuilder )
  {
    setTaxonomyBuilder( taxoBuilder );
  }
    
  public void setTaxonomyBuilder( ITaxonomyBuilder taxoBuilder )
  {
    this.taxoBuilder = taxoBuilder;
  }
    
  public void setCategoryPropertyName( String categoryPropertyName )
  {
    this.categoryPropertyName = categoryPropertyName;
  }

  public void setPathPropertyName( String pathPropertyName )
  {
    this.pathPropertyName = pathPropertyName;
  }
    
  public void addCategoryProperty( String categoryProperty )
  {
    if (categoryProperties == null) categoryProperties = new ArrayList<String>( );
    categoryProperties.add( categoryProperty );
  }
    
  public void setReplaceCategoryNameSpacesWith( String replaceCategoryNameSpacesWith )
  {
    this.replaceCategoryNameSpacesWith = replaceCategoryNameSpacesWith;
  }
    
  public void addEvidenceProperty( String evidenceProperty )
  {
    if (evidenceProperties == null) evidenceProperties = new ArrayList<String>( );
    evidenceProperties.add( evidenceProperty );
  }
    
  public void addQueryProperty( String queryProperty )
  {
    if (queryProperties == null) queryProperties = new ArrayList<String>( );
    queryProperties.add( queryProperty );
  }
    
  public void addExcludeProperty( String excludeProperty )
  {
    if (excludeProperties == null) excludeProperties = new ArrayList<String>( );
    excludeProperties.add( excludeProperty );
  }

  public void setMinimumHitCount( int minimumHitCount )
  {
    this.minHitCount = new Integer( minimumHitCount );
  }
    
  public void setMinimumHitCount( String minimumHitCount )
  {
    this.minHitCount = new Integer( minimumHitCount );
  }
    
  public void setAncestorsAreCategories( boolean ancestorsAreCategories )
  {
    this.ancestorsAreCategories = ancestorsAreCategories;
  }

  public void setAncestorsAreCategories( String ancestorsAreCategories )
  {
    this.ancestorsAreCategories = (ancestorsAreCategories != null && ancestorsAreCategories.equalsIgnoreCase( "true" ));
  }
    
  public void setIncludeRootCategory( boolean includeRootCategory )
  {
    this.includeRootCategory = includeRootCategory;
  }
    
  public void setIncludeRootCategory( String includeRootCategory )
  {
    this.includeRootCategory = (includeRootCategory != null && includeRootCategory.equalsIgnoreCase( "true" ));
  }
  
  public void setDescendantsAreEvidence( boolean descendantsAreEvidence )
  {
    this.descendantsAreEvidence = descendantsAreEvidence;
  }
    
  public void setDescendantsAreEvidence( String descendantsAreEvidence )
  {
    this.descendantsAreEvidence = (descendantsAreEvidence != null && descendantsAreEvidence.equalsIgnoreCase( "true" ));
  }
    
  /**
   * Create an IndexMatcher from each taxonomy 'VALUE' node.
   *
  */
  @Override
  public IIndexMatcher[] createIndexMatchers()
  {
    ITaxonomyNode theTaxonomy = getTaxonomy( );
        
    LOG.debug( "Got Taxonomy: " + theTaxonomy.getValue( IProperty.XML_FORMAT ));
        
    ArrayList<ITaxonomyNode> valueNodes = getValueNodes( theTaxonomy );
    if (valueNodes != null)
    {
      LOG.debug( "Got " + valueNodes.size( ) + " value nodes." );
      ArrayList<IIndexMatcher> matchers = new ArrayList<IIndexMatcher>( );
      for (int i = 0; i < valueNodes.size(); i++)
      {
        ITaxonomyNode valueNode = valueNodes.get( i );
        IIndexMatcher nodeMatcher = createIndexMatcher( valueNode );
                
        if (minHitCount != null)
        {
          MinCountIndexMatcher minCountMatcher = new MinCountIndexMatcher( nodeMatcher, minHitCount.intValue( ) );
          matchers.add( minCountMatcher );
        }
        else
        {
          matchers.add( nodeMatcher );
        }
                
        List<String> paths = valueNode.getPaths( );
        for (int p = 0, psz = paths.size( ); p < psz; p++ )
        {
          nodeMatcher.addProperty( new StringProperty( pathPropertyName, paths.get( p ) ));
        }
                
        ArrayList<ITaxonomyNode> categoryNodes = getCategoryNodes( valueNode );
        // add category property to nodeMatcher
        if (categoryNodes != null && categoryNodes.size() > 0)
        {
          for (int c = 0; c < categoryNodes.size(); c++)
          {
            ITaxonomyNode catNode = categoryNodes.get( c );
            // This is where we need to filter the name for spaces
            String categoryName = catNode.getName( );
            if ( replaceCategoryNameSpacesWith != null )
            {
              categoryName = StringTransform.replaceSubstring( categoryName, " ",  replaceCategoryNameSpacesWith );
            }
                        
            LOG.debug( "Adding node matcher property: " + categoryName + " = " + valueNode.getName( ) );
            nodeMatcher.addProperty( new StringProperty( categoryName, valueNode.getName() ));
          }
        }
        else
        {
          LOG.debug( "Adding node matcher property: " + categoryPropertyName + " = " + valueNode.getName( ) );
          nodeMatcher.setProperty( new StringProperty( categoryPropertyName, valueNode.getName( ) ));
        }
                
        // Add any value node categorical properties to node matcher
        if (categoryProperties != null)
        {
          LOG.debug( "Adding Category Properties " );
          for (int c = 0; c < categoryProperties.size(); c++)
          {
            String catPropName = categoryProperties.get( c );
            IProperty catProp = valueNode.getProperty( catPropName );
            if (catProp != null)
            {
              nodeMatcher.addProperty( catProp.copy() );
            }
          }
        }
      }
            
      LOG.debug( "Created " + matchers.size( ) + " IndexMatchers" );
      IIndexMatcher[] matcherLst = new IIndexMatcher[ matchers.size( ) ];
      matchers.toArray( matcherLst );
      return matcherLst;
    }
    else
    {
      LOG.error( "No Value Nodes to create Matchers with!" );
    }
        
    return null;
  }
    
    
  private ITaxonomyNode getTaxonomy(  )
  {
    LOG.debug( "getTaxonomy( )" );
        
    if (this.taxoBuilder != null)
    {
      return this.taxoBuilder.buildTaxonomy( );
    }
        
    if (taxonomyName == null && taxonomyBuilderName == null) return null;
    
    // Read the taxonomy from persistent storage ...
    ApplicationManager appMan = ApplicationManager.getInstance( );
    if (taxonomyName != null)
    {
      return (ITaxonomyNode)appMan.getApplicationObject( taxonomyName, "TaxonomyNode" );
    }
    else
    {
      ITaxonomyBuilder taxoBuilder = (ITaxonomyBuilder)appMan.getApplicationObject( taxonomyBuilderName, "TaxonomyBuilder" );
      LOG.debug( "Building Taxonomy with " + taxoBuilder );
      return (taxoBuilder != null) ? taxoBuilder.buildTaxonomy( ) : null;
    }
  }
    
  private ArrayList<ITaxonomyNode> getValueNodes( ITaxonomyNode rootNode )
  {
    LOG.debug( "getValueNodes( ) " + rootNode );
        
    ArrayList<ITaxonomyNode> valueNodes = new ArrayList<ITaxonomyNode>( );
    if (rootNode != null)
    {
      List<ITaxonomyNode> descendants = rootNode.getDescendants( );
      LOG.debug( "Got " + descendants.size( ) + " descendants " );
      for (int i = 0; i < descendants.size(); i++)
      {
        ITaxonomyNode descendant = descendants.get( i );
        // LOG.debug( "descendant.getNodeType( ) = " + descendant.getNodeType( ) );
                
        if (descendant.getNodeType() != null && descendant.getNodeType().equals( ITaxonomyNode.VALUE_NODE) ||
            descendant.isLeafNode( ))
        {
          LOG.debug( "Adding value node " + descendant.getName( ) );
          valueNodes.add( descendant );
        }
      }
    }
        
    return valueNodes;
  }
    
  // ========================================================================
  // Category nodes are any ancestor nodes that are Category Nodes
  // ========================================================================
  private ArrayList<ITaxonomyNode> getCategoryNodes( ITaxonomyNode valueNode )
  {
    ArrayList<ITaxonomyNode> categoryNodes = new ArrayList<ITaxonomyNode>( );
    List<ITaxonomyNode> ancestors = valueNode.getAncestors( );
    for (int i = 0; i < ancestors.size( ); i++)
    {
      ITaxonomyNode ancestor = ancestors.get( i );
      if ( ancestorsAreCategories || (ancestor.getNodeType() != null && ancestor.getNodeType().equals( ITaxonomyNode.CATEGORY_NODE)))
      {
        if ( !ancestor.isRootNode() || includeRootCategory )
        {
          categoryNodes.add( ancestor );
          return categoryNodes;
        }
      }
    }

    return categoryNodes;
  }
    
    
  private IIndexMatcher createIndexMatcher( ITaxonomyNode valueNode )
  {
    QueryTree nodeQuery = (descendantsAreEvidence) ? getDescendantQuery( valueNode ) : getNodeQuery( valueNode );
        
    IIndexMatcher ndxMatcher = nodeQuery.getIndexMatcher( );
    ndxMatcher.setName( valueNode.getName( ) );
    return ndxMatcher;
  }
    
  // ==================================================================================
  // Creates a composite query from a Taxonomy Node and its descendants (if present)
  // ==================================================================================
  private QueryTree getDescendantQuery( ITaxonomyNode taxoNode )
  {
    LOG.debug( "getDescendantQuery for " + taxoNode.getName( ) );
        
    List<ITaxonomyNode> children = taxoNode.getChildren( );
    if (children == null || children.size() == 0)
    {
      return getNodeQuery( taxoNode );
    }
        
    QueryTree qTree = new QueryTree( );
    qTree.setName( taxoNode.getName( ) );
    qTree.setOperator( "OR" );
    qTree.addChild( getNodeQuery( taxoNode ));
    
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode child = children.get( i );
      qTree.addChild( getDescendantQuery( child ) );
    }
        
    LOG.debug( qTree.getValue( IProperty.XML_FORMAT ));
    return qTree;
  }
    
  // =============================================================================
  // Create a Query tree from the value node.
  // OR value Term (name) + any evidence properties such as synonyms etc.
  // also add any IQuery properties added to the node.
  // =============================================================================
  private QueryTree getNodeQuery( ITaxonomyNode valueNode )
  {
    LOG.debug( "getNodeQuery for " + valueNode.getName( ) );
    QueryTree nodeQuery = new QueryTree( );
    nodeQuery.setName( valueNode.getName( ) );
        
    boolean hasChildQueries = false;
    
    if (evidenceProperties != null)
    {
      for (int i = 0; i < evidenceProperties.size(); i++)
      {
        String evidencePropName = evidenceProperties.get( i );
        IProperty evidenceProp = valueNode.getProperty( evidencePropName );
        if (evidenceProp != null && evidenceProp.getValue() != null
             && evidenceProp.getValue().trim().length() > 0)
        {
          LOG.debug( valueNode.getName( ) + " has evidence prop '" + evidenceProp.getValue( ) + "'" );
          QueryTree childQuery = getPropertyQuery( evidenceProp );
          nodeQuery.addChild( childQuery );
          hasChildQueries = true;
        }
      }
    }
        
    if (queryProperties != null)
    {
      LOG.debug( "Adding queryProperties" );
      for (int i = 0; i < queryProperties.size(); i++)
      {
        String queryProp = queryProperties.get( i );
        IProperty theQuery = valueNode.getProperty( queryProp );
        if (theQuery != null && theQuery instanceof IQuery)
        {
          hasChildQueries = true;
          if (theQuery instanceof Query )
          {
            nodeQuery.addChild( ((Query)theQuery).convertToQueryTree() );
          }
          else if (theQuery instanceof QueryTree )
          {
            nodeQuery.addChild( (QueryTree)theQuery );
          }
        }
        else if ( theQuery != null )
        {
          QueryTree chQuery = getPropertyQuery( theQuery );
          if (chQuery != null)
          {
            nodeQuery.addChild( chQuery );
            hasChildQueries = true;
          }
        }
      }
    }
        
    if (hasChildQueries)
    {
      LOG.debug( valueNode.getName( ) + " has Child Queries" );
      QueryTree nodeChild = new QueryTree( );
      nodeChild.setQueryText( valueNode.getName( ) );
            
      nodeChild.setOperator( getQueryOperator( valueNode.getName( ) ) );
      nodeQuery.addChild( nodeChild );
            
      nodeQuery.setOperator( "OR" );
    }
    else
    {
      LOG.debug( "Setting query text: '" + valueNode.getName( ) + "'" );
      String nodeVal = valueNode.getName( );
      nodeQuery.setQueryText( nodeVal );
      nodeQuery.setOperator( getQueryOperator( nodeVal ) );
    }
        
    if ( excludeProperties != null )
    {
      QueryTree excludeQuery = null;
      boolean needComposite = false;
      for (int i = 0; i < excludeProperties.size(); i++)
      {
        String excludePropName = excludeProperties.get( i );
        IProperty excludeProp = valueNode.getProperty( excludePropName );
        if (excludeProp != null && excludeProp.getValue() != null
            && excludeProp.getValue().trim().length() > 0 )
        {
          LOG.debug( "Adding exclusion query: " + excludeProp.getValue( ) );
                    
          QueryTree childQuery = getPropertyQuery( excludeProp );
          if (excludeQuery == null)
          {
            excludeQuery = childQuery;
            needComposite = true;
          }
          else if (needComposite)
          {
            QueryTree compositeQuery = new QueryTree( );
            compositeQuery.setOperator( "OR" );
            compositeQuery.addChild( excludeQuery );
            compositeQuery.addChild( childQuery );
            excludeQuery = compositeQuery;
            needComposite = false;
          }
          else
          {
            excludeQuery.addChild( childQuery );
          }
        }
      }
            
      if (excludeQuery != null)
      {
        QueryTree andNotQuery = new QueryTree( );
        andNotQuery.setOperator( "AND" );
        andNotQuery.addChild( nodeQuery );
                
        QueryTree notQuery = new QueryTree( );
        notQuery.setOperator( "NOT" );
        notQuery.addChild( excludeQuery );
        andNotQuery.addChild( notQuery );
    
        nodeQuery = andNotQuery;
      }
    }
        
    return nodeQuery;
  }
    
    
  private QueryTree getPropertyQuery( IProperty prop )
  {
    // if its a single property, create a Query tree with prop.getValue( ) as its
    // query text.  else, create an OR QueryTree with the
    if (prop instanceof PropertyList)
    {
      QueryTree qTree = new QueryTree( );
      qTree.setName( prop.getName( ) );
      qTree.setOperator( "OR" );
      PropertyList pList = (PropertyList)prop;
      for (Iterator<IProperty> propIt = pList.getProperties(); propIt.hasNext(); )
      {
        IProperty chProp = propIt.next( );
        QueryTree chQuery = getPropertyQuery( chProp );
        qTree.addChild( chQuery );
      }
            
      return qTree;
    }
    else if (prop instanceof StringListProperty)
    {
      // create an OR query tree with leaf nodes for each String value
      QueryTree qTree = new QueryTree( );
      qTree.setName( prop.getName( ) );
      qTree.setOperator( "OR" );
      StringListProperty sListProp = (StringListProperty)prop;
      String[] strings = sListProp.getStringList( );
      for (int i = 0; i < strings.length; i++)
      {
        QueryTree chTree = new QueryTree( );
        chTree.setQueryText( strings[i] );
        chTree.setOperator( getQueryOperator( strings[i] ));
        qTree.addChild( chTree );
      }
            
      return qTree;
    }
    else
    {
      QueryTree qTree = new QueryTree( );
      qTree.setName( prop.getName( ) );
      qTree.setQueryText( prop.getValue( ) );
      qTree.setOperator( getQueryOperator( prop.getValue( ) ));
      return qTree;
    }
  }
    
  private String getQueryOperator( String queryText )
  {
    String[] queryTerms = StringMethods.getStringArray( queryText, InvertedIndex.tokenDelimiter );
    return (queryTerms.length > 1) ? QueryTree.PHRASE : QueryTree.TERM;
  }

}
