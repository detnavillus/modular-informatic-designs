package com.modinfodesigns.property.persistence;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.string.StringProperty;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persisted version of a DataObject. Implements Lazy Evaluation strategy for its owner.
 * 
 * @author Ted Sullivan
 */
public class DataObjectPlaceholder extends DataObject implements IDataObjectPlaceholder
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataObjectPlaceholder.class );

  protected DataObject proxyObject;
  private SQLTemplatePersistenceManager templateMan;
    
  private boolean modified = false;
   
  public DataObjectPlaceholder( ) { }
    
  public DataObjectPlaceholder( String name, String id,
                                String dataObjectSchema, SQLTemplatePersistenceManager templateMan )
  {
    setName( name );
    setID( id );
    setDataObjectSchema( dataObjectSchema );
    setSQLTemplatePersistenceManager( templateMan );
  }
    
  @Override
  public DataObject getProxyObject( )
  {
    LOG.debug( "getProxyObject( )" );
        
    if (proxyObject != null) return proxyObject;
        
    synchronized( this )
    {
      if (proxyObject != null) return proxyObject;
        
      if (templateMan != null)
      {
        proxyObject = templateMan.read( getName( ),  getID( ), getDataObjectSchema( ) );
        if (proxyObject != null)
        {
          proxyObject.setProperty( new StringProperty( DataObject.OBJECT_NAME_PARAM, getName( ) ) );
          proxyObject.setProperty( new StringProperty( DataObject.OBJECT_ID_PARAM, getID( ) ) );
          setModified( false );
        }
      }
    }
        
    return proxyObject;
  }
    
  protected synchronized void setModified( boolean modified )
  {
    this.modified = modified;
    if (modified) proxyObject = null;
  }


  @Override
  public String getType()
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getType( ) : null;
  }

  @Override
  public String getValue()
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getValue() : null;
  }

  @Override
  public String getValue(String format)
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getValue( format ) : null;
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
    DataObject proxyObject = getProxyObject( );
    if (proxyObject != null)
    {
      proxyObject.setValue( value, format );
      setModified( true );
    }
  }

  @Override
  public String getDefaultFormat()
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getDefaultFormat() : null;
  }

  @Override
  public IProperty copy()
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.copy() : null;
  }

  @Override
  public Object getValueObject()
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getValueObject() : null;
  }
    
  @Override
  public IProperty getProperty( String name )
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getProperty( name ) : super.getProperty( name );
  }

  @Override
  public IProperty getNestedProperty( String localName, String subPath )
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getNestedProperty( localName, subPath ) : null;
  }
    
  @Override
  public Iterator<IProperty> getProperties()
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getProperties( ) : null;
  }
    
  @Override
  public Iterator<String> getPropertyNames( )
  {
    DataObject proxyObject = getProxyObject( );
    return (proxyObject != null) ? proxyObject.getPropertyNames( ) : null;
  }
    
  @Override
  public void addProperty( IProperty property)
  {
    if (proxyObject != null)
    {
      proxyObject.addProperty( property );
      setModified( true );
    }
    else super.addProperty( property );
  }
    
  @Override
  public void setProperty( IProperty property)
  {
    LOG.debug( "setProperty: " + property );
    if (proxyObject != null)
    {
      proxyObject.setProperty( property );
      setModified( true );
    }
    else super.setProperty( property );
  }
    
  @Override
  public void setProperties( Iterator<IProperty> properties )
  {
    if (proxyObject != null)
    {
      proxyObject.setProperties( properties );
      setModified( true );
    }
    else super.setProperties( properties );
  }
    
  @Override
  public void removeProperty( String propName )
  {
    DataObject proxyObject = getProxyObject( );
    if (proxyObject != null)
    {
      proxyObject.removeProperty( propName );
      setModified( true );
    }
  }

  @Override
  public void setSQLTemplatePersistenceManager( SQLTemplatePersistenceManager templateMan )
  {
    this.templateMan = templateMan;
  }

  @Override
  public void setSchemaName( String schemaName )
  {
    setDataObjectSchema( schemaName );
  }

}
