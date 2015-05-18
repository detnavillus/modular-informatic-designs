package com.modinfodesigns.property.geolocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;

import com.modinfodesigns.property.quantity.Distance;

// TO DO: Extend Coordinate which is a Property Object with two IQuantity objects ...

public class GeographicLocation implements IProperty, IComputableProperties
{
  private static ArrayList<String> intrinsics;
	
  // No these are types of Angle objects -
  private double latitude;
  private double longitude;
    
  private String name;
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Latitude" );
    intrinsics.add( "Longitude" );
  }
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public String getType()
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue()
  {
    return null;
  }

  @Override
  public String getValue(String format)
  {
    return null;
  }

  @Override
  public void setValue(String value, String format)
  {

  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    GeographicLocation copy = new GeographicLocation( );
    copy.latitude = this.latitude;
    copy.longitude = this.longitude;
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return null;
  }

  @Override
  public List<String> getIntrinsicProperties()
  {
    return intrinsics;
  }

  @Override
  public IProperty getIntrinsicProperty(String name)
  {
    if (name == null) return null;
		
    // Latitude or Longitude
    if (name.equals( "Latitude" ))
    {
			
    }
    else if (name.equals( "Longitude" ))
    {
			
    }
    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    // Distance - GeoLocation
    return null;
  }

  @Override
  public IProperty getComputedProperty( String name, IProperty fromProp)
  {
    // if name = 'Distance' and fromProp instanceof GeoLocation --
    return null;
  }
	
  public Distance getDistance( GeographicLocation another )
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
    String[] values = new String[1];
    values[0] = getValue( format );
    return values;
  }

}
