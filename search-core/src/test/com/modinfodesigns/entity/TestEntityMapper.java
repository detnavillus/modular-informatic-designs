package com.modinfodesigns.entity;

import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;

import java.util.List;

import junit.framework.TestCase;

public class TestEntityMapper extends TestCase
{
  public void testEntityMapper( )
  {
    System.out.println( "Testing Entity Mapper" );
        
    boolean caseSensitive = false;
        
    PhrasePositionMap eMapper = new PhrasePositionMap( "there now to", testString, caseSensitive );
        
    List<IntegerRangeProperty> ranges = eMapper.getWordPositions( );
    assertNotNull( ranges );
        
    System.out.println( "Ranges for caseSensitive = " + caseSensitive );
    for (int i = 0; i < ranges.size(); i++)
    {
      IntegerRangeProperty range = ranges.get( i );
      System.out.println( "Got Range: " + range.getValue( ) );
    }
        
    List<LongRangeProperty> charRanges = eMapper.getCharacterPositions( );
    for (int i = 0; i < charRanges.size(); i++)
    {
        LongRangeProperty charRange = charRanges.get( i );
        System.out.println( "character range: " + charRange.getValue( ) );
    }
  }

  private static final String testString = "Hello there, -  now to try for some highlighting, etc. "
                                         + "This code is a lot simpler and hopefully just as fast if not "
                                         + "faster than the old version. It is certainly more elegant. "
                                         + " There now to the point - this is working nicely.";
}
