package com.modinfodesigns.entity;

import java.util.Iterator;
import java.util.Set;

import com.modinfodesigns.classify.TermIndexMatcher;
import com.modinfodesigns.classify.PhraseIndexMatcher;
import com.modinfodesigns.classify.AndIndexMatcher;
import com.modinfodesigns.classify.OrIndexMatcher;
import com.modinfodesigns.classify.NearIndexMatcher;
import com.modinfodesigns.classify.NotIndexMatcher;

import junit.framework.TestCase;

public class TestEntityExtractor extends TestCase
{
  public void testEntityExtractor( )
  {
    System.out.println( "Testing TermIndexMatcher as an Entity Extractor" );
    IndexMatcherEntityExtractor imee = new IndexMatcherEntityExtractor( );
		
    TermIndexMatcher tim = new TermIndexMatcher( "Prometheus" );
    imee.setIndexMatcher( tim );
    printMappedEntities( imee );
		
    // ===================================================================
    System.out.println( "\n ---- Testing PhraseIndexMatcher  ----" );
    PhraseIndexMatcher pim = new PhraseIndexMatcher( "alabaster seas" );
    imee.setIndexMatcher( pim );
    printMappedEntities( imee );
		
    // ===================================================================
    System.out.println( "\n ---- Testing OrIndexMatcher ---- " );
    OrIndexMatcher ormatcher = new OrIndexMatcher( );
    ormatcher.addIndexMatcher( tim );
    ormatcher.addIndexMatcher( pim );
    ormatcher.addIndexMatcher( new TermIndexMatcher( "foobar" ));
    imee.setIndexMatcher( ormatcher );
    printMappedEntities( imee );
		
    // ===================================================================
    System.out.println( "\n ---- Testing AndIndexMatcher ---- " );
    AndIndexMatcher andmatcher = new AndIndexMatcher( );
    andmatcher.addIndexMatcher( tim );
    andmatcher.addIndexMatcher( pim );
    // andmatcher.addIndexMatcher( new TermIndexMatcher( "foobar" ));
    imee.setIndexMatcher( andmatcher );
    printMappedEntities( imee );
		
    // ===================================================================
    System.out.println( "\n ---- Testing NearIndexMatcher ---- ");
    NearIndexMatcher nearMatcher = new NearIndexMatcher( );
    nearMatcher.addIndexMatcher( new TermIndexMatcher( "Prometheus" ));
    nearMatcher.addIndexMatcher( new TermIndexMatcher( "testing" ) );
    nearMatcher.addIndexMatcher( new PhraseIndexMatcher( "create some pipelines" ));
    nearMatcher.addIndexMatcher( new PhraseIndexMatcher( "next step" ));
    nearMatcher.setMinimumDistance( 3 );
    nearMatcher.setOrdered( false );
    imee.setIndexMatcher( nearMatcher );
    printMappedEntities( imee );
    
    // ===================================================================
    System.out.println( "\n ---- Testing NotIndexMatcher ---- ");
    NotIndexMatcher notMatcher = new NotIndexMatcher( nearMatcher );
    imee.setIndexMatcher( notMatcher );
    printMappedEntities( imee );
  }
	
  private static void printMappedEntities( IndexMatcherEntityExtractor imee )
  {
    EntityPositionMap eMapSet = imee.extractEntities( "sample", testText );
    if (eMapSet != null)
    {
      System.out.println( "Extracted Entities:");
      Set<String> mappedEntities = eMapSet.getMappedPhrases( true );
      for (Iterator<String> enIt = mappedEntities.iterator(); enIt.hasNext(); )
      {
        System.out.println( "  " + enIt.next( ) );
      }
    }
    else
    {
      System.out.println( "No Match Statistics." );
    }
  }
	
  private static String testText = "To be on alabaster seas - what joy. There is much "
                                 + "more to test but this is a good start at testing Prometheus again."
                                 + " The next step is to create some pipelines with XML.";

}
