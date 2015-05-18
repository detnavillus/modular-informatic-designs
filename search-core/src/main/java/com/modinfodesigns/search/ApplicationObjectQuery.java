package com.modinfodesigns.search;


import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.property.schema.ICreateDataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import java.util.List;

public class ApplicationObjectQuery extends DataObject implements IQuery, ICreateDataObjectSchema
{
  public static final String QUERY_NAME = "QueryName";
	
  private int pageSize;
  private int startRecord;
	
  public void setQueryName( String queryName )
  {
    setProperty( new StringProperty( QUERY_NAME, queryName ) );
  }
	
  public String getQueryName(  )
  {
    IProperty queryNameProp = getProperty( QUERY_NAME );
    return (queryNameProp != null) ? queryNameProp.getValue( ) : null;
  }

  @Override
  public String getType()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getType( ) : null;
  }

  @Override
  public String getValue()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getValue( ) : null;
  }

  @Override
  public String getValue(String format)
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getValue( format ) : null;
  }

  @Override
  public void setValue(String value, String format)
           throws PropertyValidationException
  {

  }

  @Override
  public String getDefaultFormat()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getDefaultFormat(  ) : null;
  }

  @Override
  public IProperty copy()
  {
    return null;
  }

  @Override
  public Object getValueObject()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getValueObject(  ) : null;
  }

  @Override
  public void setPageSize(int pageSize)
  {
    this.pageSize = pageSize;
  }

  @Override
  public int getPageSize()
  {
    return this.pageSize;
  }

  @Override
  public void setStartRecord(int startRecord)
  {
    this.startRecord = startRecord;
  }

  @Override
  public int getStartRecord()
  {
    return this.startRecord;
  }

  @Override
  public void setSortProperty(SortProperty sortProp)
  {
    IQuery appQuery = getApplicationObjectQuery( );
    if (appQuery != null)
    {
      appQuery.setSortProperty( sortProp );
    }
  }

  @Override
  public SortProperty getSortProperty()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getSortProperty( ) : null;
  }

  @Override
  public IUserCredentials getUserCredentials()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getUserCredentials( ) : null;
  }

  @Override
  public void setDefaultQueryField(QueryField defaultQuery)
  {
    IQuery appQuery = getApplicationObjectQuery( );
    if (appQuery != null)
    {
      appQuery.setDefaultQueryField( defaultQuery );
    }
  }

  @Override
  public QueryField getDefaultQueryField()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getDefaultQueryField( ) : null;
  }

  @Override
  public List<QueryField> getQueryFields()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getQueryFields( ) : null;
  }

  @Override
  public QueryField getQueryField( String fieldName )
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getQueryField( fieldName ) : null;
  }

  @Override
  public void setMultiFieldOperator( String fieldOperator )
  {
    IQuery appQuery = getApplicationObjectQuery( );
    if (appQuery != null)
    {
      appQuery.setMultiFieldOperator( fieldOperator );
    }
  }

  @Override
  public String getMultiFieldOperator()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getMultiFieldOperator( ) : null;
  }

  @Override
  public void setNavigatorFields( List<String> navFields )
  {
    IQuery appQuery = getApplicationObjectQuery( );
    if (appQuery != null)
    {
      appQuery.setNavigatorFields( navFields );
    }
  }

  @Override
  public List<String> getNavigatorFields()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getNavigatorFields( ) : null;
  }

  @Override
  public void setDisplayFields(List<String> displayFields)
  {
    IQuery appQuery = getApplicationObjectQuery( );
    if (appQuery != null)
    {
      appQuery.setDisplayFields( displayFields );
    }
  }

  @Override
  public List<String> getDisplayFields()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getDisplayFields( ) : null;
  }

  @Override
  public void setScopeQuery(IQuery scopeQuery)
  {
    IQuery appQuery = getApplicationObjectQuery( );
    if (appQuery != null)
    {
      appQuery.setScopeQuery( scopeQuery );
    }
  }

  @Override
  public IQuery getScopeQuery()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getScopeQuery( ) : null;
  }

  @Override
  public void setRelevanceQuery(IQuery relevanceQuery)
  {
    IQuery appQuery = getApplicationObjectQuery( );
    if (appQuery != null)
    {
      appQuery.setRelevanceQuery( relevanceQuery );
    }
  }

  @Override
  public IQuery getRelevanceQuery()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.getRelevanceQuery( ) : null;
  }

  @Override
  public QueryTree convertToQueryTree()
  {
    IQuery appQuery = getApplicationObjectQuery( );
    return (appQuery != null) ? appQuery.convertToQueryTree( ) : null;
  }
	
  private IQuery getApplicationObjectQuery( )
  {
    ApplicationManager appMan = ApplicationManager.getInstance( );
    String queryName = getQueryName( );
    return (queryName != null) ? (IQuery)appMan.getApplicationObject( queryName, "Query" ) : null;
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "ApplicationObjectQuery" );
    dos.setDataObjectType( this.getClass().getCanonicalName( ) );
    
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( QUERY_NAME );
    
    ApplicationManager appMan = ApplicationManager.getInstance( );
    List<Object> queryList = appMan.getApplicationObjects( "Query" );
    String[] queryNames = new String[ queryList.size( ) ];
    for (int i = 0; i < queryList.size( ); i++)
    {
      IQuery query = (IQuery)queryList.get( i );
      queryNames[i] = query.getName( );
    }
    pd.setPropertyValues( queryNames );
    dos.addPropertyDescriptor( pd );
    
    return dos;
  }

  @Override
  public String getCollectionName()
  {
    return null;
  }

  @Override
  public void setCollectionName( String collectionName )
  {
		
  }

  @Override
  public void setMultiTermOperator( String multiTermOperator )
  {
		
  }

  @Override
  public String getMultiTermOperator()
  {
    return null;
  }

}
