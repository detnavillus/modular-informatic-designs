package com.modinfodesigns.classify;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import java.util.Set;

/**
 * IIndexMatcher that checks the hit count of its delegate IIndexMatcher.
 * If the hit count is less than a set threshold, the match is negated.
 * 
 * @author Ted Sullivan
 */

public class MinCountIndexMatcher extends DataObject implements IIndexMatcher
{
  private IIndexMatcher proxyMatcher;
  private int minCount = 2;
    
  public MinCountIndexMatcher( ) {  }
    
  public MinCountIndexMatcher( IIndexMatcher proxyMatcher, int minCount )
  {
    this.proxyMatcher = proxyMatcher;
    this.minCount = minCount;
  }
    
  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    return (proxyMatcher != null) ? proxyMatcher.equals( user, property) : false;
  }

  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex )
			               throws IndexMatcherException
  {
    if (proxyMatcher == null) return null;
		
    MatchStatistics proxStats = proxyMatcher.getMatchStatistics( invIndex );
    if (proxStats.getHitCounts( ) >= minCount)
    {
      return proxStats;
    }

    MatchStatistics noMatchStats = new MatchStatistics( );
    noMatchStats.setMatches( false );
		
    return noMatchStats;
  }

  @Override
  public Set<String> getMatchTerms()
  {
    return (proxyMatcher != null) ? proxyMatcher.getMatchTerms( ) : null;
  }

  @Override
  public Set<String> getMatchPhrases()
  {
    return (proxyMatcher != null) ? proxyMatcher.getMatchPhrases( ) : null;
  }

  @Override
  public void addMatchProperties( IPropertyHolder input,
                                  MatchStatistics matchStats)
  {
    if (proxyMatcher != null)
    {
      proxyMatcher.addMatchProperties( input,  matchStats );
    }
  }

  @Override
  public void initialize( QueryTree qTree )
  {
    proxyMatcher = qTree.getIndexMatcher( );
  }

  @Override
  public void setAddMatchPhrasesMode(String addMatchPhrasesMode)
  {
    if (proxyMatcher != null)
    {
      proxyMatcher.setAddMatchPhrasesMode( addMatchPhrasesMode );
    }
  }

  @Override
  public String getAddMatchPhrasesMode()
  {
    return (proxyMatcher != null) ? proxyMatcher.getAddMatchPhrasesMode( ) : null;
  }

  @Override
  public void setAddMatchPhrasesProperty( String addMatchPhrasesProperty )
  {
    if (proxyMatcher != null)
    {
      proxyMatcher.setAddMatchPhrasesProperty( addMatchPhrasesProperty );
    }
  }

  @Override
  public String getAddMatchPhrasesProperty()
  {
    return (proxyMatcher != null) ? proxyMatcher.getAddMatchPhrasesProperty( ) : null;
  }

}
