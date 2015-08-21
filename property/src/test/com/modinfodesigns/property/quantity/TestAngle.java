package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.PropertyValidationException;
import junit.framework.TestCase;

public class TestAngle extends TestCase
{
  public void testDegRadianConversion( ) throws PropertyValidationException
  {
    Angle angle = new Angle( );
    angle.setValue( "90.0", "degrees" );
    assertEquals( angle.getValue( "radians" ), "1.5707963267948966" );
    assertEquals( angle.getValue( "degrees" ), "90.0" );
      
    angle = new Angle( );
    angle.setValue( "180.0", "degrees" );
    assertEquals( angle.getValue( ), "3.141592653589793" );
  }
}
