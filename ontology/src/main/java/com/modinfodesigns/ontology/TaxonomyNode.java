package com.modinfodesigns.ontology;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.ICreateDataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.search.IQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation of ITaxonomyNode. Handles parent-child relationships. 
 * 
 * Enforces an open-graph architecture required of a Taxonomy 
 * by not allowing ancestors to also be descendants. If a taxonomy node is added as a child
 * node that is already among the node's ancestors, a TaxonomyException is thrown.
 * 
 * Supports Poly-hierarchy - if multipleParents property is set to 'true'.
 * 
 * @author Ted Sullivan
 */

public class TaxonomyNode extends DataList implements ITaxonomyNode, ICreateDataObjectSchema
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TaxonomyNode.class );

  public static final String NODE_QUERY = "NodeQuery";
  public static final String NODE_TYPE  = "NodeType";
  public static final String PARENT_KEY = "ParentKey";
  public static final String PATH = "path";
    
  public static final String[] NODE_TYPES = { ITaxonomyNode.CATEGORY_NODE, ITaxonomyNode.VALUE_NODE,
                                                ITaxonomyNode.EVIDENCE_NODE, ITaxonomyNode.NONE };
    
  private List<ITaxonomyNode> children;
  private ArrayList<ITaxonomyNode> parents;
    
  private HashMap<String,List<ITaxonomyNode>> namedChildren;
    
  private boolean multipleParents = false;
    
  public TaxonomyNode(  ) {  }
    
  public TaxonomyNode( String name )
  {
    setName( name );
  }
    
  @Override
  protected String getListName( )
  {
    return "children";
  }
    
  @Override
  protected String getXMLTagName( )
  {
    return "TaxonomyNode";
  }
    
  public void setMultipleParents( boolean multipleParents )
  {
    this.multipleParents = multipleParents;
  }
    
  @Override
  public boolean isRootNode( )
  {
    return (parents == null || parents.size() == 0);
  }
    

  @Override
  public boolean isLeafNode()
  {
    return (children == null || children.size() == 0);
  }

  @Override
  public List<ITaxonomyNode> getChildren()
  {
    return children;
  }
    
  // Implements 'prototypal' inheritance of schema.
  // if have a schema, return it.
  // if not get the nearest parent schema

  @Override
  public String getDataObjectSchema(  )
  {
    String mySchema = super.getDataObjectSchema( );
    if (mySchema != null) return mySchema;
    
    if (this.parents != null)
    {
      for (int i = 0; i < parents.size(); i++)
      {
        ITaxonomyNode parentNode = parents.get( i );
        String parentSchema = parentNode.getDataObjectSchema( );
        if (parentSchema != null) return parentSchema;
      }
    }
        
    return null;
  }
    
    
    
  @Override
  public List<ITaxonomyNode> getDescendants()
  {
    ArrayList<ITaxonomyNode> descendants = new ArrayList<ITaxonomyNode>( );
    List<ITaxonomyNode> children = getChildren( );
    if (children == null) return descendants;
    
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode child = children.get( i );
      descendants.add( child );
      List<ITaxonomyNode> grandKids = child.getDescendants( );
      if (grandKids != null && grandKids.size( ) > 0 )
      {
        descendants.addAll( grandKids );
      }
    }
        
    return descendants;
  }
    
  /**
   * return true if node is descendant of this node, false otherwise
   */
  @Override
  public boolean isDescendant( ITaxonomyNode node )
  {
    List<ITaxonomyNode> children = getChildren( );
    if (children == null) return false;
    
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode child = children.get( i );
      if ( child.equals( node ) || child.isDescendant( node ))
      {
        return true;
      }
    }
        
    return false;
  }
    
  public String[] getParentKeys( )
  {
    IProperty parentKeyProp = getProperty( PARENT_KEY );
    if (parentKeyProp == null) return null;
    
    if (parentKeyProp instanceof StringListProperty)
    {
      StringListProperty parentProps = (StringListProperty)parentKeyProp;
      return parentProps.getStringList( );
    }
    else
    {
      String[] keys = new String[1];
      keys[0] = parentKeyProp.getValue( );
      return keys;
    }
  }

  @Override
  public List<ITaxonomyNode> getParents()
  {
    return this.parents;
  }

  @Override
  public List<ITaxonomyNode> getAncestors()
  {
    ArrayList<ITaxonomyNode> ancestors = new ArrayList<ITaxonomyNode>( );
    List<ITaxonomyNode> parents = getParents( );
    if (parents == null) return ancestors;
    
    for (int i = 0; i < parents.size(); i++)
    {
      ITaxonomyNode parent = parents.get( i );
      ancestors.add( parent );
      List<ITaxonomyNode> grandParents = parent.getAncestors( );
      if (grandParents != null)
      {
        ancestors.addAll( grandParents );
      }
    }
        
    return ancestors;
  }
    
  /**
   * returns true if node is ancestor of this node, false otherwise
   */
  @Override
  public boolean isAncestor( ITaxonomyNode node )
  {
    if (node.equals( this ))
    {
      return true;
    }
        
    List<ITaxonomyNode> parents = getParents( );
    if (parents == null) return false;
        
    for (int i = 0; i < parents.size(); i++)
    {
      ITaxonomyNode parent = parents.get( i );
      if ( parent.equals( node ) || parent.isAncestor( node ))
      {
        return true;
      }
    }
        
    return false;
  }
    
  /**
   * Override addDataObject - if its an ITaxonomyNode - add as child node,
   * else convert to an ITaxonomyNode by copying name, id and properties
   */
  @Override
  public void addDataObject( DataObject dObj )
  {
    ITaxonomyNode taxoNode = null;
    if (dObj instanceof ITaxonomyNode)
    {
      taxoNode = (ITaxonomyNode)dObj;
    }
    else
    {
      taxoNode = createTaxonomyNode( dObj );
    }
        
    try
    {
      addChildNode( taxoNode );
    }
    catch ( TaxonomyException te )
    {
      LOG.debug( "Got TaxnomyException: " + te );
    }
  }

  @Override
  public void addChildNode( ITaxonomyNode childNode ) throws TaxonomyException
  {
    if (isAncestor( childNode ) || childNode.equals( this ))
    {
      LOG.debug( "Got Taxonomy Exception!" );
      throw new TaxonomyException( "Child cannot be self or an ancestor!" );
    }
        
    LOG.debug( "OK to addChildNode: " + childNode );
        
    childNode.setMultipleParents( multipleParents );
        
    if (children == null) children = new ArrayList<ITaxonomyNode>( );
    children.add( childNode );
    childNode.addParentNode( this );
        
    // add it to namedChildren --
    
    if (childNode instanceof DataObject)
    {
      ((DataObject)childNode).setJsonNameLabel( "name" );
      super.addDataObject( (DataObject)childNode );
    }
        
    LOG.debug( "addChildNode: " + childNode + " DONE." );
  }
    
  public static TaxonomyNode createTaxonomyNode( DataObject fromObject )
  {
    TaxonomyNode taxoNode = new TaxonomyNode( );
    taxoNode.setName( fromObject.getName( ) );
    taxoNode.setID( fromObject.getID( ) );
    taxoNode.setProperties( fromObject.getProperties( ) );
      
    return taxoNode;
  }
    
  @Override
  public void removeDataObject( String objectID )
  {
    if (children == null) return;
    	
    super.removeDataObject( objectID );
        
    int childNdx = -1;
    for (int i = 0, isz = children.size( ); i < isz; i++)
    {
      ITaxonomyNode childNode = children.get( i );
      if (childNode.getID( ) != null && childNode.getID( ).equals( objectID ))
      {
        childNdx = i;
        break;
      }
    }
        
    if ( childNdx >= 0 )
    {
      ITaxonomyNode childNode = children.get( childNdx );
      childNode.removeParentNode( this );
      children.remove( childNdx );
    }
  }
    
  @Override
  public void removeDataObjectByName( String name )
  {
    super.removeDataObjectByName( name );
		
    // TO DO: remove from children ...
  }
    
  public void replaceDataObject( int index, DataObject dObj )
  {
    if (!(dObj instanceof TaxonomyNode)) return;
		
    super.replaceDataObject( index, dObj );
		
    // replace child at index with this one
    //set new parent to this
    // remove this from old child parent list
  }
	
	
  @Override
  public void addParentNode( ITaxonomyNode parentNode )
  {
    if (parents == null) parents = new ArrayList<ITaxonomyNode>( );
    parents.add( parentNode );
           
    setParentKeys( );
      
    List<String> paths = getPaths( );
    setProperty( new StringListProperty( PATH, paths ) );
  }
    
  @Override
  public void removeParentNode( ITaxonomyNode parentNode )
  {
    int parentNdx = -1;
    for (int i = 0, isz = parents.size( ); i < isz; i++)
    {
      ITaxonomyNode pNode = parents.get( i );
      if (pNode.getID( ) != null && pNode.getID( ).equals( parentNode.getID( ) ))
      {
        parentNdx = i;
        break;
      }
    }
        
    if (parentNdx >= 0)
    {
      parents.remove( parentNdx );
      setParentKeys( );
    }
  }
    
  private void setParentKeys( )
  {
    if (parents == null || parents.size( ) == 0)
    {
      removeProperty( PARENT_KEY );
    }
    else
    {
      StringListProperty parentKeys = new StringListProperty( PARENT_KEY );
      boolean hasKeys = false;
      for ( ITaxonomyNode parent: parents )
      {
        if (parent.getID() != null)
        {
          parentKeys.addString( parent.getValue( DataObject.NAME_ID_SCHEMA ) );
          hasKeys = true;
        }
      }
       
      if (hasKeys)
      {
        setProperty( parentKeys );
      }
    }
  }

  @Override
  public ITaxonomyNode getRootNode()
  {
    if (parents == null) return this;
        
    for( int i = 0, isz = parents.size( ); i < isz; ++i)
    {
      ITaxonomyNode parent = parents.get( i );
      if (parent.getParents() == null) return parent;
      else
      {
        return parent.getRootNode( );
      }
    }
        
    return null;
  }

  @Override
  public List<String> getPaths()
  {
    ArrayList<String> paths = new ArrayList<String>( );
    if (parents == null)
    {
      paths.add( "/" + getName( ) );
    }
    else
    {
      for (int i = 0; i < parents.size(); i++)
      {
        ITaxonomyNode parent = parents.get( i );
        List<String> parentPaths = parent.getPaths( );
        if (parentPaths != null)
        {
          for (int p = 0; p < parentPaths.size( ); p++ )
          {
            // need to know if I have siblings with the same name
            paths.add( parentPaths.get( p ) + "/" + getName( ) );
          }
        }
      }
    }
        
    return paths;
  }
    
    
    
  @Override
  public IProperty getProperty( String name )
  {
    if (name != null && name.equalsIgnoreCase( NODE_TYPE ))
    {
      IProperty nodeTypeProp = super.getProperty( NODE_TYPE );
      return (nodeTypeProp != null) ? nodeTypeProp : new StringProperty (NODE_TYPE, ITaxonomyNode.NONE );
    }
        
    return super.getProperty( name );
  }
    
  @Override
  public ITaxonomyNode getChildNode( String name )
  {
    if (children == null) return null;
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode childNode = children.get( i );
      if (childNode.getName().equals( name ))
      {
        return childNode;
      }
    }
        
    return null;
  }


  @Override
  public List<ITaxonomyNode> getChildNodes( String name )
  {
    if (children == null) return null;
    ArrayList<ITaxonomyNode> childNodes = new ArrayList<ITaxonomyNode>( );
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode childNode = children.get( i );
      if (childNode.getName().equals( name ))
      {
        childNodes.add( childNode );
      }
    }
            
    return childNodes;
  }
    
  public int getChildNodeOrd( String name, String childID )
  {
    if (children == null) return -1;
        
    int ord = 0;
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode childNode = children.get( i );
      if (childNode.getName().equals( name ))
      {
        if (childNode.getID().equals( childID ))
        {
          return ord;
        }
        ord++;
      }
    }
        
    return -1;
  }
    
  public int getNChildren( String name )
  {
    int ord = 0;
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode childNode = children.get( i );
      if (childNode.getName().equals( name ))
      {
        ord++;
      }
    }
        
    return ord;
  }
    
  public ITaxonomyNode getChildNodeByID( String childID )
  {
    if (children == null) return null;
    for (int i = 0; i < children.size(); i++)
    {
      ITaxonomyNode childNode = children.get( i );
      if (childNode.getID().equals( childID ))
      {
        return childNode;
      }
    }
      
    return null;
  }


  /**
   * Returns one of CATEGORY, VALUE, EVIDENCE or NONE
   */
  @Override
  public String getNodeType()
  {
    IProperty nodeTypeProp = getProperty( NODE_TYPE );
    return (nodeTypeProp != null) ? nodeTypeProp.getValue( ) : ITaxonomyNode.NONE;
  }


  @Override
  public void setNodeType( String nodeType ) throws TaxonomyNodeException
  {
    if ( nodeType == null ) return;
        
    if (nodeType.equals( ITaxonomyNode.CATEGORY_NODE ) ||
        nodeType.equals( ITaxonomyNode.VALUE_NODE ) ||
        nodeType.equals( ITaxonomyNode.EVIDENCE_NODE ) ||
        nodeType.equals( ITaxonomyNode.NONE ))
    {
      setProperty( new StringProperty( NODE_TYPE, nodeType ));
    }
    else
    {
      throw new TaxonomyNodeException( "Wrong Taxonomy NODE type: " + nodeType );
    }
  }

  @Override
  public ITaxonomyNode getDescendantNode( String path )
  {
    if (path == null || path.equals( getName())) return this;
        
    if (children != null)
    {
      String myPath = "/" + getName( );
      String currentPath = (path.startsWith( myPath )) ? myPath : "";
      for (int i = 0; i < children.size(); i++)
      {
        ITaxonomyNode child = children.get( i );
        if (child instanceof TaxonomyNode)
        {
          TaxonomyNode descendant = ((TaxonomyNode)child).getDescendantAtPath(path, currentPath );
          if (descendant != null) return descendant;
        }
      }
    }
        
    LOG.debug( getName( ) +  ": No descendant at: " + path );
    return null;
  }
    
  private TaxonomyNode getDescendantAtPath( String finalPath, String parentPath )
  {
    LOG.debug( "getDescendantAtPath( " + finalPath + "," + parentPath + " )" );
        
    String myPath = parentPath + "/" + getName( );
    if (finalPath.equals( myPath )) return this;
    else if (finalPath.startsWith( myPath ) && children != null)
    {
      for (int i = 0; i < children.size(); i++)
      {
        ITaxonomyNode child = children.get( i );
        if (child instanceof TaxonomyNode)
        {
          TaxonomyNode descendant = ((TaxonomyNode)child).getDescendantAtPath( finalPath, myPath );
          if (descendant != null) return descendant;
        }
      }
    }
        
    return null;
  }
    
    
  /**
   * Creates a TaxonomyNode at the path specified, creating intermediate nodes
   * as necessary. Path separator is assumed to be '/'
   *
   * @param path
   * @return
   */
  public TaxonomyNode createDescendantAtPath( String path ) throws TaxonomyException
  {
    if (path == null || path.equals( getName( ) )) return this;
        
    TaxonomyNode descendantNode = (TaxonomyNode)getDescendantNode( path );
    if (descendantNode != null) return descendantNode;
        
    String childName = (path.startsWith( "/" )) ? new String( path.substring( 1 ) ) : path;
    String subPath = (childName.indexOf( "/" ) > 0)
                   ? new String( childName.substring( childName.indexOf( "/" ) + 1 ))
                   : null;
    childName = (childName.indexOf( "/" ) > 0)
              ? new String( childName.substring( 0, childName.indexOf( "/" )))
              : childName;
                  
    TaxonomyNode childNode = (TaxonomyNode)getChildNode( childName );
    if (childNode == null)
    {
      LOG.debug( getName() + " creating child node: " + childName );
      childNode = new TaxonomyNode( childName );
      addChildNode( childNode );
    }
            
    return (subPath == null) ? childNode : childNode.createDescendantAtPath( subPath );
  }

  @Override
  public void setQuery(IQuery query)
  {
    query.setName( NODE_QUERY );
    setProperty( query );
  }

  @Override
  public IQuery getQuery()
  {
    return (IQuery)getProperty( NODE_QUERY );
  }
    
  @Override
  public String getValue( String format )
  {
    if (format.equals( JSON_CHILD_ARRAY ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "[" );
      List<ITaxonomyNode> children = getChildren( );
      if (children != null)
      {
        for (int i = 0, isz = children.size(); i < isz; i++)
        {
          ITaxonomyNode childNode = children.get( i );
          String chNameLabel = childNode.getNameLabel( );
          childNode.setJsonNameLabel( this.jsonNameLabel );
          strbuilder.append( childNode.getValue( IProperty.JSON_FORMAT ));
          childNode.setJsonNameLabel( chNameLabel );
            
          if (i < (isz-1)) strbuilder.append( "," );
        }
      }
      strbuilder.append( "]" );
      return strbuilder.toString( );
    }
    else
    {
      return super.getValue( format );
    }
  }

  @Override
  public String getNameLabel()
  {
    return this.jsonNameLabel;
  }

  @Override
  public DataObjectSchema createDataObjectSchema(DataObject context)
  {
    LOG.debug( "createDataObjectSchema( ) ..." );
        
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "TaxonomyNode" );
    dos.setEntityType( "Taxonomy" );
    dos.setDataObjectType( "com.modinfodesigns.ontology.TaxonomyNode" );
    dos.setChildObjectSchema( "TaxonomyNode" );
    dos.setChildPlaceholderClass( "com.modinfodesigns.ontology.TaxonomyNodePlaceholder" );
        
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( NODE_TYPE );
    pd.setPropertyType( "EnumerationProperty" );
    pd.setPropertyValues( NODE_TYPES );
    pd.setDefaultValue( ITaxonomyNode.NONE );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( PARENT_KEY );
    pd.setPropertyType( "StringList" );
    pd.setPropertyFormat( IProperty.DELIMITED_FORMAT );
    dos.addPropertyDescriptor( pd );
    
    return dos;
  }

}
