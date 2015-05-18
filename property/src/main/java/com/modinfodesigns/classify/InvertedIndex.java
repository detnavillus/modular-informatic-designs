package com.modinfodesigns.classify;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import java.util.StringTokenizer;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.property.quantity.IntegerListProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates an Inverted Index of input Text expressed as as an IPropertyHolder
 * 
 * each Token is an IPropertyHolder with a set of word positions (IntegerListProperty) and a set of character positions (LongListProperty)
 * 
 * @author Ted Sullivan
 *
 */

public class InvertedIndex
{
  private transient static final Logger LOG = LoggerFactory.getLogger( InvertedIndex.class );

  private static final boolean DEBUG_SENTENCES = false;
  private static final boolean DEBUG_TOKENS = true;
	
  private int totalTermCount;
	
  public static String tokenDelimiter = " .,:;-!(){}[]<>|/\\\r\n\"'?";
	
  private boolean caseSensitive = false;
  private boolean removeTags = true;
  private boolean detectSentences = true;
    
  ArrayList<Sentence> sentences;
	
  // map of token -> Map of field -> tokenProperties
  private HashMap<String,HashMap<String,TokenProperties>> tokenMap = new HashMap<String,HashMap<String,TokenProperties>>( );
  private HashMap<String,String> fieldStringsMap = new HashMap<String,String>( );
	
  private HashMap<String,TokenProperties> allFieldTokens = new HashMap<String,TokenProperties>( );
	
  private HashMap<String,HitCount> fieldCounts = new HashMap<String,HitCount>( );
	
  public InvertedIndex( )
  {
		
  }
	
  public InvertedIndex( String name, String inputText )
  {
    tokenize( name, inputText );
  }
	
	
  /**
   * Use this to convert a StringProperty to an InvertedIndex property in place.
   *
   * @param inputProperty
   */
  public InvertedIndex( IProperty inputProperty )
  {
    String name = inputProperty.getName( );
    String textInput = inputProperty.getValue( );
		
    tokenize( name, textInput );
  }
	
  public void tokenize( IPropertyHolder propHolder, ArrayList<String> fields )
  {
    for (int i = 0; i < fields.size( ); i++)
    {
      String field = fields.get( i );
      IProperty prop = propHolder.getProperty( field );
      if (prop != null)
      {
        String text = prop.getValue( );
        tokenize( field, text );
      }
      else
      {
        LOG.error( "No data for field: " + field );
      }
    }
  }
	
