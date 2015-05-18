package com.modinfodesigns.ontology;

import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.search.IQuery;

import java.util.List;

/**
 * Base Interface for Taxonomy node objects. Adds control of Parent-Child associative
 * relationships. Poly-hierarchy is by setting the multiple parents attribute to 'true'
 * 
 * Node types: CATEGORY, VALUE, EVIDENCE - A Category node indicates that the VALUE nodes
 * that are this node's descendents should be used to create a Categorical matcher (facets).
 * EVIDENCE nodes are sub-nodes of a VALUE node that add evidence to the classification.
 * 
 * @author Ted Sullivan
 */

public interface ITaxonomyNode extends IDataList
{
  public static final String PARENT_TYPE = "PARENT";
  public static final String CHILD_TYPE = "CHILD";
	
  public static final String JSON_CHILD_ARRAY = "JsonChildArray";
	

  public static final String CATEGORY_NODE = "CategoryNode";
  public static final String VALUE_NODE    = "ValueNode";
  public static final String EVIDENCE_NODE = "EvidenceNode";
  public static final String NONE          = "None";  // use as placeholder
	
  // Sets the renderer label to the object name at rendering (getValue() execution)
	
  public void setJsonNameLabel( String nameLabel );
  public String getNameLabel( );
	
  public boolean isRootNode( );
	
  public boolean isLeafNode( );
	
  // returns CATEGORY_NODE, VALUE_NODE, EVIDENCE_NODE or NONE (equivalent to NULL)
  public String getNodeType(  );
	
  public void setNodeType( String nodeType ) throws TaxonomyNodeException;  // should be a ENUM of possible types ...
	
  public String getDataObjectSchema(  );
	
  /**
   * Child Association - find all associations where the type of association is "CHILD"
   * @return
   */
  public List<ITaxonomyNode> getChildren( );

  public List<ITaxonomyNode> getDescendants( );
    
  public boolean isDescendant( ITaxonomyNode node );
    
  public void setMultipleParents( boolean multipleParents );
    
  public void addParentNode( ITaxonomyNode parentNode );
  public void removeParentNode( ITaxonomyNode parentNode );
  /**
   * @return the list of parents or empty list if the node is a Root node
   */
  public List<ITaxonomyNode> getParents( );
    
  /**
   * @return the list of ancestors or empty list of the node is a Root Node
   */
  public List<ITaxonomyNode> getAncestors( );
    
  public boolean isAncestor( ITaxonomyNode node );
    
  /**
   * Creates an IAssociation - adds a 'CHILD' association to the current node (this) where the child node is the target node.
   * adds a 'PARENT' type association to the child node where the target node is the parent node (this node).
   *
   * Rule - a PARENT cannot have an ancestor that is currently a descendant so if the childNode is already an
   * ancestor, throw a TaxonomyException.
   *
   * @param childNode
   */
  public void addChildNode( ITaxonomyNode childNode ) throws TaxonomyException;
    
  public ITaxonomyNode getChildNode( String name );
    
  public List<ITaxonomyNode> getChildNodes( String name );
    
  public int getChildNodeOrd( String name, String id );

  public ITaxonomyNode getRootNode( );
    
  /**
   * Returns the set of paths leading from this node to a root node (a node with no parent node )
   * @return
   */
  public List<String> getPaths( );
    
  public ITaxonomyNode getDescendantNode( String path );
    
    
  /**
   * Sets/gets a query object that is associated with the TaxonomyNode
   *
   */
  public void setQuery( IQuery query );
  public IQuery getQuery( );
}
