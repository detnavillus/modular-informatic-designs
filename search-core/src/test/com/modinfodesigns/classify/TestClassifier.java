package com.modinfodesigns.classify;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.PropertyTransformException;

import java.util.Iterator;

import junit.framework.TestCase;

public class TestClassifier extends TestCase
{

  public void testClassifier( ) throws IndexMatcherException, PropertyTransformException
  {
    Classifier classifier = new Classifier( );
    PhraseIndexMatcher ironManMatcher = new PhraseIndexMatcher( "Iron Man" );
    ironManMatcher.addProperty( new StringProperty( "Keyphrase", "Iron Man" ));
    classifier.addIndexMatcher( ironManMatcher );
            
    PhraseIndexMatcher notInMatcher = new PhraseIndexMatcher( "Not a match phrase" );
    notInMatcher.addProperty( new StringProperty( "Keyphrase", "Not a match phrase" ));
    classifier.addIndexMatcher( notInMatcher );
            
    PhraseIndexMatcher cynthiaMatcher = new PhraseIndexMatcher( "Cynthia's hospital room" );
    cynthiaMatcher.addProperty( new StringProperty( "Keyphrase", "Cynthia's hospital room" ));
    classifier.addIndexMatcher( cynthiaMatcher );
        
    // Test classifier as an IIndexMatcher
    InvertedIndex invIndex = new InvertedIndex( "text", sampleText );
    MatchStatistics matchStats = classifier.getMatchStatistics( invIndex );
    if (matchStats != null)
    {
      System.out.println( "classifier matches sample Text = " + matchStats.matches( )  );
    }
    else
    {
      System.out.println( "classifier matches sample Text = false" );
    }
            
    DataObject dObject = new DataObject( );
    dObject.addProperty( new StringProperty( "body", sampleText ) );
    dObject.addProperty( new StringProperty( "title", "Sample Text" ) );
            
    classifier.addClassifyField( "body" );
            
    classifier.transformPropertyHolder( dObject );
            
    System.out.println( "\r\n Post classification properties: ");
    for (Iterator<IProperty> propIt = dObject.getProperties(); propIt.hasNext(); )
    {
      IProperty prop = propIt.next();
      System.out.println( prop.getName() + ":" + prop.getValue( ) );
    }
  }
	
  private static String sampleText = "We are watching the Avengers in Cynthia's hospital"
                                   + " room. Iron Man is fighting Loki";

}
