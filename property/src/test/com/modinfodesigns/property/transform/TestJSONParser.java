package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import junit.framework.TestCase;

public class TestJSONParser extends TestCase
{

  public void testJSONParser( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "name", "{\"bob's\":{\"relation\":\"your uncle\",\"array\":[1,2,3]}}" );
		
    IProperty output = jsonParser.transform( strprop );
    System.out.println( output.getClass().getName() );
    System.out.println( output.getValue( IProperty.XML_FORMAT ) );
		
    strprop = new StringProperty( "name", "{{\"relation\":{\"bob's\":\"your uncle\"}},\"array\":[1,2,3]}" );
			
    output = jsonParser.transform( strprop );
    System.out.println( output.getClass().getName() );
    System.out.println( output.getValue( IProperty.XML_FORMAT ) );
  }
}
