package com.modinfodesigns.property;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import java.util.Date;

import junit.framework.TestCase;

/**
 * Tests MappedProperty aliasing and Many-to-one mapping.
 * 
 * @author Ted Sullivan
 */

public class TestMappedProperty extends TestCase
{
  public void testMappedProperty( ) throws PropertyValidationException
  {
    // add a property
    // create some aliases
    // change the property: alias values should change
		
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "real", "This is the real deal." ));
    
    MappedProperty.create( "alias", "real", dobj );
      
    assertNotSame( dobj.getProperty( "real" ), dobj.getProperty( "alias" ));
    assertEquals( dobj.getProperty( "real" ).getValue(), dobj.getProperty( "alias" ).getValue( ) );
		
    dobj.setProperty( new StringProperty( "real", "Fooled you!" ));
    assertEquals( dobj.getValue( ), "{\"real\":\"Fooled you!\"}" );
      
    IProperty aliasProp = dobj.getProperty( "alias" );
    assertEquals( aliasProp.getValue( ), "Fooled you!" );
		
    dobj.setProperty( new StringProperty( "real2", "This is the real deja vu." ));
    //assertEquals( dobj.getValue( ), "{\"real2\":\"This is the real deja vu.\",\"real\":\"Fooled you!\"}" );
		
    // switch alias to new property
    MappedProperty.create( "alias", "real2", dobj );
    aliasProp = dobj.getProperty( "alias" );
    assertEquals( aliasProp.getValue( ), "This is the real deja vu." );
		
    // change property aliased to:
    dobj.setProperty( new IntegerProperty( "real2", "32" ));
    aliasProp = dobj.getProperty( "alias" );
    assertEquals( aliasProp.getValue( ), "32" );
      
    // test change real property value
    IntegerProperty intProp = (IntegerProperty)dobj.getProperty( "real2" );
    intProp.increment( );
    assertEquals( aliasProp.getValue( ), "33" );
      
    // mapped property cannot call increment( ) though
    
    // changing alias property changes real property - get/set work
    aliasProp.setValue( "42", null );
    IProperty real2Prop = dobj.getProperty( "real2" );
    assertEquals( real2Prop.getValue( ), "42" );
  }
    
  public void testMappedPropertyError( ) throws Exception
  {
     // set alias to non-existent property - should throw PropertyValidationException  - lookup JUnit test expected Exception
  }
    
  public void testInfiniteLoop( )
  {
    // ====================================================
    // Test infinite loop
    // ====================================================
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "uno", "The First Property" ));
        
    MappedProperty.create( "one", "uno", dobj );
    MappedProperty.create( "uno", "one", dobj );
        
    assertEquals( dobj.getValue( ), "{\"uno\":\"The First Property\"}" );
    IProperty oneProp = dobj.getProperty( "one" );
    IProperty unoProp = dobj.getProperty( "uno" );
    assertTrue( oneProp.equals( unoProp ) == false );
    assertEquals( oneProp.getValue(), unoProp.getValue() );
      
    System.out.println( "Safe Return" );
  }

}
