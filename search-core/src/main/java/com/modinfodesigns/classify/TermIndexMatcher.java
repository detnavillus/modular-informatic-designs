package com.modinfodesigns.classify;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;

import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;
import com.modinfodesigns.utils.StringMethods;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TermIndexMatcher extends DataObject implements IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TermIndexMatcher.class );
	
  private String term;
  private HashSet<String> termSet;
    
  protected boolean caseSensitive = false;
    
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  public TermIndexMatcher( ) {  }

  public TermIndexMatcher( String term )
  {
    caseSensitive = getDefaultCase( term );
    setTerm( term );
  }
    
  public TermIndexMatcher( String term, boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
    setTerm( term );
  }
    
  public TermIndexMatcher( QueryTree qTree )
  {
    initialize( qTree );
  }
    
  // assumption is that if a term is ALL CAPS OR mixed case but not
  // initial caps (in which only the first character is a capital letter
  // then the intent is to preserve case.
  private boolean getDefaultCase( String term )
  {
    if (StringMethods.isAllCaps( term )) return true;
    return (StringMethods.isMixedCase( term ) && !StringMethods.isInitialCaps( term ));
  }
    
  @Override
  public String getName( )
  {
    if (super.getName() == null || super.getName().trim().length() == 0) return this.term;
    return super.getName( );
  }

  @Override
  public String getType( )
  {
    return "com.modinfodesigns.classify.TermIndexMatcher";
  }
    
  public void initialize( QueryTree qTree )
  {
    setTerm( qTree.getQueryText( ) );
  }
    
  public void setTerm( String term )
  {
    this.term = term;
    if (termSet == null) termSet = new HashSet<String>( );
    termSet.clear( );
    termSet.add( term );
    
    if ( !caseSensitive )
    {
      termSet.add( term.toLowerCase( ) );
    }
  }
    
  public void setCaseSensitive( boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
    if (this.term != null) setTerm( this.term );
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
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
      LOG.error( "Got IndexMatcherException " + ime );
      return false;
    }
  }


  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex )  throws IndexMatcherException
  {
    LOG.debug( "getMatchStatistics" );
        
    MatchStatistics matchStats = new MatchStatistics( );
        
    Set<String> fields = invIndex.getFields(  );
    boolean matches = false;
        
    for ( Iterator<String> fieldIt = fields.iterator(); fieldIt.hasNext(); )
    {
      String field = fieldIt.next();
      List<Integer> wordPositions = invIndex.getWordPositions( field, this.term, caseSensitive);
      int wordCounts = (wordPositions != null) ? wordPositions.size( ) : 0;
      HitCount fieldCounts = invIndex.getFieldCounts( field );
      if (fieldCounts != null)
      {
        TermFrequency termFreq = new TermFrequency( wordCounts, fieldCounts.getIntegerValue( ) );
        matchStats.addScore( field, termFreq );
        matchStats.addHitCounts( wordCounts );
      }
            
      if (wordPositions != null && wordPositions.size() > 0)
      {
        LOG.debug( term + " Matches! " );
        matches = true;
        for (int i = 0; i < wordPositions.size(); i++)
        {
          Integer wordPos = wordPositions.get( i );
                    
          IntegerRangeProperty irp = new IntegerRangeProperty( field, wordPos.intValue(), wordPos.intValue() );
          Long charStartPos = invIndex.getCharacterPosition( field, wordPos.intValue() );
          long charEndPos = charStartPos.longValue() + (long)term.length();
          LongRangeProperty lrp = new LongRangeProperty( field, charStartPos.longValue(), charEndPos );
          matchStats.addMatchRange( lrp );
                    
          matchStats.addPhrase( field, this.term, irp, lrp );
        }
      }
    }
        
    matchStats.setMatches( matches );
    if (matches)
    {
      LOG.debug( "set Match Phrase = " + this.term );
      // matchStats.setMatchPhrase( this.term );
      matchStats.setIndexMatcher( this );
    }
        
    return matchStats;
  }
    
  public Set<String> getMatchTerms(  )
  {
    return termSet;
  }
    
  public Set<String> getMatchPhrases( )
  {
    return termSet;
  }

    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
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
