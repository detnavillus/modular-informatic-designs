package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.utils.FileMethods;

import junit.framework.TestCase;

public class TestComplexJSONParser extends TestCase
{
  private static final String complexJSON = "test.json";
    

  public void testJSONParser( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
      
    String jsonString = FileMethods.readFile( complexJSON );
    StringProperty strprop = new StringProperty( "json", jsonString );
		
    IProperty output = jsonParser.transform( strprop );
    System.out.println( output.getValue( ) );
  }
}
