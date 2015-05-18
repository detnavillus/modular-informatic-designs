package com.modinfodesigns.ontology;

import com.modinfodesigns.property.persistence.DataListPlaceholder;
import com.modinfodesigns.property.persistence.SQLTemplatePersistenceManager;
import com.modinfodesigns.search.IQuery;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxonomyNodePlaceholder extends DataListPlaceholder implements ITaxonomyNode
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TaxonomyNodePlaceholder.class );

  public TaxonomyNodePlaceholder( ) {  }
	
  public TaxonomyNodePlaceholder( String name, String id,
                                  String dataObjectSchema, SQLTemplatePersistenceManager templateMan )
  {
    super( name, id, dataObjectSchema, templateMan );
  }

  private ITaxonomyNode getProxyNode( )
  {
    return (ITaxonomyNode)super.getProxyList( );
  }

  @Override
  public boolean isRootNode()
  {
    return false;
  }
	
  @Override
  public boolean isLeafNode()
  {
    List<ITaxonomyNode> children = getChildren( );
    return (children != null && children.size( ) > 0);
  }

  @Override
  public String getNodeType()
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getNodeType() : null;
  }

  @Override
  public void setNodeType( String nodeType ) throws TaxonomyNodeException
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    if (proxyNode != null)
    {
      proxyNode.setNodeType( nodeType );
      setModified( true );
    }
  }

  @Override
  public List<ITaxonomyNode> getChildren( )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getChildren() : null;
  }
  
  @Override
  public List<ITaxonomyNode> getChildNodes( String name )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getChildNodes( name ) : null;
  }
    
  @Override
  public int getChildNodeOrd( String name, String id )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getChildNodeOrd( name, id ) : -1;
  }
    

  @Override
  public List<ITaxonomyNode> getDescendants( )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getDescendants() : null;
  }

  @Override
  public boolean isDescendant( ITaxonomyNode node )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.isDescendant( node ) : false;
  }

  @Override
  public void setMultipleParents( boolean multipleParents )
  {
    LOG.debug( "setMultipleParents ..." );
		
    ITaxonomyNode proxyNode = getProxyNode( );
    if (proxyNode != null)
    {
      proxyNode.setMultipleParents( multipleParents );
      setModified( true );
    }

  }

  @Override
  public void addParentNode( ITaxonomyNode parentNode )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    if (proxyNode != null)
    {
      proxyNode.addParentNode( parentNode );
      setModified( true );
    }
    else
    {
      LOG.debug( "could not add parent node!" );
    }
  }

  @Override
  public List<ITaxonomyNode> getParents( )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getParents() : null;
  }

  @Override
  public List<ITaxonomyNode> getAncestors( )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getAncestors() : null;
  }

  @Override
  public boolean isAncestor( ITaxonomyNode node )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.isAncestor( node ) : false;
  }

  @Override
  public void addChildNode( ITaxonomyNode childNode ) throws TaxonomyException
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    if (proxyNode != null)
    {
      proxyNode.addChildNode( childNode );
      setModified( true );
    }
  }

  @Override
  public ITaxonomyNode getChildNode( String name )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getChildNode( name ) : null;
  }

  @Override
  public ITaxonomyNode getRootNode()
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getRootNode(  ) : null;
  }

  @Override
  public List<String> getPaths()
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getPaths(  ) : null;
  }

  @Override
  public ITaxonomyNode getDescendantNode( String path )
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getDescendantNode( path ) : null;
  }

  @Override
  public void setQuery(IQuery query)
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    if (proxyNode != null)
    {
      proxyNode.setQuery( query );
    }
  }

  @Override
  public IQuery getQuery()
  {
    ITaxonomyNode proxyNode = getProxyNode( );
    return (proxyNode != null) ? proxyNode.getQuery( ) : null;
  }

  @Override
  public String getNameLabel()
  {
    return null;
  }

  @Override
  public void removeParentNode(ITaxonomyNode parentNode)
  {
    LOG.debug( "removeParentNode " );
    ITaxonomyNode proxyNode = getProxyNode( );
    if (proxyNode != null)
    {
      proxyNode.removeParentNode( parentNode );
    }
  }

}
