package com.modinfodesigns.classify.subject;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.IndexMatcherException;
import com.modinfodesigns.classify.IndexMatcherFactory;
import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.MatchStatistics;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.search.QueryTree;

import com.modinfodesigns.security.IUserCredentials;

import java.util.Set;

public class SubjectMatcher extends DataObject  implements ISubjectMatcher
{
  private String subjectName;
  private IIndexMatcher indexMatcher;
    
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  public String getSubject( MatchStatistics matchStats )
  {
    return subjectName;
  }
    
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    if (indexMatcher == null)
    {
      throw new IndexMatcherException( "IndexMatcher is NULL!" );
    }
      
    return indexMatcher.getMatchStatistics( invIndex );
  }
    
  public Set<String> getMatchTerms(  )
  {
    return (indexMatcher != null ) ? indexMatcher.getMatchTerms( ) : null;
  }
    
  public Set<String> getMatchPhrases( )
  {
    return (indexMatcher != null ) ? indexMatcher.getMatchPhrases( ) : null;
  }
    
    
  public void initialize( QueryTree qTree )
  {
    this.indexMatcher = IndexMatcherFactory.createIndexMatcher( qTree );
  }
    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
    MatchStatistics.addMatchProperties( input, matchStats, this );
  }
    
  @Override
  public void setAddMatchPhrasesMode( String addMatchPhrasesMode )
  {
    this.addMatchPhrasesMode = addMatchPhrasesMode;
  }
    
  @Override
  public void setAddMatchPhrasesProperty( String addMatchPhrasesProperty )
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

  @Override
  public boolean equals( IUserCredentials user, IProperty prop) {
    return false;
  }
}