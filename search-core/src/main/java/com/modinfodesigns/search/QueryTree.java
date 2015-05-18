package com.modinfodesigns.search;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.IntrinsicPropertyDelegate;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.utils.StringMethods;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.NotIndexMatcher;
import com.modinfodesigns.classify.AndIndexMatcher;
import com.modinfodesigns.classify.OrIndexMatcher;
import com.modinfodesigns.classify.PhraseIndexMatcher;
import com.modinfodesigns.classify.AcronymIndexMatcher;
import com.modinfodesigns.classify.TermIndexMatcher;
import com.modinfodesigns.classify.NearIndexMatcher;

import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.ICreateDataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.transform.xml.XMLParserTransform;

import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A QueryTree contains one or more nested QueryTree subnodes that are linked by a boolean operator.
 * Leaf nodes have text strings.
 * 
 * @author Ted Sullivan
 *
 */

public class QueryTree extends Query implements IQuery, ICreateDataObjectSchema
{
  private transient static final Logger LOG = LoggerFactory.getLogger( QueryTree.class );

  public static final String TERM = "Term";
  public static final String PHRASE = "Phrase";
    
  private ArrayList<QueryTree> children = new ArrayList<QueryTree>( );
    
  private PropertyList queryModifiers;
    
  private boolean isNot = false;
    
  private HashMap<String,IQueryTreeRenderer> queryRenderers = new HashMap<String,IQueryTreeRenderer>( );

  private String defaultQueryRenderer;
    
  public QueryTree( )
  {
    setDataObjectSchema( "QueryTree" );
  }
    

  @Override
  public String getType()
  {
    return "com.modinfodesigns.search.QueryTree";
  }

