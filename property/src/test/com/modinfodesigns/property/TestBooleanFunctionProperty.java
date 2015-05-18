package com.modinfodesigns.property;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.time.DateProperty;

import java.util.Date;

import junit.framework.TestCase;

public class TestBooleanFunctionProperty extends TestCase
{
        
  public void testBooleanFunctionProperty( )
  {
    DataObject parent = new DataObject( );
    DataObject child = new DataObject( );
	
    child.setName( "Horace" );
    child.setProperty( new StringProperty( "age", "22" ) );
    parent.setProperty( child );
		
    BooleanFunctionProperty boolFunProp = new BooleanFunctionProperty( );
    String childNameComp = "Horace/age=22";
    boolFunProp.setFunction( childNameComp );
    assertEquals( parent.getValue( "JSON" ), "{\"Horace\":{\"age\":\"22\"}}" );
    assertTrue( boolFunProp.equals( null, parent ) );

    childNameComp = "Horace.type contains String";
    boolFunProp.setFunction( childNameComp );
    assertTrue( boolFunProp.equals( null, parent ) );

    DataObject addressObj = new DataObject( );
    addressObj.setName( "address" );
    addressObj.setProperty(  new StringProperty( "StreetAddress", "123 Maple St." ));
    addressObj.setProperty(  new StringProperty( "ZipCode", "12345" ));
    parent.setProperty( addressObj );

    childNameComp = "address/StreetAddress starts-with 123";
    boolFunProp.setFunction( childNameComp );
    assertTrue( boolFunProp.equals( null, parent ) );
		
    childNameComp = "address/StreetAddress starts-with 123 AND address/ZipCode starts-with 123";
    boolFunProp.setFunction( childNameComp );
    assertTrue(  boolFunProp.equals( null, parent ) );
  }
    
  public void testAgeFunction( )
  {
    DataObject dObj = new DataObject( );
    dObj.setProperty( new DateProperty( "Birth Date", "05/12/99", "MM/dd/yy"));
    MappedProperty.create( "Age", "Birth Date.Duration", dObj );
      
    // get now date
    // calculate age in years to now
    // assertEquals( calculated age, property age)
    Date now = new Date( );
    int theYear = now.getYear(  ) + 1900;
    System.out.println( theYear );
    int realAge = theYear - 1999;
    assertEquals( Integer.toString( realAge ), dObj.getProperty( "Age" ).getValue( "years" ));
		
    BooleanFunctionProperty boolFunProp = new BooleanFunctionProperty( );
    boolFunProp.setFunction( "Birth Date < Date(09/06/1954)" );
		
    assertFalse( boolFunProp.equals( null, dObj ) );
  }

}
