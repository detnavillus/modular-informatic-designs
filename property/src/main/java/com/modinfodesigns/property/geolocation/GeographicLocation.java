package com.modinfodesigns.property.geolocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.PropertyValidationException;

import com.modinfodesigns.property.quantity.Distance;
import com.modinfodesigns.property.quantity.IQuantity;

// TO DO: Extend Coordinate which is a Property Object with two IQuantity objects ...

/**
 Formats: From Wikipedia
 
 [-]d.d, [-]d.d                 Decimal degrees with negative numbers for South and West.	12.3456, -98.7654
 d° m.m′ {N|S}, d° m.m′ {E|W}	Degrees and decimal minutes with N, S, E or W suffix for North, South, East, West	12° 20.736′ N, 98° 45.924′ W
 {N|S} d° m.m′ {E|W} d° m.m′	Degrees and decimal minutes with N, S, E or W prefix for North, South, East, West	N 12° 20.736′, W 98° 45.924′
 d° m' s" {N|S}, d° m' s" {E|W}	Degrees, minutes and seconds with N, S, E or W suffix for North, South, East, West	12° 20' 44" N, 98° 45' 55" W
 {N|S} d° m' s", {E|W} d° m' s" Degrees, minutes and seconds with N, S, E or W prefix for North, South, East, West	N 12° 20' 44", W 98° 45' 55"
 
 Could also be ZIP CODE or AREA Code - get centroid
 
 Note negative longitudes are W and positive longitudes are E
      negative latitudes are S and positive latitudes are N
 
 Longitude goes from -180.0 to 180.0 Degrees
 Latitude goes from -90.0 to 90.0 Degrees
 */

public class GeographicLocation implements IProperty, IComputableProperties
{
  private static ArrayList<String> intrinsics;
	
  // No these are types of Angle objects -
  private Latitude  latitude;
  private Longitude longitude;
    
  private Distance altitude;
    
  private String separator = ",";
    
  private String name;
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Latitude" );
    intrinsics.add( "Longitude" );
  }
    
  public GeographicLocation( ) {  }
    
  public GeographicLocation( Latitude latitude, Longitude longitude )
  {
    this.latitude = latitude;
    this.longitude = longitude;
  }
    
  public GeographicLocation( double latitude, double longitude )
  {
    try
    {
      this.latitude = new Latitude( latitude );
      this.longitude = new Longitude( longitude );
    }
    catch ( PropertyValidationException pve )
    {
          
    }
  }
    
  public GeographicLocation( String latitude, String longitude )
  {
    try
    {
      this.latitude = new Latitude( );
      this.latitude.setValue( latitude, "degrees" );
            
      this.longitude = new Longitude( );
      this.longitude.setValue( longitude, "degrees" );
    }
    catch (PropertyValidationException pve )
    {

    }
  }
    
  public GeographicLocation( IQuantity latitude, IQuantity longitude )
  {
    try
    {
      this.latitude = new Latitude( latitude );
      this.longitude = new Longitude( longitude );
    }
    catch (PropertyValidationException pve )
    {
          
    }
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
    return getValue( "," );
  }

  @Override
  public String getValue(String format)
  {
    if (format != null && format.indexOf( "," ) > 0)
    {
      String[] parts = format.split( "[\\w,:;/]" );
      return new String( latitude.getValue( parts[0] ) + separator + longitude.getValue( parts[1] ));
    }

    return new String( latitude.getValue( ) + separator + longitude.getValue( ) );
  }

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    if (format == null || format.equals( "degrees" ))
    {
      String[] parts = value.split( "[\\w,:;/]" );
      this.latitude  = new Latitude( );
      latitude.setValue( parts[0], "degrees" );
      this.longitude = new Longitude( );
      longitude.setValue( parts[1], "degrees" );
    }
  }

  @Override
  public String getDefaultFormat()
  {
    return "lat,lon";
  }

  @Override
  public IProperty copy()
  {
    GeographicLocation copy = new GeographicLocation( );
    copy.latitude  = (Latitude)this.latitude.copy( );
    copy.longitude = (Longitude)this.longitude.copy( );
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
        return latitude;
    }
    else if (name.equals( "Longitude" ))
    {
        return longitude;
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
