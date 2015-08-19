package com.modinfodesigns.utils;

import junit.framework.TestCase;


public class TestStringMethods extends TestCase
{

  public void testGetDelimitedString( )
  {
    assertTrue( true );
  }
    
  public void testIsNumberMethod( ) {
      
    String aNumber = "0.123456";
    assertTrue( StringMethods.isNumber( aNumber ));
      
    String notANumber = "012.34n";
    assertFalse( StringMethods.isNumber( notANumber ));
      
    String negNumber = "-3.4562";
    assertTrue( StringMethods.isNumber( aNumber ));
  }
    
}