package com.modinfodesigns.entity;

import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps the word and character position of a Phrase within a String. Generated by IEntityExtractors and
 * InvertedIndex matchers.
 * 
 * @author Ted Sullivan
 *
 */
public class PhrasePositionMap
{
  private String phrase;
    
  // list of individual tokens that make up the phrase
  private String[] phraseTokens;
    
  // list of match positions within the String that the Entity
  // was extracted from
  private ArrayList<IntegerRangeProperty> wordPositions;
    
  private ArrayList<LongRangeProperty> charPositions;
    
  private boolean caseSensitive = false;
    
  public PhrasePositionMap( String phrase )
  {
    setPhrase( phrase );
  }
    
  public PhrasePositionMap( String phrase, String text )
  {
    this( phrase, text, false );
  }
    
  public PhrasePositionMap( String phrase, String text, boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
    setPhrase( phrase );
        
    InvertedIndex invIndex = new InvertedIndex( );
    invIndex.setCaseSensitive( this.caseSensitive );
    
    invIndex.tokenize( "text", text );
    
    setPhrasePositions( "text", invIndex );
  }
    
  public PhrasePositionMap( String phrase, List<IntegerRangeProperty> phrasePositions, List<LongRangeProperty> characterPositions )
  {
    setPhrase( phrase );
    setPhrasePositions( phrasePositions );
    setCharacterPositions( characterPositions );
  }
    
  public String[] getEntityTokens( )
  {
    return phraseTokens;
  }
    
  public String getFirstToken( )
  {
    return phraseTokens[0];
  }
    
  public void setPhrasePositions( List<IntegerRangeProperty> phrasePositions  )
  {
    this.wordPositions = new ArrayList<IntegerRangeProperty>( );
    this.wordPositions.addAll( phrasePositions );
  }
    
  public void setPhrasePositions( String field, InvertedIndex ndx )
  {
    List<Integer> phrasePositions = ndx.getPhrasePositions( field, this.phrase, caseSensitive );
    List<Long> characterPositions = ndx.getCharacterPositions( field, phrasePositions );
 
    if (phrasePositions == null) return;
    
    this.wordPositions = new ArrayList<IntegerRangeProperty>( );
    this.charPositions = new ArrayList<LongRangeProperty>( );
    for (int i = 0; i < phrasePositions.size(); i++)
    {
      Integer startPosInt = phrasePositions.get( i );
      int startWordPos = startPosInt.intValue();
      int endWordPos = startWordPos + phraseTokens.length - 1;
        
      wordPositions.add( new IntegerRangeProperty( Integer.toString( i ), startWordPos, endWordPos ) );

      Long charStartPos = characterPositions.get( i );
      long startPosC = charStartPos.longValue();
        
      Long charEndPos = ndx.getCharacterPosition( field, endWordPos );
      if (charEndPos != null)
      {
        charEndPos = ndx.getCharacterPositionEnd( field, charEndPos.longValue( ) );
        if (charEndPos != null)
        {
          charPositions.add( (new LongRangeProperty( Integer.toString( i ), startPosC, charEndPos.longValue() )) );
        }
      }
    }
  }
    
  public List<IntegerRangeProperty> getWordPositions( )
  {
    return wordPositions;
  }
    
  public void setCharacterPositions( List<LongRangeProperty> characterPositions )
  {
    this.charPositions = new ArrayList<LongRangeProperty>( );
    this.charPositions.addAll( characterPositions );
  }
    
  public List<LongRangeProperty> getCharacterPositions(  )
  {
    return getCharacterPositions( 0L );
  }
    
  public List<LongRangeProperty> getCharacterPositions( long offset )
  {
    if ( offset == 0L || charPositions == null )
    {
      return charPositions;
    }
    else
    {
      ArrayList<LongRangeProperty> shifted = new ArrayList<LongRangeProperty>( );
      for (int i = 0; i < charPositions.size(); i++)
      {
        LongRangeProperty lrp = charPositions.get( i );
        LongRangeProperty copy = (LongRangeProperty)lrp.copy( );
        copy.add( offset );
        shifted.add( copy );
      }
        
      return shifted;
    }
  }
    
  public void setPhrase( String phrase )
  {
    if (phrase == null) return;
    	
    this.phrase = phrase;
    phraseTokens = StringTransform.getStringArray( phrase, InvertedIndex.tokenDelimiter );
  }
    
  public String getPhrase( )
  {
    return this.phrase;
  }
    
  public void setCaseSensitive( boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
  }
}
