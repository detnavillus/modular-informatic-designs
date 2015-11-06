package com.modinfodesigns.property;

import junit.framework.TestCase;

/**
 * Test setValue( value, format ) methods of DataObject and DataList using XML and JSON Strings.
 * 
 * @author Ted Sullivan
 */
public class TestDataObjectSetValue extends TestCase
{
  public void testSetJSONValue( ) throws PropertyValidationException
  {
    DataObject nameData = new DataObject( );
    nameData.setValue( person, "JSON" );
    System.out.println( "Name Data = " + nameData.getValue( ) );
      
   /* IProperty firstnameprop = nameData.getProperty( "firstName" );
    assertEquals( firstnameprop.getValue( ), "John" );
      
    IProperty phoneprop = nameData.getProperty( "phoneNumber" );
    System.out.println( phoneprop.getValue( ) );
    assertEquals( phoneprop.getType( ), PropertyList.class.getCanonicalName() );
      
    PropertyList pl = (PropertyList)phoneprop;
    assertEquals( pl.size(), 2 );
     
    assertEquals( nameData.getValue( "phoneNumber[0]/type" ), "home" );
    assertEquals( nameData.getValue( "phoneNumber[1]/number" ), "646 555-4567" );
      
    assertEquals( nameData.getValue( "address/city"), "New York" );
    assertEquals( nameData.getValue( "address/postalCode"), "10021" ); */
  }
    
  public void testSetXMLValue( ) throws PropertyValidationException
  {
    DataObject nameData = new DataObject( );
    nameData.setValue( personXML, "XML" );
      
    IProperty firstnameprop = nameData.getProperty( "firstName" );
    assertEquals( firstnameprop.getValue( ), "John" );
      
    assertEquals( nameData.getValue( "phoneNumber[0]/type" ), "home" );
    assertEquals( nameData.getValue( "phoneNumber[1]/number" ), "646 555-4567" );
      
    assertEquals( nameData.getValue( "address/city"), "New York" );
    assertEquals( nameData.getValue( "address/postalCode"), "10021" );
  }
	
    private static String person = "{\"firstName\":\"John\"}";
    
  /*private static String person = "{"
                                 + "  \"firstName\": \"John\","
                                 + "  \"lastName\": \"Smith\","
                                 + "  \"age\":\"25\","
                                 + "  \"address\": {"
                                 + "  \"streetAddress\": \"21 2nd Street\","
                                 + "  \"city\": \"New York\","
                                 + "  \"state\": \"NY\","
                                 + "  \"postalCode\":\"10021\""
                                 + "  \"phoneNumber\": ["
                                 + "  {"
                                 + "    \"type\": \"home\","
                                 + "    \"number\": \"212 555-1234\""
                                 + "  },"
                                 + "  {"
                                 + "    \"type\": \"fax\","
                                 + "    \"number\": \"646 555-4567\""
                                 + "  }"
                                 + "]"
                                 + "}";*/
    
  private static String personXML = "<record firstName=\"John\" lastName=\"Smith\" age=\"25\" >"
                                  + "<address streetAddress=\"21 2nd Street\" city=\"New York\" state=\"NY\" postalCode=\"10021\" />"
                                  + "<phoneNumber type=\"home\" number=\"212 555-1234\" />"
                                  + "<phoneNumber type=\"fax\" number=\"646 555-4567\" />"
                                  + "</record>";
}