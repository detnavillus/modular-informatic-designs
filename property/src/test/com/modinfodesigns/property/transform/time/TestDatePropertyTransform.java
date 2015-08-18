package com.modinfodesigns.property.transform.time;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.string.StringProperty;

import junit.framework.TestCase;

public class TestDatePropertyTransform extends TestCase
{
  public void testDatePropTransform( ) throws PropertyTransformException
  {
    StringProperty inputProp = new StringProperty( "DateTime", "2015/08/10 13:08:33" );
      
    DatePropertyTransform dpt = new DatePropertyTransform( );
    dpt.setDateFormat( "yyyy/MM/dd HH:mm:ss" );
    IProperty outputProp = dpt.transform( inputProp );
      
    assertEquals( outputProp.getClass( ).getName( ), "com.modinfodesigns.property.time.DateProperty" );
    assertEquals( outputProp.getValue( "MMM dd, yyyy" ), "Aug 10, 2015" );
  }
}
