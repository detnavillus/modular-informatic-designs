package com.modinfodesigns.property.transform.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.transform.IPropertyTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.IPropertyTransformListener;

import com.modinfodesigns.property.string.TextProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextPropertyTransform implements IPropertyTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TextPropertyTransform.class );
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    LOG.info( "transform  val: " + input.getValue( ) + " class: " + input.getClass().getName( ) );
    try
    {
      TextProperty textProp = new TextProperty( );
      textProp.setValue( input.getValue( ), null );
      textProp.setName( input.getName( ) );
            
      return textProp;
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
