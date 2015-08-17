package com.modinfodesigns.property;

import com.modinfodesigns.property.CompositeProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.string.RegularExpressionProperty;
import com.modinfodesigns.property.string.StringProperty;

import java.util.ArrayList;

import junit.framework.TestCase;

public class TestCompositeProperty extends TestCase
{

  public void testCompositeProperty(  ) throws PropertyValidationException
  {
    ArrayList<IProperty> components = new ArrayList<IProperty>( );
    RegularExpressionProperty firstPart = new RegularExpressionProperty( "FirstPart", "\\d\\d\\d" );
    components.add( firstPart );
    StringProperty dashProp = new StringProperty( "dash", "-" );
    components.add( dashProp );
    RegularExpressionProperty secondPart = new RegularExpressionProperty( "SecondPart", "\\d\\d" );
    components.add( secondPart );
    components.add( dashProp );
    RegularExpressionProperty thirdPart = new RegularExpressionProperty( "ThirdPart", "\\d\\d\\d\\d" );
    components.add( thirdPart );
        
    CompositeProperty ssnProp = new CompositeProperty( components, "-" );
    assertEquals( ssnProp.getDefaultFormat( ), "\\d\\d\\d||\\d\\d||\\d\\d\\d\\d" );
      
    /*ssnProp.setValue( "123-45-4567", null );
        
    assertEquals( "123-45-4567", ssnProp.getValue( ) );
    IProperty firstPartOut = ssnProp.getIntrinsicProperty( "FirstPart" );
    if (firstPartOut != null)
    {
      // change to assertEquals
      System.out.println( "First Part = '" + firstPartOut.getValue( ) + "'" );
    }
      

    try
    {
      ssnProp.setValue( "foo-ba-rrrr", null );
    }
    catch ( PropertyValidationException pve )
    {
      // test that this throws an exception
    }

        
    // Construct using addComponent method
        
    CompositeProperty ssnProp2 = new CompositeProperty( );
    ssnProp2.addComponent( firstPart );
    ssnProp2.addComponent( dashProp );
    ssnProp2.addComponent( secondPart );
    ssnProp2.addComponent( dashProp );
    ssnProp2.addComponent( thirdPart );
        
    try
    {
      ssnProp2.setValue( ssnProp.getValue( ), "" );
    }
    catch ( PropertyValidationException pve )
    {
      fail( );
    }
        
    System.out.println( ssnProp2.getValue( ) );
         */
  }

}
