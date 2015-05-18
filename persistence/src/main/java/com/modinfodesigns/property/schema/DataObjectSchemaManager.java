package com.modinfodesigns.property.schema;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.persistence.database.IDataSourceFactory;
import com.modinfodesigns.app.persistence.database.SQLMethods;

import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.utils.StringMethods;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.EnumerationProperty;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains a set of Data Object Schema tables in a relational database. 
 * 
 * @author Ted Sullivan
 */
public class DataObjectSchemaManager
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataObjectSchemaManager.class );

  private IDataSourceFactory dataSourceFactory;
  protected String dataSourceFactoryName;
    
  private SQLMethods sqlMethods;
    
  private static HashMap<String,DataObjectSchema> dataSchemas = new HashMap<String,DataObjectSchema>( );
    
  public static final String SCHEMA_TABLE        = "OBJECT_SCHEMA_TABLE";
  public static final String PROPERTY_TABLE      = "PROP_DESCRIPTOR_TABLE";
  public static final String SCHEMA_NAME_COL     = "schema_name";
  public static final String VERSION_COL         = "version";
  public static final String CREATED_COL         = "created";
  public static final String SCHEMA_ID_COL       = "schema_id";
  public static final String ENTITY_TYPE_COL     = "entity_type";
  public static final String OBJECT_CLASS_COL    = "object_class";
  public static final String PLHLDR_CLASS_COL    = "placeholder_class";
  public static final String PUBLISHED_COL       = "published";
  public static final String PROPERTY_NAME_COL   = "property_name";
  public static final String DISPLAY_NAME_COL    = "display_name";
  public static final String PROPERTY_CLASS_COL  = "property_class";
  public static final String PROPERTY_SCHEMA_COL = "property_schema";
  public static final String DEFAULT_FORMAT_COL  = "default_format";
  public static final String ALLOWED_VALUES_COL  = "allowed_values";
  public static final String DEFAULT_VALUE_COL   = "default_value";
  public static final String REQUIRED_COL        = "required";
  public static final String MULTIVALUE_COL      = "multi_value";
  public static final String MAPPINGS_COL        = "mappings";
  public static final String IS_FUNCTION_COL     = "is_function";
  public static final String FUNCTION_DESC_COL   = "function_desc";
  public static final String COMPONENTS_COL      = "components";
    
  public static final String DELIMITER = "|";
    
  public DataObjectSchemaManager( ) {  }
    
  public DataObjectSchemaManager( IDataSourceFactory dataSourceFactory )
  {
    this.dataSourceFactory = dataSourceFactory;
  }

  public DataObjectSchemaManager( String dataSourceFactoryName )
  {
    this.dataSourceFactoryName = dataSourceFactoryName;
  }
    
  public void setDataSourceFactoryName( String dataSourceFactoryName )
  {
    this.dataSourceFactoryName = dataSourceFactoryName;
  }
    
  public List<String> getDataObjectSchemas(  )
  {
    return getDataObjectSchemas( false );
  }
    
  /**
   * Creates a DataObjectSchema from a DataSchemaTemplate given the name of the schema to create
   * and the name of the template.  Assumes that the configuration for the template is available from
   * the application Object Factory set.
   *
   * @param schemaName        The name to use for the newly created DataObjectSchema
   * @param schemaTemplate    The name of the DataSchemaTemplate used to initialize the pre-configured portions
   *                          of the schema.
   * @return
   */
  public DataObjectSchema createSchema( String schemaName, String schemaTemplate )
  {
    LOG.debug( "createSchema: " + schemaName + " template: " + schemaTemplate );
        
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( schemaName );
        
    if (schemaTemplate != null)
    {
      ApplicationManager appMan = ApplicationManager.getInstance( );
      DataSchemaTemplate dst = (DataSchemaTemplate)appMan.getApplicationObject( schemaTemplate, "DataSchemaTemplate" );
      if (dst != null)
      {
        String dataObjectType = dst.getDataObjectType( );
        if (dataObjectType != null)
        {
          dos.setDataObjectType( dataObjectType );
        }
            
        String childSchema = dst.getChildObjectSchema( );
        if (childSchema != null)
        {
          dos.setChildObjectSchema( childSchema );
        }
        
        String childPlaceholderClass = dst.getChildPlaceholderClass( );
        if (childPlaceholderClass != null)
        {
          dos.setChildPlaceholderClass( childPlaceholderClass );
        }
                
        String entityType = dst.getEntityType( );
        if (entityType != null)
        {
          dos.setEntityType( entityType );
        }
            
        List<PropertyDescriptor> tempDescriptors = dst.getPropertyDescriptors( );
        if (tempDescriptors != null)
        {
          for (int i = 0; i < tempDescriptors.size( ); i++)
          {
            PropertyDescriptor pd = tempDescriptors.get( i );
            LOG.debug( "Adding Property Descriptor: " + pd.getName( ) + " " + pd );
            LOG.debug( "components " + pd.getProperty( PropertyDescriptor.COMPONENTS ) );
            PropertyDescriptor pdCopy = (PropertyDescriptor)pd.copy( );
            LOG.debug( "copied components " + pdCopy.getProperty( PropertyDescriptor.COMPONENTS ) );
            dos.addPropertyDescriptor( pdCopy );
          }
        }
      }
      else
      {
        LOG.debug( "Could not get Schema Template: " + schemaTemplate );
      }
    }
        
    return dos;
  }
    
  /**
   * Returns the list of Data Object Schema names that have been created and persisted.
   *
   * @param deduplicate  if true returns only unique names.
   *
   * @return
   */
  public List<String> getDataObjectSchemas( boolean deduplicate )
  {
    LOG.debug( "getDataObjectSchemas( " + deduplicate + " )" );
        
    ArrayList<String> dataObjectSchemas = new ArrayList<String>( );
    String selectStr = "select " + SCHEMA_NAME_COL + " from " + SCHEMA_TABLE;
        
    if ( tableExists( SCHEMA_TABLE, SCHEMA_NAME_COL ))
    {
      SQLMethods sqlMethods = getSQLMethods( );
      ResultSet rs = sqlMethods.executeSQLQuery( selectStr );
      try
      {
        while ( rs != null && rs.next( ) )
        {
          String schema_name = rs.getString( SCHEMA_NAME_COL );
          if (schema_name != null)
          {
            if ( !deduplicate || dataObjectSchemas.contains( schema_name ) == false )
            {
              dataObjectSchemas.add( schema_name );
            }
          }
        }
      }
      catch ( SQLException sqle )
      {
        LOG.error( "gotSQLException: " + sqle );
      }
      finally
      {
        LOG.debug( "Close Database Connection - 1" );
        sqlMethods.closeDatabaseConnection( );
      }
    }
        
    return dataObjectSchemas;
  }
    
  public DataObjectSchema getDataObjectSchema( String schemaName, DataObject context )
  {
    return getDataObjectSchema( schemaName, true );
  }

  public DataObjectSchema getDataObjectSchema( String schemaName )
  {
    return getDataObjectSchema( schemaName, true );
  }
    
  protected DataObjectSchema getDataObjectSchema( String schemaName, boolean useCache )
  {
    LOG.debug( "getDataObjectSchema( " + schemaName + ", " + useCache + " )" );
        
    if (schemaName == null)
    {
      LOG.error( "getDataObjectSchema - called with NULL pointer!" );
      return null;
    }
        
    if (useCache && dataSchemas.get( schemaName ) != null)
    {
      DataObjectSchema cachedSchema = dataSchemas.get( schemaName );
      LOG.debug( "Returning cached DataObjectSchema: " + schemaName + " version " + Integer.toString( cachedSchema.getVersion( ) ));
      return cachedSchema;
    }
        
    // =============================================================================
    // look for data schema in table with name - find the latest version ...
    // select * from schema_table where Schema_Name = 'schemaName'
    // =============================================================================
    String selectStr = "select * from " + SCHEMA_TABLE + " where " + SCHEMA_NAME_COL + "='" + schemaName + "'";
    SQLMethods sqlMethods = getSQLMethods( );
    ResultSet rs = sqlMethods.executeSQLQuery( selectStr );
        
    int latestVersion = 0;
    boolean schemaExists = false;
    String schemaClass = null;
    String placeholderClass = null;
    String entityType = null;
        
    try
    {
      while (rs != null && rs.next() )
      {
        int version = rs.getInt( VERSION_COL );
        latestVersion = Math.max( version, latestVersion );
        if (version == latestVersion)
        {
          schemaClass = rs.getString( OBJECT_CLASS_COL );
          placeholderClass = rs.getString( PLHLDR_CLASS_COL );
          entityType  = rs.getString( ENTITY_TYPE_COL );
        }
                
        schemaExists = true;
      }
    }
    catch ( SQLException sqle )
    {
      LOG.error( "getDataObjectSchema( ) Got SQLException: " + sqle );
    }
    finally
    {
      LOG.debug( "Closing Database Connection - 2" );
      sqlMethods.closeDatabaseConnection( );
    }

    if (schemaExists)
    {
      // get all of the property rows
      // select * from property_table where Schema='schemaName_latestVersion'
      // Create a DataObjectSchema, set name to schemaName
      // add all the PropertyDescriptors
    
      DataObjectSchema dos = new DataObjectSchema( );
      dos.setName( schemaName );
      dos.setVersion( latestVersion );
      dos.setDataObjectType( schemaClass );
      if (entityType != null && entityType.trim().length() > 0)
      {
        dos.setEntityType( entityType );
      }
      if (placeholderClass != null && placeholderClass.trim().length() > 0)
      {
        dos.setChildPlaceholderClass( placeholderClass );
      }
        
      ArrayList<PropertyDescriptor> propDescriptors = getPropertyDescriptors( schemaName, latestVersion );
      if (propDescriptors != null)
      {
        dos.setPropertyDescriptors( propDescriptors );
      }
        
      LOG.debug( "getDataObjectSchema( " + schemaName + " ) DONE." );
            
      dos.setIsPublished( isPublished( schemaName, latestVersion ) );
            
      dataSchemas.put( dos.getName(), dos );
      return dos;
    }
        
    return null;
  }
    
  public int[] getSchemaVersions( String schemaName )
  {
    LOG.debug( "getSchemaVersions( " + schemaName + " )" );
        
    String selectStr = "select * from " + SCHEMA_TABLE + " where " + SCHEMA_NAME_COL + "='" + schemaName + "'";
    SQLMethods sqlMethods = getSQLMethods( );
    ResultSet rs = sqlMethods.executeSQLQuery( selectStr );
    ArrayList<Integer> versions = new ArrayList<Integer>( );
        
    try
    {
      while (rs != null && rs.next() )
      {
        int version = rs.getInt( VERSION_COL );
        versions.add( new Integer( version ) );
      }
    }
    catch ( SQLException sqle )
    {
      LOG.error( "getSchemaVersions( ) Got SQLException: " + sqle );
    }
    finally
    {
      LOG.debug( "Closing Database Connection - 3" );
      sqlMethods.closeDatabaseConnection( );
    }
        
    Collections.sort( versions );
    
    int[] outArray = new int[ versions.size( ) ];
    for (int i = 0; i < versions.size( ); i++)
    {
      outArray[i] = versions.get( i ).intValue( );
    }
        
    return outArray;
  }

    
  public void saveDataSchema( DataObjectSchema dataObjectSchema )
  {
    saveDataSchema( dataObjectSchema, false );
  }
    
  public void saveDataSchema( DataObjectSchema dataObjectSchema, boolean updateProperties )
  {
    if (dataObjectSchema == null)
    {
      LOG.error( "Cannot save - DataObjectSchema pointer is NULL!" );
      return;
    }
        
    LOG.debug( "saveDataSchema( " + dataObjectSchema.getName( ) + ", " + updateProperties + " )" );
        
    // if Data table schema exists for this object schema (it has the same
    // name and same set of property descriptors ...
    DataObjectSchema oldSchema = getDataObjectSchema( dataObjectSchema.getName( ), false );
    if (oldSchema != null) LOG.debug( "got oldSchema version: " + oldSchema.getVersion( ) );
    
    if (updateProperties || (oldSchema == null || oldSchema.equals( dataObjectSchema ) == false ))
    {
      LOG.debug( "Updating Schema" );
            
      int version = (oldSchema != null) ? oldSchema.getVersion() : 0;
      if (oldSchema != null && oldSchema.isPublished( ))
      {
        version = oldSchema.getVersion( ) + 1;
        LOG.debug( "Creating new version " + version );
      }
            
      if (!updateProperties)
      {
        LOG.debug( "saveSchemaRow( '" + dataObjectSchema.getName( ) + "'," + Integer.toString( version ) + " )" );
        String entityType = dataObjectSchema.getEntityType( );
        if (entityType == null) entityType = "";
        String placeholderClass = dataObjectSchema.getChildPlaceholderClass( );
        if (placeholderClass == null) placeholderClass = "";
        saveSchemaRow( dataObjectSchema.getName( ), dataObjectSchema.getDataObjectType( ), entityType, placeholderClass, version );
      }
            
      List<PropertyDescriptor> propDescriptors = dataObjectSchema.getPropertyDescriptors( );
      if (propDescriptors != null)
      {
        for (int i = 0; i < propDescriptors.size(); i++)
        {
          PropertyDescriptor propDescriptor = propDescriptors.get( i );
          savePropertyRow( dataObjectSchema.getName( ), version, propDescriptor );
        }
      }
            
      dataObjectSchema.setVersion( version );
      if (oldSchema != null && dataObjectSchema.getVersion() != oldSchema.getVersion() )
      {
        dataObjectSchema.setIsPublished( false );
      }
            
      dataSchemas.put( dataObjectSchema.getName( ), dataObjectSchema  );
    }
    else
    {
      LOG.debug( "No changes to Schema - not resaving." );
    }
  }
    
  public void publishDataObjectSchema( String schemaName )
  {
    // get the old schema - if not published, run an update and set the published flag == 'T'
    DataObjectSchema oldSchema = getDataObjectSchema( schemaName );
    LOG.debug( "publishDataObjectSchema( " + schemaName + " version = " + Integer.toString( oldSchema.getVersion( ) )+ " )" );
        
    if (oldSchema != null && oldSchema.isPublished() == false)
    {
      oldSchema.setIsPublished( true );
      SQLMethods sqlMethods = getSQLMethods( );
            
      String schemaID = StringTransform.replaceSubstring( schemaName, " ", "_" ) + "_" + Integer.toString( oldSchema.getVersion( ) );
      String sqlCommand = StringTransform.replaceSubstring( UPDATE_PUBLISHED_TEMPLATE, "{SCHEMA_ID}", schemaID );
      sqlMethods.executeSQLUpdate( sqlCommand );
            
      // =============================================================
      // Make sure that all PropertyDescriptors types are set
      //   any that have null types to "String"
      // =============================================================
      for ( PropertyDescriptor pd: oldSchema.getPropertyDescriptors( ) )
      {
        if (pd.getPropertyType( null ) == null)
        {
          LOG.debug( "Setting " + pd.getName( ) + " property descriptor to String." );
          pd.setPropertyType( "String" );
          savePropertyRow( oldSchema.getName( ), oldSchema.getVersion( ), pd );
        }
      }
    }
  }
    
  public void deleteDataObjectSchema( String schemaName, int version )
  {
    deleteDataObjectSchema( schemaName, Integer.toString( version ) );
  }
    
  public void deleteDataObjectSchema( String schemaName, String version )
  {
    LOG.debug( "deleteDataObjectSchema( " + schemaName + ", " + version );
        
    String schemaID = StringTransform.replaceSubstring(schemaName, " ", "_") + "_" + version;
    String sqlStatement = "delete from " + SCHEMA_TABLE + " where " + SCHEMA_ID_COL + "='" + StringTransform.replaceSubstring( schemaID, " ", "_" ) + "'";
    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( sqlStatement );
  }
    
  public void deletePropertyDescriptor( DataObjectSchema dobjSchema, int version, String propertyName )
  {
    LOG.debug( "deletePropertyDescriptor( " + dobjSchema.getName( ) + ", " + propertyName + " )" );
    String deleteStatement = "delete from " + PROPERTY_TABLE
     + " where " + SCHEMA_ID_COL + " = '" + StringTransform.replaceSubstring( dobjSchema.getName( ), " ", "_" ) + "_" + Integer.toString( version ) + "'"
     + " AND " + PROPERTY_NAME_COL + " = '" + propertyName + "'";
        
    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( deleteStatement );
  }
    
  private ArrayList<PropertyDescriptor> getPropertyDescriptors( String schemaName, int version )
  {
    LOG.debug( "getPropertyDescriptors( " + schemaName + "," + Integer.toString( version ) + " )" );
    ArrayList<PropertyDescriptor> propDescriptors = null;
        
    // --------------------------------------------------------------------
    // run the SQL query:
    // select * from property_table where schema=schemaName_version
    // --------------------------------------------------------------------
    SQLMethods sqlMethods = getSQLMethods( );
    try
    {
      String selectStr = "select * from " + PROPERTY_TABLE
                             + " where " + SCHEMA_ID_COL + " = '" + StringTransform.replaceSubstring( schemaName, " ", "_" ) + "_" + Integer.toString( version ) + "'";
      ResultSet results = sqlMethods.executeSQLQuery( selectStr );
      if (results != null)
      {
        propDescriptors = getPropertyDescriptors( results );
      }
    }
    catch ( Exception e )
    {
      LOG.debug( "getPropertyDescriptors get Exception " + e );
    }
    finally
    {
      LOG.debug( "Closing Database Connection - 4" );
      sqlMethods.closeDatabaseConnection( );
    }
        
    return propDescriptors;
  }
    
    
  private ArrayList<PropertyDescriptor> getPropertyDescriptors( ResultSet sqlResults )
  {
    ArrayList<PropertyDescriptor> propDescriptors = new ArrayList<PropertyDescriptor>( );
        
    try
    {
      while ( sqlResults != null && sqlResults.next( ) )
      {
        PropertyDescriptor pd = createPropertyDescriptor( sqlResults );
        propDescriptors.add( pd );
      }
    }
    catch (SQLException sqle )
    {
      LOG.error( "getPropertyDescriptors( ) Got SQLException: " + sqle );
    }
        
    return propDescriptors;
  }
    

  private PropertyDescriptor createPropertyDescriptor( ResultSet sqlResults )
                            throws SQLException
  {
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( sqlResults.getString( PROPERTY_NAME_COL ));
    pd.setPropertyType( sqlResults.getString( PROPERTY_CLASS_COL ) );
        
    LOG.debug( "createPropertyDescriptor: " + pd.getName( ) );
        
    String defaultFormat = sqlResults.getString( DEFAULT_FORMAT_COL );
    if (defaultFormat != null && defaultFormat.trim().length() > 0)
    {
      LOG.debug( "setPropertyFormat: '" + defaultFormat + "'" );
      pd.setPropertyFormat( defaultFormat );
    }

    // String propValues = getCLOBString( sqlResults.getClob( ALLOWED_VALUES_COL ) );
    String propValues = sqlResults.getString( ALLOWED_VALUES_COL );
    if (propValues != null)
    {
      LOG.debug( "Got Property Values = '" + propValues );
      String[] values = StringMethods.getStringArray( propValues,  DELIMITER );
      pd.setPropertyValues( values );
    }
        
    String defaultValue = sqlResults.getString( DEFAULT_VALUE_COL );
    if (defaultValue != null && defaultValue.trim().length() > 0)
    {
      LOG.debug( "setDefaultValue '" + defaultValue + "'" );
      pd.setDefaultValue( defaultValue );
    }
        
    pd.setRequired( sqlResults.getString( REQUIRED_COL ) );
    pd.setMultiValue( sqlResults.getString( MULTIVALUE_COL ));
        
    String displayName = sqlResults.getString( DISPLAY_NAME_COL );
    if (displayName != null && displayName.trim().length() > 0)
    {
      LOG.debug( "read Display Name = " + displayName );
      pd.setDisplayName( displayName );
    }
        
    String dataSchema = sqlResults.getString( PROPERTY_SCHEMA_COL );
    if ( dataSchema != null && dataSchema.trim().length() > 0)
    {
      LOG.debug( "Set DataObjectSchema: '" + dataSchema + "'" );
      pd.setDataObjectSchema( dataSchema );
    }
        
    String mappingStr = sqlResults.getString( MAPPINGS_COL );
    if (mappingStr != null && mappingStr.trim().length() > 0)
    {
      Map<String,String> mappingMap = StringMethods.unpackString( mappingStr, DELIMITER, "=" );
      pd.setPropertyMappings( mappingMap );
    }
        
    String isFunStr = sqlResults.getString( IS_FUNCTION_COL );
    pd.setIsFunction( (isFunStr != null && isFunStr.equals( "T" )));
    if (pd.isFunction( ))
    {
      LOG.debug( pd.getName( ) + " Is a Function Property!" );
    }
        
    String functionStr = sqlResults.getString( FUNCTION_DESC_COL );
    if (functionStr != null)
    {
      LOG.debug( "Setting function = '" + functionStr + "'" );
      pd.setFunctionDescription( functionStr );
    }
        
    String componentsStr = sqlResults.getString( COMPONENTS_COL );
    if (componentsStr != null)
    {
      LOG.debug( "Got Components value: " + componentsStr );
      // retrieve the property list
      // for each property, set that property on the PropertyDescriptor ...
      PropertyList componentList = new PropertyList( );
      componentList.setName( PropertyDescriptor.COMPONENTS );
      try
      {
        componentList.setValue( componentsStr, IProperty.XML_FORMAT );
        Iterator<IProperty> propIt = componentList.getProperties( );
        while (propIt != null && propIt.hasNext() )
        {
          IProperty prop = propIt.next( );
          LOG.debug( "Creating Component Property: " + prop );
          pd.createComponentProperty( prop );
        }
      }
      catch ( Exception e )
      {
        LOG.debug( "Got Exception: " + e );
      }
    }
    else
    {
      LOG.debug( "No Components for " + pd.getName( ) );
    }
        
    return pd;
  }
    
  private SQLMethods getSQLMethods( )
  {
    if (this.sqlMethods != null) return this.sqlMethods;
        
    LOG.debug( "Creating SQLMethods" );
    IDataSourceFactory dataSourceFac = getDataSourceFactory( );
        
    this.sqlMethods = new SQLMethods( dataSourceFac );
    return this.sqlMethods;
  }
    
  protected IDataSourceFactory getDataSourceFactory( )
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
    
  private boolean isPublished( String schemaName, int version )
  {
    String schema_id = StringTransform.replaceSubstring(schemaName, " ", "_") + "_" + Integer.toString( version );
    String select = "select " + PUBLISHED_COL + " from " + SCHEMA_TABLE + " where " + SCHEMA_ID_COL + " ='" + schema_id + "'";
    SQLMethods sqlMethods = getSQLMethods( );
    try
    {
      ResultSet rs = sqlMethods.executeSQLQuery( select );
      if (rs != null && rs.next( ))
      {
        String published = rs.getString( PUBLISHED_COL );
        return (published != null && published.equals( "T" ));
      }
    }
    catch ( Exception e )
    {
      LOG.debug( "isPublished got Exception " + e );
    }
    finally
    {
      LOG.debug( "Close Database Connection " + 5 );
      sqlMethods.closeDatabaseConnection( );
    }
        
    return false;
  }
    
  private void saveSchemaRow( String schemaName, String dataObjectType, String entityType, String placeholderClass, int version )
  {
    if (schemaName == null || schemaName.trim().length() == 0)
    {
      LOG.debug( "ERROR: Schema Name is not specified!" );
      return;
    }
    	
    String schema_id = StringTransform.replaceSubstring(schemaName, " ", "_") + "_" + Integer.toString( version );
    if (!tableExists( SCHEMA_TABLE, SCHEMA_NAME_COL ))
    {
      createSchemaTable( );
    }
        
    if (!schemaRowExists( schema_id ))
    {
      SQLMethods sqlMethods = getSQLMethods( );

      // create an insert of schema Name, version, schema_id, the date.
      String insertStatement = StringTransform.replaceSubstring(INSERT_SCHEMA_TEMPLATE, "{SCHEMA_ID}", schema_id );
      insertStatement = StringTransform.replaceSubstring( insertStatement, "{NAME}", schemaName );
      insertStatement = StringTransform.replaceSubstring( insertStatement, "{VERSION}", Integer.toString( version ) );
      insertStatement = StringTransform.replaceSubstring( insertStatement, "{CREATED}", getCurrentDate( ) );
            
      String dataObjectClass = (dataObjectType != null) ? dataObjectType : new DataObject( ).getType( );
      insertStatement = StringTransform.replaceSubstring( insertStatement, "{OBJECT_CLASS}", dataObjectClass );
      insertStatement = StringTransform.replaceSubstring( insertStatement, "{ENTITY_TYPE_COL}", entityType );
      insertStatement = StringTransform.replaceSubstring( insertStatement, "{PLHLDR_CLASS_COL}", placeholderClass );
      insertStatement = StringTransform.replaceSubstring( insertStatement, "{PUBLISHED}", "F" );

      sqlMethods.executeSQLUpdate( insertStatement );
    }
  }
        
  private boolean schemaRowExists( String schema_id )
  {
    String select = "select * from " + SCHEMA_TABLE + " where " + SCHEMA_ID_COL + " ='" + StringTransform.replaceSubstring(schema_id, " ", "_") + "'";
    SQLMethods sqlMethods = getSQLMethods( );
    try
    {
      ResultSet rs = sqlMethods.executeSQLQuery( select );
      if (rs != null && rs.next( ))
      {
        return true;
      }
    }
    catch ( Exception e )
    {
      LOG.debug( "schemaRowExists got Exception: " + e );
    }
    finally
    {
      LOG.debug( "Closing Database Connection - 6" );
      sqlMethods.closeDatabaseConnection( );
    }
        
    return false;
  }
    
  public void savePropertyRow( String schemaName, int version, PropertyDescriptor propDescriptor )
  {
    LOG.debug( "savePropertyRow( " + schemaName + ", "
                                   + Integer.toString( version ) + "," + propDescriptor.getName( ) + " ) " + propDescriptor );
        
    String schema_id = StringTransform.replaceSubstring(schemaName, " ", "_") + "_" + Integer.toString( version );
    boolean createdTable = false;
    if (!tableExists( PROPERTY_TABLE))
    {
      createPropertyTable( );
      createdTable = true;
    }
        
    // check if property with name exists - if so use UPDATE, else use INSERT
    String sqlStatement = null;
    
    if (!createdTable && propertyRowExists( schema_id, propDescriptor.getName( ) ))
    {
      LOG.debug( "Updating property row " + propDescriptor.getName( ) );
      sqlStatement = UPDATE_PROPERTY_TEMPLATE;
    }
    else
    {
      LOG.debug( "Creating property row " + propDescriptor.getName( ) );
      sqlStatement = INSERT_PROPERTY_TEMPLATE;
    }
        
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{SCHEMA_ID}", schema_id );
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{NAME}", propDescriptor.getName( ) );
        
    // Note this call will return the default type "String" if property type is not defined
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{CLASS}", propDescriptor.getPropertyType( "String" ) );
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{DISPLAY}", propDescriptor.getDisplayName( ) );
        
    String defaultFormat = propDescriptor.getPropertyFormat( );
    if (defaultFormat == null)
    {
      defaultFormat = "";
    }
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{FORMAT}", defaultFormat );
    
    PropertyList pList = propDescriptor.getPropertyValues( );
    String propValues = "";
    if (pList != null)
    {
      propValues = pList.getValue( PropertyList.DELIMITED_VALUES + DELIMITER );
      LOG.debug( "Got Property Values: " + propValues );
    }
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{VALUES}", propValues );
        
    String defaultValue = propDescriptor.getDefaultValue( );
    if (defaultValue == null) defaultValue = "";
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{DEFAULT}", defaultValue );
        
    String required = (propDescriptor.isRequired( ) ) ? "T" : "F";
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{REQUIRED}", required );
        
    String multiValue = (propDescriptor.isMultiValue( ) ) ? "T" : "F";
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{MULTIVALUE}", multiValue );
    
    String dataSchemaName = propDescriptor.getDataObjectSchema( );
    if (dataSchemaName == null) dataSchemaName = "";
    LOG.debug( "got dataSchemaName = '" + dataSchemaName + "'" );
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{SCHEMA}", dataSchemaName );
        
    Map<String,String> mappings = propDescriptor.getPropertyMappings( );
    String mappingStr = "";
    if (mappings != null)
    {
      mappingStr = StringMethods.getDelimitedString( mappings, DELIMITER, "=" );
    }
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{MAPPINGS}", mappingStr );
        
    String isFunc = (propDescriptor.isFunction( )) ? "T" : "F";
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{IS_FUNCTION}", isFunc );
        
    String function = (propDescriptor.isFunction() ) ? propDescriptor.getFunctionDescription( ) : "";
    sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{FUNCTION}", function );
    
    LOG.debug( "Looking for components" );
    List<PropertyDescriptor> components = propDescriptor.getComponents( false );
        
    LOG.debug( "got component list: " + components );
    if (components != null)
    {
      LOG.debug( "Have Components ..." );
      // for each component, if the PropertyDescriptor has a property set for that
      // component - add it to a PropertyList
      // store the PropertyList in the {COMPONENTS} column ...
      PropertyList componentList = null;
      for (PropertyDescriptor component : components )
      {
        IProperty componentProp = propDescriptor.getProperty( component.getName( ) );
        if (componentProp == null && component.getDefaultValue() != null)
        {
          LOG.debug( "Creating default component property: '" + component.getDefaultValue( ) + "'" );
          try
          {
            componentProp = (IProperty)Class.forName( component.getPropertyType() ).newInstance( );
            if (componentProp instanceof EnumerationProperty)
            {
              EnumerationProperty enumProp = (EnumerationProperty)componentProp;
              enumProp.setChoices( component.getPropertyValues( ) );
            }
            componentProp.setName( component.getName( ) );
            componentProp.setValue( component.getDefaultValue( ), component.getPropertyFormat( ) );
          }
          catch ( Exception e )
          {
            LOG.debug( "Could not create IProperty: '" + component.getPropertyType() + "' " + e );
          }
        }
                
        if (componentProp != null)
        {
          LOG.debug( "Adding component property " + component.getName( ) + " = " + componentProp.getValue( ) );
          if (componentList == null)
          {
            componentList = new PropertyList( );
            componentList.setName( "Components" );
          }
          componentList.addProperty( componentProp );
        }
      }
            
      String componentsVal = (componentList != null) ? componentList.getValue( IProperty.XML_FORMAT ) : "";
      LOG.debug( "Setting Components col = " + componentsVal );
      sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{COMPONENTS}", componentsVal );
    }
    else
    {
      LOG.debug( "No Components ..." );
      sqlStatement = StringTransform.replaceSubstring( sqlStatement, "{COMPONENTS}", "" );
    }
        
    LOG.debug( "savePropertyRow executing SQL: " + sqlStatement );
        
    LOG.debug( "Ready to execute ..." );
    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( sqlStatement );
  }
    
  private boolean propertyRowExists( String schema_id, String propName )
  {
    String select = "select * from " + PROPERTY_TABLE + " where "
                  + SCHEMA_ID_COL + "='" + StringTransform.replaceSubstring(schema_id, " ", "_") + "' and "
                  + PROPERTY_NAME_COL + "='" + propName + "'";
        
    SQLMethods sqlMethods = getSQLMethods( );
    try
    {
      ResultSet rs = sqlMethods.executeSQLQuery( select );
      if (rs != null && rs.next( ))
      {
        return true;
      }
    }
    catch ( Exception e )
    {
      LOG.debug( "propertyRowExists get Exception " + e );
    }
    finally
    {
      LOG.debug( "Closing Database Connection - 7" );
      sqlMethods.closeDatabaseConnection( );
    }
        
    return false;
  }
    
  private String getCurrentDate( )
  {
    Date now = new Date( );
    return now.toString( );
  }
    
  private boolean tableExists( String tableName )
  {
    return tableExists( tableName, "*" );
  }
    
  private boolean tableExists( String tableName, String columnName )
  {
    String checkSelect = "select " + columnName + " from " + tableName;
    SQLMethods sqlMethods = getSQLMethods( );
        
    try
    {
      ResultSet rs = sqlMethods.executeSQLQuery( checkSelect );
      if ( rs != null && rs.next() )
      {
        return true;
      }
    }
    catch ( Exception e )
    {
      LOG.debug( "tableExists got Exception: " + e );
    }
    finally
    {
      LOG.debug( "Closing Database Connection - 8" );
      sqlMethods.closeDatabaseConnection( );
    }
        
    return false;
  }
    
  private void createSchemaTable( )
  {
    String createStatement = CREATE_SCHEMA_TABLE;
    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( createStatement );
  }
    
  private void createPropertyTable( )
  {
    String createStatement = CREATE_PROPERTY_TABLE;
    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( createStatement );
  }
    
  private String getCLOBString( Clob clob )
  {
    if (clob == null)
    {
      LOG.debug( "Clob was NULL!" );
      return null;
    }
        
    try
    {
      InputStream is = clob.getAsciiStream();
      BufferedReader bufReader = new BufferedReader( new InputStreamReader( is ) );
      return bufReader.readLine( );
    }
    catch ( Exception e )
    {
      LOG.debug( "Could not read CLOB! " + e );
      e.printStackTrace( System.out );
    }
        
    return null;
  }
    
  // -----------------------------------------------------------
  // Data Object Schema Table
  //
  // Schema_Name
  // Schema_Version
  // ID   (Name_Version)
  // Created Date
  // -----------------------------------------------------------

  // -----------------------------------------------------------
  // Property Descriptor Table
  //
  // Schema (Name_Version)
  // Property Name
  // Property Class (class name)
  // Default format
  // Allowed Values (delimited list)
  // Default value
  // Is required
  // Is multi-value
  // -----------------------------------------------------------
    
  // ================================================================================================
  // SQL Templates
  // ================================================================================================
  private static final String CREATE_SCHEMA_TABLE = "Create Table " + SCHEMA_TABLE
                                                  + " (" + SCHEMA_ID_COL    + " varchar(32) PRIMARY KEY,"
                                                  + " "  + SCHEMA_NAME_COL  + " varchar(24),"
                                                  + " "  + VERSION_COL      + " int,"
                                                  + " "  + CREATED_COL      + " varchar(32),"
                                                  + " "  + OBJECT_CLASS_COL + " varchar(128),"
                                                  + " "  + PLHLDR_CLASS_COL + " varchar(128),"
                                                  + " "  + ENTITY_TYPE_COL  + " varchar(32),"
                                                  + " "  + PUBLISHED_COL    + " varchar(1)"
                                                  + " )";
    
  private static final String CREATE_PROPERTY_TABLE = "Create Table " + PROPERTY_TABLE
                                                    + " (" + SCHEMA_ID_COL      + " varchar(32),"
                                                    + " " + PROPERTY_NAME_COL   + " varchar(32),"
                                                    + " " + PROPERTY_CLASS_COL  + " varchar(64),"
                                                    + " " + DISPLAY_NAME_COL    + " varchar(64),"
                                                    + " " + PROPERTY_SCHEMA_COL + " varchar(16),"
                                                    + " " + DEFAULT_FORMAT_COL  + " varchar(16),"
                                                    + " " + ALLOWED_VALUES_COL  + " varchar(256),"
                                                    + " " + DEFAULT_VALUE_COL   + " varchar(256),"
                                                    + " " + REQUIRED_COL        + " varchar(1),"
                                                    + " " + MULTIVALUE_COL      + " varchar(1),"
                                                    + " " + MAPPINGS_COL        + " varchar(128),"
                                                    + " " + IS_FUNCTION_COL     + " varchar(1),"
                                                    + " " + FUNCTION_DESC_COL   + " varchar(256),"
                                                    + " " + COMPONENTS_COL      + " varchar(1024)"
                                                    + " )";
    
  private static final String INSERT_SCHEMA_TEMPLATE = "INSERT INTO " + SCHEMA_TABLE + " VALUES( "
        + "'{SCHEMA_ID}','{NAME}',{VERSION},'{CREATED}','{OBJECT_CLASS}','{PLHLDR_CLASS_COL}','{ENTITY_TYPE_COL}','{PUBLISHED}')";
    
  private static final String UPDATE_PUBLISHED_TEMPLATE = "UPDATE " + SCHEMA_TABLE + " set " + PUBLISHED_COL + "='T' "
                                                        + " where " + SCHEMA_ID_COL + "='{SCHEMA_ID}'";
    
  private static final String INSERT_PROPERTY_TEMPLATE = "INSERT INTO " + PROPERTY_TABLE + " VALUES( "
        + "'{SCHEMA_ID}','{NAME}','{CLASS}','{DISPLAY}','{SCHEMA}','{FORMAT}','{VALUES}','{DEFAULT}',"
        + "'{REQUIRED}','{MULTIVALUE}','{MAPPINGS}','{IS_FUNCTION}','{FUNCTION}','{COMPONENTS}')";
    
  private static final String UPDATE_PROPERTY_TEMPLATE = "UPDATE " + PROPERTY_TABLE + " set " + PROPERTY_CLASS_COL + "='{CLASS}', "
                                                       + DISPLAY_NAME_COL    + "='{DISPLAY}', "
                                                       + DEFAULT_FORMAT_COL  + "='{FORMAT}', "
                                                       + ALLOWED_VALUES_COL  + "='{VALUES}', "
                                                       + DEFAULT_VALUE_COL   + "='{DEFAULT}', "
                                                       + PROPERTY_SCHEMA_COL + "='{SCHEMA}', "
                                                       + REQUIRED_COL        + "='{REQUIRED}', "
                                                       + MULTIVALUE_COL      + "='{MULTIVALUE}',"
                                                       + MAPPINGS_COL        + "='{MAPPINGS}',"
                                                       + IS_FUNCTION_COL     + "='{IS_FUNCTION}',"
                                                       + FUNCTION_DESC_COL   + "='{FUNCTION}',"
                                                       + COMPONENTS_COL      + "='{COMPONENTS}' where "
                                                       + SCHEMA_ID_COL       + "='{SCHEMA_ID}' and "
                                                       + PROPERTY_NAME_COL   + "='{NAME}'";
}
