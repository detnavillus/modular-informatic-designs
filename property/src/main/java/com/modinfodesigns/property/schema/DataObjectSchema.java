package com.modinfodesigns.property.schema;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.string.StringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes the data types and properties that a DataObject that conforms to this Schema
 * can have.
 * 
 * Consists of a list of PropertyDescriptor(s) that describe the property fields
 * of the data object's schema.
 * 
 * For DataObjects that are DataList objects - defines the data type of the child
 * data objects that comprise the contained DataObject list.
 * 
 * @author Ted Sullivan
 */

public class DataObjectSchema extends DataList
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataObjectSchema.class );

  public static final String SCHEMA_PARAM = DataObject.SCHEMA_PARAM;
  public static final String CUSTOM_SCHEMA_PARAM = "customSchema";
	
  public static final String SCHEMA_TEMPLATE_PARAM = "schemaTemplate";
	
  public static String OBJECT_NAME_PARAM = DataObject.OBJECT_NAME_PARAM;
  public static String OBJECT_ID_PARAM   = DataObject.OBJECT_ID_PARAM;
  public static String OBJECT_TYPE_PARAM = "objectType";
  public static String CHILD_SCHEMA_NAME = "childSchema";
	
  public static final String DTD_FORMAT = "DTDFormat";
  public static final String XSD_FORMAT = "XSDFormat";
  public static final String OWL_FORMAT = "OWLFormat";
	
  public static final String VERSION   = "Version";
  public static final String PUBLISHED = "Published";
	
  public static final String DATA_OBJECT_TYPE    = "DataObjectType";
  public static final String CHILD_OBJECT_SCHEMA = "ChildObjectSchema";
	
  public static final String ENTITY_TYPE = "EntityType";
	
  public static final String CHILD_PLACEHOLDER_CLASS = "ChildPlaceholderClass";
	
  public static final String VERSION_CHANGED = "VersionChanged";
	
  private HashMap<String,PropertyDescriptor> propertyDescriptors;
  private ArrayList<DataObjectSchema> childSchemas;
    
  private String parentSchema;
	
  private boolean lazyEvaluation = true;
	
  @Override
  public String getType( )
  {
    return getClass().getCanonicalName( );
  }
	
  protected String getXMLTagName( )
  {
    return "DataObjectSchema";
  }

  public void addPropertyDescriptor( PropertyDescriptor propertyDescriptor )
  {
    LOG.debug( this.getName( ) + " addPropertyDescriptor( " + propertyDescriptor.getName( ) + " ): " + propertyDescriptor );
		
    if (propertyDescriptors == null) propertyDescriptors = new HashMap<String,PropertyDescriptor>( );
    propertyDescriptors.put( propertyDescriptor.getName( ), propertyDescriptor );
    super.addDataObject( propertyDescriptor );
  }
	
  @Override
  public void addDataObject( DataObject dObject )
  {
    if (dObject == null) return;
		
    if (dObject instanceof PropertyDescriptor )
    {
      addPropertyDescriptor( (PropertyDescriptor)dObject );
    }
    else if (dObject instanceof DataObjectSchema )
    {
      addChildSchema( (DataObjectSchema)dObject );
    }
    else
    {
      super.addDataObject( dObject );
    }
  }
	
  public void deletePropertyDescriptor( String propertyName )
  {
    LOG.debug( "removing property: " + propertyName + " prop descriptors has " + propertyDescriptors.size( ) );
    propertyDescriptors.remove( propertyName );
    removeDataObjectByName( propertyName );	// removes from super class DataList
    LOG.debug( "property descriptor is now: " + getPropertyDescriptor( propertyName ));
  }
	
  public void setPropertyDescriptors( List<PropertyDescriptor> propDescriptors )
  {
    if (propDescriptors != null)
    {
      for (int i = 0, isz = propDescriptors.size(); i < isz; i++)
      {
        addPropertyDescriptor( propDescriptors.get( i ) );
      }
    }
  }
	
  public PropertyDescriptor getPropertyDescriptor( String name )
  {
    return (propertyDescriptors != null) ? propertyDescriptors.get( name ) : null;
  }
	
  public Iterator<String> getPropertyNames(  )
  {
    return (propertyDescriptors != null) ? propertyDescriptors.keySet().iterator() : null;
  }
    
  public void setParentSchema( String parentSchema )
  {
    this.parentSchema = parentSchema;
  }
    
  public String getParentSchema( )
  {
    return this.parentSchema;
  }
    
  public void addChildSchema( DataObjectSchema childSchema )
  {
    LOG.debug( "addChildSchema " + childSchema.getName( ) );
    if (childSchemas == null) childSchemas = new ArrayList<DataObjectSchema>( );
    childSchemas.add( childSchema );
    LOG.debug( "childSchemas has " + childSchemas.size( ) );
  }
    
  public List<DataObjectSchema> getChildSchemas(  )
  {
    LOG.debug( this + " childSchemas is not null ?" + (childSchemas != null) );
    return this.childSchemas;
  }
	
  public void setVersion( int version )
  {
    setProperty( new IntegerProperty( VERSION, version ), false );
  }
	
  public int getVersion(  )
  {
    IntegerProperty versProp = (IntegerProperty)getProperty( VERSION );
    return (versProp != null) ? versProp.getIntegerValue( ) : 0;
  }
	
  public void setIsPublished( boolean isPublished )
  {
    setProperty( new BooleanProperty( PUBLISHED, isPublished ), false );
  }
	
  public boolean isPublished( )
  {
    BooleanProperty pubProp = (BooleanProperty)getProperty( PUBLISHED );
    return (pubProp != null && pubProp.getBooleanValue( ) );
  }
	
  /**
   * Sets the DataObject type (class name) for the root object.
   *
   * @param dataObjectType
  */
  public void setDataObjectType( String dataObjectType )
  {
    setProperty( new StringProperty( DATA_OBJECT_TYPE, dataObjectType ), false );
  }
	
  /**
   * returns the class name of the root Data Object.
   *
   * @return
   */
  public String getDataObjectType( )
  {
    StringProperty typeProp = (StringProperty)getProperty( DATA_OBJECT_TYPE );
    return (typeProp != null) ? typeProp.getValue( ) : null;
  }
	
  /**
   * Sets the Data Object semantic entity or "meta" type. E.g. "Person", "Taxonomy", etc.
   *
   * @param entityType
   */
  public void setEntityType( String entityType )
  {
    setProperty( new StringProperty( ENTITY_TYPE, entityType ), false );
  }
	
  public String getEntityType( )
  {
    StringProperty typeProp = (StringProperty)getProperty( ENTITY_TYPE );
    return (typeProp != null) ? typeProp.getValue( ) : null;
  }
	
  /**
   * Sets the DataObjectSchema name of IDataList child objects. (not
   * needed if the DataObject type does not implement IDataList)
   *
   * @param childObjectType
   */
  public void setChildObjectSchema( String childObjectSchema )
  {
    setProperty( new StringProperty( CHILD_OBJECT_SCHEMA, childObjectSchema ), false );
  }
	
  /**
   * returns the class name (type) of child objects (if this is an IDataList)
   *
   * @return
   */
  public String getChildObjectSchema( )
  {
    StringProperty typeProp = (StringProperty)getProperty( CHILD_OBJECT_SCHEMA );
    return (typeProp != null) ? typeProp.getValue( ) : this.getName( );
  }
	
  public void setChildPlaceholderClass( String childPlaceholderClass )
  {
    setProperty( new StringProperty( CHILD_PLACEHOLDER_CLASS, childPlaceholderClass ), false );
  }
	
  public String getChildPlaceholderClass( )
  {
    StringProperty placeClassProp = (StringProperty)getProperty( CHILD_PLACEHOLDER_CLASS );
    return (placeClassProp != null) ? placeClassProp.getValue( ) : null;
  }
	
  public List<PropertyDescriptor> getPropertyDescriptors( )
  {
    ArrayList<PropertyDescriptor> propDescriptors = new ArrayList<PropertyDescriptor>( );
    List<DataObject> dobjs = super.getDataList( );
    if (dobjs != null)
    {
      for (int i = 0, isz = dobjs.size(); i < isz; i++)
      {
        DataObject dob = dobjs.get( i );
        if (dob instanceof PropertyDescriptor)
        {
          LOG.debug( "retrieving " + dob.getName( )  + " " + dob );
          propDescriptors.add( (PropertyDescriptor)dob );
        }
      }
    }
		
    return propDescriptors;
  }
	
  public void setLazilyEvaluated( boolean lazyEvaluation )
  {
    this.lazyEvaluation = lazyEvaluation;
  }
	
  public boolean isLazilyEvaluated( )
  {
    return this.lazyEvaluation;
  }
	
  @Override
  public boolean equals( Object another )
  {
    if ((another instanceof DataObjectSchema) == false)
    {
      return false;
    }
		
    DataObjectSchema anotherSchema = (DataObjectSchema)another;
    if (anotherSchema.getName( ).equals( this.getName( ) ) == false )
    {
      return false;
    }
      
    if ((parentSchema != null && anotherSchema.parentSchema == null)
     || (parentSchema == null && anotherSchema.parentSchema != null))
    {
        return false;
    }
    if (parentSchema != null && !parentSchema.equals( anotherSchema.parentSchema))
    {
      return false;
    }
		
    List<PropertyDescriptor> anotherProps = anotherSchema.getPropertyDescriptors( );
    List<PropertyDescriptor> thisProps = getPropertyDescriptors( );
		
    if ((anotherProps == null && thisProps != null) ||
        (thisProps == null && anotherProps != null))
    {
      return false;
    }
		
    if (anotherProps != null && thisProps != null)
    {
      if (anotherProps.size( ) != thisProps.size( )) return false;
        
      for (int i = 0, isz = thisProps.size(); i < isz; i++)
      {
        PropertyDescriptor aProp = thisProps.get( i );
        PropertyDescriptor anotherProp = anotherSchema.getPropertyDescriptor( aProp.getName( ) );
        if (anotherProp == null || aProp.equals( anotherProp ) == false)
        {
          return false;
        }
      }
    }
      
    // make sure all child schemas are the same
		
    return true;
  }
	
  @Override
  public String getValue( String format )
  {
    if (format != null && format.equals( DTD_FORMAT ))
    {
      // create a DTD output
    }
    else if (format != null && format.equals( XSD_FORMAT ))
    {
      // create an XSD output
    }
    
    return super.getValue( format );
  }
	

  // ===========================================================================================
  //
  // ===========================================================================================
}
