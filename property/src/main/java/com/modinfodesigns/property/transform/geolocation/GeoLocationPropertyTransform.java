package com.modinfodesigns.property.transform.geolocation;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.IPropertyTransformListener;

import com.modinfodesigns.property.geolocation.GeographicLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoLocationPropertyTransform implements IPropertyHolderTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( GeoLocationPropertyTransform.class );
    
  private String geoFormat;
    
  private String latitudeProperty;
    
  private String longitudeProperty;
    
  private String geoLocationProperty;
    
  public void setGeoFormat( String geoFormat )
  {
    this.geoFormat = geoFormat;
  }
    
  public void setLatitudeProperty( String latitudeProperty )
  {
    this.latitudeProperty = latitudeProperty;
  }
    
  public void setLongitudeProperty( String longitudeProperty )
  {
    this.longitudeProperty = longitudeProperty;
  }
    
  public void setGeoLocationProperty( String geoLocationProperty )
  {
    this.geoLocationProperty = geoLocationProperty;
  }
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    LOG.info( "transform  val: " + input.getValue( ) + " class: " + input.getClass().getName( ) );
    try
    {
      if (input instanceof DataObject)
      {
        if (latitudeProperty == null || longitudeProperty == null || geoLocationProperty == null)
        {
          throw new PropertyTransformException( "Cannot transform DataObject - need lat,lon and geoloc properties defined!" );
        }
        // build from lat, lon and altitude? field
        DataObject dobj = (DataObject)input;
        IProperty latProp = dobj.getProperty( latitudeProperty );
        IProperty lonProp = dobj.getProperty( longitudeProperty );
        if (latProp instanceof IQuantity && lonProp instanceof IQuantity )
        {
          GeographicLocation geoLoc = new GeographicLocation( (IQuantity)latProp, (IQuantity)lonProp );
          geoLoc.setName( geoLocationProperty );
          dobj.setProperty( geoLoc );
        }
        else
        {
          throw new PropertyTransformException( "latitude and longitude must be quantities!" );
        }
      }
      else
      {
        GeographicLocation geoProp = new GeographicLocation( );
        geoProp.setValue( input.getValue( ), geoFormat );
        geoProp.setName( input.getName( ) );

        return geoProp;
      }
    }
    catch (PropertyValidationException pve )
    {
      throw new PropertyTransformException( pve.getMessage( ) );
    }
      
    return input;
  }

  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    if (latitudeProperty != null && longitudeProperty != null)
    {
      IProperty latProp = input.getProperty( latitudeProperty );
      IProperty lonProp = input.getProperty( longitudeProperty );
        
      if (latProp instanceof IQuantity && lonProp instanceof IQuantity )
      {
        GeographicLocation geoLoc = new GeographicLocation( (IQuantity)latProp, (IQuantity)lonProp );
        geoLoc.setName( geoLocationProperty );
        input.setProperty( geoLoc );
      }
      else
      {
        throw new PropertyTransformException( "latitude and longitude must be quantities!" );
      }
    }
        
    return input;
  }
 
  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener )
                              throws PropertyTransformException
  {
        
  }
}
