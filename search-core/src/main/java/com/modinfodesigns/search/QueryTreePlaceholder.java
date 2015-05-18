package com.modinfodesigns.search;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.persistence.IDataObjectPlaceholder;
import com.modinfodesigns.property.persistence.SQLTemplatePersistenceManager;

import com.modinfodesigns.property.string.StringProperty;

import java.util.List;

public class QueryTreePlaceholder extends QueryTree implements
		                                  IDataObjectPlaceholder
{
  private SQLTemplatePersistenceManager templateManager;
  private String schemaName;
  private QueryTree proxyTree;

  @Override
  public void setSchemaName(String schemaName)
  {
    this.schemaName = schemaName;
  }

  @Override
  public void setSQLTemplatePersistenceManager( SQLTemplatePersistenceManager templateMan)
  {
    this.templateManager = templateMan;
  }


  @Override
  public DataObject getProxyObject()
  {
    if (proxyTree != null)
    {
      return proxyTree;
    }
		
    if (templateManager != null)
    {
      proxyTree = (QueryTree)templateManager.read( getName( ),  getID( ), this.schemaName );
      if (proxyTree != null)
      {
        proxyTree.setProperty( new StringProperty( DataObject.OBJECT_NAME_PARAM, getName( ) ) );
        proxyTree.setProperty( new StringProperty( DataObject.OBJECT_ID_PARAM, getID( ) ) );
        IProperty proxyProp = getProperty( "proxy" );
        if (proxyProp != null)
        {
          proxyTree.setProperty( proxyProp );
        }
      }
    }
		
    return proxyTree;
  }
	
  public String getQueryField( )
  {
    QueryTree proxyTree = (QueryTree)getProxyObject( );
    return proxyTree.getQueryField( );
  }
    
    
  public String getOperator( )
  {
    QueryTree proxyTree = (QueryTree)getProxyObject( );
    return proxyTree.getOperator( );
  }
	
  public String getQueryText( )
  {
    QueryTree proxyTree = (QueryTree)getProxyObject( );
    return proxyTree.getQueryText( );
  }

  // Override other QueryTree methods by getting proxyObject first ...
  @Override
  public String getValue( String format )
  {
    QueryTree proxyTree = (QueryTree)getProxyObject( );
    return proxyTree.getValue( format );
  }

  @Override
  public IIndexMatcher getIndexMatcher(  )
  {
    QueryTree proxyTree = (QueryTree)getProxyObject( );
    return proxyTree.getIndexMatcher( );
  }
    
  @Override
  public List<IQuery> getChildren( )
  {
    QueryTree proxyTree = (QueryTree)getProxyObject( );
    return proxyTree.getChildren( );
  }
    
  @Override
  public List<QueryTree> getSubTrees( )
  {
    QueryTree proxyTree = (QueryTree)getProxyObject( );
    return proxyTree.getSubTrees( );
  }
    
  @Override
  public QueryTree convertToQueryTree( )
  {
    return (QueryTree)getProxyObject( );
  }

}
