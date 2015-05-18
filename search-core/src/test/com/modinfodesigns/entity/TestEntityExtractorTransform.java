package com.modinfodesigns.entity;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.string.StringProperty;

import junit.framework.TestCase;

/**
 * Test the com.modinfodesigns.entity.EntityExtractorTransform 
 * 
 * Create a DataObject, and and EntityExtractor, put it through the EntityExtractoryTransform
 * 
 * @author Ted Sullivan
 */
public class TestEntityExtractorTransform extends TestCase
{
  public void testEntityExtractorTransform( ) throws Exception
  {
    EntityExtractorTransform eet = new EntityExtractorTransform( );
    TermListEntityExtractor tlee = new TermListEntityExtractor( );
    tlee.addEntityTerm( "foo" );
    tlee.addEntityTerm( "bar" );
    tlee.addEntityTerm( "foobar" );
    tlee.addEntityTerm( "fubar" );
    tlee.addEntityTerm( "clean version" );
        
    eet.setEntityExtractor( tlee );
    eet.setInputProperty( "text" );
    eet.setOutputProperty( "entities" );
        
    DataObject dobj = new DataObject( );
    dobj.addProperty( new StringProperty( "text", "Software programmers like to use foobar and foo, bar to stand  " +
                                                  "for test terms. It is probably a \"clean version\" of the military " +
                                                  "phrase 'fubar' (leave translation to the imagination)." ));

    eet.transformPropertyHolder( dobj );
    System.out.println( dobj.getValue( ) );
  }

}