  public void tokenize( String field, String inputText )
  {
    fieldStringsMap.put( field, inputText );
		
    // ===============================================================================
    // create the inverted index, for each token, collect a set of positions.
    // when complete, store each token as an IntegerListProperty and LongListProperty
    // (for char position) can also do char position and no case
    // ===============================================================================
    StringTokenizer strtok = new StringTokenizer( inputText, tokenDelimiter, true );
    boolean withinTag = false;
      
    boolean sentStarted = false;
    boolean sentEnded = false;
    int lastSentenceEnd = 0;      // token position of sentence end
    int lastSentenceStart = 0;    // token position of sentence start
    
    int sentenceNumber = 0;
      
    StringBuffer sentenceBuf = new StringBuffer( );

    int wordPos = 0;
    long charPos = 0;
    while ( strtok.hasMoreTokens( ) )
    {
      String token = strtok.nextToken( );
      String tokString = token;
        
      if ( token == null ) continue;
        
      if ( removeTags && token.equals( "<" ) )
      {
        withinTag = true;
      }
      else if ( removeTags && withinTag && ( token.equals( ">" ) ) )
      {
        withinTag = false;
      }
      else
      {
        if ( !withinTag )
        {
          if ( token.trim( ).length( ) > 0 && tokenDelimiter.indexOf( token ) < 0)
          {
            ++wordPos;

            if ( detectSentences )
            {
              if (!sentStarted && !sentEnded)
              {
                if (isSentenceStart( token ))
                {
                  if (DEBUG_SENTENCES) LOG.debug( "  sentence started on '" + token + "'" );
                  sentStarted = true;
                  sentenceBuf = new StringBuffer(  );
                }
              }
              else if (sentStarted && !sentEnded)
              {
                if ((wordPos > lastSentenceStart + 1) && isSentenceEnd( token ))
                {
                  if (DEBUG_SENTENCES) LOG.debug( "  sentence ended on '" + token + "'"  );

                  sentEnded = true;
                  lastSentenceEnd = wordPos;
                  // clean up token
                  token = cleanUp( token );
                    
                  if (DEBUG_SENTENCES) LOG.debug( "Cleaned up last token = '" + token + "'" );
                }

                if ( !strtok.hasMoreTokens( ))
                {
                  if (sentences == null) sentences = new ArrayList<Sentence>( );

                  sentenceBuf.append( token );
                  sentences.add( new Sentence( sentenceNumber++, lastSentenceStart, lastSentenceEnd, sentenceBuf.toString( ) ) );

                  token = cleanUp( token );

                  if (DEBUG_SENTENCES) LOG.debug( "Cleaned up last Last token = '" + token + "'" );
                  if (DEBUG_SENTENCES) LOG.debug( " Now have " + sentences.size( ) + " sentences." );
                }
              }
              else if (sentStarted && sentEnded)
              {
                if (DEBUG_SENTENCES) LOG.debug( "Looking for new start." );
                                
                // ========================================================================================
                // It might have been a false ending, check if the next token really starts a new sentence.
                // ========================================================================================
                if (isSentenceStart( token ))
                {
                  // create new sentence...
                  if (sentences == null) sentences = new ArrayList<Sentence>( );

                  if (DEBUG_SENTENCES)
                  {
                    LOG.debug( "adding sentence: " + sentenceBuf.toString( ) );
                  }

                  sentences.add( new Sentence( sentenceNumber++, lastSentenceStart, lastSentenceEnd, sentenceBuf.toString( ) ) );
                  if (DEBUG_SENTENCES) LOG.debug( " Now have " + sentences.size( ) + " sentences." );

                  sentStarted = true;
                  sentEnded = false;

                  lastSentenceStart = wordPos;
                  sentenceBuf = new StringBuffer( );
                }
                else
                {
                  if (DEBUG_SENTENCES) LOG.debug( "false end - '" + token + "' is not a new start." );
                  sentEnded = false;
                }
              }
            }

            if (DEBUG_TOKENS)
            {
              LOG.debug( "adding token: '" + token + "' at: " + wordPos + " (" + charPos + ")" );
            }

            addToken( field, token, wordPos, charPos );
          }

          if (detectSentences)
          {
            sentenceBuf.append( tokString );
          }
        }
      }
            
      charPos += tokString.length( );
    }
  }
	
  private boolean isSentenceStart( String str )
  {
    return false;
  }
	
  private boolean isSentenceEnd( String str )
  {
    return false;
  }
	
  private String cleanUp( String str )
  {
    return str;
  }
	
  private void addToken( String field, String token, int wordPosition, long charPosition )
  {
    LOG.debug( "addToken( " + token + " ) " + caseSensitive );
    	
    HashMap<String,TokenProperties> fieldMap = tokenMap.get( token );
    TokenProperties allTokens = allFieldTokens.get( field );
    if (allTokens == null)
    {
      allTokens = new TokenProperties( );
      allFieldTokens.put( field,  allTokens );
    }
    	
    if (fieldMap == null)
    {
      fieldMap = new HashMap<String,TokenProperties>( );
      tokenMap.put( token, fieldMap );
    }
    	
    TokenProperties tokProps = fieldMap.get( field );
    if (tokProps == null)
    {
      tokProps = new TokenProperties( );
      LOG.debug( "Adding TokenProps for " + field );
      fieldMap.put( field, tokProps );
    }
    	
    tokProps.addWordPosition( wordPosition );
    tokProps.addCharacterPosition( wordPosition, charPosition );
    	
    allTokens.addWordPosition( wordPosition );
    allTokens.addCharacterPosition( wordPosition, charPosition);
    	
    if (!caseSensitive && !token.equals( token.toLowerCase()))
    {
      LOG.debug( "adding lower case props for " + token.toLowerCase( ) );
    		
      HashMap<String,TokenProperties> lcFieldMap = tokenMap.get( token.toLowerCase( ) );
      if (lcFieldMap == null)
      {
        lcFieldMap = new HashMap<String,TokenProperties>( );
        tokenMap.put( token.toLowerCase(), lcFieldMap );
      }
    		
      TokenProperties lcProps = lcFieldMap.get( field );
      if (lcProps == null)
      {
        lcProps = new TokenProperties( );
        LOG.debug( this + " creating TokenProperties for " + field + " " + token.toLowerCase() );
        lcFieldMap.put( field, lcProps );
      }
    		
      lcProps.addWordPosition( wordPosition );
      lcProps.addCharacterPosition( wordPosition, charPosition );
    }
    	
    // Increment the number of tokens in this field
    HitCount fieldCount = fieldCounts.get( field );
    if (fieldCount == null)
    {
      fieldCount = new HitCount( );
      fieldCounts.put( field,  fieldCount );
    }
    	
    fieldCount.increment( );
  }
    
