package com.modinfodesigns.classify;

import junit.framework.TestCase;

public class TestIndexMatcher extends TestCase
{

  public void testIndexMatcher( ) throws IndexMatcherException
  {
    InvertedIndex invIndex = new InvertedIndex( "text", sampleText );
		
    TermIndexMatcher termIndexMatcher = new TermIndexMatcher( "Every", true );
    MatchStatistics matchStats = termIndexMatcher.getMatchStatistics( invIndex );
    System.out.println( "'every' matches = " + (matchStats != null && matchStats.matches( )) );
		    
    PhraseIndexMatcher phraseIndexMatcher = new PhraseIndexMatcher( "good boy", true );
    matchStats = phraseIndexMatcher.getMatchStatistics( invIndex );
    System.out.println( "'good boy' matches = " + (matchStats != null && matchStats.matches( )) );
		    
    AndIndexMatcher andNdxMatcher = new AndIndexMatcher( );
    andNdxMatcher.addIndexMatcher( termIndexMatcher );
    andNdxMatcher.addIndexMatcher( phraseIndexMatcher );
    matchStats = andNdxMatcher.getMatchStatistics( invIndex );
    System.out.println( "AndMatcher matches = " + (matchStats != null && matchStats.matches( )) );
		    
    andNdxMatcher.addIndexMatcher( new TermIndexMatcher( "blah" ) );
    matchStats = andNdxMatcher.getMatchStatistics( invIndex );
    System.out.println( "After adding 'blah' matcher AndMatcher matches = " + (matchStats != null && matchStats.matches( )) );
		    
    OrIndexMatcher orNdxMatcher = new OrIndexMatcher( );
    orNdxMatcher.addIndexMatcher( termIndexMatcher );
    orNdxMatcher.addIndexMatcher( phraseIndexMatcher );
    matchStats = orNdxMatcher.getMatchStatistics( invIndex );
    System.out.println( "OrMatcher matches = " + (matchStats != null && matchStats.matches( )) );
		    
    orNdxMatcher.addIndexMatcher( new PhraseIndexMatcher( "foo bar" ) );
    matchStats = orNdxMatcher.getMatchStatistics( invIndex );
    System.out.println( "After adding 'foo bar' OrMatcher matches = " + (matchStats != null && matchStats.matches( )) );

    NearIndexMatcher nearMatcher = new NearIndexMatcher( );
    nearMatcher.setMinimumDistance( 1 );
    nearMatcher.addIndexMatcher( new PhraseIndexMatcher( "good boy" ) );
    nearMatcher.addIndexMatcher( new TermIndexMatcher( "deserves" ) );
    matchStats = nearMatcher.getMatchStatistics( invIndex );
    System.out.println( "NearMatcher matches = " + (matchStats != null && matchStats.matches( )) );
		    
    nearMatcher.addIndexMatcher( new TermIndexMatcher( "favor" ));
    matchStats = nearMatcher.getMatchStatistics( invIndex );
    System.out.println( "After adding 'favor' NearMatcher matches = " + (matchStats != null && matchStats.matches( )) );
		    
    nearMatcher = new NearIndexMatcher( );
    nearMatcher.setMinimumDistance( 5 );
    nearMatcher.addIndexMatcher( new PhraseIndexMatcher( "very simple test" ) );
    nearMatcher.addIndexMatcher( new PhraseIndexMatcher( "boy deserves favor" ) );
    matchStats = nearMatcher.getMatchStatistics( invIndex );
    System.out.println( "NearMatcher matches = " + (matchStats != null && matchStats.matches( )) );

    nearMatcher.setOrdered( true );
    matchStats = nearMatcher.getMatchStatistics( invIndex );
    System.out.println( "After setOrdered(true) NearMatcher matches = " + (matchStats != null && matchStats.matches( )) );
  }
	
  private static String sampleText = "Every good boy deserves favor. This is a very simple test, more testing is needed certainly."
                                   + "This is just the way we need to work to get things done.";

}
