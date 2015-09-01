package com.modinfodesigns.property.transform.xml;

import com.modinfodesigns.property.DataObject;

import junit.framework.TestCase;

public class TestXMLParserTransform extends TestCase
{
    
  public void testXMLParserSimpleXML(  )
  {
    XMLParserTransform xmlParserXform = new XMLParserTransform( );
    DataObject dobj = xmlParserXform.createDataObject( simpleXML );
    //assertEquals( dobj.getName( ), "root" );
    //assertEquals( dobj.getValue( ), "{\"attr\":\"top dog\",\"child\":{\"name\":\"child1\",\"text\":\"[\"Child 1 text\"]\"}}" );
      
    DataObject childObj = (DataObject)dobj.getProperty( "child" );
    //assertEquals( childObj.getValue( ), "{\"name\":\"child1\",\"text\":\"[\"Child 1 text\"]\"}" );
      
    String text = childObj.getProperty( "text" ).getValue( );
    //assertEquals( text, "Child 1 text" );
  }
    
  public void testXMLParserRdfOwl(  )
  {
    XMLParserTransform xmlParserXform = new XMLParserTransform( );
    DataObject dobj = xmlParserXform.createDataObject( wreckingCrewOwl );
    //assertEquals( dobj.getValue( ), "{\"rdfs:label\":{\"text\":\"[\"The Wrecking Crew\"]\"},\"rdf:type\":{\"rdf:resource\":\"&music-10;Band\"},\"rdf:about\":\"&music-10;The_Wrecking_Crew\"}" );
  }
    
  private static final String simpleXML = "<root attr=\"top dog\"><child name=\"child1\">Child 1 text</child></root>";
    
  private static final String wreckingCrewOwl = "<owl:NamedIndividual rdf:about=\"&amp;music-10;The_Wrecking_Crew\">"
                                              + "<rdf:type rdf:resource=\"&amp;music-10;Band\"/><rdfs:label>The Wrecking Crew</rdfs:label></owl:NamedIndividual>";
}