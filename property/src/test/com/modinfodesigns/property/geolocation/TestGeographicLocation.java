package com.modinfodesigns.property.geolocation;

import com.modinfodesigns.property.PropertyValidationException;

import junit.framework.TestCase;

public class TestGeographicLocation extends TestCase
{
  public void testLatLon(  ) throws PropertyValidationException
  {
    GeographicLocation geoloc = new GeographicLocation( );
    geoloc.setValue( "12.3456,45.6789", "lat,lon" );
        
  }
}
