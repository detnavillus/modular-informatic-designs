package com.modinfodesigns.classify.subject;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.IndexMatcherException;
import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.MatchStatistics;
import com.modinfodesigns.classify.MatchStatisticsScoreComparator;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.IntegerProperty;

import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a Subject level classification by using a set of individual subject matchers. These should use 'evidence'
 * terms or rules to define a Subject that will be scored (based on TermFrequency). The SubjectMatcher 
 * determines the most likely subjects based on ranked scores.
 *
 * SubjectClassifier is also an ISubjectMatcher. This enables Subject classification to be hierarchical
 * 
 * @author Ted Sullivan
 */

public class SubjectClassifier extends DataObject implements IIndexMatcher, ISubjectMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SubjectClassifier.class );

  private ArrayList<ISubjectMatcher> subjectMatchers;
  private String subjectPropertyName = "subject";
    
  private int maxSubjects = 2;
    
  // Need a threshold TermFrequency
  IQuantity classificationThreshold;
    
  private HashMap<String,IQuantity> fieldBoosts;
    
  public String getSubject( MatchStatistics matchStats )
  {
    return getName( );
  }
    
  public void setSubjectPropertyName( String subjectPropertyName )
  {
    this.subjectPropertyName = subjectPropertyName;
  }
    
  public void setMaxSubjects( String maxSubjectSt )
  {
    this.maxSubjects = Integer.parseInt( maxSubjectSt );
  }
    
  public void setMaxSubjects( int maxSubjects )
  {
    this.maxSubjects = maxSubjects;
  }
    
  public void addSubjectMatcher( ISubjectMatcher subjectMatcher )
  {
    if (subjectMatchers == null) subjectMatchers = new ArrayList<ISubjectMatcher>( );
    subjectMatchers.add( subjectMatcher );
  }
    
  public void setClassificationThreshold( IQuantity classificationThreshold )
  {
    this.classificationThreshold = classificationThreshold;
  }
    
  /**
   * Add Field Boost Map:  Input data should be in the format:
   *  field="[ field name ]" boost="[ integer value ]"
   * @param fieldBoostList
   */
  public void setFieldBoosts( DataList fieldBoostList )
  {
    fieldBoosts = new HashMap<String,IQuantity>( );
    for ( Iterator<DataObject> dit = fieldBoostList.getData(); dit.hasNext(); )
    {
      DataObject dob = dit.next();
      IProperty field = dob.getProperty( "field" );
      IProperty boost = dob.getProperty( "boost" );
      if (field != null && boost != null)
      {
        int boostVal = Integer.parseInt( boost.getValue( ) );
        IntegerProperty ip = new IntegerProperty( field.getName(), boostVal );
        fieldBoosts.put( field.getValue( ), ip );
      }
    }
  }
    
  public void addFieldBoost( String field, IQuantity boost )
  {
    if (fieldBoosts == null ) fieldBoosts = new HashMap<String,IQuantity>( );
    fieldBoosts.put( field, boost );
  }
    
  @Override
  public boolean equals( IUserCredentials user,  IProperty property )
  {
    return false;
  }

  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    LOG.debug( "getMatchStatistics( ) ..." );
    ArrayList<MatchStatistics> matchStats = new ArrayList<MatchStatistics>( );
        
    for ( Iterator<ISubjectMatcher> matchIt = subjectMatchers.iterator(); matchIt.hasNext(); )
    {
      ISubjectMatcher subjectMatcher = matchIt.next();
      MatchStatistics subjectStats = subjectMatcher.getMatchStatistics( invIndex );
      if (subjectStats.matches())
      {
        LOG.debug( "SubjectMatcher: " + subjectMatcher.getSubject( subjectStats ) + " matches!" );
        subjectStats.setFieldBoosts( fieldBoosts );
                
        if (classificationThreshold == null || subjectStats.getScore().getQuantity() > classificationThreshold.getQuantity())
        {
          matchStats.add( subjectStats );
        }
      }
      else
      {
        LOG.debug( "SubjectMatcher " + subjectMatcher.getSubject( subjectStats ) + " doesn't match!" );
      }
    }
        
    MatchStatisticsScoreComparator mssc = new MatchStatisticsScoreComparator( );
    mssc.setAscending( false );
    Collections.sort( matchStats, mssc );
        
    // =====================================================================
    // Find the top N MatchStatistics by score
    // Create a new MatchStatistics and add these top N as Child Matchers
    // =====================================================================
    MatchStatistics topStats = new MatchStatistics( );
    topStats.setMatches( matchStats.size() > 0);
    for (int i = 0; i < matchStats.size() && i < maxSubjects; i++ )
    {
      MatchStatistics matchStat = matchStats.get( i );
      topStats.addMatchStatistics( matchStat );
        
      // ==================================================================
      // Add Subject as a MatchStatistics property
      // Should check that this is not already added to MatchStatistics ...
      // ==================================================================
      ISubjectMatcher topSubjectMatcher = (ISubjectMatcher)matchStat.getIndexMatcher( );
        
      LOG.debug( "Adding " + subjectPropertyName + " = " + topSubjectMatcher.getSubject( matchStat ) );
      topStats.addProperty( new StringProperty(subjectPropertyName, topSubjectMatcher.getSubject( matchStat ) ));
    }
        
    return topStats;
  }

  @Override
  public Set<String> getMatchTerms()
  {
    HashSet<String> matchTerms = new HashSet<String>( );
    for (int i = 0; i < subjectMatchers.size(); i++)
    {
      IIndexMatcher subjectMatcher = subjectMatchers.get( i );
      Set<String> subjectTerms = subjectMatcher.getMatchTerms( );
      if ( subjectTerms != null )
      {
        matchTerms.addAll( subjectTerms );
      }
    }
        
    return matchTerms;
  }
    
  @Override
  public Set<String> getMatchPhrases()
  {
    HashSet<String> matchPhrases = new HashSet<String>( );
    for (int i = 0; i < subjectMatchers.size(); i++)
    {
      IIndexMatcher subjectMatcher = subjectMatchers.get( i );
      Set<String> subjectPhrases = subjectMatcher.getMatchPhrases( );
      if ( subjectPhrases != null )
      {
        matchPhrases.addAll( subjectPhrases );
      }
    }
        
    return matchPhrases;
  }

  @Override
  public void addMatchProperties(IPropertyHolder input, MatchStatistics matchStats)
  {
    MatchStatistics.addMatchProperties( input, matchStats, this );
  }

  @Override
  public void initialize(QueryTree qTree)
  {
      
  }

  @Override
  public void setAddMatchPhrasesMode(String addMatchPhrasesMode)
  {

  }

  @Override
  public String getAddMatchPhrasesMode()
  {
    return null;
  }

  @Override
  public void setAddMatchPhrasesProperty(String addMatchPhrasesProperty)
  {
      
  }

  @Override
  public String getAddMatchPhrasesProperty( )
  {
    return null;
  }

}
