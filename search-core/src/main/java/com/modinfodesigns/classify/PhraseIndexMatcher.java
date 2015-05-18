package com.modinfodesigns.classify;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.utils.StringMethods;

import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhraseIndexMatcher extends DataObject implements IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PhraseIndexMatcher.class );

  private String phrase;
  private boolean caseSensitive = false;
    
  private String termDelimiter = " .,()/-";

  private HashSet<String> termSet;
  private HashSet<String> phraseSet;
    
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  public PhraseIndexMatcher( ) {  }
    
  public PhraseIndexMatcher( String phrase )
  {
    setPhrase( phrase );
    initSets( );
  }
    
  public PhraseIndexMatcher( String phrase, boolean caseSensitive )
  {
    setPhrase( phrase );
    this.caseSensitive = caseSensitive;
    initSets( );
  }
    
  public PhraseIndexMatcher( QueryTree qTree )
  {
    initialize( qTree );
  }
    
  public void initialize( QueryTree qTree )
  {
    setPhrase( qTree.getQueryText( ) );
    initSets( );
  }
    
  // Set case sensitive to false except if the terms are all caps indicating
  // an acronym set OR mixed but not initial capitals -
  // - call initSets after setting phrase and case sensitivity
  // so that case insensitive matching will work in all cases.
  public void setPhrase( String phrase )
  {
    this.phrase = phrase;
    this.caseSensitive = getDefaultCase( phrase );
    initSets( );
  }
    
  private boolean getDefaultCase( String phrase )
  {
    return (StringMethods.isAllCaps( phrase ));
  }
    
  @Override
  public String getName( )
  {
    if (super.getName() == null || super.getName().trim().length() == 0) return this.phrase;
    return super.getName( );
  }
    
  @Override
  public String getType( )
  {
    return "com.modinfodesigns.classify.PhraseIndexMatcher";
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property)
  {
    if (property == null || !(property instanceof InvertedIndex))
    {
      return false;
    }
    try
    {
      MatchStatistics matchStats = getMatchStatistics( (InvertedIndex)property );
      return matchStats.matches( );
    }
    catch (IndexMatcherException ime )
    {
      return false;
    }
  }

  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex )  throws IndexMatcherException
  {
    // LOG.debug( "getMatchStatistics( ): " + phrase );
        
    MatchStatistics matchStats = new MatchStatistics( );
    Set<String> fields = invIndex.getFields( (caseSensitive) ? phrase : phrase.toLowerCase( ) );
    if (fields == null || fields.size() == 0)
    {
      // LOG.debug( "Doesn't match!" );
      matchStats.setMatches( false );
      return matchStats;
    }
        
    // ---------------------------------------------------------------------
    // for each field, get Phrase count - save field - counts -
    // used for field boosting...
    // ---------------------------------------------------------------------
    for (Iterator<String> it = fields.iterator(); it.hasNext(); )
    {
      String field = it.next( );
      int phraseCount = invIndex.getPhraseCount( field, phrase, caseSensitive );
      HitCount fieldCounts = invIndex.getFieldCounts( field );
      if (phraseCount > 0 && fieldCounts != null)
      {
        TermFrequency termFreq = new TermFrequency( phraseCount, fieldCounts.getIntegerValue( ) );
        matchStats.addScore( field, termFreq );
        matchStats.addHitCounts( phraseCount );
      }
            
      List<Integer> phrasePositions = invIndex.getPhrasePositions( field,  phrase, caseSensitive);
      List<String> phraseTerms = StringTransform.getStringList( phrase, invIndex.getDelimiter() );
      if (phrasePositions != null && phrasePositions.size() > 0)
      {
        matchStats.setMatches( true );
        matchStats.setIndexMatcher( this );
                
        LOG.debug( phrase + " Matches! (caseSensitive = " + caseSensitive + " )" );
                
        for (int i = 0; i < phrasePositions.size(); i++)
        {
          Integer wordPos = phrasePositions.get( i );
          IntegerRangeProperty irp = new IntegerRangeProperty( field, wordPos.intValue( ), (wordPos.intValue() + phraseTerms.size() - 1));
                    
          Long charStartPos = invIndex.getCharacterPosition( field, wordPos.intValue() );
          long charEndPos = charStartPos.longValue() + (long)phrase.length();
          LongRangeProperty lrp = new LongRangeProperty( field, charStartPos.longValue(), charEndPos );
          matchStats.addMatchRange( lrp );
                    
          matchStats.addPhrase( field, this.phrase, irp, lrp );
        }
      }
    }
        
    // ---------------------------------------------------------------------
    // get the Phrase Spans (word and character)
    // ---------------------------------------------------------------------
    // LOG.debug( "returning " + matchStats );
    // LOG.debug( "" + matchStats.getIndexMatcher() );
    return matchStats;
  }
    
  public Set<String> getMatchTerms(  )
  {
    return termSet;
  }

    
  public Set<String> getMatchPhrases( )
  {
    return this.phraseSet;
  }

  // Initialize the term sets used by the classifier, and other clients
  // such as entity extractors. If case insensitive (caseSensitive = false)
  // provide both normal and lower-cased versions.
  private void initSets( )
  {
    if (termSet == null)
    {
      termSet = new HashSet<String>( );
    }
    else
    {
      termSet.clear( );
    }
        
    String[] terms = StringMethods.getStringArray( this.phrase, termDelimiter );
    for (int i = 0; i < terms.length; i++)
    {
      termSet.add( terms[i] );
      if (!caseSensitive)
      {
        termSet.add( terms[i].toLowerCase( ) );
      }
    }
        
    if (phraseSet == null)
    {
      phraseSet = new HashSet<String>( );
    }
    else
    {
      phraseSet.clear( );
    }
        
    phraseSet.add( this.phrase );
    if (!caseSensitive)
    {
      phraseSet.add( this.phrase.toLowerCase( ) );
    }
  }
    
    
  public IQuantity getScore( InvertedIndex invIndex )
  {
    HitCount hitCount = new HitCount( );
    int totalHits = 0;
    Set<String> fields = invIndex.getFields( this.phrase );
    if (fields != null)
    {
      for (Iterator<String> it = fields.iterator(); it.hasNext(); )
      {
        int nPhrases = invIndex.getPhraseCount( it.next(), this.phrase );
        totalHits += nPhrases;
      }
    }
        
    hitCount.setValue( totalHits );
    return hitCount;
  }
    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
    // LOG.debug( "addMatchProperties( )..." );
    MatchStatistics.addMatchProperties( input, matchStats, this );
  }

  @Override
  public void setAddMatchPhrasesMode(String addMatchPhrasesMode)
  {
    this.addMatchPhrasesMode = addMatchPhrasesMode;
  }

  @Override
  public void setAddMatchPhrasesProperty(String addMatchPhrasesProperty)
  {
    this.addMatchPhrasesProperty = addMatchPhrasesProperty;
  }

  @Override
  public String getAddMatchPhrasesMode()
  {
    return this.addMatchPhrasesMode;
  }

  @Override
  public String getAddMatchPhrasesProperty()
  {
    return this.addMatchPhrasesProperty;
  }
}
