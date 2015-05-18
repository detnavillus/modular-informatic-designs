package com.modinfodesigns.property.schema.soap;

import com.modinfodesigns.utils.FileMethods;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import com.modinfodesigns.property.DataObject;

import junit.framework.TestCase;

import java.util.List;
import java.util.HashMap;

public class TestSOAPDataObjectManager extends TestCase
{
  private static final String weatherWSDL = "WeatherService.wsdl";
  private static final String weatherServiceResponse = "WeatherServiceResponse.xml";
    
  private static final boolean debugConsole = false;
    
  public void testSOAPDataObjectManager( ) throws Exception
  {
    String wsdlString = FileMethods.readFile( weatherWSDL );
      
    SOAPDataObjectManager sdoam = new SOAPDataObjectManager( wsdlString, "Weather", "WeatherSoap" );
      
    assertEquals( sdoam.getServiceAddress( ), "http://wsf.cdyne.com/WeatherWS/Weather.asmx" );
      
    DataObjectSchema doSchema = sdoam.getDataObjectSchema( "GetCityForecastByZIP" );
    assertNotNull( doSchema );
      
    // get the property descriptor
    PropertyDescriptor propDesc = doSchema.getPropertyDescriptor( "ZIP" );
    assertNotNull( propDesc );
      
    DataObjectSchema rSchema = sdoam.getDataObjectSchema( "ForecastReturn" );
    assertNotNull( rSchema );
    List<DataObjectSchema> childSchemas = rSchema.getChildSchemas(  );
    assertNotNull( childSchemas );
    assertEquals( childSchemas.size(), 1 );
  }
    
  public void testCreateDataObject( ) throws Exception
  {
    String wsdlString = FileMethods.readFile( weatherWSDL );
    SOAPDataObjectManager sdoam = new SOAPDataObjectManager( wsdlString, "Weather", "WeatherSoap" );
    
    HashMap<String,String> values = new HashMap<String,String>( );
    values.put( "ZIP", "08840" );
    
    DataObject dobj = sdoam.createDataObject( "GetCityForecastByZIP", values );
    assertEquals( dobj.getValue( ), "{\"ZIP\":\"08840\"}" );
  }
    
  public void testCreateSOAPRequest( ) throws Exception
  {
    String wsdlString = FileMethods.readFile( weatherWSDL );
    SOAPDataObjectManager sdoam = new SOAPDataObjectManager( wsdlString, "Weather", "WeatherSoap" );
      
    HashMap<String,String> values = new HashMap<String,String>( );
    values.put( "ZIP", "08840" );
    
    String soapRequest = sdoam.createSOAPRequest( "GetCityForecastByZIP", values );
    String expected = "<ns1:GetCityForecastByZIP xmlns:ns1=\"http://ws.cdyne.com/WeatherWS/\"><ns1:ZIP>08840</ns1:ZIP></ns1:GetCityForecastByZIP>";
    assertEquals( soapRequest, expected );
  }
    
    
  public void testCreateDataObjectFromResponse(  ) throws Exception
  {
    String wsdlString = FileMethods.readFile( weatherWSDL );
    SOAPDataObjectManager sdoam = new SOAPDataObjectManager( wsdlString, "Weather", "WeatherSoap" );
    String weatherResponse = FileMethods.readFile( weatherServiceResponse );
      
    DataObject responseObj = sdoam.createDataObject( weatherResponse );
    if (debugConsole)
    {
      System.out.println( responseObj.getValue( "JSON" ) );
      System.out.println( responseObj.getValue( "GetCityForecastByZIPResult/City" ));
      System.out.println( responseObj.getValue( "GetCityForecastByZIPResult/State" ));
    }
    assertEquals( responseObj.getValue( "GetCityForecastByZIPResult/City" ), "Metuchen" );
    assertEquals( responseObj.getValue( "GetCityForecastByZIPResult/State" ), "NJ" );
    assertEquals( responseObj.getValue( "GetCityForecastByZIPResult/WeatherStationCity" ), "Somerville" );
  }
    
  public void testEndToEndSOAPRequest( ) throws Exception
  {
    String wsdlString = FileMethods.readFile( weatherWSDL );
    SOAPDataObjectManager sdoam = new SOAPDataObjectManager( wsdlString, "Weather", "WeatherSoap" );
      
    HashMap<String,String> values = new HashMap<String,String>( );
    values.put( "ZIP", "08840" );
      
    DataObject soapResponse = sdoam.executeSOAPRequest( "GetCityForecastByZIP", values );
    if (debugConsole)
    {
      System.out.println( soapResponse.getValue( "JSON" ) );
    }
      
    assertEquals( soapResponse.getValue( "GetCityForecastByZIPResult/City" ), "Metuchen" );
    assertEquals( soapResponse.getValue( "GetCityForecastByZIPResult/State" ), "NJ" );
    assertEquals( soapResponse.getValue( "GetCityForecastByZIPResult/WeatherStationCity" ), "Somerville" );
  }

}