package com.modinfodesigns.entity;

import com.modinfodesigns.entity.tagging.EntityTaggerStringTransform;
import com.modinfodesigns.property.transform.string.StringTransformException;

import java.util.List;

import junit.framework.TestCase;

public class TestRegularExpressionExtractor extends TestCase
{
  public void testRegularExpressionExtractor( ) throws StringTransformException
  {
    String pattern = "(BM[ST][-\\s]?[0-9][0-9][0-9][0-9][0-9][0-9]?)";
        
    RegularExpressionEntityExtractor reee = new RegularExpressionEntityExtractor( );
    reee.addPattern( pattern );
        
    EntityPositionMap ePositionMap = reee.extractEntities( "text", testString );
    System.out.println( ePositionMap );
    List<PhrasePositionMap> mappers = ePositionMap.getPhrasePositionMaps( );
    for (int i = 0; i < mappers.size(); i++)
    {
      PhrasePositionMap phraseMap = mappers.get( i );
      System.out.println( "'" + phraseMap.getPhrase( ) + "'"  );
    }
        
    EntityTaggerStringTransform etst = new EntityTaggerStringTransform( );
    etst.addEntityExtractor( reee );
        
    String tagged = etst.transformString( testString );
    System.out.println( "\r\n" + tagged );
  }
	
  private static final String testString = "I never know what to write in these things. The pattern"
                                         + " that I am testing should have numbers in it like 123 or 4587. 321 aaa should work too but"
                                         + " the word positions will be a bit funny. Now go for a phrase like BMS-90210. The question"
                                         + " is whether the dash will also work as in BMS 123456 and BMS 345678."
                                         + " Yeas but it should not mark up pieces of content like BMS or 90210.  Try an earlier one again"
                                         + " without traversing all instances of a phrase, BMS-90210 should not work a second time (it does now because I fixed it!).";

}
