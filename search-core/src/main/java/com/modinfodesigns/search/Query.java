package com.modinfodesigns.search;

import com.modinfodesigns.classify.InvertedIndex;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.EnumerationProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.schema.ICreateDataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.transform.xml.XMLParserTransform;
import com.modinfodesigns.property.persistence.DataObjectPlaceholder;

import com.modinfodesigns.security.IUserCredentials;
import com.modinfodesigns.utils.StringMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a fielded Query.
 * 
 * @author Ted Sullivan
 */

public class Query extends DataList implements IQuery,ICreateDataObjectSchema
{
  private transient static final Logger LOG = LoggerFactory.getLogger( Query.class );

  public static final String MULTI_FIELD_OPERATOR = "MultiFieldOperator";
  public static final String MULTI_TERM_OPERATOR = "MultiTermOperator";
  public static final String SCOPE_QUERY = "ScopeQuery";
    
  public static final String USER_ID = "UserID";
    
  private int pageSize = 10;
  private int startRecord = 1;
    
  private IUserCredentials userCredentials;
    
  private SortProperty sortProperty;
    
  private QueryField defaultQuery;
    
  private String collectionName;
    
  // this is an enumeration property
  // private String fieldOperator = "AND";
    
  private List<String> navigatorFields;
  private List<String> displayFields;
    
  private IQuery relevanceQuery;
    
  private ArrayList<IQuery> children;
    
  public Query( )
  {
    setDataObjectSchema( "Query" );
  }
    
  @Override
  protected String getListName( )
  {
    return "Fields";
  }
    
  @Override
  public String getValue( String format )
  {
    if ( format == null || format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "{" );
    		
      QueryField defaultQuery = getDefaultQueryField( );
      if (defaultQuery != null)
      {
        strbuilder.append( "\"defaultQueryField\":" ).append( defaultQuery.getValue( IProperty.XML_FORMAT ));
      }
    		
      List<QueryField> queryFields = getQueryFields(  );
      if (queryFields != null && queryFields.size( ) > 0)
      {
        if (strbuilder.length() > 1) strbuilder.append( "," );
        strbuilder.append( "\"Fields\":[" );
        for (int i = 0; i < queryFields.size( ); i++)
        {
          QueryField qf = queryFields.get( i );
          strbuilder.append( qf.getValue( IProperty.JSON_FORMAT ));
          if (i < queryFields.size( ) - 1 ) strbuilder.append( "," );
        }
        strbuilder.append( "]" );
      }
    		
      strbuilder.append( "}" );
      return strbuilder.toString( );
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "<Query" );
      if (getName() != null)
      {
        strbuilder.append( "name=\"" ).append( getName( ) ).append( "\"" );
      }
      if (getID() != null)
      {
        strbuilder.append( " ID=\"" ).append( getID( ) ).append( "\"" );
      }
      strbuilder.append( ">" );
        
      QueryField defaultQuery = getDefaultQueryField( );
      if (defaultQuery != null)
      {
        strbuilder.append( defaultQuery.getValue( IProperty.XML_FORMAT ));
      }
        	
      List<QueryField> queryFields = getQueryFields(  );
      if (queryFields != null && queryFields.size( ) > 0)
      {
        for (int i = 0; i < queryFields.size( ); i++)
        {
          QueryField qf = queryFields.get( i );
          strbuilder.append( qf.getValue( IProperty.XML_FORMAT ));
        }
      }
        	
