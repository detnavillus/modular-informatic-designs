package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.MappedProperty;
import com.modinfodesigns.property.string.StringProperty;

import junit.framework.TestCase;

public class TestPropertyMapTransform extends TestCase
{

  public void testPropertyMapTransform( ) throws PropertyTransformException
  {
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "yahoo", "Venerable Web Company" ));
    assertEquals( dobj.getValue( ), "{\"yahoo\":\"Venerable Web Company\"}" );
        
    PropertyMapTransform pmt = new PropertyMapTransform( );
    pmt.addPropertyMapping( "yahoosonfirst", "yahoo" );
        
    pmt.transformPropertyHolder( dobj );
        
    assertEquals( dobj.getValue( ), "{\"yahoo\":\"Venerable Web Company\"}" );
        
    // Turning aliasing OFF
    dobj.setProperty( new StringProperty( "yahoo", "Ye Olde Web Company" ));
    assertEquals( dobj.getValue( ), "{\"yahoo\":\"Ye Olde Web Company\"}" );
    pmt.setCreateAliases( false );
        
    pmt.transformPropertyHolder( dobj );
    assertEquals( dobj.getValue( ),"{\"yahoosonfirst\":\"Ye Olde Web Company\"}" );
        
    // ====================================================
    // Test infinite loop
    // ====================================================
    dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "uno", "The First Property" ));
        
    pmt = new PropertyMapTransform( );

    MappedProperty.create( "one", "uno", dobj );
    pmt.addPropertyMapping( "uno", "one" );
        
    pmt.transformPropertyHolder( dobj );
        
    assertEquals( dobj.getValue( ), "{\"uno\":\"The First Property\"}" );
  }

}
