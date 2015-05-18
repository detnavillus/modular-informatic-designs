package com.modinfodesigns.property;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.PropertyMapTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import junit.framework.TestCase;

/**
 * MappedProperty "network"s needs to be an open graph.  Test that closed 
 * graphs cannot be created.
 * 
 * @author Ted Sullivan
 */

public class TestMappedPropertyGraph extends TestCase
{
  public void testMappedPropertyGraph( ) throws Exception
  {
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "base", "This is ground zero." ));
    MappedProperty.create( "second", "base", dobj );
    MappedProperty.create( "third", "second", dobj );
        
    assertEquals( dobj.getValue( ), "{\"base\":\"This is ground zero.\"}" );
        
    IProperty baseProp = dobj.getProperty( "base" );
    assertEquals( baseProp.getValue( ), "This is ground zero." );
    IProperty mappedProp = dobj.getProperty( "second" );
    assertEquals( mappedProp.getValue( ), "This is ground zero." );
    mappedProp = dobj.getProperty( "third" );
    assertEquals( mappedProp.getValue( ), "This is ground zero." );
  }
    
  public void testCircularMapping( ) throws Exception
  {
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "base", "This is ground zero." ));
    MappedProperty.create( "second", "base", dobj );
    MappedProperty.create( "third", "second", dobj );
    MappedProperty.create( "base", "third", dobj );
        
    IProperty mappedProp = dobj.getProperty( "second" );
    assertEquals( mappedProp.getValue( ), "This is ground zero." );
  }
    
  public void testPropertyMapTransform( )  throws PropertyTransformException
  {
    // Try to create it with the PropertyMapTransform
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "base", "This is ground zero." ));
    MappedProperty.create( "second", "base", dobj );
      
    PropertyMapTransform pmt = new PropertyMapTransform( );
    pmt.addPropertyMapping( "third", "second" );
        
    pmt.transformPropertyHolder( dobj );
        
    IProperty mappedProp = dobj.getProperty( "third" );
    assertEquals( mappedProp.getValue( ), "This is ground zero." );
  }
    
    
  public void testOneTransformSession( ) throws PropertyTransformException
  {
    // Try to create it with the PropertyMapTransform in one transform session
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "base", "This is ground zero." ));
      
    PropertyMapTransform pmt = new PropertyMapTransform( );
    pmt.addPropertyMapping( "second", "base" );
    pmt.addPropertyMapping( "third", "second" );
    pmt.addPropertyMapping( "base", "third" );

    pmt.transformPropertyHolder( dobj );
        
    IProperty mappedProp = dobj.getProperty( "third" );
    assertEquals( mappedProp.getValue( ), "This is ground zero." );
  }

}