      strbuilder.append( "</Query>" );
      return strbuilder.toString( );
    }
    else
    {
      return super.getValue( format );
    }
  }
    
  @Override
  public void setValue( String value, String format )
  {
    // need to create QueryFields, etc....
    if ( format != null && format.equals( IProperty.JSON_FORMAT))
    {
      JSONParserTransform jpt = new JSONParserTransform( );
      DataObject dobj = jpt.createDataObject( value );
      initializeQuery( dobj, this );
    }
    else if (format != null && format.equals( IProperty.XML_FORMAT ))
    {
      XMLParserTransform xpt = new XMLParserTransform( );
      DataObject dobj = xpt.createDataObject( value );
      initializeQuery( dobj, this );
    }
  }
    
  private static void initializeQuery( DataObject dobj, Query query )
  {
    if (dobj == null) return;
		
    LOG.debug( "initializeQuery: " + dobj.getValue( IProperty.XML_FORMAT ));
		
    IProperty defaultQFProp = dobj.getProperty( "defaultQueryField" );
    if (defaultQFProp != null)
    {
      QueryField defaultQField = new QueryField( );
      defaultQField.setFieldName( ((DataObject)defaultQFProp).getProperty( "fieldName" ).getValue( ) );
      defaultQField.setFieldValue( ((DataObject)defaultQFProp).getProperty( "fieldValue" ).getValue( ) );
      query.setDefaultQueryField( defaultQField );
    }
		
    IProperty fieldsProp = dobj.getProperty( "Fields" );
    if (fieldsProp != null && fieldsProp instanceof PropertyList )
    {
      PropertyList children = (PropertyList)fieldsProp;
      Iterator<IProperty> chIt = children.getProperties( );
      while ( chIt != null && chIt.hasNext() )
      {
        IProperty qFieldProp = chIt.next( );
        QueryField qField = new QueryField( );
        LOG.debug( "Got query field prop: " + qFieldProp.getValue( IProperty.JSON_FORMAT ));
        qField.setFieldName(  ((DataObject)qFieldProp).getProperty( "fieldName" ).getValue( ) );
        qField.setFieldValue( ((DataObject)qFieldProp).getProperty( "fieldValue" ).getValue( ) );
        query.addQueryField( qField );
      }
    }
  }
    
  public void addQueryField( QueryField queryField )
  {
    LOG.debug( "addQueryField" );
    super.addDataObject( queryField );
  }
    
  @Override
  public void addDataObject( DataObject dObj )
  {
    if (dObj instanceof IQuery)
    {
      addChild( (IQuery)dObj );
    }
    else
    {
      super.addDataObject( dObj );
    }
  }
    
  @Override
  public List<QueryField> getQueryFields(  )
  {
    List<DataObject> dobjs = super.getDataList( );
    ArrayList<QueryField> qFields = new ArrayList<QueryField>( );
        
    for (int i = 0; i < dobjs.size(); i++)
    {
      DataObject chObj = dobjs.get( i );
      if (chObj instanceof DataObjectPlaceholder)
      {
        DataObjectPlaceholder doph = (DataObjectPlaceholder)chObj;
        DataObject proxyObj = doph.getProxyObject( );
        if (proxyObj != null && proxyObj instanceof QueryField)
        {
          qFields.add( (QueryField)proxyObj );
        }
      }
      else if (chObj instanceof QueryField )
      {
        qFields.add( (QueryField)chObj );
      }
    }
    
    return qFields;
  }
    
  @Override
  public QueryField getQueryField( String fieldName )
  {
    List<DataObject> dobjs = super.getDataList( );
    if (dobjs == null) return null;
    for (int i = 0; i < dobjs.size(); i++)
    {
      DataObject chObj = dobjs.get( i );
      QueryField qField = null;
    
      if (chObj instanceof DataObjectPlaceholder)
      {
        DataObjectPlaceholder doph = (DataObjectPlaceholder)chObj;
        DataObject proxyObj = doph.getProxyObject( );
        if (proxyObj != null && proxyObj instanceof QueryField)
        {
          qField = (QueryField)proxyObj;
        }
      }
      else if (chObj instanceof QueryField)
      {
        qField = (QueryField)chObj;
      }
        
      if (qField != null && qField.getFieldName().equals( fieldName )) return qField;
    }
        
    return null;
  }
    
  public void setPageSize( int pageSize )
  {
    this.pageSize = pageSize;
  }
    
  public int getPageSize( )
  {
    return this.pageSize;
  }
    
  public void setStartRecord( int startRecord )
  {
    this.startRecord = startRecord;
  }
    
  public int getStartRecord( )
  {
    return this.startRecord;
  }
    
  public void setSortProperty( SortProperty sortProp )
  {
    this.sortProperty = sortProp;
  }
    
  public SortProperty getSortProperty( )
  {
    return this.sortProperty;
  }

  public void setUserCredentials( IUserCredentials userCredentials )
  {
    this.userCredentials = userCredentials;
    setProperty( new StringProperty( USER_ID, userCredentials.getUsername( ) ));
  }
    
  public IUserCredentials getUserCredentials( )
  {
    return this.userCredentials;
  }

  @Override
  public void setDefaultQueryField( QueryField defaultQuery )
  {
    this.defaultQuery = defaultQuery;
    defaultQuery.setName( "DefaultQuery" );
    setProperty( defaultQuery );
  }

  @Override
  public QueryField getDefaultQueryField(  )
  {
    return this.defaultQuery;
  }
    
  /**
   * Boolean operator used for multi-field searches (typically "AND" or "OR" )
   *
   * @param fieldOperator
   */
  @Override
  public void setMultiFieldOperator( String fieldOperator )
  {
    EnumerationProperty enumProp = new EnumerationProperty( );
    enumProp.setName( MULTI_FIELD_OPERATOR );
    enumProp.addChoice( new StringProperty( "AND", "AND" ));
    enumProp.addChoice( new StringProperty( "OR", "OR" ));
    try
    {
      enumProp.setValue( fieldOperator, null );
      setProperty( enumProp );
    }
    catch ( PropertyValidationException pve )
    {
      // complain!
    }
  }
    
  @Override
  public String getMultiFieldOperator( )
  {
    IProperty multiFieldOp = getProperty( MULTI_FIELD_OPERATOR );
    return (multiFieldOp != null) ? multiFieldOp.getValue( ) : null;
  }
    
  @Override
  public void setMultiTermOperator( String multiTermOperator )
  {
    setProperty( new StringProperty( MULTI_TERM_OPERATOR, multiTermOperator ));
  }
    
  @Override
  public String getMultiTermOperator( )
  {
    IProperty multiTermOp = getProperty( MULTI_TERM_OPERATOR );
    return (multiTermOp != null) ? multiTermOp.getValue( ) : null;
  }

  @Override
  public void setNavigatorFields( List<String> navFields )
  {
    this.navigatorFields = navFields;
  }

  @Override
  public List<String> getNavigatorFields()
  {
    return this.navigatorFields;
  }
    
  @Override
  public void setDisplayFields( List<String> displayFields )
  {
    this.displayFields = displayFields;
  }

  @Override
  public List<String> getDisplayFields()
  {
    return this.displayFields;
  }
    
  public void addChild( IQuery query )
  {
    LOG.debug( "addChild: " + query );
    if (children == null) children = new ArrayList<IQuery>( );
    children.add( query );
  }
    
  public List<IQuery> getChildren( )
  {
    return children;
  }
    
  /**
   * Sets the scope or query filter associated with this request. Depending on the IFinder
   * implementation, this may be used as a filter or ANDed with the parent IQuery.
   *
   */
  @Override
  public void setScopeQuery( IQuery scopeQuery )
  {
    if (scopeQuery != null)
    {
      QueryTree qTree = scopeQuery.convertToQueryTree( );
      qTree.setName( SCOPE_QUERY );
      setProperty( qTree );
    }
  }
    
  @Override
  public IQuery getScopeQuery( )
  {
    return (IQuery)getProperty( SCOPE_QUERY );
  }
    

  /**
   * Sets the custom relevance query (sort formula, function query, etc depending on
   * the target IFinder implementation.  Relevance queries may contain Term boosts or
   * field boosts.
   */
  @Override
  public void setRelevanceQuery( IQuery relevanceQuery )
  {
    this.relevanceQuery = relevanceQuery;
  }
    
  @Override
  public IQuery getRelevanceQuery( )
  {
    return this.relevanceQuery;
  }
    

  /**
   * Converts to a Boolean query-ready QueryTree.
   *
   * @return
   */
  @Override
  public QueryTree convertToQueryTree( )
  {
    LOG.debug( "convertToQueryTree " + getMultiFieldOperator( ) );
        
    QueryTree qTree = new QueryTree( );
    qTree.setOperator( getMultiFieldOperator( ) );
    addToQueryTree( this, qTree );
    qTree.setScopeQuery( getScopeQuery( ) );
    qTree.setRelevanceQuery( this.relevanceQuery );
    qTree.setStartRecord( this.startRecord );
        
    return qTree;
  }
    
  private void addToQueryTree( Query query, QueryTree qTree )
  {
    // ================================================
    // for each Field, create a QueryTree child
    // ================================================
    addFieldsToQueryTree( query, qTree );
        
    if (query.children != null)
    {
      for ( int i = 0; i < children.size(); i++ )
      {
        IQuery childQ = children.get( i );
        if (childQ instanceof QueryTree)
        {
          LOG.debug( "Adding QueryTree as child" );
          qTree.addChild( (QueryTree)childQ );
        }
        else if (childQ instanceof Query)
        {
          addToQueryTree( (Query)childQ, qTree );
        }
      }
    }
  }
    
    
  public static void addFieldsToQueryTree( IQuery query, QueryTree queryTree )
  {
    if (query == null) return;
        
    List<QueryField> qFields = query.getQueryFields( );
    if ( query.getDefaultQueryField() != null )
    {
      if ( qFields == null ) qFields = new ArrayList<QueryField>( );
      qFields.add( query.getDefaultQueryField( ) );
    }
        
    LOG.debug( "Have " + ((qFields != null) ? qFields.size( ) : 0) + " query fields." );
    if (qFields != null && qFields.size() > 0)
    {
      if (qFields.size() == 1)
      {
        QueryField qField = qFields.get( 0 );
        String fieldValue = qField.getFieldValue( );
        String[] terms = StringMethods.getStringArray( fieldValue, InvertedIndex.tokenDelimiter );
        if ( terms.length == 1 )
        {
          queryTree.setQueryField( qField.getFieldName( ) );
          queryTree.setQueryText( qField.getFieldValue( ) );
          queryTree.setOperator( QueryTree.TERM );
        }
        else
        {
          queryTree.setOperator( "OR" );
          addTermQueryTrees( queryTree, qField.getFieldName( ), terms );
        }
      }
      else
      {
        for (int q = 0; q < qFields.size(); q++)
        {
          QueryField qField = qFields.get( q );
          String fieldValue = qField.getFieldValue( );
          if (fieldValue != null && fieldValue.trim().length() > 0)
          {
            String[] terms = StringMethods.getStringArray( fieldValue, InvertedIndex.tokenDelimiter );
            QueryFieldOperator qOp = qField.getQueryFieldOperator( );
                    
            QueryTree chTree = new QueryTree( );
            queryTree.addChild( chTree );
              
            if (qOp == null)
            {
              if ( terms.length == 1 )
              {
                chTree.setQueryField( qField.getFieldName( ) );
                chTree.setQueryText( qField.getFieldValue( ) );
                chTree.setOperator( QueryTree.TERM );
              }
              else
              {
                chTree.setOperator( "OR" );
                                
                addTermQueryTrees( chTree, qField.getFieldName( ), terms );
              }
            }
            else
            {
              chTree.setQueryField( qField.getFieldName( ) );
              chTree.setQueryText( qField.getFieldValue( ) );
                
              // build child tree based on qOp and multiple terms
              QueryFieldOperator qFieldOp = qField.getQueryFieldOperator( );
              if ( terms.length == 1 )
              {
                chTree.setOperator( QueryTree.TERM );
              }
              else if (terms.length > 1 )
              {
                if ( qFieldOp.getFieldOperator().equals( QueryFieldOperator.ALL ) )
                {
                  chTree.setMultiTermOperator( QueryFieldOperator.ALL );
                  // addTermQueryTrees( chTree, qField.getFieldName( ), terms );
                }

                else if ( qFieldOp.getFieldOperator().equals( QueryFieldOperator.ANY ) )
                {
                  chTree.setMultiTermOperator( QueryFieldOperator.ANY );
                  // addTermQueryTrees( chTree, qField.getFieldName( ), terms );
                }
                else if ( qFieldOp.getFieldOperator().equals( QueryFieldOperator.EXACT ) )
                {
                  chTree.setOperator( QueryTree.PHRASE );
                  chTree.setQueryField( qField.getFieldName( ) );
                  chTree.setQueryText( fieldValue );
                }
                else if ( qFieldOp.getFieldOperator().equals( QueryFieldOperator.ONEAR ) )
                {
                  chTree.setMultiTermOperator( "ONEAR" );
                  // set distance ...
                  Iterator<IProperty> modifiers = qFieldOp.getModifiers( );
                  while (modifiers != null && modifiers.hasNext() )
                  {
                    chTree.addQueryModifier( modifiers.next( ) );
                  }

                  // addTermQueryTrees( chTree, qField.getFieldName( ), terms );
                }
                else if (qFieldOp.getFieldOperator().equals( QueryFieldOperator.NEAR ))
                {
                  LOG.debug( "adding NEAR tree " + terms );
                  chTree.setMultiTermOperator( "NEAR" );
                  // set distance ...
                  Iterator<IProperty> modifiers = qFieldOp.getModifiers( );
                  while ( modifiers != null && modifiers.hasNext() )
                  {
                    IProperty modifier = modifiers.next( );
                    LOG.debug( "Adding modifier " + modifier );
                    chTree.addQueryModifier( modifier );
                  }

                  // addTermQueryTrees( chTree, qField.getFieldName( ), terms );
                }
              }
            }
          }
        }
      }
    }
  }
    
    
  private static void addTermQueryTrees( QueryTree chTree, String fieldName, String[] terms )
  {
    LOG.debug( "addTermQueryTrees " + terms.length );
    for (int t = 0; t < terms.length; t++)
    {
      QueryTree gc = new QueryTree( );
      gc.setOperator( QueryTree.TERM );
      gc.setQueryField( fieldName );
      gc.setQueryText( terms[t] );
      LOG.debug( "Adding term: " + terms[t] );
      chTree.addChild( gc );
    }
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    LOG.debug( "Query: createDataObjectSchema( )" );
    	
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "Query" );
    dos.setDataObjectType( "com.modinfodesigns.search.Query" );
    dos.setChildObjectSchema( "QueryField" );
        
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( MULTI_FIELD_OPERATOR );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
        
    pd = new PropertyDescriptor( );
    pd.setIsDataObject( true );
    pd.setDataObjectSchema( "QueryTree" );
    pd.setName( SCOPE_QUERY );
    dos.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( USER_ID );
    pd.setPropertyType( "String" );
    dos.addPropertyDescriptor( pd );
    
    return dos;
  }

  @Override
  public String getCollectionName()
  {
    return this.collectionName;
  }

  @Override
  public void setCollectionName( String collectionName)
  {
    this.collectionName = collectionName;
  }
}
