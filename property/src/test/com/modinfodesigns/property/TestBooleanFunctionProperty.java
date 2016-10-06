package com.modinfodesigns.property;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.time.DateProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

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
    
  public void testPropertyListContains( )
  {
    System.out.println( "testPropertyListContains( )..." );
    DataObject parent = new DataObject( );
    
    DataObject child = new DataObject( );
    child.setName( "children" );
    child.setProperty( new StringProperty( "firstname", "Orion" ));
    child.setProperty( new IntegerProperty( "age", 4 ));
    parent.addProperty( child );
      
    String childContainsComp = "children contains (firstname EQ Orion)";
    BooleanFunctionProperty boolFunProp = new BooleanFunctionProperty( );
    boolFunProp.setFunction( childContainsComp );
    assertTrue( boolFunProp.equals( null, parent ));
      
    child = new DataObject( );
    child.setName( "children" );
    child.setProperty( new StringProperty( "firstname", "Lynx" ));
    child.setProperty( new IntegerProperty( "age", 1 ));
    parent.addProperty( child );
    
    Object chobj = parent.getProperty( "children" );
    System.out.println( "chobj is a " + chobj.getClass().getName() );
      
    childContainsComp = "children contains (firstname EQ Lynx AND age == 1)";
    boolFunProp.setFunction( childContainsComp );
    assertTrue( boolFunProp.equals( null, parent ));
      
    childContainsComp = "children matches ((firstname EQ Orion OR firstname == Lynx) AND (age <= 4))";
    boolFunProp.setFunction( childContainsComp );
    assertTrue( boolFunProp.equals( null, parent ));
  }
    
  public void testPropertyListContainsPath( )
  {
    DataObject parent = new DataObject( );
      
    DataObject child = new DataObject( );
    child.setName( "children" );
    child.setProperty( new StringProperty( "firstname", "Meghan" ));
    parent.addProperty( child );
      
    DataObject grandchild = new DataObject( );
    grandchild.setName( "children" );
    grandchild.setProperty( new StringProperty( "firstname", "Orion" ) );
    grandchild.setProperty( new IntegerProperty( "age", 4 ) );
    child.addProperty( grandchild );
      
    grandchild = new DataObject( );
    grandchild.setName( "children" );
    grandchild.setProperty( new StringProperty( "firstname", "Lynx" ) );
    grandchild.setProperty( new IntegerProperty( "age", 1 ) );
    child.addProperty( grandchild );
      
    child = new DataObject( );
    child.setName( "children" );
    child.setProperty( new StringProperty( "firstname", "Timothy" ) );
    parent.addProperty( child );
      
    grandchild = new DataObject( );
    grandchild.setName( "children" );
    grandchild.setProperty( new StringProperty( "firstname", "TBD" ));
    grandchild.setProperty( new IntegerProperty( "age", -2 ) );
    child.addProperty( grandchild );
      
    grandchild = new DataObject( );
    grandchild.setName( "children" );
    grandchild.setProperty( new StringProperty( "firstname", "whatsitsname" ));
    grandchild.setProperty( new IntegerProperty( "age", -5 ) );
    child.addProperty( grandchild );
      
    IProperty prop = parent.getProperty( "children/children" );
    System.out.println( "got grandchildren prop " + prop );
      
    BooleanFunctionProperty boolFunProp = new BooleanFunctionProperty( );
    String grandchildComp = "children/children contains (firstname == Lynx AND age == 1)";
    boolFunProp.setFunction( grandchildComp );
    assertTrue( boolFunProp.equals( null, parent ));
      
    grandchildComp = "children/children matches (firstname == Lynx AND age == 1)";
    boolFunProp.setFunction( grandchildComp );
    assertFalse( boolFunProp.equals( null, parent ));
  }
    
  public void testListContainsStringProperty(  )
  {
    DataObject parent = new DataObject( );
    parent.addProperty( new StringProperty( "foo", "bar" ));
    parent.addProperty( new StringProperty( "foo", "baz" ));
    parent.addProperty( new StringProperty( "foo", "bat" ));
    parent.addProperty( new StringProperty( "foo", "bim" ));
      
    BooleanFunctionProperty boolFun = new BooleanFunctionProperty( );
    boolFun.setFunction( "foo contains bar" );
    assertTrue( boolFun.equals( null, parent ));
      
    boolFun.setFunction( "foo contains bam" );
    assertFalse( boolFun.equals( null, parent ));
      
    boolFun.setFunction( "foo contains bar AND foo contains bat" );
    assertTrue( boolFun.equals( null, parent ));
  }
    
  public void testMissingProperty( )
  {
    DataObject dobj = new DataObject( );
    dobj.setName( "AnObject" );
    dobj.setProperty( new StringProperty( "foo", "bar" ));
      
    BooleanFunctionProperty boolFun = new BooleanFunctionProperty( );
    String comp = "foo == bar AND baz EQ bat";
    boolFun.setFunction( comp );
    assertFalse( boolFun.equals( null, dobj ));
      
    // Test Short circuiting
    comp = "foo == bar OR baz == bat";
    boolFun.setFunction( comp );
    assertTrue( boolFun.equals( null, dobj ));
      
    // reverse OR should be equivalent
    comp = "baz == bat OR foo == bar";
    boolFun.setFunction( comp );
    assertTrue( boolFun.equals( null, dobj ));
      
    comp = "foo EQ bar AND baz EQ bat";
    boolFun.setFunction( comp );
    dobj.setProperty( new StringProperty( "baz", "bat" ));
    assertTrue( boolFun.equals( null, dobj ));
      
    comp = "(((foo EQ bar OR baz EQ bat) OR (badabing == badaboom)))";
    boolFun.setFunction( comp );
    assertTrue( boolFun.equals( null, dobj ));
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
    // System.out.println( theYear );
    int realAge = theYear - 1999;
    assertEquals( Integer.toString( realAge ), dObj.getProperty( "Age" ).getValue( "years" ));
		
    BooleanFunctionProperty boolFunProp = new BooleanFunctionProperty( );
    boolFunProp.setFunction( "Birth Date < Date(09/06/1954)" );
		
    assertFalse( boolFunProp.equals( null, dObj ) );
  }

}
