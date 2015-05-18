package com.modinfodesigns.property;


import com.modinfodesigns.property.transform.PropertyTemplateTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to convert a DataObject to a String using a template with {PROP_NAME} place holders 
 * for the DataObject properties.  Uses a PropertyTemplateTransform internally.
 * 
 * Used by DataObjectTemplateManager to generate a String given a PropertyHolder object.
 * 
 * @author Ted Sullivan
 */

public class DataObjectTemplate implements IPropertyHolder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataObjectTemplate.class );

  private String name;
  private String id;
  private String type;
	
  private String template;  // contains placeholders for property names {prop_name}
  private boolean isRootObject;
	
  private HashMap<String,IProperty> propertyMap;
    
  public DataObjectTemplate( String sqlTemplate )
  {
    this.template = sqlTemplate;
  }
    
  public DataObjectTemplate( String template, IPropertyHolder properties )
  {
    this.template = template;
    setProperties( properties.getProperties( ) );
    setType( properties.getType( ) );
    setIsRootObject( properties.isRootObject() );
  }
    
  public DataObjectTemplate( String name, String type, String template, boolean isRootObject, Iterator<IProperty> properties )
  {
    this.name = name;
    this.type = type;
    this.template = template;
    this.isRootObject = isRootObject;
    if (properties != null) setProperties( properties );
  }
    
    
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }
	
  public void setType( String type )
  {
    this.type = type;
  }

  @Override
  public String getType()
  {
    return type;
  }

  @Override
  public String getValue()
  {
    // replace the template place-holders with values of the properties ...
    PropertyTemplateTransform templateXForm = new PropertyTemplateTransform( this.template, "output" );
    try
    {
      templateXForm.transformPropertyHolder( this );
      return getProperty( "output" ).getValue( );
    }
    catch (PropertyTransformException pte )
    {
      LOG.error( pte.getMessage() );
    }
		
    return null;
  }

  @Override
  public String getValue(String format)
  {
    return getValue( );
  }

  @Override
  public void setValue(String value, String format)
  {
    this.template = value;
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    return new DataObjectTemplate( this.name, this.type, this.template, this.isRootObject,
                                   ((propertyMap != null) ? propertyMap.values().iterator() : null) );
  }

  @Override
  public Object getValueObject()
  {
    return null;
  }

  @Override
  public Map<String, IProperty> getPropertyMap()
  {
    return propertyMap;
  }

  @Override
  public String getID()
  {
    return this.id;
  }

  @Override
  public void setID( String ID )
  {
    this.id = ID;
  }

  @Override
  public IProperty getProperty(String name)
  {
    return (propertyMap != null) ? propertyMap.get( name ) : null;
  }

  @Override
  public Iterator<IProperty> getProperties()
  {
    return (propertyMap != null) ? propertyMap.values().iterator() : null;
  }

  @Override
  public Iterator<String> getPropertyNames()
  {
    return (propertyMap != null) ? propertyMap.keySet().iterator() : null;
  }

  @Override
  public void addProperty(IProperty property)
  {
    setProperty( property );
  }

  @Override
  public void setProperty(IProperty property)
  {
    if (propertyMap == null) propertyMap = new HashMap<String,IProperty>( );
    propertyMap.put( property.getName( ), property );
  }
	
  @Override
  public void setProperties( Iterator<IProperty> properties )
  {
    LOG.debug( "setProperties( ) " );
		
    while (properties != null && properties.hasNext() )
    {
      setProperty( properties.next() );
    }
  }

  @Override
  public void removeProperty( String propName )
  {
    if (propertyMap != null) propertyMap.remove( propName );
  }
	
  public void removeProperties( )
  {
    if (propertyMap != null) propertyMap.clear( );
  }

  @Override
  public void addAssociation(IAssociation association)
                             throws AssociationException
  {

  }

  @Override
  public void setAssociation(IAssociation association)
  {

  }

  @Override
  public Iterator<String> getAssociationNames()
  {
    return null;
  }

  @Override
  public List<IAssociation> getAssociations(String name)
  {
    return null;
  }

  @Override
  public IAssociation getAssociation(String name, String ID)
  {
    return null;
  }

  @Override
  public void removeAssociations(String name)
  {

  }

  @Override
  public void removeAssociation(String name, String ID)
  {

  }

  @Override
  public void clear()
  {
		
  }

  @Override
  public boolean containsKey(Object key)
  {
    return false;
  }

  @Override
  public boolean containsValue(Object value)
  {
    return false;
  }

  @Override
  public Set entrySet()
  {
    return null;
  }

  @Override
  public Object get(Object key)
  {
    return null;
  }

  @Override
  public boolean isEmpty()
  {
    return false;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Set keySet()
  {
    return null;
  }

  @Override
  public Object put(Object key, Object value)
  {
    return null;
  }

  @Override
  public void putAll( Map m )
  {
		
  }

  @Override
  public Object remove(Object key)
  {
    return null;
  }

  @Override
  public int size()
  {
    return 0;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Collection values()
  {
    return null;
  }

  @Override
  public boolean isMultiValue()
  {
    return false;
  }

  @Override
  public String[] getValues(String format)
  {
    return null;
  }

  @Override
  public void setIsRootObject(boolean isRoot)
  {
    this.isRootObject = isRoot;
  }

  @Override
  public boolean isRootObject()
  {
    return this.isRootObject;
  }

}
