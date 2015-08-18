package com.modinfodesigns.property.transform.time;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.transform.IPropertyTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.IPropertyTransformListener;

import com.modinfodesigns.property.time.DateProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatePropertyTransform implements IPropertyTransform 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( DatePropertyTransform.class );
    
  private String dateFormat;
    
  public void setDateFormat( String dateFormat )
  {
    this.dateFormat = dateFormat;
  }
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    LOG.info( "transform  val: " + input.getValue( ) + " class: " + input.getClass().getName( ) );
    try
    {
      DateProperty dateProp = new DateProperty( );
      dateProp.setValue( input.getValue( ), dateFormat );
      dateProp.setName( input.getName( ) );
        
      return dateProp;
    }
    catch (PropertyValidationException pve )
    {
      throw new PropertyTransformException( pve.getMessage( ) );
    }
  }
 
  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener )
                              throws PropertyTransformException
  {
        
  }
}
