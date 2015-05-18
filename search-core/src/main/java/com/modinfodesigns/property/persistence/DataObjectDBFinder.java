package com.modinfodesigns.property.persistence;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.persistence.database.IDataSourceFactory;
import com.modinfodesigns.app.persistence.database.SQLMethods;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.persistence.DataObjectPlaceholder;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.MappedProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchemaManager;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.search.FinderException;
import com.modinfodesigns.search.FinderField;
import com.modinfodesigns.search.IFinder;
import com.modinfodesigns.search.IQuery;
import com.modinfodesigns.search.QueryField;
import com.modinfodesigns.search.IResultList;
import com.modinfodesigns.search.BasicResultList;
import com.modinfodesigns.utils.StringMethods;

import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IFinder implementation for DataObjects persisted through SQLTemplatePersistenceManager and DataObjectSchemaManager.
 * 
 * @author Ted Sullivan
 */

public class DataObjectDBFinder implements IFinder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ApplicationManager.class );

  private String name;
  private String dataSchemaName;
    
  private String dataSourceFactoryName;   // name of IDataSourceFactory
  private IDataSourceFactory dataSourceFactory;
  private SQLMethods sqlMethods;
    
  private DataObjectSchemaManager dataObjectSchemaManager;
  private SQLTemplatePersistenceManager stpm;
    
  private String dataObjectSchemaManagerClass;
    
  private int MAX_QUERY_FIELD_LENGTH = 20;
    
  // If both are false, use '=' else use LIKE with % wildcards
  private boolean usePrefix = false;
  private boolean usePostfix = false;
    
  private boolean useLazyEvaluation = false;
    
  private boolean rootObjectsOnly = true;
    
  public DataObjectDBFinder( ) {  }
    
  public DataObjectDBFinder( IDataSourceFactory dataSourceFactory, String dataObjectSchemaName )
  {
    this.dataSourceFactory = dataSourceFactory;
    this.dataSchemaName = dataObjectSchemaName;
  }
    
  public DataObjectDBFinder( String dataSourceFactoryName, String dataObjectSchemaName )
  {
    this.dataSourceFactoryName = dataSourceFactoryName;
    this.dataSchemaName = dataObjectSchemaName;
  }
    
  public void setDataSourceFactory( String dataSourceFactory )
  {
    this.dataSourceFactoryName = dataSourceFactory;
  }
    
  public void setDataSourceFactoryName( String dataSourceFactory )
  {
    this.dataSourceFactoryName = dataSourceFactory;
  }
    
  public void setDataObjectSchemaName( String dataSchemaName )
  {
    this.dataSchemaName = dataSchemaName;
  }
    
  public void setSchemaName( String schemaName )
  {
    this.dataSchemaName = schemaName;
  }
    
  public void setDataObjectSchemaManager( DataObjectSchemaManager dosm )
  {
    this.dataObjectSchemaManager = dosm;
  }
    
  public void setDataObjectSchemaManagerClass( String dataObjectSchemaManagerClass )
  {
    this.dataObjectSchemaManagerClass = dataObjectSchemaManagerClass;
  }
    
  public void setUsePrefixWildcard( boolean usePrefix )
  {
    this.usePrefix = usePrefix;
  }
    
  public void setUsePrefixWildcard( String usePrefix )
  {
    this.usePrefix = (usePrefix != null && usePrefix.equalsIgnoreCase( "true" ));
  }
    
  public void setUsePostfixWildcard( boolean usePostfix )
  {
    this.usePostfix = usePostfix;
  }
    
  public void setUsePostfixWildcard( String usePostfix )
  {
    this.usePostfix = (usePostfix != null && usePostfix.equalsIgnoreCase( "true" ));
  }
    
  public void setRootObjectsOnly( String rootObjectsOnly )
  {
    this.rootObjectsOnly = (rootObjectsOnly != null && rootObjectsOnly.equalsIgnoreCase( "true" ));
  }
    
  @Override
  public String getName()
  {
    return this.name;
  }
    
  public void setName( String name )
  {
    this.name = name;
  }
    

  @Override
  public IResultList executeQuery( IQuery query ) throws FinderException
  {
    LOG.debug( "executeQuery: " + ((query != null) ? query.getValue( ) : "Query is NULL!") );
    	
    String schemaName = getSchemaName( query );
    
    String[] databaseTables = getDatabaseTables( schemaName );
    if (databaseTables.length == 1 )
    {
      String sqlQuery = createSQLQuery( databaseTables[0], query, schemaName );
      LOG.debug( "executing sql query: " + sqlQuery );
      return executeQuery( query, sqlQuery, schemaName, databaseTables[0] );
    }
    else
    {
      BasicResultList wholeList = new BasicResultList( );
      for (int i = databaseTables.length-1; i >= 0; --i)
      {
        String sqlQuery = createSQLQuery( databaseTables[i], query, schemaName );
        LOG.debug( "executing sql query: " + sqlQuery );
                
        IResultList verList = executeQuery( query, sqlQuery, schemaName, databaseTables[i] );
        if (verList != null)
        {
          Iterator<DataObject> verObjs = verList.getData( );
          while ( verObjs != null && verObjs.hasNext( ))
          {
            wholeList.addDataObject( verObjs.next( ) );
          }
        }
      }
            
      return wholeList;
    }
  }
    
  private String createSQLQuery( String databaseTable, IQuery query, String schemaName )
  {
    LOG.debug( "createSQLQuery: " + databaseTable );
    	
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    DataObjectSchema dos = dosm.getDataObjectSchema( schemaName );
        
    if (query == null || query.getQueryFields( ) == null)
    {
      return "select * from " + databaseTable;
    }
        
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( "select * from " ).append( databaseTable ).append( " where " );
    
    boolean haveField = false;
    List<QueryField> queryFields = query.getQueryFields( );
    if (queryFields != null)
    {
      for (int i = 0; i < queryFields.size( ); i++)
      {
        QueryField qf = queryFields.get( i );
        if (qf.getFieldName().equals( DataObject.OBJECT_NAME_PARAM ))
        {
          if (haveField) strbuilder.append( " and " );
            
          strbuilder.append( " " ).append( DataObject.OBJECT_NAME_PARAM );
          if (usePrefix || usePostfix )
          {
            strbuilder.append( " LIKE " );
            strbuilder.append( ((usePrefix) ? "'%" : "'" ) );
            strbuilder.append( qf.getFieldValue( ) );
            strbuilder.append( ((usePostfix) ? "%'" : "'" ) );
          }
          else
          {
            strbuilder.append( "='" ).append( qf.getFieldValue( )).append( "'" );
          }
          haveField = true;
        }
        else if (qf.getFieldName().equals( DataObject.OBJECT_ID_PARAM ))
        {
          if (haveField) strbuilder.append( " and " );
          strbuilder.append( " " ).append( DataObject.OBJECT_ID_PARAM ).append( "='" ).append( qf.getFieldValue( )).append( "'" );
          haveField = true;
        }
        else if (qf.getFieldName().equals( DataObject.ENTITY_TYPE_PARAM ))
        {
          if (haveField) strbuilder.append( " and " );
          strbuilder.append( " " ).append( DataObject.ENTITY_TYPE_PARAM ).append("='" ).append( qf.getFieldValue( )).append( "'" );
          haveField = true;
        }
                
        else if (dos != null && qf.getFieldName().equals( DataObjectSchema.SCHEMA_PARAM ) == false )
        {
          PropertyDescriptor fieldDesc = dos.getPropertyDescriptor( qf.getFieldName( ) );
          if (fieldDesc != null)
          {
            if (haveField) strbuilder.append( " and " );
            strbuilder.append( fieldDesc.getName( ) ).append( "=" );
            if (fieldDesc.getDataType().equals( "String" ))
            {
              strbuilder.append( "'" );
            }
            strbuilder.append( qf.getFieldValue( ) );
            if (fieldDesc.getDataType().equals( "String" ))
            {
              strbuilder.append( "'" );
            }
                        
            haveField = true;
          }
        }
      }
    }
        
    if (rootObjectsOnly)
    {
      strbuilder.append( " and is_root='T'" );
    }
        
    return strbuilder.toString( );
  }
    
  private IResultList executeQuery( IQuery query, String sqlStatement, String schemaName, String tableName )
  {
    LOG.debug( "executeQuery( " + sqlStatement + ", " + schemaName + ", " + tableName + " )" );
    
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    DataObjectSchema dos = dosm.getDataObjectSchema( schemaName );
        
    BasicResultList brl = new BasicResultList( );
    
    SQLMethods sqlMethods = getSQLMethods( );
    ResultSet rs = sqlMethods.executeSQLQuery( sqlStatement );
    try
    {
      while (rs != null && rs.next() )
      {
        DataObject dobj = createDataObject( dos, query, rs);
        brl.addDataObject( dobj );
      }
    }
    catch ( SQLException sqle )
    {
      LOG.debug( "Got SQLException: " + sqle );
    }
    finally
    {
      sqlMethods.closeDatabaseConnection( );
    }
        
    LOG.debug( "Got " + brl.size( ) + " results." );
    brl.setTotalResults( brl.size( ) );
    return brl;
  }
    
  private DataObject createDataObject( DataObjectSchema dos, IQuery query, ResultSet rs )
  {
    LOG.debug( "createDataObject( ) " + query );
        
    DataObject dobj = null;
        
    // get the object class from the Schema ...
    // The schema may be null if the search table is master_object_table
    String dataType = (dos != null) ? dos.getDataObjectType( ) : null;
    LOG.debug( "Creating Data Type: " + dataType );
    if (dataType != null)
    {
      try
      {
        dobj = (DataObject)Class.forName( dataType ).newInstance( );
      }
      catch ( Exception e )
      {
        dobj = new DataObject( );
      }
    }
    else
    {
      dobj = new DataObject( );
    }
        
    LOG.debug( "Creating Data Object: " + dobj );
        
    try
    {
      String name = rs.getString( DataObject.OBJECT_NAME_PARAM );
      dobj.setName( name );
      dobj.setJsonNameLabel( DataObject.OBJECT_NAME_PARAM );
        
      dobj.setProperty( new StringProperty( DataObject.OBJECT_NAME_PARAM, name ));
            
      LOG.debug( "Got name = " + name );
            
      String obj_id = rs.getString( DataObject.OBJECT_ID_PARAM );
      dobj.setID( obj_id );
      dobj.setIDLabel( DataObject.OBJECT_ID_PARAM );
      dobj.setProperty( new StringProperty( DataObject.OBJECT_ID_PARAM, obj_id ));
            
      String schema = (dos != null) ? dos.getName( ) : rs.getString( DataObjectSchemaManager.SCHEMA_NAME_COL );
      dobj.setDataObjectSchema( schema );

      LOG.debug( "Got Schema: " + schema );
      if (schema.equals( SQLTemplatePersistenceManager.MASTER_TABLE ))
      {
        String entityType = rs.getString( DataObject.ENTITY_TYPE_PARAM );
        if (entityType != null)
        {
          LOG.debug( "Setting entityType: " + entityType );
          dobj.setProperty( new StringProperty( DataObject.ENTITY_TYPE_PARAM, entityType ));
        }
      }
            
      String proxyKey = null;
      try
      {
        proxyKey = rs.getString( SQLTemplatePersistenceManager.PROXY_FIELD );
      }
      catch ( Exception e )
      {
            	
      }
        
      if (proxyKey != null && proxyKey.trim().length() > 0)
      {
        // Create a Proxy Object placeholder
        String[] components = StringMethods.getStringArray( proxyKey, "|" );
        String childPlaceholderClass = dos.getChildPlaceholderClass( );
        IDataObjectPlaceholder dopl = null;
        if (childPlaceholderClass != null && components != null && components.length == 3)
        {
          LOG.debug( "Creating Proxy Placeholder: " + components[0] + ", " + components[1] + ", " + components[2] );
          try
          {
            dopl = (IDataObjectPlaceholder)Class.forName( childPlaceholderClass ).newInstance( );
            if (dopl instanceof DataObject)
            {
              dopl.setName( components[0] );
              dopl.setID( components[1] );
              dopl.setSchemaName( components[2] );
              dopl.setSQLTemplatePersistenceManager( getSQLTemplatePersistenceManager( ) );
              dobj.setProxyObject( (DataObject)dopl );
              ((DataObject) dopl).setProperty( new StringProperty( "proxy", "true" ));
              return dobj;
            }
          }
          catch ( Exception e )
          {
            LOG.error( "Could not create Proxy Object Placeholder class '" + childPlaceholderClass + "'" );
            LOG.debug( "Could not create Proxy Object Placeholder class '" + childPlaceholderClass + "'" );
          }
        }

        if (dopl == null)
        {
          dopl = new DataObjectPlaceholder( components[0], components[1], components[2], getSQLTemplatePersistenceManager( ) );
          dobj.setProxyObject( (DataObject)dopl );
          ((DataObject) dopl).setProperty( new StringProperty( "proxy", "true" ));
          return dobj;
        }
      }
            
      List<String> displayFields = (query != null) ? query.getDisplayFields( ) : null;
            
      List<PropertyDescriptor> propDescriptors = (dos != null) ? dos.getPropertyDescriptors( ) : null;
      if (propDescriptors != null)
      {
        for (int i = 0; i < propDescriptors.size(); i++)
        {
          PropertyDescriptor pd = propDescriptors.get( i );
          String fieldName = pd.getName( );
          if (pd.isFunction())
          {
            LOG.debug( "Adding Function property: " + fieldName );
                        
            try
            {
              IFunctionProperty funProp = (IFunctionProperty)Class.forName( pd.getPropertyType() ).newInstance( );
              funProp.setFunction( pd.getFunctionDescription( ) );
              funProp.setName( fieldName );
           
              dobj.setProperty( funProp );
            }
            catch ( Exception e )
            {
              LOG.error( "Cannot create IFunctionProperty " + pd.getPropertyType( ) + " " + e );
              e.printStackTrace( );
            }
          }
          else if ( displayFields == null || displayFields.contains( fieldName ))
          {
            LOG.debug( "getting field: " + fieldName );
                    
            String value = rs.getString( StringTransform.replaceSubstring( fieldName, " ", "_" ) );
            if (value != null)
            {
              LOG.debug( " Creating prop for " + fieldName + " = " + value );
                            
              String propType = pd.getPropertyType( );
              if (propType == null)
              {
                propType = "com.modinfodesigns.property.string.StringProperty";
              }
                        
              try
              {
                IProperty prop = (IProperty)Class.forName( propType ).newInstance( );
                String format = (prop instanceof PropertyList) ? IProperty.XML_FORMAT : pd.getPropertyFormat( );
                LOG.debug( "Using format " + format );
                prop.setName( fieldName );
                prop.setValue( value, format );
                dobj.setProperty( prop );
              }
              catch ( Exception e )
              {
                LOG.error( "Could not create property! " + pd.getName( ) + " Exception was" + e );
              }
            }
          }
                    
          Map<String,String> mappedProps = pd.getPropertyMappings( );
          if (mappedProps != null)
          {
            for (Iterator<String> mapIt = mappedProps.keySet().iterator(); mapIt.hasNext(); )
            {
              String from = mapIt.next( );
              String to = mappedProps.get( from );
                            
              // MappedProperty wires itself to dobj in constructor
              MappedProperty mapProp = new MappedProperty( to, from, dobj );
              LOG.debug( "Created MappedProperty: " + mapProp.getName( ) );
              LOG.debug( mapProp.getName( ) + " = " + mapProp.getValue( ) );
            }
          }
        }
      }
            
      if (dobj instanceof DataList)
      {
        DataList dList = (DataList)dobj;
            	
        LOG.debug( "Need to pick up the Kids!" );
            	
        String childIDs = rs.getString( "children" );
        LOG.debug( "Got children IDs: " + childIDs );
        if (childIDs != null)
        {
          String[] childList = StringMethods.getStringArray( childIDs,  ";" );
          if (childList != null)
          {
            for (int i = 0; i < childList.length; i++)
            {
              String[] components = StringMethods.getStringArray( childList[i], "|" );
              String chName = components[0];
              String ID = components[1];
              String childSchema = components[2];
            				
              DataObject chObj = null;
                            
              if (useLazyEvaluation)
              {
                LOG.debug( "Creating DataObjectPlaceholder for " + chName );
                String placeholderClass = dos.getChildPlaceholderClass( );
                if (placeholderClass != null)
                {
                  LOG.debug( "Getting placeholder: " + placeholderClass );
                  try
                  {
                    IDataObjectPlaceholder doph = (IDataObjectPlaceholder)Class.forName( placeholderClass ).newInstance( );
                    doph.setName( chName );
                    doph.setID( ID );
                    doph.setSQLTemplatePersistenceManager( getSQLTemplatePersistenceManager( ) );
                    doph.setSchemaName( childSchema );
                                    
                    chObj = (DataObject)doph;
                  }
                  catch ( Exception e )
                  {
                    LOG.error( "Could not create DataObjectPlaceholder: " + placeholderClass + " Exception was " + e );
                  }
                }
                else
                {
                  chObj = new DataObjectPlaceholder( chName, ID, childSchema, getSQLTemplatePersistenceManager( ) );
                }
                        
                if (chObj != null)
                {
                  dList.addDataObject( chObj );
                }
              }
              else
              {
                LOG.debug( "Reading child " + chName + ", " + ID + ", " + childSchema );
                SQLTemplatePersistenceManager stpm = getSQLTemplatePersistenceManager( );
                chObj = stpm.read( chName, ID, childSchema );
                if (chObj != null)
                {
                  dList.addDataObject( chObj );
                }
              }
            }
          }
        }
      }
    }
    catch (SQLException sqle )
    {
      LOG.debug( "Got SQLException " + sqle );
    }
    catch ( Throwable t )
    {
      LOG.debug( "Got Throwable " + t );
      t.printStackTrace( System.out );
    }

    LOG.debug( "Got DataObject: " + dobj.getValue( ) );
    return dobj;
  }

  @Override
  public DataObject getSingleResult( String resultID ) throws FinderException
  {
    String[] databaseTables = getDatabaseTables( );
    if (databaseTables.length == 1)
    {
      String select = "select * from " + databaseTables[0] + " where object_id='" + resultID + "'";
      IResultList results = executeQuery( null, select, dataSchemaName, databaseTables[0] );
      if (results != null)
      {
        Iterator<DataObject> dataObjs = results.getData( );
        return (dataObjs != null && dataObjs.hasNext( )) ? dataObjs.next( ) : null;
      }
    }
    else
    {
      // use the latest and the one before that.  If no results, try older versions
      // if they exist
      for (int i = databaseTables.length-1; i >= 0; --i)
      {
        String select = "select * from " + databaseTables[i] + " where object_id='" + resultID + "'";
        IResultList results = executeQuery( null, select, dataSchemaName, databaseTables[i] );
        if (results != null)
        {
          Iterator<DataObject> dataObjs = results.getData( );
          return (dataObjs != null && dataObjs.hasNext( )) ? dataObjs.next( ) : null;
        }
      }
    }
        
    return null;
  }
    
  private String getSchemaName( IQuery query )
  {
    if (query == null) return this.dataSchemaName;
    	
    QueryField qf = query.getQueryField( DataObjectSchema.SCHEMA_PARAM );
    if (qf != null)
    {
      return qf.getFieldValue();
    }
    	
    return this.dataSchemaName;
  }

  /**
   * Returns object_name, object_id plus all fields in the DataObject Schema
   * that are:
   * <ul>
   *    <li> fixed width < max query field width
   *    <li> have fixed values
   * </ul>
   */
  @Override
  public Set<String> getQueryFields()
  {
    HashSet<String> queryFields = new HashSet<String>( );
    queryFields.add( DataObject.OBJECT_NAME_PARAM );
    queryFields.add( DataObject.OBJECT_ID_PARAM );
    
    addQueryFields( queryFields, this.dataSchemaName );
    
    return queryFields;
  }
    
  private void addQueryFields( HashSet<String> queryFields, String dataSchemaName )
  {
    if (dataSchemaName == null) return;
    
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    DataObjectSchema dos = dosm.getDataObjectSchema( dataSchemaName );
    
    List<PropertyDescriptor> propDescriptors = dos.getPropertyDescriptors( );
    if (propDescriptors != null)
    {
      for (int i = 0; i < propDescriptors.size(); i++)
      {
        PropertyDescriptor pd = propDescriptors.get( i );
        if ( pd.isDataObject( ) )
        {
          String childSchemaName = pd.getDataObjectSchema( );
          addQueryFields( queryFields, childSchemaName );
        }
        else if ( isSearchable( pd ))
        {
          queryFields.add( pd.getName( ) );
        }
      }
    }
  }

  @Override
  public Set<String> getNavigatorFields()
  {
    return null;
  }

  @Override
  public Set<String> getResultFields()
  {
    HashSet<String> resultFields = new HashSet<String>( );
    resultFields.add( DataObject.OBJECT_NAME_PARAM );
    resultFields.add( DataObject.OBJECT_ID_PARAM );
    
    if (dataSchemaName != null)
    {
      DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
      DataObjectSchema dos = dosm.getDataObjectSchema( this.dataSchemaName );
    
      List<PropertyDescriptor> propDescriptors = (dos != null) ? dos.getPropertyDescriptors( ) : null;
      if (propDescriptors != null)
      {
        for (int i = 0; i < propDescriptors.size(); i++)
        {
          PropertyDescriptor pd = propDescriptors.get( i );
          resultFields.add( pd.getName( ) );
        }
      }
    }
        
    return resultFields;
  }

  @Override
  public Iterator<String> getFinderFields()
  {
    Set<String> finderFields = new HashSet<String>( );
        
    Set<String> resFields = getResultFields( );
    finderFields.addAll( resFields );
    
    Set<String> queryFields = getQueryFields( );
    finderFields.addAll( queryFields );
    
    return finderFields.iterator( );
  }

  @Override
  public FinderField getFinderField( String name )
  {
    if (name.equals( DataObject.OBJECT_NAME_PARAM ))
    {
      FinderField nameField = new FinderField( );
      nameField.setName( DataObject.OBJECT_NAME_PARAM );
      nameField.setDisplayName( "Name" );
      nameField.setDisplayable( true );
      nameField.setSearchable( true );
            
      return nameField;
    }
    else if (name.equals( DataObject.OBJECT_ID_PARAM ))
    {
      FinderField idField = new FinderField( );
      idField.setName( DataObject.OBJECT_ID_PARAM );
      idField.setDisplayName( "ID" );
      idField.setDisplayable( true );
      idField.setSearchable( true );
        
      return idField;
    }
    else if (this.dataSchemaName != null)
    {
      DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
      DataObjectSchema dos = dosm.getDataObjectSchema( this.dataSchemaName );
    
      PropertyDescriptor pd = (dos != null) ? dos.getPropertyDescriptor( name ) : null;
      if (pd != null && pd.isDataObject( ) == false)
      {
        // create a FinderField from the PropertyDescriptor - return it.
        FinderField ff = new FinderField( );
        ff.setName( pd.getName( ) );
        String displayName = (pd.getDisplayName( ) != null) ? pd.getDisplayName( ) : pd.getName( );
        ff.setDisplayName( displayName );
          
        ff.setDisplayable( true );
        ff.setSearchable(  isSearchable( pd ) );

        ff.setIsFacet( false );
      }
    }
        
    return null;
  }
    
  private boolean isSearchable( PropertyDescriptor pd )
  {
    int maxLength = pd.getMaximumLength( );
    PropertyList propValues = pd.getPropertyValues( );

    return (propValues != null || ( maxLength > 0 && maxLength < MAX_QUERY_FIELD_LENGTH ));
  }
    

  private DataObjectSchemaManager getDataObjectSchemaManager( )
  {
    if (this.dataObjectSchemaManager != null) return this.dataObjectSchemaManager;
        
    synchronized( this )
    {
      if (this.dataObjectSchemaManager != null) return this.dataObjectSchemaManager;
      IDataSourceFactory dataSourceFac = getDataSourceFactory( );
      LOG.debug( "Creating DataObjectSchemaManager with data source: " + dataSourceFac );
            
      if (this.dataObjectSchemaManagerClass != null)
      {
        try
        {
          LOG.debug( "Creating " + dataObjectSchemaManagerClass + " instance." );
          this.dataObjectSchemaManager = (DataObjectSchemaManager)Class.forName( dataObjectSchemaManagerClass ).newInstance( );
          this.dataObjectSchemaManager.setDataSourceFactoryName( this.dataSourceFactoryName );
        }
        catch ( Exception e )
        {
            		
        }
      }
      else
      {
        this.dataObjectSchemaManager = new DataObjectSchemaManager( dataSourceFac );
      }
    }
        
    return this.dataObjectSchemaManager;
  }
    
  private SQLTemplatePersistenceManager getSQLTemplatePersistenceManager( )
  {
    if (this.stpm != null) return this.stpm;
    this.stpm = new SQLTemplatePersistenceManager( this.dataSourceFactoryName );
    this.stpm.setDataObjectSchemaManager( getDataObjectSchemaManager( ) );
    return this.stpm;
  }
    
  private SQLMethods getSQLMethods( )
  {
    if (this.sqlMethods != null) return this.sqlMethods;
        
    LOG.debug( "Creating SQLMethods" );
    IDataSourceFactory dataSourceFac = getDataSourceFactory( );
        
    this.sqlMethods = new SQLMethods( dataSourceFac );
    return this.sqlMethods;
  }
    
  private IDataSourceFactory getDataSourceFactory( )
  {
    if (this.dataSourceFactory != null) return this.dataSourceFactory;
        
    synchronized ( this )
    {
      if (this.dataSourceFactoryName == null)
      {
        LOG.error( "No DataSourceFactory name set - cannot load DataSourceFactory!" );
        return null;
      }
            
      ApplicationManager appMan = ApplicationManager.getInstance( );
      this.dataSourceFactory = (IDataSourceFactory)appMan.getApplicationObject( dataSourceFactoryName, "DataSourceFactory" );
      LOG.debug( "Got DataSourceFactory: " + dataSourceFactoryName + " = " + dataSourceFactory);
            
      if (dataSourceFactory == null)
      {
        LOG.error( "Cannot load IDataSourceFactory: " + dataSourceFactoryName );
        return null;
      }
    }
        
    return this.dataSourceFactory;
  }
    

  private String[] getDatabaseTables( )
  {
    return getDatabaseTables( this.dataSchemaName );
  }
    
  private String[] getDatabaseTables( String schemaName )
  {
    if (schemaName.equals( SQLTemplatePersistenceManager.MASTER_TABLE ))
    {
      String[] tables = new String[1];
      tables[0] = SQLTemplatePersistenceManager.MASTER_TABLE;
      return tables;
    }
    	
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    int[] versions = dosm.getSchemaVersions( schemaName );
    
    String[] tableNames = new String[ versions.length ];
    for (int i = 0; i < versions.length; i++)
    {
      tableNames[i] = StringTransform.replaceSubstring(schemaName, " ", "_") + "_" + Integer.toString( versions[i] );
    }
        
    return tableNames;
  }

  @Override
  public void setSourceFieldName( String sourceFieldName )
  {
		
  }

  @Override
  public FinderField getSourceField()
  {
    return null;
  }


}
