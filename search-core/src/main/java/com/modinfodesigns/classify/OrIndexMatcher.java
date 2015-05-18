package com.modinfodesigns.classify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;

import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrIndexMatcher extends DataObject implements IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( OrIndexMatcher.class );

  private ArrayList<IIndexMatcher> childMatchers = new ArrayList<IIndexMatcher>( );
	
  private HashSet<String> termSet;
  private HashSet<String> phraseSet;
	
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
	
  public OrIndexMatcher( ) {  }
	
  public OrIndexMatcher( QueryTree qTree )
  {
    initialize( qTree );
  }
	
  public void initialize( QueryTree qTree )
  {
    List<QueryTree> children = qTree.getSubTrees( );
    for( int q = 0; q < children.size(); q++ )
    {
      QueryTree child = children.get( q );
      if (child != null)
      {
        IIndexMatcher chMatcher = child.getIndexMatcher( );
        if (chMatcher != null)
        {
          childMatchers.add( chMatcher );
        }
      }
    }
  }
	
  public void addIndexMatcher( IIndexMatcher matcher )
  {
    childMatchers.add( matcher );
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
      return false;
    }
  }

  @Override
  public String getType()
  {
    return "com.modinfodesigns.classify.OrIndexMatcher";
  }

  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex )  throws IndexMatcherException
  {
    MatchStatistics compositeStats = new MatchStatistics( );
    compositeStats.setIndexMatcher( this );
    compositeStats.setMatches( false );
		
    HashMap<String,IQuantity> fieldScoreMap = new HashMap<String,IQuantity>( );
    
    for (int i = 0; i < childMatchers.size(); i++)
    {
      IIndexMatcher chMatcher = childMatchers.get( i );
      MatchStatistics chStats = chMatcher.getMatchStatistics( invIndex );
      if (chStats != null && chStats.matches() )
      {
        LOG.debug( "chMatcher: " + chMatcher.getType() + ": " + chMatcher.getName() + " matches!" );
        compositeStats.addMatchStatistics( chStats );
        compositeStats.setMatches( true );
        List<LongRangeProperty> chRanges = chStats.getMatchRanges( );
        compositeStats.addMatchRanges( chRanges );
            
        for (Iterator<String> fieldIt = chStats.getFields(); (fieldIt != null && fieldIt.hasNext()); )
        {
          String field = fieldIt.next();
          IQuantity fieldScore = chStats.getScore( field );
          if (fieldScore != null)
          {
            IQuantity compositeFieldScore = fieldScoreMap.get( field );
            if (compositeFieldScore == null)
            {
              fieldScoreMap.put( field, fieldScore );
            }
            else
            {
              try
              {
                fieldScoreMap.put( field, compositeFieldScore.add( fieldScore ) );
              }
              catch ( QuantityOperationException qoe )
              {
                LOG.error( "QuantityOperationException !!" + qoe.getMessage( ) );
              }
            }
          }
        }
      }
    }
		
    if (compositeStats.matches( ) )
    {
      for (Iterator<String> fieldIt = fieldScoreMap.keySet().iterator(); fieldIt.hasNext(); )
      {
        String field = fieldIt.next();
        IQuantity fieldScore = fieldScoreMap.get( field );
        if (fieldScore != null)
        {
          compositeStats.addScore( field,  fieldScore );
        }
      }
    }
		
    // LoggingManager.debug( this,  "returning " + compositeStats );
    return compositeStats;
  }
	
  public Set<String> getMatchTerms(  )
  {
    if (termSet != null) return termSet;
    	
    synchronized( this )
    {
      if (termSet != null) return termSet;

      termSet = new HashSet<String>( );
      for (int i = 0; i < childMatchers.size(); i++)
      {
        IIndexMatcher chMatcher = childMatchers.get( i );
        Set<String> childSet = chMatcher.getMatchTerms( );
        if (childSet != null && childSet.size() > 0)
        {
          termSet.addAll( childSet );
        }
      }
    }
		
    return termSet;
  }
    
  public Set<String> getMatchPhrases( )
  {
    if (phraseSet != null) return phraseSet;
    	
    synchronized( this )
    {
      if (phraseSet != null) return phraseSet;
        
      phraseSet = new HashSet<String>( );
      for (int i = 0; i < childMatchers.size(); i++)
      {
        IIndexMatcher chMatcher = childMatchers.get( i );
        Set<String> childSet = chMatcher.getMatchPhrases( );
        if (childSet != null && childSet.size() > 0)
        {
          phraseSet.addAll( childSet );
        }
      }
    }
    	
    return phraseSet;
  }
   
    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
    LOG.debug( "addMatchProperties" );
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
