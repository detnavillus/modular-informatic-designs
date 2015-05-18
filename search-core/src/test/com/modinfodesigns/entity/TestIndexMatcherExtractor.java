package com.modinfodesigns.entity;

import com.modinfodesigns.entity.tagging.EntityTaggerStringTransform;

import com.modinfodesigns.classify.PhraseIndexMatcher;
import com.modinfodesigns.classify.TermIndexMatcher;
import com.modinfodesigns.classify.NearIndexMatcher;

import com.modinfodesigns.property.transform.string.StringTransformException;

import junit.framework.TestCase;

/**
 * Tests the EntityTaggerStringTransform to see if it will only
 * Tag elements within an IndexMatcher's match 'zone'.  To test this,
 * set up an NearIndexMatcher where some of the internal matches are near
 * and some are not. The EntityTagger should only tag the components that
 * meet the NearIndexMatcher match criteria.
 */
public class TestIndexMatcherExtractor extends TestCase
{
  public void testIndexMatcherExtractor( ) throws StringTransformException
  {
    PhraseIndexMatcher phraseMatcher = new PhraseIndexMatcher( "come to the aid of the party" );
		
    IndexMatcherEntityExtractor imee = new IndexMatcherEntityExtractor( );
    imee.setPrefixString( "<div name=\"foo\">" );
    imee.setPostfixString( "</div>" );
    imee.setIndexMatcher( phraseMatcher );
		
    EntityTaggerStringTransform etst = new EntityTaggerStringTransform( );
    etst.addEntityExtractor( imee );

    String markedUp = etst.transformString( testString );
    System.out.println( "\r\n\r\n PHRASE MARKUP \r\n" + markedUp + "\r\n" );
		
    // Now make a NearIndexMatcher
    NearIndexMatcher nearMatcher = new NearIndexMatcher( );
    nearMatcher.addIndexMatcher( phraseMatcher );
    PhraseIndexMatcher anotherPhrase = new PhraseIndexMatcher( "good men" );
    nearMatcher.addIndexMatcher( anotherPhrase );
    nearMatcher.setMinimumDistance( 3 );
		
    imee.setIndexMatcher( nearMatcher );
		
    // Now try another one in a new tag style
    TermIndexMatcher termMatcher = new TermIndexMatcher( "tagger" );
    IndexMatcherEntityExtractor tmee = new IndexMatcherEntityExtractor( );
    tmee.setIndexMatcher( termMatcher );
    tmee.setPrefixString( "<font color=\"red\">" );
    tmee.setPostfixString( "</font>" );
    etst.addEntityExtractor( tmee );

    markedUp = etst.transformString( testString );
    System.out.println( "\r\n\r\n NEAR MARKUP \r\n" + markedUp );
  }
	
  private static String testString = "All good men should come to the aid of the party. Don't really know what\r\n"
                                   + "this means but its a start. Now try another come to the aid of the party phrase. \r\n"
                                   + "This second one should not be marked up by the near tagger because it is not near \r\n"
                                   + "the other phrase good men.  Both instances should be marked up by the phrase tagger though.";

}