  @Override
  public String getValue()
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getValue(  );
    }
        
    if (defaultQueryRenderer != null) return getValue( defaultQueryRenderer );
    
    return getValue( IProperty.JSON_FORMAT );
  }

  /**
   * Renders the query in a 'QL' format.
   */
  @Override
  public String getValue( String format )
  {
    LOG.debug( "getValue: " + format );
        
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      String proxyVal =  qtp.getValue( format );
      LOG.debug( "returning value for Proxy object: " + proxyVal  );
      return proxyVal;
    }
        
    IQueryTreeRenderer qtr = queryRenderers.get( format );
    if (qtr != null)
    {
      return qtr.renderQueryTree( this );
    }

    if (format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "{ \"type\":\"QueryTree\", \"operator\":\"" ).append( getOperator() ).append( "\"" );
        
      IProperty objNameProp = getProperty( DataObject.OBJECT_NAME_PARAM );
      if (objNameProp != null)
      {
        strbuilder.append( ", \"" ).append( DataObject.OBJECT_NAME_PARAM ).append( "\":\"" ).append( objNameProp.getValue( ) ).append( "\"" );
      }

      IProperty objIDProp = getProperty( DataObject.OBJECT_ID_PARAM );
      if (objIDProp != null)
      {
        strbuilder.append( ", \"" ).append( DataObject.OBJECT_ID_PARAM ).append( "\":\"" ).append( objIDProp.getValue( ) ).append( "\"" );
      }
            
      IProperty proxyProp = getProperty( "proxy" );
      if (proxyProp != null)
      {
        strbuilder.append( ", \"proxy\":\"true\"" );
      }
            
      if (getQueryField( ) != null)
      {
        strbuilder.append( ", \"queryField\":\"" ).append( getQueryField( ) ).append( "\"" );
      }
      if (getQueryText( ) != null)
      {
        strbuilder.append( ", \"queryText\":\"" ).append( getQueryText( ) ).append( "\"" );
      }
      if (children != null && children.size( ) > 0)
      {
        strbuilder.append( ", \"children\": [" );
        for (int i = 0; i < children.size(); i++)
        {
          QueryTree child = children.get( i );
          strbuilder.append( child.getValue( IProperty.JSON_FORMAT) );
          if (i < children.size() - 1 )
          {
            strbuilder.append( ", " );
          }
        }
        strbuilder.append( "]" );
      }
            
      if (queryModifiers != null && queryModifiers.size( ) > 0)
      {
        strbuilder.append( ", \"queryModifiers\":" );
        strbuilder.append( queryModifiers.getValue( IProperty.JSON_VALUE ));
      }
        
      if ( getMultiTermOperator() != null )
      {
        strbuilder.append( ", \"queryMode\":\"" ).append( getMultiTermOperator( ) ).append( "\"" );
      }
            
      strbuilder.append( "}" );
    
      return strbuilder.toString();
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "<QueryTree " );
      if (getName() != null)
      {
        strbuilder.append( "name=\"" ).append( getName( ) ).append( "\"" );
      }
      if (getID() != null)
      {
        strbuilder.append( " ID=\"" ).append( getID( ) ).append( "\"" );
      }
      strbuilder.append( " operator=\"" ).append( getOperator() ).append( "\"" );
      strbuilder.append( ">" );
            
      if ( getQueryField( ) != null )
      {
        strbuilder.append( "<QueryField>" ).append( getQueryField( ) ).append( "</QueryField>" );
      }
            
      if ( getQueryText() != null )
      {
        strbuilder.append( "<QueryText>" ).append( getQueryText( ) ).append( "</QueryText>" );
      }
            
      if ( children != null && children.size() > 0 )
      {
        strbuilder.append( "<Children>" );
        for (int i = 0; i < children.size(); i++)
        {
          QueryTree child = children.get( i );
          strbuilder.append( child.getValue( IProperty.XML_FORMAT) );
        }
        strbuilder.append( "</Children>" );
      }
            
      if (queryModifiers != null)
      {
        strbuilder.append( queryModifiers.getValue( IProperty.XML_FORMAT ));
      }
            
      strbuilder.append( "</QueryTree>" );
      return strbuilder.toString( );
    }
        
    return super.getValue( format );
  }
    
  @Override
  public void setValue( String value, String format )
  {
    if (format == null)
    {
      LOG.error( "format is NULL! Can't set value" );
      return;
    }
        
    if ( format.equals( IProperty.JSON_FORMAT ) )
    {
      LOG.debug( "Creating Object from JSON Data: " + value );
      JSONParserTransform jpt = new JSONParserTransform( );
      DataObject dobj = jpt.createDataObject( value );
      LOG.debug( dobj.getValue( IProperty.JSON_FORMAT ));
            
      initializeQueryTree( dobj, this );
    }
    else if ( format.equals( IProperty.XML_FORMAT ) )
    {
      XMLParserTransform xpt = new XMLParserTransform( );
      DataObject dobj = xpt.createDataObject( value );
      initializeQueryTree( dobj, this );
    }
    else
    {
      LOG.error( "Unknown format for setValue: '" + format + "'" );
    }
  }
    
  private void initializeQueryTree( DataObject dobj, QueryTree qTree )
  {
    if (dobj == null) return;
        
    LOG.debug( "initializeQueryTree( ): " + qTree );
        
    IProperty proxyProp = dobj.getProperty( "proxy" );
    if (proxyProp != null && proxyProp.getValue().equals( "true" ))
    {
      IProperty objectName = dobj.getProperty( DataObject.OBJECT_NAME_PARAM );
      IProperty objectId = dobj.getProperty( DataObject.OBJECT_ID_PARAM );
      if (objectName != null && objectId != null)
      {
        LOG.debug( "Creating Proxy Object: " + objectName.getValue( ) + "|" + objectId.getValue( ) );
                
        QueryTreePlaceholder proxyObj = new QueryTreePlaceholder( );
        proxyObj.setName( objectName.getValue( ) );
        proxyObj.setID( objectId.getValue( ) );
        proxyObj.setSchemaName( "QueryTree" );
        proxyObj.setProperty( proxyProp.copy( ) );
        qTree.setProxyObject( proxyObj );
      }
      else
      {
        LOG.debug( "Could not create Proxy!!!: Name or ID is NULL!" );
      }
            
      return;
    }
        
    Iterator<IProperty> propIt = dobj.getProperties( );
    
    while ( propIt != null && propIt.hasNext() )
    {
      IProperty prop = propIt.next( );
      LOG.debug( prop.getName( ) );

      if (prop.getName().equals( "children" ) && prop instanceof PropertyList)
      {
        PropertyList children = (PropertyList)prop;

        Iterator<IProperty> chIt = children.getProperties( );
        while ( chIt != null && chIt.hasNext() )
        {
          IProperty child = chIt.next( );
          if (child instanceof DataObject)
          {
            QueryTree childTree = new QueryTree( );
            initializeQueryTree( (DataObject)child, childTree );
            qTree.addChild( childTree );
          }
        }
      }
      else if (prop.getName( ).equals( "queryModifiers" ) && prop instanceof PropertyList )
      {
        setQueryModifiers( (PropertyList)prop );
      }
      else if ((prop instanceof IntrinsicPropertyDelegate) == false)
      {
        qTree.setProperty( prop );
      }
    }
        
    LOG.debug( "initializeQueryTree: DONE" );
    LOG.debug( getValue( IProperty.JSON_FORMAT ));
  }
    
  @Override
  public void setProperty( IProperty prop )
  {
    if (prop == null)
    {
      LOG.debug( "Can't set property with null property!" );
      return;
    }
    	
    if (!(prop instanceof IntrinsicPropertyDelegate))
    {
      LOG.debug( "setProperty " + prop.getName( ) + " " + prop );
    }
    	
    if (prop.getName().equals( "operator" ))
    {
      setOperator( prop.getValue( ) );
    }
    else if (prop.getName().equals( "queryField" ))
    {
      setQueryField( prop.getValue( ) );
    }
    else if (prop.getName().equals( "queryText" ))
    {
      setQueryText( prop.getValue( ) );
    }
    else if (prop.getName().equals( "queryMode" ))
    {
      setMultiTermOperator( prop.getValue( ) );
    }
    else if (prop.getName( ).equals( "queryModifiers" ) && prop instanceof PropertyList)
    {
      super.setProperty( prop );
      this.queryModifiers = (PropertyList)prop;
    }
    else if (!(prop instanceof IntrinsicPropertyDelegate))
    {
      super.setProperty( prop );
    }
  }
    
  public void setQueryRenderers( Map<String,IQueryTreeRenderer> queryRenderers )
  {
    this.queryRenderers.putAll( queryRenderers );
  }
    
  public String getQueryField( )
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getQueryField( );
    }
        
    IProperty queryFieldProp = getProperty( "queryField" );
    return (queryFieldProp != null) ? queryFieldProp.getValue( ) : null;
  }
    
  public void setQueryField( String queryField )
  {
    super.setProperty( new StringProperty( "queryField", queryField ));
  }
    
  public String getQueryText( )
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getQueryText( );
    }
        
    IProperty queryTextProp = getProperty( "queryText" );
    return (queryTextProp != null) ? queryTextProp.getValue( ) : null;
  }
    
  public void setQueryText( String queryText )
  {
    super.setProperty( new StringProperty( "queryText", queryText ));
  }
    
  public String getOperator( )
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getOperator( );
    }
        
    IProperty operatorProp = getProperty( "operator" );
    return (operatorProp != null) ? operatorProp.getValue( ) : null;
  }
    
  // AND | OR | NOT | PHRASE | TERM | NEAR | ONEAR | FIELD | MINMAX | ALL | ANY
  public void setOperator( String operator )
  {
    super.setProperty( new StringProperty( "operator", operator ));
  }
    
  public String getQueryMode( )
  {
    return getMultiTermOperator( );
  }
    

  public PropertyList getQueryModifiers( )
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getQueryModifiers( );
    }
        
    return this.queryModifiers;
  }
    
    
  public void setQueryModifiers( PropertyList queryModifiers )
  {
    LOG.debug( "setQueryModifiers: " + queryModifiers );
    if (queryModifiers != null)
    {
      queryModifiers.setName( "queryModifiers" );
      this.queryModifiers = queryModifiers;
    }
        
    setProperty( queryModifiers );
  }
    
  public void addQueryModifier( IProperty modifier )
  {
    LOG.debug( "addQueryModifier: " + modifier );
    if (queryModifiers == null)
    {
      queryModifiers = new PropertyList( );
      queryModifiers.setName( "queryModifiers" );
      setProperty( queryModifiers );
    }
        
    queryModifiers.addProperty( modifier );
  }
    
  @Override
  public void addChild( IQuery childQ )
  {
    if (childQ instanceof QueryTree)
    {
      addChild( (QueryTree)childQ );
    }
    else
    {
      super.addChild( childQ );
    }
  }
    
  public void addChild( QueryTree childQ )
  {
    this.children.add( childQ );
    
    dataObjects.add( childQ );
    LOG.debug( "QueryTree: DataList now has " + this.size( ) );
  }
    
  @Override
  public List<IQuery> getChildren( )
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getChildren( );
    }
        
    if (this.children != null)
    {
      ArrayList<IQuery> queryList = new ArrayList<IQuery>( );
      for ( QueryTree child : this.children )
      {
        queryList.add( child );
      }
        
      return queryList;
    }
        
    return null;
  }


  // ------------------------------------------------------------
  //   Classifier methods
  // ------------------------------------------------------------
  public Set<String> getQueryTerms( )
  {
    return null;
  }
    
  public Set<String> getQueryPhrases( )
  {
    return null;
  }
    
  public List<QueryTree> getSubTrees( )
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getSubTrees( );
    }
        
    return this.children;
  }
    
  public IIndexMatcher getIndexMatcher(  )
  {
    if (isProxied( ))
    {
      QueryTreePlaceholder qtp = (QueryTreePlaceholder)getProxyObject( );
      return qtp.getIndexMatcher(  );
    }
        
    LOG.debug( "getIndexMatcher " + getOperator( ) );
    IIndexMatcher ndxMatcher = null;
        
    String operator = getOperator( );
    if (operator == null || operator.equalsIgnoreCase( "AND" ))
    {
      ndxMatcher = (isNot) ? new NotIndexMatcher( new AndIndexMatcher( this ) ) : new AndIndexMatcher( this );
    }
    else if (operator.equalsIgnoreCase( "OR" ))
    {
      ndxMatcher = (isNot) ? new NotIndexMatcher( new OrIndexMatcher( this ) ) : new OrIndexMatcher( this );
    }
    else if (operator.equalsIgnoreCase( "NOT" ))
    {
      ndxMatcher = new NotIndexMatcher( this );
    }
    else if (operator.equalsIgnoreCase( PHRASE ))
    {
      ndxMatcher = (isNot) ? new NotIndexMatcher( new PhraseIndexMatcher( this ) ) : new PhraseIndexMatcher( this );
    }
    else if (operator.equalsIgnoreCase( TERM ))
    {
      ndxMatcher = (StringMethods.isAllCaps( getQueryText( ) )) ? new AcronymIndexMatcher( this ) : new TermIndexMatcher( this );
      if (isNot) ndxMatcher = new NotIndexMatcher( ndxMatcher );
    }
    else if ( operator.equalsIgnoreCase( "NEAR" ) || operator.equalsIgnoreCase( "ONEAR" ))
    {
      ndxMatcher = (isNot) ? new NotIndexMatcher( new NearIndexMatcher( this )) : new NearIndexMatcher( this );
    }
        
    String queryMode = getMultiTermOperator( );
    if (queryMode != null && queryMode.equalsIgnoreCase( "NEAR" ) || queryMode.equalsIgnoreCase( "ONEAR" ))
    {
      ndxMatcher = (isNot) ? new NotIndexMatcher( new NearIndexMatcher( this )) : new NearIndexMatcher( this );
    }
        
    if (ndxMatcher != null)
    {
      for (Iterator<IProperty> propIt = getProperties( ); propIt.hasNext(); )
      {
        IProperty prop = propIt.next();
        if (prop.getName().equals( "queryModifiers" ) == false && prop.getName( ).equals( "queryText" ) == false
         && prop.getName().equals( "queryField" ) == false && prop.getName( ).equals( "operator" ) == false
         && prop.getName().equals( "queryMode" ) == false)
        {
          ndxMatcher.addProperty( prop.copy( ) );
        }
      }
    }
        
    LOG.debug( "Created IndexMatcher " + ndxMatcher );
    return ndxMatcher;
  }
    
  @Override
  public QueryTree convertToQueryTree( )
  {
    return this;
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    LOG.debug( "QueryTree: createDataObjectSchema" );
        
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setDataObjectType( getType( ) );
    dos.setName( "QueryTree" );
    dos.setEntityType( "QueryTree" );
    dos.setChildObjectSchema( "QueryTree" );
    dos.setChildPlaceholderClass( "com.modinfodesigns.search.QueryTreePlaceholder" );
    
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( "queryField" );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
        
    pd = new PropertyDescriptor( );
    pd.setName( "queryText" );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( "operator" );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( Query.MULTI_TERM_OPERATOR );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
        
    pd = new PropertyDescriptor( );
    pd.setName( "queryModifiers" );
    pd.setPropertyType( "com.modinfodesigns.property.PropertyList" );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( USER_ID );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
        
    return dos;
  }
}
