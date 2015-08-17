package com.modinfodesigns.property.persistence;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.IObjectFactory;
import com.modinfodesigns.app.persistence.database.IDataSourceFactory;
import com.modinfodesigns.app.persistence.database.SQLMethods;

import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectTemplate;
import com.modinfodesigns.property.EnumerationProperty;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.IntrinsicPropertyDelegate;
import com.modinfodesigns.property.MappedProperty;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.HiddenProperty;
import com.modinfodesigns.property.transform.xml.XMLDataObjectParser;

import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchemaManager;

import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.transform.PropertyTemplateTransform;

import com.modinfodesigns.search.FinderException;
import com.modinfodesigns.search.IResultList;
import com.modinfodesigns.utils.DOMMethods;
import com.modinfodesigns.utils.StringMethods;
import com.modinfodesigns.utils.UniqueIDGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Clob;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists Data Objects that can be described by a set of configured PropertyDescriptors.
 * Uses a DataObjectTemplate internally to render a DataObject into a SQL create, update or delete
 * command. 
 * 
 * Read functions take a list of IProperty with the criterion to be used to find the DataObject.
 * These properties must be in the list of specified PropertyDescriptors for the configured
 * table schema. of the SQLTemplatePersistenceMananger instance.
 * 
 * @author Ted Sullivan
 */

