package com.modinfodesigns.entity;

import com.modinfodesigns.entity.TermListEntityExtractor;
import com.modinfodesigns.entity.tagging.EntityTaggerStringTransform;

import com.modinfodesigns.property.transform.string.StringTransformException;

import java.util.ArrayList;

import junit.framework.TestCase;

public class TestEntityTaggerStringTransform extends TestCase
{
  public void testEntityTaggerStringTransform( ) throws StringTransformException
  {
    ArrayList<String> entityTerms = new ArrayList<String>( );
    entityTerms.add( "Search" );
		
    TermListEntityExtractor tlee = new TermListEntityExtractor( );
    tlee.setEntityTerms( entityTerms );
		
    EntityTaggerStringTransform etst = new EntityTaggerStringTransform( );
    etst.addEntityExtractor( tlee );
		
    etst.setPrefix( "<b>" );
    etst.setPostfix( "</b>" );
		
    String transformed = etst.transformString( "Why does it highlight search but not Search even though it is case insensitive?" );
    System.out.println( transformed );
  }

}
