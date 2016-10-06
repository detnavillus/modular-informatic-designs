package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.transform.json.JSONParserTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.string.StringProperty;

import junit.framework.TestCase;

public class TestJSONParser extends TestCase
{

  public void testJSONParser( ) throws PropertyTransformException
  {
    System.out.println( "testJSONParser ..." );
      
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "name", "{\"bob's\":{\"relation\":\"your uncle\",\"array\":[1,2,3]}}" );
    //StringProperty strprop = new StringProperty( "name", "{\"relation\":\"your uncle\",\"array\":\"my value\",\"foo\":\"bar\"}" );
    IProperty output = jsonParser.transform( strprop );
    assertEquals( output.getClass().getName( ), "com.modinfodesigns.property.DataObject" );
    System.out.println( "output value " + output.getValue( IProperty.JSON_FORMAT ) );
    
      //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<DataObject name=\"bob's\"><PropertyList name=\"array\"><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>1</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>2</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>3</Value></Property></PropertyList><Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>relation</Name><Value>your uncle</Value></Property></DataObject>" );
		
    strprop = new StringProperty( "name", "{{\"relation\":{\"bob's\":\"your uncle\"}},\"array\":[1,2,3]}" );
			
    output = jsonParser.transform( strprop );
    assertEquals( output.getClass().getName( ), "com.modinfodesigns.property.DataObject" );
      
    System.out.println( "output value " + output.getValue( IProperty.JSON_FORMAT ));
    
      //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<DataObject><PropertyList name=\"array\"><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>1</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>2</Value></Property><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>array</Name><Value>3</Value></Property></PropertyList><DataObject name=\"relation\"><Property type=\"com.modinfodesigns.property.string.StringProperty\"><Name>bob's</Name><Value>your uncle</Value></Property></DataObject></DataObject>" );
      
    strprop = new StringProperty( "name", "{\"array\":[{\"val\":1},{\"val\":2},{\"val\":3}],{\"relation\":{\"ted's\":\"your daddy\"}}}" );
    output = jsonParser.transform( strprop );
    assertEquals( output.getClass().getName( ), "com.modinfodesigns.property.DataObject" );
      
    System.out.println( "output value " + output.getValue( IProperty.JSON_FORMAT ));
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
    StringProperty strprop = new StringProperty( "json", "{\"Sender_Internal\":  1}" );
    IProperty output = jsonParser.transform( strprop );
    //assertEquals( output.getValue( IProperty.XML_FORMAT ), "<DataObject><Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>Sender_Internal</Name><Value>1</Value></Property></DataObject>" );
    System.out.println( "Integer dobj is " + output.getValue( IProperty.JSON_FORMAT ));
  }
 

  public void testStringListProperty( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "json", "{\"Type\":[\"text\", \"Subject\"]}" );
    IProperty output = jsonParser.transform( strprop );
    //assertEquals( output.getValue( IProperty.JSON_FORMAT ), "{\"Type\":\"[\"text\",\"Subject\"]\"}" );
      
      System.out.println( "String list property " + output.getValue( IProperty.JSON_FORMAT ));
  }
  

  public void testEmbededBracesJSON( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "json", "{\"content\":\"has some {} chars in it.\"}" );
    IProperty output = jsonParser.transform( strprop );
  }
    
  public void testEmbededQuotesJSON( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strprop = new StringProperty( "json", "{\"content\":\"has some \"quoted strings\" in it.\", \"anotherProp\":\"howdy\" }" );
    IProperty output = jsonParser.transform( strprop );
    System.out.println( output.getValue( ) );
  }
    

  public void testMalformedJSON( ) throws PropertyTransformException
  {
    try {
      JSONParserTransform jsonParser = new JSONParserTransform( );
      StringProperty strprop = new StringProperty( "json", "{{\"content\":\"has some  data\",} \"anotherProp\":\"howdy\"" );
      IProperty output = jsonParser.transform( strprop );
        System.out.println( output );
      //assertTrue( false );   // we shouldn't get here
    }
    catch ( PropertyTransformException pte )
    {
            
    }
  }

    
  public void testListOfObjects( ) throws PropertyTransformException
  {
    JSONParserTransform jsonParser = new JSONParserTransform( );
    StringProperty strProp = new StringProperty( "json", "{\"list\":[{\"foo\":\"foo 1\",\"bar\":\"bar 1\"},{\"foo\":\"foo 2\",\"bar\":\"bar 2\"}]}" );
    IProperty output = jsonParser.transform( strProp );
    System.out.println( output.getValue( ) );
      
    DataObject dobj = (output instanceof DataObject) ? (DataObject)output : null;
    if (dobj == null)
    {
      assertTrue( false );
    }
    else
    {
      IProperty nestedProp = dobj.getProperty( "/list/foo" );
      System.out.println( nestedProp instanceof PropertyList );
      System.out.println( nestedProp.getValue( IProperty.JSON_FORMAT ) );
    }
  }


  public void testStringWithCommas( ) throws PropertyTransformException
  {
      JSONParserTransform jsonParser = new JSONParserTransform( );
      StringProperty strProp = new StringProperty( "json", "{\"list\":[{\"foo\":\"foo 1 with a comma, and another, and another, should skip these\",\"bar\":\"bar 1\"},{\"foo\":\"foo 2\",\"bar\":\"bar 2\"}]}" );
      IProperty output = jsonParser.transform( strProp );
      System.out.println( output.getValue( ) );
      DataObject dobj = (output instanceof DataObject) ? (DataObject)output : null;

      IProperty nestedProp = dobj.getProperty( "/list/foo" );
      System.out.println( nestedProp instanceof PropertyList );
      System.out.println( nestedProp.getValue( IProperty.JSON_FORMAT ) );
  }
    


  public void testMalformedJSON_2( ) throws PropertyTransformException
  {
    try {
      JSONParserTransform jsonParser = new JSONParserTransform( );
      StringProperty strprop = new StringProperty( "json", "{ \"list\":{\"foo\":\"a list\"},{ \"foo\":\"of\"},{\"foo\":\"obj}ects\" }}]" );
      IProperty output = jsonParser.transform( strprop );
      //assertTrue( false );   // we shouldn't get here
    }
    catch ( PropertyTransformException pte )
    {
        
    }
  }

  public void testMalformedJSON_3( ) throws PropertyTransformException
  {
    try {
      JSONParserTransform jsonParser = new JSONParserTransform( );
      StringProperty strprop = new StringProperty( "json", "{ \"list\":{\"foo\":\"a list\"},{ \"foo\":\"of\"},{\"foo\":\"obj{ects\" }}]" );
      IProperty output = jsonParser.transform( strprop );
      //assertTrue( false );   // we shouldn't get here
    }
    catch ( PropertyTransformException pte )
    {
            
    }
  }

}