public class SQLTemplatePersistenceManager implements IObjectFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SQLTemplatePersistenceManager.class );

  private static final String PARENT_ID = "_PARENT_ID_";
  private static final String CHILDREN  = "_CHILDREN_";
    
  public static final String MASTER_TABLE       = "master_object_table";
  public static final String OBJECT_NAME_COL    = DataObject.OBJECT_NAME_PARAM;
  public static final String OBJECT_ID_COL      = DataObject.OBJECT_ID_PARAM;
  public static final String OBJECT_CLASS_COL   = "object_class";
  public static final String ENTITY_TYPE_COL    = DataObject.ENTITY_TYPE_PARAM;
  public static final String SCHEMA_NAME_COL    = DataObjectSchemaManager.SCHEMA_NAME_COL;
  public static final String SCHEMA_VERSION_COL = "schema_version";
  public static final String CREATED_COL        = "created_date";
  public static final String OWNER_COL          = "owner";
  public static final String ACL_COL            = "permissions";
    
  private SimpleDateFormat createdFormatter = new SimpleDateFormat( "yyyy-MM-dd hh:mm:ss" );
    
  public static final String PROXY_FIELD = "object_proxy";
    
  private HashMap<String,SQLTemplateSet> dataObjTemplateMap = new HashMap<String,SQLTemplateSet>( );
    
  private DataObjectSchemaManager dataObjectSchemaManager;

  private String dataSourceFactoryName;
  private IDataSourceFactory dataSourceFactory;
    
  private String idField       = DataObject.OBJECT_ID_PARAM;
  private String nameField     = DataObject.OBJECT_NAME_PARAM;
  private String typeField     = "object_type";
  private String rootField     = "is_root";
  private String proxyField    = PROXY_FIELD;
  private String parentIDField = "parent_id";
  private String adhocField    = "adhoc_props";
    
  private SQLMethods sqlMethods;

  private String varcharLength = "256";
    
  public SQLTemplatePersistenceManager( ) { }
    
  public SQLTemplatePersistenceManager( IDataSourceFactory dataSourceFactory )
  {
    setDataSourceFactory( dataSourceFactory );
  }
    
  public SQLTemplatePersistenceManager( String dataSourceFactoryName )
  {
    setDataSourceFactoryName( dataSourceFactoryName );
  }
    
  public void setDataSourceFactoryName( String dataSourceFactoryName )
  {
    this.dataSourceFactoryName = dataSourceFactoryName;
  }
    
  public void setDataSourceFactory( IDataSourceFactory dataSourceFac )
  {
    this.dataSourceFactory = dataSourceFac;
  }
    
  public void setDataObjectSchemaManager( DataObjectSchemaManager dosm )
  {
    LOG.debug( "setDataObjectSchemaManager: " + dosm );
    this.dataObjectSchemaManager = dosm;
  }
    
  public void setNameField( String nameField )
  {
    this.nameField = nameField;
  }
    
  public void setIDField( String idField )
  {
    LOG.debug( "setIDField: " + idField );
    this.idField = idField;
  }
    
  public void save( DataObject dObj )
  {
    save( dObj, true );
  }
    
  public void save( DataObject dObj, boolean isRoot )
  {
    if (dObj == null)
    {
      return;
    }
        
    LOG.debug( "save( DataObject ): " + dObj.getName( ) + " to Schema " + dObj.getDataObjectSchema( ) );
        
    if (dObj.getDataObjectSchema( ) == null)
    {
      LOG.error( "Cannot save NULL DataObjectSchema!" );
      return;
    }
        
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    DataObjectSchema dobjSchema = getDataObjectSchema( dObj.getDataObjectSchema( ) );
        
    save( dObj, dobjSchema, dosm, isRoot );
  }
    
  public void save( DataObject dObj, DataObjectSchema dobjSchema, DataObjectSchemaManager dosm )
  {
    save( dObj, dobjSchema, dosm, true );
  }
    
  public void save( DataObject dObj, DataObjectSchema dobjSchema, DataObjectSchemaManager dosm, boolean isRoot )
  {
    LOG.debug( "Save " + dObj.getName( ) + " with DataObjectSchema: "
                       + dobjSchema.getName( ) + "_" + dobjSchema.getVersion( ) + " isRoot: " + isRoot);
    LOG.debug( "Saving: " + dObj.getValue( ) );
        
    dObj.setIsRootObject( isRoot );
    setID( dObj, dobjSchema );
        
    if (!dobjSchema.isPublished( ) )
    {
      initSQLTemplates( dObj, dobjSchema );
    }
        
    if ( !dObj.isProxied( ) )
    {
      LOG.debug( "Object is Not Proxied." );
        
      updateDataListChildLinks( dObj, dobjSchema );
    
      // need to stub out any properties that are needed for
      // persistence - e.g. any Numerical properties, if not
      // present must be set to some value (0).
      setNumericDefaults( dObj, dobjSchema );
        
      // Saves child property objects
      saveChildPropertyObjects( dObj, dobjSchema );
    }
    else
    {
      LOG.debug( "Object is Proxied." );
      DataObject proxy = dObj.getProxyObject( );
      if (proxy instanceof IDataObjectPlaceholder)
      {
        IDataObjectPlaceholder dop = (IDataObjectPlaceholder)proxy;
        dop.setSQLTemplatePersistenceManager( this );
      }
    }
        
    // if dObj has no ID, create one and use CREATE template
    // else use UPDATE template
    if (dataObjectExists( dObj, dobjSchema.getName( ), dobjSchema.getVersion( ) ))
    {
      LOG.debug( "Updating Row for DataObject: " + dObj.getName( ) );
      updateRow( dObj, dobjSchema );
    }
    else
    {
      LOG.debug( "Creating Row for DataObject: " + dObj.getName( ) + " isRoot = " + isRoot );
      dObj.setIsRootObject( isRoot );
      createRow( dObj, dobjSchema );
      if (isRoot) insertMasterTableRow( dObj, dobjSchema );
    }
        
    updateAdhocProperties( dObj, dobjSchema );
        
    if (dObj instanceof DataList)
    {
      LOG.debug( "Saving DataList children of " + dObj.getName( ) );
      DataList dList = (DataList)dObj;
      Iterator<DataObject> childIt = dList.getData( );
      while ( childIt != null && childIt.hasNext() )
      {
        DataObject childOb = childIt.next( );
        String childSchema = childOb.getDataObjectSchema( );
        if (childSchema == null)
        {
          childOb.setDataObjectSchema( dobjSchema.getName( ) );
        }
                
        LOG.debug( "Saving child object in list: " + childOb.getName( ) + " with schema " + dobjSchema.getName( ) );
        save( childOb, dobjSchema, dosm, false );
      }
    }
        
    dosm.publishDataObjectSchema( dObj.getDataObjectSchema( ) );
  }
    
  // Saves any child objects (not those included in a DataList)
  private void saveChildPropertyObjects( DataObject dObj, DataObjectSchema dobjSchema )
  {
    LOG.debug( "saveChildPropertyObjects: " + dObj.getName( ) );
        
    if (dObj != null && dobjSchema != null)
    {
      List<PropertyDescriptor> propDescList = dobjSchema.getPropertyDescriptors( );
      for (int i = 0; i < propDescList.size( ); i++ )
      {
        PropertyDescriptor pd = propDescList.get( i );
        if ( pd.isDataObject( ) )
        {
          LOG.debug( "Looking for " + dObj.getName( ) + " child: " + pd.getName( ) );
                    
          DataObject chObj = (DataObject)dObj.getProperty( pd.getName( ) );
          if (chObj != null)
          {
            if (chObj.getName( ) == null) chObj.setName( pd.getName( ) );  // ???
            LOG.debug( "Saving Child Object: " + chObj.getName( ) );
                        
            if (chObj instanceof DataList)
            {
              LOG.debug( "Child is a DataList - saving the list objects as children." );
              StringBuilder keybuilder = new StringBuilder( );
              DataList childList = (DataList)chObj;
              for (Iterator<DataObject> chObIt = childList.getData(); chObIt.hasNext(); )
              {
                DataObject listOb = chObIt.next( );
                DataObjectSchema childSchema = getDataObjectSchema( listOb.getDataObjectSchema(), dObj );
                                
                LOG.debug( "Saving child object: " + listOb.getName( ) );
                save( listOb, childSchema, getDataObjectSchemaManager( ), false );
                  
                keybuilder.append( listOb.getValue( DataObject.NAME_ID_SCHEMA ));
                if (chObIt.hasNext( ) ) keybuilder.append( ";" );
              }
                            
              // need to save the keys:
              String childrenProp = CHILDREN + pd.getName( );
              LOG.debug( "Setting " + dObj.getName( ) + " '" + childrenProp + "' property to " + keybuilder.toString( ) );
              dObj.setProperty( new HiddenProperty( childrenProp, keybuilder.toString( ) ));
            }
            else
            {
              DataObjectSchema childSchema = getDataObjectSchema( chObj.getDataObjectSchema(), dObj );
              save( chObj, childSchema, getDataObjectSchemaManager( ), false );
            }
          }
        }
      }
    }
  }

  public void delete( DataObject dObj )
  {
    DataObjectSchema dos = getDataObjectSchema( dObj.getDataObjectSchema( ) );
    delete( dObj, dos.getVersion( ) );
  }
    
  public void delete( String objectID, DataObjectSchema dos )
  {
    deleteRow( objectID, dos );
  }
    
  private void delete( DataObject dObj, int version )
  {
    LOG.debug( "delete( " + dObj.getName( ) + "," + dObj.getID( ) + "," + Integer.toString( version ) + " )" );
        
    if (dataObjectExists( dObj, dObj.getDataObjectSchema( ), version ))
    {
      deleteRow( dObj, version );
            
      if (dObj instanceof DataList)
      {
        DataList dList = (DataList)dObj;
        Iterator<DataObject> childIt = dList.getData( );
        while ( childIt != null && childIt.hasNext() )
        {
          DataObject childOb = childIt.next( );
                    
          // get the right version from the childOb ID
          String childID = childOb.getID( );
          if (childID != null)
          {
            String chVersion = (childID.indexOf( "_" ) > 0) ? new String( childID.substring( childID.lastIndexOf( "_" ) + 1 ) ) : "0";
            delete( childOb, Integer.parseInt( chVersion ) );
          }
        }
      }
            
      if (dObj.isRootObject() )
      {
        LOG.debug( "Deleted Root Object: delete row in master_object_table ..." );
        deleteMasterTableRow( dObj );
      }
    }
  }
    
    
  public DataObject read( String name, String dataObjectSchemaName )
  {
    LOG.debug( "read( " + name + "," + dataObjectSchemaName + " )" );
        
    DataObjectSchema dataObjectSchema = getDataObjectSchema( dataObjectSchemaName );
    DataObject dobj = read( name, (String)null, dataObjectSchema );
    if (dobj == null)
    {
      LOG.debug( "Read FAILED - returning null!" );
    }

    LOG.debug( "read returning: " + dobj );
    return dobj;
  }
    

  public DataObject read( String name, String ID, String dataObjectSchemaName )
  {
    LOG.debug( "read( " + name + " " + ID + " " + dataObjectSchemaName );
        
    if (name == null || ID == null || dataObjectSchemaName == null)
    {
      LOG.error( "read called with Null Pointer: " + name + "," + ID + ", " + dataObjectSchemaName );
      return null;
    }
        
    LOG.debug( "Read( " + name + ", " + ID + ", " + dataObjectSchemaName + " )" );
        
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    if (dosm == null)
    {
      LOG.debug( "Could not get DataObjectSchemaManager " );
      return null;
    }
        
    DataObjectSchema dobjSchema = dosm.getDataObjectSchema( dataObjectSchemaName );
    
    return read( name, ID, dobjSchema );
  }
    
  // Here is where the version needs to be intercepted
  // If this is an old object, it may not be in the latest schema table
  public DataObject read( String name, String ID, DataObjectSchema dobjSchema )
  {
    LOG.debug( "read( " + name + " " + ID + " " + dobjSchema );
    if (dobjSchema == null)
    {
      LOG.error( "Cannot read object: dobjSchema is NULL!" );
      return null;
    }
        
    String version = (ID != null && ID.indexOf( "_" ) > 0)
                   ? new String( ID.substring( ID.lastIndexOf( "_" ) + 1 ) )
                   : Integer.toString( dobjSchema.getVersion() );
                       
    DataObject dobj = doRead( name, ID, dobjSchema, ((version != null) ? version : "0" )  );
    if (dobj != null)
    {
      LOG.debug( "read returning: " + dobj );
      return dobj;
    }
        
    // Check if the object is in an older version of this schema
    int verInt = Integer.parseInt( version );
    if (verInt > 0)
    {
      for (int olderVer = verInt-1; olderVer >= 0; olderVer--)
      {
        LOG.debug( "Checking older version: " + olderVer );
        dobj = doRead( name, ID, dobjSchema, Integer.toString( olderVer ));
        if (dobj != null)
        {
          LOG.debug( "Found older version: " + olderVer );
          return dobj;
        }
      }
    }
        
    return null;
  }
    
    
  private DataObject doRead( String name, String ID, DataObjectSchema dobjSchema, String version )
  {
    LOG.debug( "doRead( " + name + " " + ID + " " + dobjSchema.getName( ) + " " + version );
    String databaseTable = getDatabaseTable( dobjSchema.getName( ), version );
    if (databaseTable != null)
    {
      StringBuilder strbuilder = new StringBuilder( );
      if (name != null && name.trim().length() > 0 && ID != null && ID.trim().length() > 0)
      {
        strbuilder.append( "select * from " ).append( databaseTable )
                  .append( " where " ).append( nameField ).append( "='" ).append( name )
                  .append( "' AND " ).append( idField ).append( "='" ).append( ID ).append( "'" );
      }
      else if (ID != null && ID.trim().length() > 0)
      {
        strbuilder.append( "select * from " ).append( databaseTable )
                  .append( " where " ).append( idField ).append( "='" )
                  .append( ID ).append( "'" );
      }
      else if (name != null && name.trim().length() > 0)
      {
        strbuilder.append( "select * from " ).append( databaseTable )
                  .append( " where " ).append( nameField ).append( "='" )
                  .append( name ).append( "'" );
      }
      else
      {
        LOG.debug( "BOTH Name and ID are NULL or empty! cannot retrieve!" );
        return null;
      }
             
      try
      {
        ResultSet rs = executeQuery( strbuilder.toString( ) );
        if (rs != null && rs.next() )
        {
          return createDataObject( rs, dobjSchema );
        }
      }
      catch (SQLException sqle )
      {
        LOG.error( "read got SQLException " + sqle );
      }
      finally
      {
        sqlMethods.closeDatabaseConnection( );
      }
    }
      
    LOG.debug( "Could not read " + name + " " + ID + " from " + dobjSchema.getName( ) );
         
    return null;
  }
    
    
  public List<String> getDataObjectNames( String dataObjectSchemaName )
  {
    LOG.debug( "getDataObjectNames( " + dataObjectSchemaName + " )" );
        
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    if (dosm == null)
    {
      LOG.debug( "getDataObjectSchemaManager returned NULL!" );
      return null;
    }
        
    DataObjectSchema dobjSchema = dosm.getDataObjectSchema( dataObjectSchemaName );
    if (dobjSchema == null)
    {
      LOG.debug( "Did not get DataObjectSchema: " + dataObjectSchemaName );
      return null;
    }
        
    int[] versions = dosm.getSchemaVersions( dataObjectSchemaName );
    ArrayList<String> nameList = new ArrayList<String>( );
    SQLMethods sqlMethods = getSQLMethods( );
        
    for (int i = 0; i < versions.length; i++)
    {
      String databaseTable = getDatabaseTable( dobjSchema.getName( ), Integer.toString( versions[i] ) );
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "select " + nameField + " from " ).append( databaseTable );
      ResultSet rs = sqlMethods.executeSQLQuery( strbuilder.toString( ) );
            
      try
      {
        while (rs != null && rs.next() )
        {
          String objName = rs.getString( nameField );
          LOG.debug( "Adding: " + objName );
          nameList.add( objName );
        }
      }
      catch ( SQLException sqle )
      {
        LOG.error( "Got SQLException: " + sqle );
      }
            
      sqlMethods.closeDatabaseConnection( );
    }
        
    return nameList;
  }
    
    
  private DataObject createDataObject( ResultSet rs, DataObjectSchema dobjSchema )
  {
    LOG.debug( "createDataObject: " + dobjSchema.getName( ) );
        
    try
    {
      DataObject dobj = null;
        
      if (dobjSchema.getDataObjectType() != null)
      {
        try
        {
          dobj = (DataObject)Class.forName( dobjSchema.getDataObjectType() ).newInstance( );
        }
        catch ( Exception e )
        {
          LOG.error( "Could not create Data Object class '" + dobjSchema.getDataObjectType() + "'" );
          dobj = new DataObject( );
        }
      }
      else
      {
        String type = getDOClass( rs.getString( typeField  ));

        try
        {
          dobj = (DataObject)Class.forName( type ).newInstance( );
        }
        catch ( Exception e )
        {
          LOG.error( "Could not create Data Object class '" + type + "'" );
          dobj = new DataObject( );
        }
      }
            
      // set name and id based on nameField and idField
      String name = rs.getString( nameField );
      dobj.setName( name );
      String id = rs.getString( idField );
      dobj.setID( id );
      dobj.setDataObjectSchema( dobjSchema.getName( ) );
            
      String proxyKey = rs.getString( proxyField );
      if (proxyKey != null && proxyKey.trim().length() > 0)
      {
        // Create a Proxy Object placeholder
        String[] components = StringMethods.getStringArray( proxyKey, "|" );
        String childPlaceholderClass = dobjSchema.getChildPlaceholderClass( );
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
              dopl.setSQLTemplatePersistenceManager( this );
              dobj.setProxyObject( (DataObject)dopl );
              ((DataObject) dopl).setProperty( new StringProperty( "proxy", "true" ));
              return dobj;
            }
          }
          catch ( Exception e )
          {
            LOG.error( "Could not create Proxy Object Placeholder class '" + childPlaceholderClass + "'" );
          }
        }

        if (dopl == null)
        {
          dopl = new DataObjectPlaceholder( components[0], components[1], components[2], this );
          dobj.setProxyObject( (DataObject)dopl );
          ((DataObject) dopl).setProperty( new StringProperty( "proxy", "true" ));
          return dobj;
        }
      }

      HashMap<String,String> dataObjectLinks = new HashMap<String,String>( );
      Iterator<String> propDescIt = dobjSchema.getPropertyNames( );
            
      while ( propDescIt != null && propDescIt.hasNext( ))
      {
        PropertyDescriptor pd = dobjSchema.getPropertyDescriptor( propDescIt.next() );
        if ( pd.isDataObject( ) )
        {
          if (pd.isMultiValue( ) )
          {
            LOG.debug( "Getting Children for " + pd.getName( ) );
                        
            String childrenProp = CHILDREN + pd.getName( );
            String childrenList = getAdhocProp( childrenProp, rs );
            LOG.debug( "Creating DataList from " + childrenList );
                        
            if ( childrenList != null )
            {
              dataObjectLinks.put( childrenProp, childrenList );
            }
          }
          else
          {
            String objName = rs.getString( pd.getName( ) );
            LOG.debug( "Adding dataObjectLink: " + pd.getName( ) );
            dataObjectLinks.put( pd.getName( ), objName );
          }
        }
        else
        {
          IProperty prop = getProperty( rs, pd, dobjSchema );
          if (prop != null )
          {
            LOG.debug( "Setting property: " + prop.getName( ) + " = " + prop.getValue( ) );
            dobj.setProperty( prop );
          }
          //else
          //{
          //    dobj.setProperty( pd.createProperty(  ) );
          //}
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
            
      if ( dataObjectLinks.size( ) > 0 )
      {
        for (Iterator<String> propNameIt = dataObjectLinks.keySet().iterator(); propNameIt.hasNext(); )
        {
          String propName = propNameIt.next( );
          String objName = dataObjectLinks.get( propName );
          if (propName.startsWith( CHILDREN ))
          {
            DataList dList = new DataList( );
            String pdName = propName.substring( CHILDREN.length() );
            dList.setName( pdName );
            dobj.setProperty( dList );
                        
            String[] childIDs = StringMethods.getStringArray( objName, ";" );
            for (int i = 0, isz = childIDs.length; i < isz; i++)
            {
              LOG.debug( "Getting child for " + childIDs[i] );
                            
              String[] components = StringMethods.getStringArray( childIDs[i], "|" );
              if (components != null && components.length == 3)
              {
                String childName = components[0];
                String childID   = components[1];
                String childSchemaName = components[2];
                DataObjectSchema childSchema = getDataObjectSchema( childSchemaName, dobj );
                  
                LOG.debug( "Reading ChildObject: " + childName + ", " + childID + ", " + childSchemaName );
                DataObject chOb = read( childName, childID, childSchema );
                if (chOb != null)
                {
                  dList.addDataObject( chOb );
                }
              }
            }
          }
          else
          {
            DataObject childOb = readChildPropertyObject( objName, dobjSchema.getPropertyDescriptor( propName )  );
            if (childOb != null)
            {
              dobj.setProperty( childOb );
            }
          }
        }
      }
            
      LOG.debug( "Checking if " + dobj.getName( ) + " is a DataList." );
      if ( dobj instanceof DataList )
      {
        LOG.debug( "Getting the children ..." );
        DataList dList = (DataList)dobj;
                
        if ( dobjSchema.isLazilyEvaluated( ) )
        {
          // get the children ...
          String children = null;
            
          LOG.debug( "Getting the children ..." );
          Clob childrenClb = rs.getClob( "children" );
          if (childrenClb != null)
          {
            children = getCLOBString( childrenClb );
          }
          else
          {
            LOG.debug( "children CLOB is null - getting adhoc property ..." );
            children = getAdhocProp( CHILDREN, rs );
          }
                    
          LOG.debug( "Got Children: '" + children + "'" );
          if (children != null && children.length() > 0)
          {
            // break up on ";"
            String[] childKeys = StringMethods.getStringArray( children, PropertyTemplateTransform.childKeyDelimiter );
            if (childKeys != null)
            {
              for (int i = 0; i < childKeys.length; i++)
              {
                String[] components = StringMethods.getStringArray( childKeys[i],  "|" );
                String chName = components[0];
                String ID = components[1];
                String dataSchema = (components.length == 3) ? components[2] : null;

                DataObject chObj = null;
                  
                String placeholderClass = dobjSchema.getChildPlaceholderClass( );
                LOG.debug( "Creating DataObjectPlaceholder for " + chName + " '" + placeholderClass + "'" );
                if (placeholderClass != null)
                {
                  try
                  {
                    IDataObjectPlaceholder doph = (IDataObjectPlaceholder)Class.forName( placeholderClass ).newInstance( );
                    doph.setName( chName );
                    doph.setID( ID );
                    doph.setSQLTemplatePersistenceManager( this );
                    doph.setSchemaName( dataSchema );
                      
                    chObj = (DataObject)doph;
                  }
                  catch ( Exception e )
                  {
                    LOG.error( "Could not create DataObjectPlaceholder: " + placeholderClass + " Exception was " + e );
                  }
                }
                else
                {
                  LOG.debug( "Creating DataObjectPlaceholder for " + chName );
                  chObj = new DataObjectPlaceholder( chName, ID, dataSchema, this );
                }
                            
                if (chObj != null)
                {
                  dList.addDataObject( chObj );
                  LOG.debug( "Added DataObjectPlaceholder" );
                }
              }
            }
          }
        }
        else
        {
          LOG.debug( "Not lazy evaluation - looking for child schema ..." );
          // find all from child schema table where parent_id = this id
          String childSchema = dobjSchema.getChildObjectSchema( );
          if (childSchema == null)
          {
            childSchema = dobjSchema.getName( );
          }
                    
          List<DataObject> childObjs = readChildObjects( id, childSchema );
          if (childObjs != null)
          {
            for (int i = 0; i < childObjs.size(); i++)
            {
              dList.addDataObject( childObjs.get( i ) );
            }
          }
        }
      }
      else
      {
        LOG.debug( "Not a DataList, moving on ..." );
      }
            
      LOG.debug( "Checking for adhoc properties" );
      // get the adhoc property and
      Clob adhocProps = null;
      try
      {
        adhocProps = rs.getClob( adhocField );
      }
      catch (SQLException sqle )
      {
        LOG.debug( "cannot get adhocProperties!" );
      }
            
      if (adhocProps != null)
      {
        LOG.debug( "Adding adhoc properties ..." );
                
        String propStr = getCLOBString( adhocProps );
                
        LOG.debug( "Got propStr = " + propStr );
                
        DataObject adhocPropHolder = new XMLDataObjectParser( ).createDataObject( propStr );
        if (adhocPropHolder != null)
        {
          Iterator<IProperty> propIt = adhocPropHolder.getProperties( );
          while( propIt != null && propIt.hasNext() )
          {
            IProperty prop = propIt.next( );
            dobj.setProperty( prop );
          }
        }
      }
        
      LOG.debug( "createDataObject returning " + dobj + " " + dobj.getName( ) );
      return dobj;
    }
    catch ( SQLException sqle )
    {
      LOG.error( "SQLException happened HERE! " + sqle );
    }
        
    LOG.debug( "Could not create DataObject! returning null" );
    return null;
  }
    
  private IProperty getProperty( ResultSet rs, PropertyDescriptor pd, DataObjectSchema dobjSchema )
  {
    if (rs == null || pd == null) return null;
        
    LOG.debug( "getProperty from ResultSet: " + pd.getName( ) );

    if (pd.isFunction( ) )
    {
      LOG.debug( "Property is a Function" );
      try
      {
        IFunctionProperty funProp = (IFunctionProperty)Class.forName( pd.getPropertyType() ).newInstance( );
        funProp.setFunction( pd.getFunctionDescription( ) );
        funProp.setName( pd.getName( ) );
        return funProp;
      }
      catch ( Exception e )
      {
        LOG.debug( "Could not create function property: " + pd.getPropertyType( ) );
      }
      return null;
    }
    else
    {
      LOG.debug( "Getting property value " );
                
      // Depending on pd type, get the appropriate row type and return the
      // appropriate Property Type, fix the name in the PropertyDescriptor for SQL querying ...
      // but use the PropertyDescriptor name to set the IProperty name

      String value = null;
      try
      {
        if (pd.getDataType().equals( "Integer" ))
        {
          value = Integer.toString( rs.getInt( fixName( pd.getName( ) )) );
        }
        else if (pd.getDataType().equals( "Double" ))
        {
          value = Double.toString( rs.getDouble( fixName( pd.getName( ) )));
        }
        else
        {
          value = rs.getString( fixName( pd.getName( ) ));
        }
            
        LOG.debug( "Got Database value = '" + value + "'" );
      }
      catch ( SQLException sqle )
      {
        LOG.debug( "getProperty got SQLException " + sqle );
      }
      catch (Exception e )
      {
        LOG.debug( "getProperty got Exception " + e );
        e.printStackTrace( );
      }
                
      if (value == null) value = "";
        
      IProperty prop = null;
      // PropertyList persists as XML
      if ( value.trim().startsWith( "<PropertyList>" ))
      {
        PropertyList pList = new PropertyList( );
        try
        {
          pList.setValue( value.trim(), IProperty.XML_FORMAT );
        }
        catch ( Exception e )
        {
            
        }
                
        prop = pList;
      }
                
      if (pd.getPropertyFormat() != null)
      {
        LOG.debug( "using format " + pd.getPropertyFormat( ) );
      }
                
      // Check for EnumerationProperty here ...
      if (pd.getPropertyValues( ) != null &&
        ((prop != null && prop instanceof PropertyList)
       || pd.getPropertyType().endsWith( "EnumerationProperty" )))
      {
        LOG.debug( "Creating EnumerationProperty using: " + pd.getPropertyValues( ) );
        EnumerationProperty enumProp = new EnumerationProperty( );
        enumProp.setName( pd.getName() );
        enumProp.setChoices( pd.getPropertyValues( ) );
          
        if ( prop instanceof PropertyList )
        {
          if ( prop.getValues( pd.getPropertyFormat() ) != null )
          {
            enumProp.setSelected( prop.getValues( pd.getPropertyFormat() ));
          }
        }
        else
        {
          try
          {
            enumProp.setValue(value, pd.getPropertyFormat() );
          }
          catch ( PropertyValidationException pve )
          {
            LOG.debug( "Can't set value to '" + value + "' got PropertyValidationException " + pve );
          }
        }
                    
        enumProp.setMultiValue( pd.isMultiValue( ) );
        prop = enumProp;
      }
      else
      {
        LOG.debug( "creating property: " + pd.getPropertyType( ) );
        try
        {
          prop = (IProperty)Class.forName( pd.getPropertyType() ).newInstance( );
          prop.setName( pd.getName( ) );
                        
          // get components ...
          pd.setInternalProperties( prop );
                        
          try
          {
            prop.setValue( value, pd.getPropertyFormat( ) );
          }
          catch ( PropertyValidationException pve )
          {
            LOG.debug( "Could not set value to " + value );
          }
        }
        catch ( Exception e )
        {
          LOG.debug( "Could not create property for Class: '" + pd.getPropertyType() + "' Got Exception " + e );
          e.printStackTrace( System.out );
        }
      }
                
      LOG.debug( "Returning prop: " + prop.getName( ) );
      return prop;
    }
  }
    
  private DataObject readChildPropertyObject( String objName, PropertyDescriptor pd )
  {
    LOG.debug( "readChildPropertyObject: " + objName );
        
    // get the dataobject schema from the name should be name|id
    String[] components = StringMethods.getStringArray( objName, "|" );
    if (components != null && components.length == 2)
    {
      String name = components[0];
      String ID = components[1];
      String dataSchema = pd.getDataObjectSchema( );
            
      DataObject chObj = null;
            
      if ( pd.isLazilyEvaluated( ) )
      {
        LOG.debug( "Creating DataObjectPlaceholder for " + name );
        chObj = new DataObjectPlaceholder( name, ID, dataSchema, this );
      }
      else
      {
        LOG.debug( "Reading object: " + name + "," + ID );
        chObj = read( name, ID, dataSchema );
      }
            
      return chObj;
    }
        
    LOG.debug( "Failed to read child object from: " + objName + " returning Null!" );
    return null;
  }
    
  private List<DataObject> readChildObjects( String parentID, String childObjectSchema )
  {
    LOG.debug( "readChildObjects( ) " + parentID + " " + childObjectSchema );
        
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    DataObjectSchema dobjSchema = dosm.getDataObjectSchema( childObjectSchema );
    if (dobjSchema == null)
    {
      return null;
    }
        
    String databaseTable = getDatabaseTable( dobjSchema );
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( "select * from " ).append( databaseTable )
              .append( " where " ).append( parentIDField ).append( "='" )
              .append( parentID ).append( "'" );
        
    SQLMethods sqlMethods = getSQLMethods( );
    try
    {
      ArrayList<DataObject> childList = new ArrayList<DataObject>( );
      ResultSet rs = sqlMethods.executeSQLQuery( strbuilder.toString( ) );
      while (rs != null && rs.next() )
      {
        childList.add( createDataObject( rs, dobjSchema ) );
      }
        
      return childList;
    }
    catch ( SQLException sqle )
    {
            
    }
    finally
    {
      sqlMethods.closeDatabaseConnection( );
    }
        
    return null;
  }
    
  private boolean dataObjectExists( DataObject dObj, String schemaName, int version)
  {
    LOG.debug( "dataObject: '" + dObj.getName( ) + "' Exists?" );
    
    String databaseTable = getDatabaseTable(  schemaName, Integer.toString( version ) );
    if (dObj.getID() == null || !tableExists( databaseTable ))
    {
      LOG.debug( "ID = " + dObj.getID( ) + " returning FALSE!" );
      return false;
    }
        
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( "select * from " ).append( databaseTable )
              .append( " where " ).append( nameField ).append( "='" ).append( dObj.getName( ) )
              .append( "' AND " ).append( idField ).append( "='" ).append( dObj.getID( ) ).append( "'" );
    try
    {
      ResultSet rs = executeQuery( strbuilder.toString( ) );
      if (rs != null && rs.next() )
      {
        return true;
      }
    }
    catch ( Exception e )
    {
      LOG.error( "Check select query got Exception: " + e );
    }
    finally
    {
      sqlMethods.closeDatabaseConnection( );
    }
        
    return false;
  }
    
  private void dropTableIfEmpty( DataObject dObj, String version )
  {
    String databaseTable = getDatabaseTable(  dObj.getDataObjectSchema( ), version );
    LOG.debug( "dropTableIfEmpty: " + databaseTable );
        
    String select = "select * from " + databaseTable;
    SQLMethods sqlMethods = getSQLMethods( );
    try
    {
      ResultSet rs = executeQuery( select );
      boolean haveData = false;
      if (rs != null && rs.next() )
      {
        haveData = true;
      }
      sqlMethods.closeDatabaseConnection( );
        
      if (!haveData)
      {
        LOG.debug( "Dropping table " + databaseTable );
        sqlMethods.executeSQLUpdate( "drop table " + databaseTable );
                
        DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
        dosm.deleteDataObjectSchema( dObj.getDataObjectSchema( ), version );
        dataObjTemplateMap.remove( new String( dObj.getDataObjectSchema(  ) + "_" + version ) );
      }
    }
    catch ( Exception e )
    {
      LOG.error( "Check select query got Exception: " + e );
    }
  }
    
    
  private void createRow( DataObject dObj, DataObjectSchema dobjSchema )
  {
    if (dObj == null) return;
        
    LOG.debug( "createRow: " + dObj.getName( ) + " " + dobjSchema.getName( ) + " " + dobjSchema.getVersion( ) );
        
    createTable( dObj, dobjSchema );
        
    executeCommand( getCreateStatement( dObj, dobjSchema ) );
  }
    
  private void setID( DataObject dObj, DataObjectSchema parentSchema )
  {
    LOG.debug( "setID( )..." );
        
    String objSchemaName = dObj.getDataObjectSchema( );
    DataObjectSchema dos = (objSchemaName != null)
                         ? getDataObjectSchema( objSchemaName )
                         : parentSchema;
                             
    if (dos == null) dos = parentSchema;
        
    String ID = dObj.getID( );
    if (ID == null)
    {
      LOG.debug( "Creating new ID for " + dObj.getName( ) );
      dObj.setID( UniqueIDGenerator.generateUniqueID( ) + "_" + Integer.toString( dos.getVersion( ) ) );
    }
    else
    {
      LOG.debug( "Updating ID to latest version of schema." );
      String justID = (ID.indexOf( "_" ) > 0) ? new String( ID.substring( 0, ID.indexOf( "_" ))) : ID;
      String version = (ID.indexOf( "_" ) > 0) ? new String( ID.substring( ID.lastIndexOf( "_" ) + 1 ) ) : null;
            
      if (dos.getVersion( ) != Integer.parseInt( version ))
      {
        LOG.debug( "Object version " + version + " is OBSOLETE - should delete old row " );
        delete( dObj, Integer.parseInt( version ) );
        dropTableIfEmpty( dObj, version );
      }
            
      dObj.setID( justID + "_" + Integer.toString( dos.getVersion( ) ) );
    }
        
    LOG.debug( "Got Object ID = " + dObj.getID( ) );
        
    if (dObj instanceof IDataList)
    {
      LOG.debug( "Setting ID on child Objects" );
            
      IDataList childList = (IDataList)dObj;
      Iterator<DataObject> chObIt = childList.getData( );
      while (chObIt != null && chObIt.hasNext() )
      {
        DataObject childOb = chObIt.next( );
        setID( childOb, parentSchema );
      }
    }
  }
    
  private String getCreateStatement( DataObject dObj, DataObjectSchema dobjSchema )
  {
    SQLTemplateSet dataObjTemplates = getSQLTemplates( dObj, dobjSchema );
    return dataObjTemplates.renderTemplate( SQLTemplateSet.CREATE, dObj );
  }
    
  public void updateDataObject( DataObject dObj )
  {
    LOG.debug( "updateDataObject( ) " + dObj );
    DataObjectSchema dobjSchema = getDataObjectSchema( dObj.getDataObjectSchema( ) );
    if (dobjSchema != null)
    {
      // check if dobjSchema version == dObj version, if not do delete row and
      // create with new version (createRow rather than updateRow)
      boolean versionChanged = false;
      String ID = dObj.getID( );
      if (ID != null)
      {
        int curVersion = dobjSchema.getVersion( );
        String versionSuffix = "_" + Integer.toString( curVersion );
        if ( ID.endsWith( versionSuffix ))
        {
          updateRow( dObj, dobjSchema );
        }
        else
        {
          versionChanged = true;
          // drop old version
          String objVersion = ID.substring( ID.lastIndexOf( "_" ) + 1 );
          delete( dObj, Integer.parseInt( objVersion ) );
          dropTableIfEmpty( dObj, objVersion );
                    
          createRow( dObj, dobjSchema );
        }
      }
            
      updateDataListChildLinks( dObj, dobjSchema );
      updateAdhocProperties( dObj, dobjSchema );
            
      dObj.setProperty( new BooleanProperty( DataObjectSchema.VERSION_CHANGED, versionChanged ));
    }
  }
    
  public void updateChildLinks( DataObject dObj )
  {
    LOG.debug( "updateChildLinks ..." + dObj );
    if (dObj == null) return;
    	
    DataObjectSchema dobjSchema = getDataObjectSchema( dObj.getDataObjectSchema( ) );
    updateDataListChildLinks( dObj, dobjSchema );
    updateAdhocProperties( dObj, dobjSchema );
  }
    
  private void updateDataListChildLinks( DataObject dObj, DataObjectSchema dobjSchema )
  {
    if (dObj instanceof IDataList)
    {
      IDataList dList = (IDataList)dObj;
      Iterator<DataObject> dobIt = dList.getData( );
      if ( dobjSchema.isLazilyEvaluated( ) )
      {
        LOG.debug( "Using Lazy Evaluation: Getting Child Keys" );
            
        StringBuilder keybuilder = new StringBuilder( );
        int childNum = 0;
        while( dobIt != null && dobIt.hasNext() )
        {
          DataObject chob = dobIt.next( );
          String childSchema = chob.getDataObjectSchema( );
          if (childSchema == null)
          {
            childSchema = dObj.getDataObjectSchema( );
          }
                
          String chName = chob.getName( );
          if (chName == null)
          {
            String childName = "child_" + Integer.toString( childNum++ );
            chob.setName( childName );
          }
                
          keybuilder.append( chob.getValue( DataObject.NAME_ID_SCHEMA ));
          if (dobIt.hasNext( ) ) keybuilder.append( ";" );
        }
        
        LOG.debug( "created " + CHILDREN + " = "  + keybuilder.toString( ) );
        dObj.setProperty( new StringProperty( CHILDREN, keybuilder.toString( ) ));
      }
      else
      {
        LOG.debug( "Not using lazy evaluation: setting parent ID on child objects " );
        while( dobIt != null && dobIt.hasNext() )
        {
          DataObject chob = dobIt.next( );
          chob.setProperty( new StringProperty( PARENT_ID, dObj.getID( ) ) );
        }
      }
    }
  }
    
  private void updateAdhocProperties( DataObject dObj, DataObjectSchema dobjSchema )
  {
    LOG.debug( "updateAdhocProperties" );
        
    Set<String> adhocProps = getAdhocProps( dObj, dobjSchema );
    if (adhocProps != null && adhocProps.size() > 0)
    {
      LOG.debug( "Updating Adhoc Properties for: " + dObj.getName( ) );
      // pack them and update to adhocField column as a JSON List
      DataObject adhocPropHolder = new DataObject( );
      Iterator<String> propIt = adhocProps.iterator();
      while( propIt.hasNext( ) )
      {
        IProperty adhocProp = dObj.getProperty( propIt.next( ) );
        if (adhocProp != null)
        {
          LOG.debug( "Adding adhoc prop: " + adhocProp.getName( ) + " = " + adhocProp.getValue( ) );
          adhocPropHolder.addProperty( adhocProp );
        }
      }
                
      String adhocPropsStr = adhocPropHolder.getValue( IProperty.XML_FORMAT );
      updateAdhocProperties( dObj, adhocPropsStr, dobjSchema );
    }
  }
    
  private void updateRow( DataObject dObj, DataObjectSchema dobjSchema )
  {
    String databaseTable = getDatabaseTable( dobjSchema );
    if (tableExists( databaseTable ) )
    {
      executeCommand( getUpdateStatement( dObj, dobjSchema ) );
    }
  }
    

  private String getUpdateStatement( DataObject dObj, DataObjectSchema dobjSchema )
  {
    SQLTemplateSet dataObjTemplates = getSQLTemplates( dObj, dobjSchema );
    return dataObjTemplates.renderTemplate( SQLTemplateSet.UPDATE, dObj );
  }


  private void deleteRow( DataObject dObj, int version )
  {
    String databaseTable = getDatabaseTable( dObj.getDataObjectSchema( ), Integer.toString( version ) );
        
    String deleteStatement = "DELETE from " + databaseTable +
                             " where "+ idField + "='" + dObj.getID( ) + "'";

    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( deleteStatement );
  }
    
  private void deleteRow( String ID, DataObjectSchema dobjSchema )
  {
    String databaseTable = getDatabaseTable( dobjSchema );
        
    String deleteStatement = "DELETE from " + databaseTable +
                             " where "+ idField + "='" + ID + "'";

    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( deleteStatement );
  }
    
    
  private String fixName( String name )
  {
    return StringTransform.replaceSubstring( name,  " ", "_" );
  }
    
    
  private SQLTemplateSet getSQLTemplates( DataObject dObj, DataObjectSchema dobjSchema )
  {
    SQLTemplateSet dataTemplates = dataObjTemplateMap.get( dobjSchema.getName( ) + "_" + Integer.toString( dobjSchema.getVersion( ) ) );
    if (dataTemplates != null) return dataTemplates;
        
    return initSQLTemplates( dObj, dobjSchema );
  }
    
  private SQLTemplateSet initSQLTemplates( DataObject dObj, DataObjectSchema dobjSchema )
  {
    LOG.debug( "initSQLTemplates: " + dObj.getDataObjectSchema( ) );
    
    SQLTemplateSet dataObjTemplates = new SQLTemplateSet( );
        
    dataObjTemplates.setCreateTemplate( createCreateTemplate( dObj, dobjSchema ) );
    dataObjTemplates.setUpdateTemplate( createUpdateTemplate( dObj, dobjSchema ) );
        
    dataObjTemplateMap.put( dobjSchema.getName( ) + "_" + Integer.toString( dobjSchema.getVersion( ) ), dataObjTemplates );
        
    return dataObjTemplates;
  }
    
    
  private DataObjectTemplate createCreateTemplate( DataObject dObj, DataObjectSchema dobjSchema )
  {
    LOG.debug( "createCreateTemplate( ) " + dobjSchema.getName( ) );
   
    StringBuilder strbuilder = new StringBuilder( );
        
    String databaseTable = getDatabaseTable( dobjSchema );
    // build the CREATE template
    strbuilder.append( "INSERT into " ).append( databaseTable )
              .append( " VALUES( '{_ID_}','{_NAME_}','{_TYPE_}','{_IS_ROOT_}','{_PROXY_}' " );
        
    IProperty parentIDProp = dObj.getProperty( PARENT_ID );
    if (parentIDProp != null)
    {
      strbuilder.append( ",'{" ).append( PARENT_ID ).append( "}' ");
    }
        
    Iterator<String> propDescIt = dobjSchema.getPropertyNames( );

    while( propDescIt != null && propDescIt.hasNext( ) )
    {
      PropertyDescriptor pd = dobjSchema.getPropertyDescriptor( propDescIt.next() );
      if (pd.isFunction() == false)
      {
        strbuilder.append( "," );
        if ( pd.isDataObject() )
        {
          DataObject chObj = (DataObject)dObj.getProperty( pd.getName( ) );
          strbuilder.append( "'" );
          if (chObj != null && (chObj instanceof DataList) == false)
          {
            LOG.debug( "Trying to get key from " + chObj.getName( ) );
            strbuilder.append( chObj.getValue( DataObject.NAME_ID ));
          }
          strbuilder.append( "'" );
        }
        else
        {
          if (pd.getDataType().equalsIgnoreCase( "String" ))
          {
            strbuilder.append( "'" );
          }
          strbuilder.append( "{" ).append( pd.getName() ).append( "}" );
          if (pd.getDataType().equalsIgnoreCase( "String" ))
          {
            strbuilder.append( "'" );
          }
        }
      }
    }
        
    if (dObj instanceof IDataList && dobjSchema.isLazilyEvaluated( ))
    {
      strbuilder.append( "," );
      strbuilder.append( "'{" ).append( CHILDREN ).append( "}'" );
    }
        
    // Append another column for the as yet to be updated Adhoc data...
    strbuilder.append( ",'')" );
    String createTemplate = strbuilder.toString( );
    LOG.debug( "Created CREATE template: " + createTemplate );
        
    DataObjectTemplate sqlTemplate = new DataObjectTemplate( createTemplate );
        
    sqlTemplate.setName( SQLTemplateSet.CREATE );
    return sqlTemplate;
  }


  private DataObjectTemplate createUpdateTemplate( DataObject dObj, DataObjectSchema dobjSchema )
  {
    String databaseTable = getDatabaseTable( dobjSchema );
        
    StringBuilder strbuilder = new StringBuilder( );
    // build the UPDATE template
    strbuilder.append( "UPDATE " ).append( databaseTable ).append( " set " )
              .append( nameField ).append( "='{_NAME_}'," );
        
    Iterator<String> propDescIt = dobjSchema.getPropertyNames( );
    while( propDescIt != null && propDescIt.hasNext( ) )
    {
      boolean appended = false;
      PropertyDescriptor pd = dobjSchema.getPropertyDescriptor( propDescIt.next() );
      if (pd.isFunction() == false)
      {
        if (pd.isDataObject( ) )
        {
          DataObject chObj = (DataObject)dObj.getProperty( pd.getName( ) );
          strbuilder.append( fixName( pd.getName() ) ).append( "='" );
          if (chObj != null && chObj instanceof DataList == false)
          {
            strbuilder.append( chObj.getValue( DataObject.NAME_ID ));
          }
          strbuilder.append( "'" );
          appended = true;
        }
        else
        {
          strbuilder.append( fixName( pd.getName() ) ).append( "=" );
          if (pd.getDataType().equals( "String" ))
          {
            strbuilder.append( "'" );
          }
          strbuilder.append( "{" ).append( pd.getName() ).append( "}" );
          if (pd.getDataType().equals( "String" ))
          {
            strbuilder.append( "'" );
          }
          appended = true;
        }
      }
            
      if (appended && propDescIt.hasNext()) strbuilder.append( "," );
    }
    strbuilder.append( " where " ).append( idField ).append( "='{_ID_}'" );
        
    String updateTemplate = strbuilder.toString( );
    LOG.debug( "Created UPDATE template: " + updateTemplate );
        
    DataObjectTemplate sqlTemplate = new DataObjectTemplate( updateTemplate );
        
    sqlTemplate.setName( SQLTemplateSet.UPDATE );
    return sqlTemplate;
  }
    
  // get the property list from the set of Property Descriptors
  // construct the create table command based on the databaseTable name
  private String createCreateTableCommand( DataObject dObj, DataObjectSchema dobjSchema )
  {
    LOG.debug( "createCreateTableCommand( ) " + dobjSchema );
        
    String databaseTable = getDatabaseTable( dobjSchema );
    StringBuilder strbuilder = new StringBuilder( );
    strbuilder.append( "Create Table " ).append( databaseTable )
              .append( "( " );
        
    strbuilder.append( idField ).append( " varchar(32) PRIMARY KEY," );
    strbuilder.append( nameField ).append( " varchar(64)," );
    strbuilder.append( typeField ).append( " varchar(64)," );
    strbuilder.append( rootField ).append( " varchar(1)," );
    strbuilder.append( proxyField ).append( " varchar(64)," );
        
    IProperty parentIDProp = dObj.getProperty( PARENT_ID );
    if ( parentIDProp != null )
    {
      strbuilder.append( parentIDField ).append( "varchar(32)," );
    }
        
    // add NAME and ID fields
        
    Iterator<String> propDescIt = dobjSchema.getPropertyNames( );
    boolean colAdded = false;
    while( propDescIt != null && propDescIt.hasNext( ) )
    {
      PropertyDescriptor pd = dobjSchema.getPropertyDescriptor( propDescIt.next() );
      if (pd.isFunction() == false)
      {
        if (colAdded) strbuilder.append( "," );
        strbuilder.append( fixName( pd.getName( ) ));
            
        String dataType = pd.getDataType( );
        if (dataType.equals( "Integer" ))
        {
          strbuilder.append( " int" );
        }
        else if (dataType.equals( "Double" ))
        {
          strbuilder.append( " double precision" );
        }
        else
        {
          if (pd.isDataObject( ) )
          {
            strbuilder.append( " varchar(64)" );
          }
          else
          {
            int varcharlen = pd.getMaximumLength( );
            String vcLen = (varcharlen == 0) ? varcharLength : Integer.toString( varcharlen );
                
            strbuilder.append( " varchar(" ).append( vcLen ).append( ")" );
          }
        }
                
        colAdded = true;
      }
    }
        
    if (dObj instanceof DataList && dobjSchema.isLazilyEvaluated( ) )
    {
      if (colAdded) strbuilder.append( "," );
      strbuilder.append( "children CLOB" );
    }
        
    strbuilder.append( ", " ).append( adhocField ).append( " CLOB" );
    strbuilder.append( ")" );
        
    LOG.debug( "created " + dobjSchema.getName( ) + " Create Table statement: " + strbuilder.toString( ) );
        
    return strbuilder.toString( );
  }
    
  private void updateAdhocProperties( DataObject dObj, String adhocProps, DataObjectSchema dobjSchema )
  {
    String databaseTable = getDatabaseTable( dobjSchema );
        
    StringBuilder strbuilder = new StringBuilder( );
    // build the UPDATE template
    strbuilder.append( "UPDATE " ).append( databaseTable ).append( " set " )
              .append( adhocField ).append( "='" ).append( adhocProps ).append( "' " )
              .append( " where " ).append( idField ).append( "='" ).append( dObj.getID( ) ).append( "'" );
        
    LOG.debug( "executing update: " + strbuilder.toString( ) );
        
    executeCommand( strbuilder.toString() );
  }
    
  private void insertMasterTableRow( DataObject dObj, DataObjectSchema dobjSchema )
  {
    LOG.debug( "insertMasterTableRow( ) ..." );
    // check if the MASTER_TABLE exists, if not, execute the Create statement,
    // copy the template and fill in the parameters
    if ( !tableExists( MASTER_TABLE ))
    {
      executeCommand( CREATE_MASTER_TABLE );
    }
        
    String insertCmd = new String( INSERT_OBJECT_TEMPLATE );
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{NAME}", dObj.getName( ) );
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{ID}", dObj.getID( ) );
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{OBJECT_CLASS}", dObj.getType( ) );
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{SCHEMA_NAME}", dobjSchema.getName( ) );
        
    String entityType = dobjSchema.getEntityType( );
    if (entityType == null) entityType = "DataObject";
        
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{ENTITY_TYPE}", entityType );
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{VERSION}", Integer.toString( dobjSchema.getVersion( ) ) );
        
    // create time stamp - yyyy-mm-dd hh:mm:ss
    try
    {
      Date now = new Date( );
      String created = createdFormatter.format( now );
      insertCmd = StringTransform.replaceSubstring( insertCmd, "{CREATED}", created );
    }
    catch ( Exception e )
    {
        
    }

    // Implement these ---
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{OWNER}", "" );
    insertCmd = StringTransform.replaceSubstring( insertCmd, "{ACL}", "" );
        
    executeCommand( insertCmd );
  }
    
  private void deleteMasterTableRow( DataObject dobj )
  {
    LOG.debug( "deleteMasterTableRow" );
    if ( !tableExists( MASTER_TABLE ))
    {
      return;
    }
        
    String deleteCommand = "delete from " + MASTER_TABLE + " where " + OBJECT_NAME_COL + "='" + dobj.getName( ) + "'";
    executeCommand( deleteCommand );
  }
    
  // Set default values for numerical properties if they are missing...
  private void setNumericDefaults( DataObject dobj, DataObjectSchema dobjSchema )
  {
    if (dobj != null && dobjSchema != null)
    {
      List<PropertyDescriptor> props = dobjSchema.getPropertyDescriptors( );
      for (int i = 0; i < props.size( ); i++)
      {
        PropertyDescriptor pd = props.get( i );
        String propName = pd.getName( );
        
        IProperty dobjProp = dobj.getProperty( propName );
        if ( dobjProp == null && (pd.getDataType().equals( "Integer" ) ||
                                  pd.getDataType().equals( "Double" )))
        {
          IProperty stubProp = null;
          try
          {
            stubProp = (IProperty)Class.forName( pd.getPropertyType() ).newInstance( );
            stubProp.setName( propName );
            dobj.setProperty( stubProp );
          }
          catch ( Exception e )
          {
              
          }
        }
      }
    }
  }
    
  private void createTable( DataObject dObj, DataObjectSchema dobjSchema )
  {
    LOG.debug( "createTable( )..." );
    String databaseTable = getDatabaseTable( dobjSchema );
        
    // Check if Table Exists .. if so execute the table e
    if (!tableExists( databaseTable ))
    {
      executeCommand( createCreateTableCommand( dObj, dobjSchema ) );
    }
  }
    


  private boolean tableExists( String databaseTable )
  {
    LOG.debug( "table '" + databaseTable + "' Exists ?" );
        
    synchronized( this )
    {
      LOG.debug( "TableExists( " + databaseTable + " )");
      SQLMethods sqlMethods = getSQLMethods( );
      if (sqlMethods == null) return false;
        
      try
      {
        String selectAll = "select 1 from " + databaseTable;
        ResultSet rs = sqlMethods.executeSQLQuery( selectAll );
        return (rs != null);
      }
      catch ( Exception e )
      {
        LOG.debug( "tableExists( " + databaseTable + " ) got EXCEPTION: " + e );
      }
      finally
      {
        sqlMethods.closeDatabaseConnection( );
      }
    }

    return false;
  }
    
  private void executeCommand( String command )
  {
    LOG.debug( "executeCommand: '" + command + "'" );
        
    // get a SQLMethods object and call execute on it.
    SQLMethods sqlMethods = getSQLMethods( );
    sqlMethods.executeSQLUpdate( command );
  }
    
  private ResultSet executeQuery( String query )
  {
    LOG.debug( "executeQuery: '" + query + "'" );
        
    SQLMethods sqlMethods = getSQLMethods( );
    return sqlMethods.executeSQLQuery( query );
  }
    
  private SQLMethods getSQLMethods( )
  {
    if (this.sqlMethods != null) return this.sqlMethods;
        
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
            
      if (dataSourceFactory == null)
      {
        LOG.error( "Cannot load IDataSourceFactory: " + dataSourceFactoryName );
        return null;
      }
    }
        
    return this.dataSourceFactory;
  }
    
  private DataObjectSchemaManager getDataObjectSchemaManager( )
  {
    LOG.debug( "getDataObjectSchemaManager( ) ..." );
        
    if (this.dataObjectSchemaManager != null)
    {
      return this.dataObjectSchemaManager;
    }
        
    IDataSourceFactory dataSourceFactory = getDataSourceFactory( );
    if (dataSourceFactory != null)
    {
      LOG.debug( "creating new DataObjectSchemaManager" );
      this.dataObjectSchemaManager = new DataObjectSchemaManager( dataSourceFactory );
    }
        
    return this.dataObjectSchemaManager;
  }
    
  private Set<String> getAdhocProps( DataObject dObj, DataObjectSchema dobjSchema )
  {
    Iterator<String> propIt = dobjSchema.getPropertyNames( );
    HashSet<String> schemaProps = new HashSet<String>( );
    while (propIt != null && propIt.hasNext() )
    {
      schemaProps.add( propIt.next( ) );
    }
        
    HashSet<String> adhocProps = new HashSet<String>( );
    propIt = dObj.getPropertyNames( );
    while (propIt != null && propIt.hasNext() )
    {
      String objProp = propIt.next( );
      IProperty theProp = dObj.getProperty( objProp );
      if (theProp != null && schemaProps.contains( objProp ) == false && (theProp instanceof IntrinsicPropertyDelegate) == false)
      {
        adhocProps.add( objProp );
      }
    }
            
    return adhocProps;
  }
    
  private String getAdhocProp( String propName, ResultSet rs )
  {
    try
    {
      Clob adhocProps = rs.getClob( adhocField );
      if (adhocProps != null)
      {
        String adhocXML = getCLOBString( adhocProps );
        DataObject adhocObj = new XMLDataObjectParser( ).createDataObject( adhocXML );
        IProperty prop = (adhocObj != null) ? adhocObj.getProperty( propName ) : null;
        return (prop != null) ? prop.getValue( ) : null;
      }
        
      return null;
    }
    catch ( Exception e )
    {
      return null;
    }
  }
    
  private DataObjectSchema getDataObjectSchema( String dataObjectSchemaName )
  {
    LOG.debug( "getDataObjectSchema: " + dataObjectSchemaName );
        
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    return (dosm != null) ? dosm.getDataObjectSchema( dataObjectSchemaName ) : null;
  }
    
  private DataObjectSchema getDataObjectSchema( String dataObjectSchemaName, DataObject context )
  {
    LOG.debug( "getDataObjectSchema: " + dataObjectSchemaName + " with context " + context );
        
    DataObjectSchemaManager dosm = getDataObjectSchemaManager( );
    return (dosm != null) ? dosm.getDataObjectSchema( dataObjectSchemaName, context ) : null;
  }
    
  private String getDatabaseTable( DataObjectSchema dataObjectSchema )
  {
    LOG.debug( "getDatabaseTable: " + dataObjectSchema );
    if (dataObjectSchema == null) return null;
        
    return getDatabaseTable( dataObjectSchema.getName( ), Integer.toString( dataObjectSchema.getVersion( ) ) );
  }
    
  private String getDatabaseTable( String dataObjectSchemaName, String version )
  {
    String tableName = StringTransform.replaceSubstring( dataObjectSchemaName, " ", "_" );
    tableName += "_" + version;
    return tableName;
  }
    
  private String getDOClass( String type )
  {
    if (type == null || type.equals( "DO" )) return "com.modinfodesigns.property.DataObject";
        
    if (type != null && type.equals( "DL" )) return "com.modinfodesigns.property.DataList";
        
    return type;
  }
    
  private String getCLOBString( Clob clob )
  {
    try
    {
      InputStream is = clob.getAsciiStream();
      BufferedReader bufReader = new BufferedReader( new InputStreamReader( is ) );
      return bufReader.readLine( );
    }
    catch ( Exception e )
    {
      LOG.debug( "Could not read CLOB! " + e );
    }
        
    return null;
  }

  @Override
  public void initialize(String configXML)
  {
    // get the name of the IDataSourceFactory
    Document doc = DOMMethods.getDocument( new StringReader( configXML ) );
    if (doc == null)
    {
      LOG.error( "Could not create Document from " + configXML );
      return;
    }
        
    Element docElem = doc.getDocumentElement( );
    setDataSourceFactoryName( docElem.getAttribute( "dataSourceFactory" ) );
        
    // Get the Class name for the DataObjectSchemaManager ....
        
        
  }

  @Override
  public Object getApplicationObject(String name, String type)
  {
    return read( name, type );
  }

  @Override
  public List<Object> getApplicationObjects( String type )
  {
    LOG.debug( "getApplicationObjects( " + type + " )" );
    // use DataObjectDBFinder
    DataObjectDBFinder dobjFinder = new DataObjectDBFinder( this.dataSourceFactoryName, type );
    dobjFinder.setDataObjectSchemaManager( getDataObjectSchemaManager( ) );

    try
    {
      IResultList allTypes = dobjFinder.executeQuery( null );
      if (allTypes != null)
      {
        Iterator<DataObject> dobjIt = allTypes.getData( );
        ArrayList<Object> objList = new ArrayList<Object>( );
        while (dobjIt != null && dobjIt.hasNext( ) )
        {
          objList.add( dobjIt.next( ) );
        }
                
        return objList;
      }
    }
    catch ( FinderException fe )
    {
            
    }
        
    return null;
  }

  @Override
  public List<String> getApplicationObjectNames( String type )
  {
    return getDataObjectNames( type );
  }
    
  @Override
  public void finalize( )
  {
    if (this.sqlMethods != null)
    {
      this.sqlMethods.closeDatabaseConnection( );
      try
      {
        this.sqlMethods.finalize( );
      }
      catch ( Exception e )
      {
                
      }
    }
  }
    
    
  private static final String CREATE_MASTER_TABLE = "Create Table " + MASTER_TABLE
                                                  + " (" + OBJECT_NAME_COL    + " varchar(32) PRIMARY KEY,"
                                                  + " "  + OBJECT_ID_COL      + " varchar(32),"
                                                  + " "  + OBJECT_CLASS_COL   + " varchar(128),"
                                                  + " "  + ENTITY_TYPE_COL    + " varchar(32),"
                                                  + " "  + SCHEMA_NAME_COL    + " varchar(32),"
                                                  + " "  + SCHEMA_VERSION_COL + " int,"
                                                  + " "  + CREATED_COL        + " varchar(20),"
                                                  + " "  + OWNER_COL          + " varchar(32),"
                                                  + " "  + ACL_COL            + " varchar(128)"
                                                  + " )";
    
  private static final String INSERT_OBJECT_TEMPLATE = "INSERT INTO " + MASTER_TABLE + " VALUES( "
                 + "'{NAME}','{ID}','{OBJECT_CLASS}','{ENTITY_TYPE}','{SCHEMA_NAME}',{VERSION},'{CREATED}','{OWNER}','{ACL}')";

}
