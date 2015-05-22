package com.modinfodesigns.property;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.time.DateProperty;

import junit.framework.TestCase;

public class TestDataObjectAsMap extends TestCase
{

  public void testDataObjectAsMap( )
  {
    DataObject dobj = new DataObject( );
		
    dobj.put( "foo", "bar" );
    assertEquals( dobj.getValue( ), "{\"foo\":\"bar\"}" );
		
    Object foobj = dobj.get( "foo" );
    assertEquals( foobj.toString(), "bar" );
    assertEquals( foobj.getClass().getName( ), "java.lang.String" );
		
    DataObject childObj = new DataObject( );
    childObj.setName( "child" );
    childObj.put( "baz", "bat" );
    childObj.setProperty( new IntegerProperty( "num", 42 ));
    childObj.setProperty( new DateProperty( "date", "06/04/2013", DateProperty.DEFAULT_FORMAT ));
		
    dobj.setProperty( childObj );
    System.out.println( dobj.getValue( ) );
    // assertEquals( dobj.getValue( ), "<[<{\"[child\":{\"num\":\"42\",\"baz\":\"bat\",\"date\":\"06/04/2013\"},\"foo\":\"bar\"]}>]>" );
      
    Object chob = dobj.get( "child" );
    assertNotNull( chob );
    assertEquals( chob.getClass().getName( ), "com.modinfodesigns.property.DataObject" );
    
    IProperty prop = dobj.getProperty( "child/baz" );
    assertEquals( prop.getClass().getName( ), "com.modinfodesigns.property.string.StringProperty" );
        
    Object obj = childObj.get( "baz" );
    assertEquals( obj.getClass().getName( ), "java.lang.String" );
        
    obj = childObj.get( "date" );
    assertEquals( obj.getClass().getName( ), "java.util.Date" );
    
    obj = dobj.getProperty( "child/date" );
    assertEquals( obj.getClass().getName(), "com.modinfodesigns.property.time.DateProperty" );
      
    obj = childObj.get( "num" );
    assertEquals( obj.getClass().getName(), "java.lang.Integer" );
      
    obj = dobj.getProperty( "child/num" );
    assertEquals( obj.getClass().getName(), "com.modinfodesigns.property.quantity.IntegerProperty" );
  }
    
  public void testMapFunction( )
  {
    // childObj.put( "age", "map(date.Duration)" );
        
    // System.out.println( childObj.getProperty( "age" ).getValue( ) );
  }

}
