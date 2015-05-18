package com.modinfodesigns.utils;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;


public class TestDOMMethods extends TestCase
{
  public void testGetDocument(  ) throws IOException {
    String filename = "simple.xml";
      
    InputStream xmlStream = FileResourceLoader.getResourceAsStream( filename );
    Document doc = DOMMethods.getDocument( xmlStream );
    assertNotNull( doc );
      
    assertEquals( doc.getDocumentElement().getTagName(), "simple" );
  }
    
  public void testNestedElement( ) throws IOException {
    String filename = "simple.xml";
      
    InputStream xmlStream = FileResourceLoader.getResourceAsStream( filename );
    Document doc = DOMMethods.getDocument( xmlStream );
    assertNotNull( doc );
      
    assertEquals( doc.getDocumentElement().getTagName(), "simple" );
      
    NodeList fooLst = doc.getElementsByTagName( "foo" );
    assertEquals( fooLst.getLength(), 1 );
  }
    
  public void testGetXML( ) throws IOException {
    String filename = "simple.xml";
      
    InputStream xmlStream = FileResourceLoader.getResourceAsStream( filename );
    Document doc = DOMMethods.getDocument( xmlStream );
      String resp = DOMMethods.getXML( doc.getDocumentElement(), false ).trim();
      System.out.println( "'" + resp + "'" );
    assertEquals( resp , simpleXML );
  }
    
  private static String simpleXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><simple><foo/></simple>";
    
    
}