  public String getFieldString( String fieldName )
  {
    return fieldStringsMap.get( fieldName );
  }
    
  public int getFieldLength( String fieldName )
  {
    String field = fieldStringsMap.get( fieldName );
    return (field != null) ? field.length() : 0;
  }
    
  public HitCount getFieldCounts( String fieldName )
  {
    return fieldCounts.get( fieldName );
  }
	
  public Set<String> getTermSet(  )
  {
    return tokenMap.keySet( );
  }
	
  public int getTermCount(  )
  {
    return totalTermCount;
  }
	
  // Query Methods
  public boolean containsTerm( String term )
  {
    return containsTerm( term, false );
  }
	
  public boolean containsTerm( String term, boolean caseSensitive )
  {
    if ( term == null || term.trim().length() == 0) return false;
		
    return (caseSensitive) ? tokenMap.keySet().contains( term ) : tokenMap.keySet().contains( term.toLowerCase( ) );
  }
	
  public boolean containsTerm( String field, String term, boolean caseSensitive )
  {
    HashMap<String,TokenProperties> fieldMap = tokenMap.get( term );
    return (fieldMap != null && fieldMap.keySet().contains( field ));
  }
	
  public boolean containsPhrase( String field, String phrase )
  {
    return containsPhrase( field, phrase, false );
  }
	
  public boolean containsPhrase( String field, String phrase, boolean caseSensitive )
  {
    if ( phrase == null || phrase.trim().length() == 0) return false;
		
    // break up phrase into tokens - check if has phrase
    List<String> phraseTerms = StringTransform.getStringList( phrase, tokenDelimiter );
    if (phraseTerms.size() == 1)
    {
      // return containsTerm of the 0th element
      String term = phraseTerms.get( 0 );
      return containsTerm( field, term, caseSensitive );
    }
    else
    {
      ArrayList<List<Integer>> posLists = new ArrayList<List<Integer>>( );
      for (int i = 0; i < phraseTerms.size(); i++)
      {
        String pTerm = phraseTerms.get( i );
        List<Integer> termPos = getWordPositions( field, pTerm, caseSensitive );
        if (termPos == null) return false;
        posLists.add( termPos );
      }
			
      // check all of the positions of the first token
      List<Integer> firstList = posLists.get( 0 );
      for (int i = 0; i < firstList.size(); i++)
      {
        int firstPos = firstList.get( i ).intValue( );
        for (int j = 1; j < posLists.size( ); j++)
        {
          int nextPos = firstPos + j;
          List<Integer> nextList = posLists.get( j );
          if (!nextList.contains( new Integer( nextPos ))) return false;
        }
      }
    }
		
    return true;
  }
	
  public int getTermCount( String field, String term )
  {
    return getTermCount( field, term, false );
  }
	
  public int getTermCount( String field, String term, boolean caseSensitive )
  {
    if ( term == null || term.trim().length() == 0) return 0;
		
    HashMap<String,TokenProperties> fieldMap = tokenMap.get( ((caseSensitive) ? term : term.toLowerCase( ) ) );
    if (fieldMap == null) return 0;
    
    TokenProperties tokenProps = fieldMap.get( field );
    if (tokenProps == null) return 0;
		
    IntegerListProperty ilp = tokenProps.getWordPositionList( );
		
    return ilp.size( );
  }
	
  public Set<String> getFields(  )
  {
    return fieldStringsMap.keySet( );
  }
	
  public Set<String> getFields( String phrase )
  {
    if (phrase == null) return null;
		
    List<String> phraseTerms = StringTransform.getStringList( phrase, tokenDelimiter );
    return getFields( phraseTerms );
  }
	
