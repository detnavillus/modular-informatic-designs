package com.modinfodesigns.classify;

import com.modinfodesigns.entity.PhrasePositionMap;
import com.modinfodesigns.entity.EntityPositionMap;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.string.StringListProperty;

import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.quantity.QuantityOperationException;
import com.modinfodesigns.property.quantity.ProductQuantity;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchStatistics extends DataObject
{
  private transient static final Logger LOG = LoggerFactory.getLogger( MatchStatistics.class );
    
  HashMap<String,IQuantity> fieldScores;
  HashMap<String,IQuantity> fieldBoosts;
    
  HashMap<String,HashMap<String,PhraseStatistics>> phraseStats;
    
  ArrayList<MatchStatistics> children;
    
  boolean doesMatch = false;
    
  ArrayList<LongRangeProperty> matchRanges;
    
  private IIndexMatcher matcher;
    
  private IProperty scoreProperty;
    
  private HitCount hitCount = null;
    
    
  public boolean matches( )
  {
    return doesMatch;
  }
   
  public void setMatches( boolean matches )
  {
    this.doesMatch = matches;
  }
    
    
  public ArrayList<String> getMatchPhrases( String field )
  {
    return getMatchPhrases( field, false );
  }
    
  public ArrayList<String> getMatchPhrases( String field, boolean getChildPhrases )
  {
    LOG.debug( "getMatchPhrases( " + field + " )" );
    ArrayList<String> phrases = new ArrayList<String>( );
    if (phraseStats != null)
    {
      HashMap<String,PhraseStatistics> fieldStats = phraseStats.get( field );
      if (fieldStats != null)
      {
        for (Iterator<String> phraseIt = fieldStats.keySet().iterator(); phraseIt.hasNext(); )
        {
          phrases.add( phraseIt.next() );
        }
      }
    }
    else
    {
      LOG.debug( "phraseStats was NULL!" );
    }
        
    if (getChildPhrases && children != null)
    {
      for (int i = 0; i < children.size(); i++)
      {
        MatchStatistics chStats = children.get( i );
        ArrayList<String> chPhrases = chStats.getMatchPhrases( field, true );
        if (chPhrases != null)
        {
          phrases.addAll( chPhrases );
        }
      }
    }
        
    return phrases;
  }
    
    
  public void setIndexMatcher( IIndexMatcher matcher )
  {
    this.matcher = matcher;
  }
    
  public IIndexMatcher getIndexMatcher( )
  {
    return this.matcher;
  }
    
    
  public void addScore( String field, IQuantity score )
  {
    LOG.debug( "addScore " + field + " = " + score );
    if (fieldScores == null) fieldScores = new HashMap<String,IQuantity>( );
    fieldScores.put( field,  score );
  }
    
  public IQuantity getScore( String field )
  {
    if (fieldScores == null) return null;
        
    IQuantity fieldScore = fieldScores.get( field );
    if (fieldBoosts != null)
    {
      if ( fieldBoosts.get( field ) != null )
      {
        return new ProductQuantity( fieldScore, fieldBoosts.get( field ) );
      }
      else
      {
        // If fieldBoosts is set and this field doesn't have a boost
        // - assume that the score from this field is not wanted
        return null;
      }
    }
        
    return fieldScore;
  }
    
  public void addHitCounts( int hits )
  {
    if ( hitCount == null)
    {
      hitCount = new HitCount( hits );
    }
    else
    {
      hitCount.add( hits );
    }
  }
    
  public int getHitCounts( )
  {
    return (hitCount != null) ? hitCount.getIntegerValue() : 0;
  }
    
  public void addFieldBoost( String field, IQuantity boost )
  {
    if (fieldBoosts == null) fieldBoosts = new HashMap<String,IQuantity>( );
    fieldBoosts.put( field, boost );
  }
    
  public void setFieldBoosts( HashMap<String,IQuantity> boostMap )
  {
    this.fieldBoosts = boostMap;
  }
    
  /**
   * @return a composite Score for all fields by summing the individual field scores.
   */
  public IQuantity getScore( )
  {
    if (fieldScores == null) return null;
        
    IQuantity quantity = null;
        
    for (Iterator<String> scoreIt = fieldScores.keySet().iterator(); scoreIt.hasNext(); )
    {
      String field = scoreIt.next();
      LOG.debug( "Getting score for field " + field );
            
      IQuantity scoreQ = getScore( field );
      if (scoreQ != null)
      {
        try
        {
          if (quantity == null)
          {
            LOG.debug( "Copying " + scoreQ );
            quantity = (IQuantity)scoreQ.copy( );
          }
          else
          {
            LOG.debug( "Adding " + scoreQ );
            quantity.add( scoreQ );
          }
        }
        catch (QuantityOperationException qoe )
        {
          LOG.error( "Got QuantityOperationException: " + qoe );
        }
      }
      else
      {
        LOG.debug( "getScore returned NULL! " );
      }
    }
        
    LOG.debug( "Returning " + ((quantity != null) ? quantity.getValue( ) : "NULL") );
    return quantity;
  }
    
  public IProperty getScoreProperty()
  {
    return this.scoreProperty;
  }
    
  public void setScoreProperty( IProperty scoreProperty )
  {
    this.scoreProperty = scoreProperty;
  }
    
  public void addMatchRange( LongRangeProperty matchRange )
  {
    if (matchRange == null) return;
        
    if (matchRanges == null) matchRanges = new ArrayList<LongRangeProperty>( );
    matchRanges.add( matchRange );
  }
    
  public void addMatchRanges( List<LongRangeProperty> matchRanges )
  {
    if (matchRanges == null) return;
        
    if (this.matchRanges == null) this.matchRanges = new ArrayList<LongRangeProperty>( );
    this.matchRanges.addAll( matchRanges );
  }
    
  public List<LongRangeProperty> getMatchRanges(  )
  {
    return matchRanges;
  }
    
  public void addMatchStatistics( MatchStatistics childStats )
  {
    if (children == null) children = new ArrayList<MatchStatistics>( );
    children.add( childStats );
    if (childStats.getHitCounts() > 0)
    {
      addHitCounts( childStats.getHitCounts( ) );
    }
  }
    
  public List<MatchStatistics> getChildMatchStatistics(  )
  {
    return getChildMatchStatistics( false );
  }
    
  public List<MatchStatistics> getChildMatchStatistics( boolean recursive )
  {
    if (!recursive || children == null) return children;
        
    ArrayList<MatchStatistics> allStats = new ArrayList<MatchStatistics>( );
    for (int i = 0; i < children.size(); i++)
    {
      MatchStatistics childStats = children.get( i );
      allStats.add( childStats );
      List<MatchStatistics> grandkidStats = childStats.getChildMatchStatistics( true );
      if (grandkidStats != null)
      {
        allStats.addAll( grandkidStats );
      }
    }
        
    return allStats;
  }
    
  /**
   * Adds a word position span for a word or phrase that matches
   * @param field
   * @param textPosition
   */
  public void addPhrase( String field, String phrase, IntegerRangeProperty wordRange, LongRangeProperty characterRange )
  {
    LOG.debug( "addPhrase( " + field + ", " + wordRange.getValue()  + " )" );
        
    if (field == null || wordRange == null) return;
        
    if ( phraseStats == null) phraseStats = new HashMap<String,HashMap<String,PhraseStatistics>>( );
        
    HashMap<String,PhraseStatistics> fieldStats = phraseStats.get( field );
    if (fieldStats == null)
    {
      fieldStats = new HashMap<String,PhraseStatistics>( );
      phraseStats.put( field, fieldStats );
    }
        
    PhraseStatistics stats = fieldStats.get( phrase );
    if (stats == null)
    {
      stats = new PhraseStatistics( phrase );
      fieldStats.put( phrase, stats );
    }
        
    stats.addWordSpan( wordRange );
    stats.addCharacterSpan( characterRange );
  }
    
  public Iterator<String> getFields(  )
  {
    if (phraseStats != null)
    {
      return phraseStats.keySet().iterator( );
    }
    return null;
  }
    
    
  public ArrayList<IntegerRangeProperty> getTextSpans( String field )
  {
    if (phraseStats != null)
    {
      ArrayList<IntegerRangeProperty> spans = new ArrayList<IntegerRangeProperty>( );
      HashMap<String,PhraseStatistics> fieldStats = phraseStats.get( field );
      if (fieldStats != null)
      {
        for (Iterator<PhraseStatistics> phraseIt = fieldStats.values().iterator(); phraseIt.hasNext(); )
        {
          PhraseStatistics fieldStat = phraseIt.next();
          spans.addAll( fieldStat.wordSpans );
        }
      }
      return spans;
    }
    else if (children != null)
    {
      ArrayList<IntegerRangeProperty> allSpans = new ArrayList<IntegerRangeProperty>( );
      for (Iterator<MatchStatistics> childIt = children.iterator(); childIt.hasNext(); )
      {
        MatchStatistics childStats = childIt.next();
        ArrayList<IntegerRangeProperty> childSpans = childStats.getTextSpans( field );
        if (childSpans != null)
        {
          allSpans.addAll( childSpans );
        }
      }
            
      return allSpans;
    }
        
    return null;
  }
    
    
  public ArrayList<LongRangeProperty> getCharacterSpans( String field )
  {
    if(phraseStats != null)
    {
      ArrayList<LongRangeProperty> spans = new ArrayList<LongRangeProperty>( );
      HashMap<String,PhraseStatistics> fieldStats = phraseStats.get( field );
      if (fieldStats != null)
      {
        for (Iterator<PhraseStatistics> phraseIt = fieldStats.values().iterator(); phraseIt.hasNext(); )
        {
          PhraseStatistics fieldStat = phraseIt.next();
          spans.addAll( fieldStat.characterSpans );
        }
      }
      return spans;
    }
    else if (children != null)
    {
      ArrayList<LongRangeProperty> allSpans = new ArrayList<LongRangeProperty>( );
      for (Iterator<MatchStatistics> childIt = children.iterator(); childIt.hasNext(); )
      {
        MatchStatistics childStats = childIt.next();
        ArrayList<LongRangeProperty> childSpans = childStats.getCharacterSpans( field );
        if (childSpans != null)
        {
          allSpans.addAll( childSpans );
        }
      }
        
      return allSpans;
    }
        
    return null;
  }
    
  public HashMap<String,EntityPositionMap> getEntityPositionMaps(  )
  {
    if (doesMatch == false) return null;
        
    HashMap<String,EntityPositionMap> fieldMaps = new HashMap<String,EntityPositionMap>( );
        
    addEntityPositionMaps( fieldMaps, matchRanges );
        
    return fieldMaps;
  }
    
  private void addEntityPositionMaps( HashMap<String,EntityPositionMap> fieldMaps, ArrayList<LongRangeProperty> matchRanges )
  {
    LOG.debug( "addEntityPositionMaps" );
    if (phraseStats != null)
    {
      for (Iterator<String> fieldIt = phraseStats.keySet().iterator(); fieldIt.hasNext(); )
      {
        String field = fieldIt.next();

        EntityPositionMap entityMap = fieldMaps.get( field );
        if (entityMap == null)
        {
          entityMap = new EntityPositionMap( );
          fieldMaps.put( field,  entityMap );
        }
          
        HashMap<String,PhraseStatistics> stats = phraseStats.get( field );
                
        for (Iterator<String> phraseIt = stats.keySet().iterator(); phraseIt.hasNext(); )
        {
          String phrase = phraseIt.next();
          LOG.debug( "checking '" + phrase + "'" );
          PhraseStatistics phraseStat = stats.get( phrase );
          ArrayList<IntegerRangeProperty> wordPositions = phraseStat.wordSpans;
          ArrayList<LongRangeProperty> charPositions = phraseStat.characterSpans;
                    
          ArrayList<IntegerRangeProperty> filteredPositions = new ArrayList<IntegerRangeProperty>( );
          ArrayList<LongRangeProperty> filteredCharPositions = new ArrayList<LongRangeProperty>( );
                    
          for (int i = 0; i < charPositions.size(); i++)
          {
            LongRangeProperty charPosition = charPositions.get( i );
            if (isInside( charPosition, matchRanges ))
            {
              filteredPositions.add( wordPositions.get( i ) );
              filteredCharPositions.add( charPositions.get( i ) );
            }
          }
                
          PhrasePositionMap eMapper = new PhrasePositionMap( phraseStat.matchPhrase, filteredPositions, filteredCharPositions );
          entityMap.addPhrasePositionMap( eMapper );
        }
      }
    }
        
    if (children != null)
    {
      for (int i = 0; i < children.size(); i++ )
      {
        MatchStatistics childStats = children.get( i );
        childStats.addEntityPositionMaps( fieldMaps, matchRanges );
      }
    }
  }
    
  private boolean isInside( LongRangeProperty lrp, ArrayList<LongRangeProperty> ranges  )
  {
    for (int i = 0; i < ranges.size(); i++)
    {
      LongRangeProperty comp = ranges.get( i );
      if (comp.contains( lrp )) return true;
    }
        
    return false;
  }
    
  class PhraseStatistics
  {
    ArrayList<LongRangeProperty> characterSpans;
    ArrayList<IntegerRangeProperty> wordSpans;
    String matchPhrase;
        
    PhraseStatistics( String phrase )
    {
      matchPhrase = phrase;
    }
        
    void addWordSpan( IntegerRangeProperty wordSpan )
    {
      if (wordSpans == null) wordSpans = new ArrayList<IntegerRangeProperty>( );
      wordSpans.add( wordSpan );
    }
        
    void addCharacterSpan( LongRangeProperty charSpan )
    {
      if (characterSpans == null) characterSpans = new ArrayList<LongRangeProperty>( );
      characterSpans.add( charSpan );
    }
  }
    
  public void addMatcherProperties( IPropertyHolder input )
  {
    addMatchProperties( input, this, this.matcher );
  }
    
  public static void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats, IIndexMatcher matcher )
  {
    if (input == null || matcher == null || matchStats == null) return;
        
    for (Iterator<IProperty> propIt = matcher.getProperties( );    propIt.hasNext(); )
    {
      IProperty prop = propIt.next( );
      input.addProperty( prop.copy( ) );
    }
        
    // Add properties added to matchStats
    if (matchStats.getPropertyNames() != null)
    {
      for (Iterator<String> strit = matchStats.getPropertyNames(); strit.hasNext(); )
      {
        String propertyName = strit.next();
        IProperty taggerProp = matchStats.getProperty( propertyName );
        input.addProperty( taggerProp.copy() );
      }
    }
    
    // Check if the matcher.getAddMatchTermsProperty is not null
    // and getAddMatchTermsMode is not null.
    // if Mode == MATCHED, get the Matched phrases from matchStats
    // if Mode == MATCHABLE, get the match terms from IIndexMatcher
    if (matcher.getAddMatchPhrasesMode() != null && matcher.getAddMatchPhrasesProperty() != null)
    {
      String matchPhrasesMode = matcher.getAddMatchPhrasesMode( );
      String matchPhrasesProperty = matcher.getAddMatchPhrasesProperty( );
        
      // Create a PropertyList where PropertyList name = matchTermsProperty
      // for each field, put the match terms in the field
      if (matchPhrasesMode.equals( IIndexMatcher.MATCHED ))
      {
        PropertyList pList = new PropertyList(  );
        pList.setName( matchPhrasesProperty );
        for (Iterator<String> fieldIt = matchStats.getFields( ); fieldIt.hasNext(); )
        {
          String field = fieldIt.next();
          ArrayList<String> phrases = matchStats.getMatchPhrases( field );
          if (phrases != null)
          {
            StringListProperty sListProp = new StringListProperty( );
            sListProp.setName( field );
            pList.addProperty( sListProp );
            sListProp.addStrings( phrases.iterator() );
          }
        }
      }
      else if (matchPhrasesMode.equals( IIndexMatcher.MATCHABLE ))
      {
        Set<String> matchPhrases = matcher.getMatchPhrases(  );
        // Will not work for certain Matchers like EntityExtractorMatcher
        if (matchPhrases != null)
        {
          StringListProperty sListProp = new StringListProperty( );
          sListProp.setName( matchPhrasesProperty );
          sListProp.addStrings( matchPhrases.iterator() );
        }
      }
    }
  }
}
