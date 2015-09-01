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
    assertEquals( output.getClass().getName( ), "com.modinfodesigns.property.DataObject" );
    //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<DataObject name=\"bob's\"><PropertyList name=\"array\"><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>1</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>2</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>3</Value></Property></PropertyList><Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>relation</Name><Value>your uncle</Value></Property></DataObject>" );
		
    strprop = new StringProperty( "name", "{{\"relation\":{\"bob's\":\"your uncle\"}},\"array\":[1,2,3]}" );
			
    output = jsonParser.transform( strprop );
    assertEquals( output.getClass().getName( ), "com.modinfodesigns.property.DataObject" );
    //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<DataObject><PropertyList name=\"array\"><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>1</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>2</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>3</Value></Property></PropertyList><DataObject name=\"relation\"><Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>bob's</Name><Value>your uncle</Value></Property></DataObject></DataObject>" );

  }
    
  public void testJSONParserWComplexQuotes( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "json", "{\"Category\": [], \"Name\": \"\\\"Smith, Tom X\\\"\", \"IntExt\": \"Internal\", \"Email\": \"tom.x.smith@company.com\"}" );
    IProperty output = jsonParser.transform( strprop );
    //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<DataObject><PropertyList name=\"Category\"></PropertyList><Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>Email</Name><Value>tom.x.smith@company.com</Value></Property><Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>IntExt</Name><Value>Internal</Value></Property><Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>Name</Name><Value>\\\"Smith, Tom X\\\"</Value></Property></DataObject>" );

  }
    
  public void testIntegerProperty( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "json", "{\"Sender_Internal\": 1}" );
    IProperty output = jsonParser.transform( strprop );
    //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<DataObject><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>Sender_Internal</Name><Value>1</Value></Property></DataObject>" );
  }
    
  public void testStringListProperty( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "json", "{\"Type\":[\"text\", \"Subject\"]}" );
    IProperty output = jsonParser.transform( strprop );
    //assertEquals( output.getValue( IProperty.JSON_FORMAT ), "{\"Type\":\"[\"text\",\"Subject\"]\"}" );
  }
}