  public Set<String> getFields( List<String> terms )
  {
    if (terms.size() == 1)
    {
      String term = terms.get( 0 );
      HashMap<String,TokenProperties> fieldMap = tokenMap.get( term );
      return (fieldMap != null) ? fieldMap.keySet() : null;
    }
    else if ( terms.size() == 2)
    {
      String term1 = terms.get( 0 );
      String term2 = terms.get( 1 );
        
      return getFieldIntersect( term1, term2 );
    }
    else if (terms.size() >= 3)
    {
      String term1 = terms.get( 0 );
      String term2 = terms.get( 1 );
      HashSet<String> intersect = getFieldIntersect( term1, term2 );
      if (intersect == null || intersect.size() == 0) return intersect;
      for (int i = 2; i < terms.size(); i++)
      {
        String termN = terms.get( i );
        HashMap<String,TokenProperties> fieldMap = tokenMap.get( termN );
        if (fieldMap == null) return null;
        intersect = getIntersect( intersect, fieldMap.keySet( ) );
        if (intersect.size( ) == 0 ) return intersect;
      }
			
      return intersect;
    }
		
    return null;
  }
	
  private HashSet<String> getFieldIntersect( String term1, String term2 )
  {
    HashMap<String,TokenProperties> fieldMap1 = tokenMap.get( term1 );
    HashMap<String,TokenProperties> fieldMap2 = tokenMap.get( term2 );
    if (fieldMap1 == null || fieldMap2 == null) return null;
    return getIntersect( fieldMap1.keySet(), fieldMap2.keySet() );
  }
	
  private HashSet<String> getIntersect( Set<String> set1, Set<String> set2 )
  {
    HashSet<String> output = new HashSet<String>( );
    for (Iterator<String> it = set1.iterator(); it.hasNext(); )
    {
      String field = it.next( );
      if (set2.contains( field ))
      {
        output.add( field );
      }
    }
    
    return output;
  }
	
  public int getPhraseCount( String field, String phrase )
  {
    return getPhraseCount( field, phrase, false );
  }
	
  public int getPhraseCount( String field, String phrase, boolean caseSensitive )
  {
    if (phrase == null || phrase.trim().length() == 0 ) return 0;
		
    List<String> phraseTerms = StringTransform.getStringList( phrase, tokenDelimiter );
    if (phraseTerms.size() == 1)
    {
      String term = phraseTerms.get( 0 );
      return getTermCount( field, term, caseSensitive );
    }
    else
    {
      ArrayList<List<Integer>> posLists = new ArrayList<List<Integer>>( );
      for (int i = 0; i < phraseTerms.size(); i++)
      {
        String pTerm = phraseTerms.get( i );
        List<Integer> termPos = getWordPositions( field, pTerm, caseSensitive );
        if (termPos == null) return 0;
        posLists.add( termPos );
      }
        
      int nPhrases = 0;
      List<Integer> firstList = posLists.get( 0 );
      for (int i = 0; i < firstList.size(); i++)
      {
        int firstPos = firstList.get( i ).intValue( );
        boolean isPhrase = true;
        for (int j = 1; j < posLists.size( ); j++)
        {
          int nextPos = firstPos + j;
          List<Integer> nextList = posLists.get( j );
          if (!nextList.contains( new Integer( nextPos )))
          {
            isPhrase = false;
            break;
          }
        }
        if (isPhrase) ++nPhrases;
      }
			
      return nPhrases;
    }
  }
	
  public Integer getMinimumDistance( String field, String termOne, String termTwo, boolean caseSensitive, boolean ordered )
  {
    if (termOne == null || termTwo == null) return null;
		
    List<Integer> termOnePos = getWordPositions( field, termOne, caseSensitive );
    List<Integer> termTwoPos = getWordPositions( field, termTwo, caseSensitive );
    if (termOnePos == null || termTwoPos == null ) return null;
		
    int minD = -1;
		
    for (int i = 0; i < termOnePos.size(); i++ )
    {
      int termOnePosition = termOnePos.get( i ).intValue( );
      for (int j = 0; j < termTwoPos.size(); j++ )
      {
        int dist = termTwoPos.get( j ).intValue( ) - termOnePosition;
        if (!ordered)
        {
          dist = Math.abs( dist );
        }
        if (dist > 0 && (minD == -1 || dist < minD))
        {
          minD = dist;
        }
      }
    }
		
    return (minD > 0) ? new Integer( minD ) : null;
  }
	
