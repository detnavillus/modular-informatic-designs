package com.modinfodesigns.classify;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.search.QueryTree;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotIndexMatcher extends DataObject implements IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( NotIndexMatcher.class );

  IIndexMatcher innerMatcher;
    
  private HashSet<String> noTerms = new HashSet<String>( );
    
  public NotIndexMatcher( ) {  }
    
  public NotIndexMatcher( IIndexMatcher inner )
  {
    this.innerMatcher = inner;
  }
    
  public NotIndexMatcher( QueryTree qTree )
  {
    initialize( qTree );
  }
    
  public void initialize( QueryTree qTree )
  {
    LOG.debug( "initialize( )..." );
    	
    // get first child in qTree ..
    List<QueryTree> children = qTree.getSubTrees( );
    if (children != null && children.size() > 0)
    {
      QueryTree firstChild = children.get( 0 );
      LOG.debug( "Got child " + firstChild.getOperator( ) );
      innerMatcher = IndexMatcherFactory.createIndexMatcher( firstChild );
    }
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
      return !matchStats.matches( );
    }
    catch (IndexMatcherException ime )
    {
      return false;
    }
  }

  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    MatchStatistics innerStats = innerMatcher.getMatchStatistics( invIndex );
		
    innerStats.setMatches( !innerStats.matches( ) );
    	
    return innerStats;
  }
	
  public Set<String> getMatchTerms(  )
  {
    return noTerms;
  }
    
  public Set<String> getMatchPhrases( )
  {
    return noTerms;
  }
    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
    	
  }
 
  @Override
  public void setAddMatchPhrasesMode(String termPropertyMode)
  {
		
  }

  @Override
  public void setAddMatchPhrasesProperty(String termPropertyName)
  {
		
  }

  @Override
  public String getAddMatchPhrasesMode()
  {
    return null;
  }

  @Override
  public String getAddMatchPhrasesProperty()
  {
    return null;
  }
}
