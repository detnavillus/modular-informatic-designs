package com.modinfodesigns.classify;

import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.MatchStatistics;

import com.modinfodesigns.classify.subject.SubjectClassifier;
import com.modinfodesigns.classify.subject.TermsSubjectMatcher;

import com.modinfodesigns.classify.TermIndexMatcher;
import com.modinfodesigns.classify.PhraseIndexMatcher;
import com.modinfodesigns.classify.OrIndexMatcher;

import com.modinfodesigns.property.quantity.ScalarQuantity;

import junit.framework.TestCase;

public class TestSubjectMatcher extends TestCase
{
  public void testSubjectMatcher( ) throws Exception
  {
    SubjectClassifier subClassifier = new SubjectClassifier( );
    TermsSubjectMatcher crimeMatcher = new TermsSubjectMatcher( );
    crimeMatcher.setSubjectName( "Crime" );
    OrIndexMatcher proxyMatcher = new OrIndexMatcher( );
    crimeMatcher.setIndexMatcher( proxyMatcher );
        
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "murder" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "police" ) );
    proxyMatcher.addIndexMatcher( new PhraseIndexMatcher( "second-degree" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "charge" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "jail" ) );
    proxyMatcher.addIndexMatcher( new PhraseIndexMatcher( "held without bond" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "weapon" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "arrest" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "arrested" ) );
    proxyMatcher.addIndexMatcher( new PhraseIndexMatcher( "cover up" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "burglarized" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "knife" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "stabbed" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "court" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "attorney" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "lawyer" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "gun" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "gunman" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "shooting" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "shots" ) );
        
    subClassifier.addSubjectMatcher( crimeMatcher );
    
    TermsSubjectMatcher cookingMatcher = new TermsSubjectMatcher( );
    cookingMatcher.setSubjectName( "Cooking" );
    proxyMatcher = new OrIndexMatcher( );
    cookingMatcher.setIndexMatcher( proxyMatcher );
    
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "knife" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "cook" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "hamburger" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "kitchen" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "ingredients" ) );
        
    // Family - mother wife daughter father brother husband
    TermsSubjectMatcher familyMatcher = new TermsSubjectMatcher( );
    familyMatcher.setSubjectName( "Family" );
    proxyMatcher = new OrIndexMatcher( );
    familyMatcher.setIndexMatcher( proxyMatcher );
    
    subClassifier.addSubjectMatcher( cookingMatcher );
    subClassifier.addSubjectMatcher( familyMatcher );
        
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "mother" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "father" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "son" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "daughter" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "husband" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "wife" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "family" ) );
    proxyMatcher.addIndexMatcher( new TermIndexMatcher( "grandmother" ) );
        
    subClassifier.setClassificationThreshold( new ScalarQuantity( "", 0.02 ) );
    subClassifier.setMaxSubjects( 3 );
        
    InvertedIndex invIndex = new InvertedIndex( );
    invIndex.tokenize( "body", document );
        
    MatchStatistics matchStats = subClassifier.getMatchStatistics( invIndex );
    System.out.println( "Matches = " + matchStats.matches( ) );
            
    System.out.println( matchStats.getValue( ) );
      
    // Get Child match statistics - check that Crime > Family > Cooking (or whatever based on TF)
    // List<MatchStatistics> getChildMatchStatistics(  )
  }
	
	
  private static String document = "An elderly Miami man faces a second-degree murder charge after he grabbed a knife from a kitchen drawer and stabbed \r\n"
                                 + "his wife to death after she wouldn't cook him a hamburger, authorities said.\r\n"
                                 + "Bartolo Gelsomino, 78, was arrested on Jan. 21, was being held without bond Monday night, according to online jail records.\r\n"
                                 + "Police said he stabbed his 71-year-old  wife Ana Gelsomino after the dispute, The Miami Herald reported.\r\n"
                                 + "Read original story on NBCMiami.com\r\n"
                                 + "The couple's daughter found her mother's body in the home at 10700 Southwest 146th Court, police said.\r\n"
                                 + "The Herald reported that he made it seem as if the home had been burglarized, so as to cover up the murder.\r\n"
                                 + "After his arrest, he confessed and detailed what had happened, the arrest report said. He then showed investigators \r\n"
                                 + "where he had put the murder weapon and the clothes he was wearing, police said.\r\n"
                                 + "It wasn't immediately known if he had an attorney. \r\n";
}