  public List<Integer> getWordPositions( String field, String term, boolean caseSensitive )
  {
    if ( term == null || term.trim().length() == 0) return null;
		
    String useTerm = (caseSensitive) ? term : term.toLowerCase( );
    LOG.debug( "getWordPositions '" + useTerm + "' " + caseSensitive );
    HashMap<String,TokenProperties> fieldMap = tokenMap.get( useTerm );
    if (fieldMap == null)
    {
      LOG.debug( "No fieldMap for " + ((caseSensitive) ? term : term.toLowerCase( )) );
      return null;
    }
		
    TokenProperties tokenProps = fieldMap.get( field );
    if (tokenProps == null)
    {
      LOG.debug( this + " No tokenProperties for " + field + " and '" + useTerm + "'" );
      return null;
    }
		
    IntegerListProperty ilp = tokenProps.getWordPositionList( );
    return ilp.getIntegerList( );
  }
	
	
  public List<Integer> getPhrasePositions( String field, String phrase, boolean caseSensitive )
  {
    if (phrase == null || phrase.trim().length() == 0 ) return null;
		
    List<String> phraseTerms = StringTransform.getStringList( phrase, tokenDelimiter );
    if (phraseTerms == null || phraseTerms.size() == 0)
    {
      return null;
    }
		
    if (phraseTerms.size() == 1)
    {
      String term = phraseTerms.get( 0 );
      return getWordPositions( field, term, caseSensitive );
    }
    else
    {
      ArrayList<List<Integer>> posLists = new ArrayList<List<Integer>>( );
      for (int i = 0; i < phraseTerms.size(); i++)
      {
        String pTerm = phraseTerms.get( i );
        List<Integer> termPos = getWordPositions( field, pTerm, caseSensitive );
        if (termPos == null)
        {
          LOG.debug( "Got no position for '" + pTerm + "'" );
          return null;
        }
				
        posLists.add( termPos );
      }
			
      ArrayList<Integer> startPositions = new ArrayList<Integer>( );
      List<Integer> firstList = posLists.get( 0 );
      for (int i = 0; i < firstList.size(); i++)
      {
        int firstPos = firstList.get( i ).intValue( );
        boolean isPhrase = true;
        for (int j = 1; j < posLists.size( ); j++)
        {
          int nextPos = firstPos + j;
          List<Integer> nextList = posLists.get( j );
          if (!nextList.contains( new Integer( nextPos )))
          {
            isPhrase = false;
            break;
          }
        }
				
        if (isPhrase) startPositions.add( new Integer( firstPos ) );
      }
			
      return startPositions;
    }
  }
	
  public List<Long> getCharacterPositions( String field, List<Integer> wordPositions )
  {
    if (wordPositions == null) return new ArrayList<Long>( );
		
    TokenProperties allTokens = allFieldTokens.get( field );
    if (allTokens == null) return null;
    ArrayList<Long> charPositions = new ArrayList<Long>( );
    for (int i = 0; i < wordPositions.size( ); i++)
    {
      Integer wordPos = wordPositions.get( i );
      Long charPos = allTokens.getCharacterPosition( wordPos.intValue( ) );
      charPositions.add( charPos );
    }
		
    return charPositions;
  }
	
  public Long getCharacterPosition( String field, int wordPosition )
  {
    TokenProperties allTokens = allFieldTokens.get( field );
    if (allTokens == null) return null;
		
    return allTokens.getCharacterPosition( wordPosition );
  }
	
