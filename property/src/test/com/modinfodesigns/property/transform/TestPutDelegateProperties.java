package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.PutDelegateProperties;
import com.modinfodesigns.property.transform.PropertyTransformException;

import junit.framework.TestCase;

public class TestPutDelegateProperties extends TestCase
{
  public void testPutDelegateProperties( )
  {
    DataObject hereObj = new DataObject( );
    hereObj.setName( "Origin" );
      
    DataObject thereObj = new DataObject( );
    thereObj.setName( "Destination" );
      
    DataObjectDelegate dobjd = new DataObjectDelegate( thereObj );
    dobjd.setName( "Edge" );
    hereObj.addProperty( dobjd );
      
    hereObj.addProperty( new StringProperty( "place", "here" ));
    hereObj.addProperty( new StringProperty( "time", "now" ));
    hereObj.addProperty( new StringProperty( "who", "we few" ));
      
    assertEquals( hereObj.getValue( IProperty.JSON_FORMAT ), "{\"place\":\"here\",\"time\":\"now\",\"Edge\":{},\"who\":\"we few\"}" );
      
    PutDelegateProperties pdp = new PutDelegateProperties( );
    pdp.setDelegateField( "Edge" );
    pdp.setSourceField( "place" );
    pdp.setTargetField( "location" );
      
    try
    {
      pdp.transformPropertyHolder( hereObj );
    }
    catch (PropertyTransformException pte )
    {
      assertTrue( false );
    }
      
    assertEquals( thereObj.getValue( IProperty.JSON_FORMAT ), "{\"location\":\"here\"}" );
    assertEquals( hereObj.getValue( IProperty.JSON_FORMAT ), "{\"place\":\"here\",\"time\":\"now\",\"Edge\":{\"location\":\"here\"},\"who\":\"we few\"}" );
      
  }
}