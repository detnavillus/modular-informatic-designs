package com.modinfodesigns.property;

import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.ICreateDataObjectSchema;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import com.modinfodesigns.utils.StringMethods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for DataObject classes that are also Java Beans -
 * synchronizes Java Bean properties and DataObject properties.
 * 
 * This enables easy serialization using getValue( ) for Java Bean
 * descendants.
 * 
 * @author Ted Sullivan
 */

public abstract class DataObjectBean extends DataObject implements ICreateDataObjectSchema
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DataObjectBean.class );
	
  public abstract DataObjectSchema createDataObjectSchema( DataObject context );
	
  @Override
  public String getDataObjectSchema(  )
  {
    return this.getClass().getSimpleName( );
  }
	
  @Override
  public void setProperty( IProperty property )
  {
    LOG.debug( "setProperty( " + property + " )" );
    if (!setBeanProperty( property ))
    {
      doSetProperty( property );
    }
  }
    
  /**
   * Called by DataObjectBean if the IProperty is not a JavaBean property
   * should be called by subclasses that override setProperty to avoid infinite recursion.
   *
   * @param property
   */
  protected void doSetProperty( IProperty property )
  {
    if (property == null) return;
    	
    LOG.debug( "doSetProperty( " + property.getName() + ": " + property.getValue() + " )" );
    super.setProperty( property, false );
  }
    
  protected void doAddProperty( IProperty property )
  {
    super.addProperty( property, false );
  }
    
  private boolean setBeanProperty( IProperty property )
  {
    LOG.debug( "setBeanProperty( " + property + " )" );
    	
    String propName = property.getName( );
    Object valObject = property.getValueObject();
    if (valObject == null) return false;
    	
    try
    {
      String methodName = getMethodName( propName );
      Object[] params = new Object[1];
      params[0] = valObject;
		    
      LOG.debug( "Trying method: " + methodName );
		    
      Method m = null;
      try
      {
        @SuppressWarnings("rawtypes")
        Class[] paramArray = new Class[1];
        paramArray[0] = valObject.getClass();
        m = this.getClass().getMethod( methodName, paramArray );
      }
      catch (NoSuchMethodException nme )
      {
        m = null;
      }
    	    
      if (m != null)
      {
        LOG.debug( "Invoking: " + methodName );
        m.invoke( this, params );
        return true;
      }
      else
      {
        @SuppressWarnings("rawtypes")
        Class[] interfaces = valObject.getClass().getInterfaces();
        if (interfaces != null)
        {
          for (int i = 0; i < interfaces.length; i++)
          {
            try
            {
              @SuppressWarnings("rawtypes")
              Class[] interfaceArray = new Class[1];
              interfaceArray[0] = interfaces[i];
    	    				
              m = this.getClass().getMethod( methodName, interfaceArray );
            }
            catch (NoSuchMethodException nme )
            {
              m = null;
            }
    	    			
            if (m != null)
            {
              LOG.debug( "Invoking " + methodName + " using Interface " + interfaces[i].getName( ) );
              m.invoke( this, params );
              return true;
            }
          }
        }
      }
    }
    catch ( SecurityException se ) { }
    catch ( InvocationTargetException ite ) { }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    	
    LOG.debug( "No Bean Property Found - returning false " );
    	
    return false;
  }
    	
  private String getMethodName( String typeName )
  {
    String method = "set" + StringMethods.initialCaps( typeName );
    return method;
  }

}
