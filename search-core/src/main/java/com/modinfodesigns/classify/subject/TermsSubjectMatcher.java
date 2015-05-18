package com.modinfodesigns.classify.subject;

import java.util.Set;

import com.modinfodesigns.classify.IndexMatcherException;
import com.modinfodesigns.classify.IndexMatcherFactory;
import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.IIndexMatcherFactory;
import com.modinfodesigns.classify.MatchStatistics;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a list of terms or phrases as "evidence" terms for the subject match.
 * 
 * @author Ted Sullivan
 */

public class TermsSubjectMatcher extends DataObject implements ISubjectMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( TermsSubjectMatcher.class );

  private String subjectName;
    
  private IIndexMatcher proxyMatcher;
    
  // At Document-Subject level - can be used to create 'keywords' field for
  // search query boosting.
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  public void setSubjectName( String subjectName )
  {
    if (subjectName == null)
    {
      LOG.error( "setSubjectName( ): NULL!" );
      return;
    }
        
    this.subjectName = subjectName;
  }
    
  @Override
  public String getSubject( MatchStatistics matchStats )
  {
    return this.subjectName;
  }
    
  public void setIndexMatcher( IIndexMatcher indexMatcher )
  {
    if (indexMatcher == null)
    {
      LOG.error( "setIndexMatcher( ): NULL!" );
      return;
    }
        
    this.proxyMatcher = indexMatcher;
  }
    
  public void setIndexMatcherFactory( IIndexMatcherFactory indexMatcherFactory )
  {
    if (indexMatcherFactory == null)
    {
      LOG.error( "setIndexMatcherFactory( ): NULL! " );
      return;
    }
        
    IIndexMatcher[] matchers = indexMatcherFactory.createIndexMatchers( );
    if (matchers != null && matchers.length > 0)
    {
      this.proxyMatcher = matchers[0];
    }
    else
    {
      LOG.error( "IndexMatcherFactory created 0 IndexMatchers!" );
    }
  }
    
  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    if (proxyMatcher == null) throw new IndexMatcherException( "Proxy Index Matcher is NULL!" );
        
    MatchStatistics matchStats = proxyMatcher.getMatchStatistics( invIndex );
    matchStats.setIndexMatcher( this );
        
    LOG.debug( getSubject( matchStats ) + " matches = " + matchStats.matches() );
    LOG.debug( "getScore( ) = " + matchStats.getScore( ) );
        
    return matchStats;
  }

  @Override
  public Set<String> getMatchTerms()
  {
    return proxyMatcher.getMatchTerms( );
  }

  @Override
  public Set<String> getMatchPhrases()
  {
    return proxyMatcher.getMatchPhrases( );
  }

  @Override
  public void addMatchProperties(IPropertyHolder input, MatchStatistics matchStats)
  {
    MatchStatistics.addMatchProperties( input, matchStats, this );
  }

  @Override
  public void initialize(QueryTree qTree)
  {
    this.proxyMatcher = IndexMatcherFactory.createIndexMatcher( qTree );
  }

  @Override
  public void setAddMatchPhrasesMode(String addMatchPhrasesMode)
  {
    this.addMatchPhrasesMode = addMatchPhrasesMode;
  }

  @Override
  public String getAddMatchPhrasesMode()
  {
    return addMatchPhrasesMode;
  }

  @Override
  public void setAddMatchPhrasesProperty(String addMatchPhrasesProperty)
  {
    this.addMatchPhrasesProperty = addMatchPhrasesProperty;
  }

  @Override
  public String getAddMatchPhrasesProperty()
  {
    return addMatchPhrasesProperty;
  }

  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    return false;
  }

}