  public Long getCharacterPositionEnd( String field, long charPositionStart )
  {
    LOG.debug( "getCharacterPositionEnd " + charPositionStart );
    TokenProperties allTokens = allFieldTokens.get( field );
    if (allTokens == null) return null;
		
    String fieldString = fieldStringsMap.get( field );
    if (fieldString == null) return null;
		
    for (long chPos = charPositionStart; chPos < fieldString.length(); chPos++ )
    {
      char ch = fieldString.charAt( (int)chPos );
      if (tokenDelimiter.indexOf( ch ) >= 0)
      {
        return new Long( chPos );
      }
    }
		
    return new Long( fieldString.length() );
  }
	
	
  private TokenProperties getTokenProperties( String field, String term, boolean caseSensitive )
  {
    if ( term == null || term.trim().length() == 0) return null;
		
    HashMap<String,TokenProperties> fieldMap = tokenMap.get( ((caseSensitive) ? term : term.toLowerCase( )) );
    if (fieldMap == null) return null;
		
    TokenProperties tokenProps = fieldMap.get( field );
    return tokenProps;
  }
	
	
  // Methods to get Snippets for a phrase (find character position of first and last words, get Snippet)
  public String getExcerpt( String field, String phrase )
  {
    List<String> phraseTerms = StringTransform.getStringList( phrase, tokenDelimiter );
    if (phraseTerms.size() == 1)
    {
      String term = phraseTerms.get( 0 );
      return term;
    }
    else
    {
      ArrayList<TokenProperties> propLists = new ArrayList<TokenProperties>( );
      for (int i = 0; i < phraseTerms.size(); i++)
      {
        String pTerm = phraseTerms.get( i );
        TokenProperties tokProps = getTokenProperties( field, pTerm, caseSensitive );
        if ( tokProps == null) return null;
        propLists.add(  tokProps );
      }
			
      String lastTerm = phraseTerms.get( phraseTerms.size( ) - 1 );
			
      TokenProperties firstProps = propLists.get( 0 );
      TokenProperties lastProps = null;
      List<Integer> firstList = firstProps.getWordPositionIntegers( );
      if (firstList == null) return null;
			
      for (int i = 0; i < firstList.size(); i++)
      {
        int firstPos = firstList.get( i ).intValue( );
        int lastPos = 0;
        boolean isPhrase = true;
        for (int j = 1; j < propLists.size( ); j++)
        {
          lastPos = firstPos + j;
          lastProps = propLists.get( j );
          List<Integer> nextList = lastProps.getWordPositionIntegers( );
          if (nextList == null || !nextList.contains( new Integer( lastPos )))
          {
            isPhrase = false;
            break;
          }
        }
        if (isPhrase)
        {
          long firstCharPos = firstProps.getCharacterPosition( firstPos );
          long lastCharPos = lastProps.getCharacterPosition( lastPos ) + lastTerm.length( );
					
          String fieldStr = fieldStringsMap.get( field );
          return new String( fieldStr.substring( (int)firstCharPos, (int)lastCharPos ));
        }
      }
			
      return null;
    }
  }
    
  public void setCaseSensitive( boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
  }
    
  public String getDelimiter( )
  {
    return tokenDelimiter;
  }
	
  // inner class to hold the token properties - word position, character position, sentence positions
  // for a token.
  // Extends DataObject so that we can attach positional metadata to terms
  class TokenProperties extends DataObject
  {
    private IntegerListProperty wordPositionList;
    private HashMap<Integer,Long> wordCharacterMap;
    private IntegerListProperty sentencePositionList;
		
    // Methods called during tokenize operation
    void addWordPosition( int position )
    {
      if (wordPositionList == null) wordPositionList = new IntegerListProperty( );
      wordPositionList.addInteger( position );
    }
		
    void addCharacterPosition( int wordPosition, long characterPosition )
    {
      if (wordCharacterMap == null) wordCharacterMap = new HashMap<Integer,Long>( );
      LOG.debug( "word pos " + wordPosition + " -> " + characterPosition );
      wordCharacterMap.put( new Integer( wordPosition ), new Long( characterPosition ) );
    }
		
    void setWordPositionList( IntegerListProperty wordPositionList )
    {
      this.wordPositionList = wordPositionList;
      wordPositionList.setName( "wordPositionList" );
    }
		
    public IntegerListProperty getWordPositionList( )
    {
      return this.wordPositionList;
    }
		
    List<Integer> getWordPositionIntegers( )
    {
      return (wordPositionList != null) ? wordPositionList.getIntegerList( ) : null;
    }
	
		
    public Long getCharacterPosition( int wordPosition )
    {
      LOG.debug( "getCharacterPosition( " + wordPosition + " ) = " + wordCharacterMap.get( new Integer( wordPosition ) ));
      return wordCharacterMap.get( new Integer( wordPosition ));
    }
		
    void setSentencePositionList( IntegerListProperty sentencePositionList )
    {
      this.sentencePositionList = sentencePositionList;
      sentencePositionList.setName( "sentencePositionList" );
    }
		
    public IntegerListProperty getSentencePositionList(  )
    {
      return this.sentencePositionList;
    }
  }
	
  public class Sentence
  {
    Sentence( int sentenceNumber, int firstToken, int lastToken, String sentence )
    {
        	
    }
  }

}
