package com.modinfodesigns.test.classify;

import com.modinfodesigns.classify.PatternMap;
import com.modinfodesigns.classify.MatchStatistics;
import com.modinfodesigns.classify.OrIndexMatcher;
import com.modinfodesigns.classify.PatternMatcher;
import com.modinfodesigns.classify.TermIndexMatcher;
import com.modinfodesigns.classify.PhraseIndexMatcher;

import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.IndexMatcherException;

import com.modinfodesigns.property.DataObject;

import junit.framework.TestCase;

public class TestPatternMatcher extends TestCase
{

  public void testPatternMatcherDrugs( ) throws IndexMatcherException
  {
    PatternMatcher pMatcher = new PatternMatcher( );
    pMatcher.setMatchPattern( "DRUG treats DISEASE" );
    pMatcher.setPatternProperty( "drugIndication" );
        
    pMatcher.addPattern( "DRUG [2] indicated [3] treatment [3] DISEASE" );
    pMatcher.addPattern( "DISEASE [2] treated [3] DRUG" );
    pMatcher.addPattern( "DRUG is used to treat DISEASE" );
        
    PatternMap drugMap = new PatternMap( );
    drugMap.setName( "DRUG" );
        
    OrIndexMatcher drugNameMatcher = new OrIndexMatcher( );
    drugNameMatcher.addIndexMatcher( new TermIndexMatcher( "Tylenol" ) );
    drugNameMatcher.addIndexMatcher( new TermIndexMatcher( "Viagra" ) );
    drugNameMatcher.addIndexMatcher( new TermIndexMatcher( "Aspirin" ) );
    drugNameMatcher.addIndexMatcher( new TermIndexMatcher( "Zoloft" ) );
    drugMap.setIndexMatcher( drugNameMatcher );
        
    pMatcher.addPatternMap( drugMap );
        
    PatternMap diseaseMap = new PatternMap( );
    diseaseMap.setName( "DISEASE" );
        
    OrIndexMatcher diseaseMatcher = new OrIndexMatcher( );
    diseaseMatcher.addIndexMatcher( new TermIndexMatcher( "headaches" ));
    diseaseMatcher.addIndexMatcher( new TermIndexMatcher( "depression" ));
    diseaseMatcher.addIndexMatcher( new TermIndexMatcher( "headache" ));
    diseaseMatcher.addIndexMatcher( new PhraseIndexMatcher( "erectile dysfunction" ));
    
    diseaseMap.setIndexMatcher( diseaseMatcher );
        
    pMatcher.addPatternMap( diseaseMap );
        
    InvertedIndex invIndex = new InvertedIndex( "text", drugText );
    MatchStatistics matchStats = pMatcher.getMatchStatistics( invIndex );
            
    DataObject dobj = new DataObject( );
    dobj.setUniquePropertiesOnly( false );
    pMatcher.addMatchProperties( dobj, matchStats );
            
    System.out.println( dobj.getValue( ) );
  }
    
  public void testFootballTeamBrags( ) throws IndexMatcherException
  {
    PatternMatcher pMatcher = new PatternMatcher( );
    pMatcher.setMatchPattern( "TEAM1 > TEAM2" );
    pMatcher.setPatternProperty( "teamHegemony" );
        
    pMatcher.addPattern( "TEAM1 [3] better [5] TEAM2" );
    pMatcher.addPattern( "TEAM2 [3] not as good [6] TEAM1" );
    pMatcher.addPattern( "TEAM1 [3] superior to [5] TEAM2" );
    pMatcher.addPattern( "TEAM2 [3] inferior [5] TEAM1" );
        
    PatternMap team1Map = new PatternMap( );
    team1Map.setName( "TEAM1" );
        
    OrIndexMatcher teamNameMatcher = new OrIndexMatcher( );
    teamNameMatcher.addIndexMatcher( new PhraseIndexMatcher( "NY Giants" ) );
    teamNameMatcher.addIndexMatcher( new PhraseIndexMatcher( "NY Jets" ) );
    teamNameMatcher.addIndexMatcher( new PhraseIndexMatcher( "Arizona Cardinals" ) );
    teamNameMatcher.addIndexMatcher( new PhraseIndexMatcher( "Green Bay Packers" ) );
    teamNameMatcher.addIndexMatcher( new PhraseIndexMatcher( "Pittsburgh Steelers" ) );
    teamNameMatcher.addIndexMatcher( new PhraseIndexMatcher( "New England Patriots" ) );
    teamNameMatcher.addIndexMatcher( new PhraseIndexMatcher( "Dallas Cowboys" ));
        
    team1Map.setIndexMatcher( teamNameMatcher );
    PatternMap team2Map = new PatternMap( );
    team2Map.setName( "TEAM2" );
    team2Map.setIndexMatcher( teamNameMatcher );
        
    pMatcher.addPatternMap( team1Map );
    pMatcher.addPatternMap( team2Map );
        
    InvertedIndex invIndex = new InvertedIndex( "text", footballText );
    MatchStatistics matchStats = pMatcher.getMatchStatistics( invIndex );
            
    DataObject dobj = new DataObject( );
    dobj.setUniquePropertiesOnly( false );
    pMatcher.addMatchProperties( dobj, matchStats );
            
    System.out.println( dobj.getValue( ) );
  }
	
  private static final String footballText = "This is a test of football team superiority. The NY Giants are better than the NY Jets, "
                                           + "the Arizona Cardinals are not as good as the New England Patriots. "
                                           + " The Green Bay Packers are superior to the Dallas Cowboys and the Pittsburgh Steelers are "
                                           + "inferior compared to the New England Patriots.";
	
  private static String drugText = "This is a bit of nonsense about drugs and diseases.  "
                                 + " Erectile dysfunction is treated with Viagra, Zoloft is indicated for the treatment of "
                                 + "depression and so on. Aspirin is used to treat headaches,";

}
