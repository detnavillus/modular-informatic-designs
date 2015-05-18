package com.modinfodesigns.classify.subject;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.ArrayList;

import com.modinfodesigns.classify.IndexMatcherException;
import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.MatchStatistics;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;

import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an ISubjectMatcher using a Thesaurus: Thesaurus should have one main Subject type.
 * ThesaurusSubjectMatcher calculates the "best" match - most hits and most specific. For multiple
 * (orthogonal) subject classifications, use separate Thesaurus matchers.
 * 
 * @author Ted Sullivan
 */

// See ThesaurusIndexMatcherFactory - creates an Or Matcher that returns data tagged with
// classification path.

// Need to consolidate the classification Path set to create a score for each "node"
// best match is the longest path that has the top score.
// how will this work with poly-hierarchy?

public class ThesaurusSubjectMatcher extends DataObject implements ISubjectMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ThesaurusSubjectMatcher.class );

  // Internal subject tag used to coordinate getMatchStatistics with getSubject( )
  // The "Subject" is determined dynamically based on the best matching sub-node.
  private static String SUBJECT_TAG = "ThesaurusSubject_";
    
  // At Document-Subject level - can be used to create 'keywords' field for
  // search query boosting.
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  private IIndexMatcher[] subjectMatchers;
    
  private HashMap<String,ArrayList<IIndexMatcher>> termMatcherMap;
    
  public void setSubjectMatchers( IIndexMatcher[] subjectMatchers )
  {
    this.subjectMatchers = subjectMatchers;
        
    if ( subjectMatchers != null )
    {
      this.termMatcherMap = new HashMap<String,ArrayList<IIndexMatcher>>( );
      for (int i = 0; i < subjectMatchers.length; i++)
      {
        IIndexMatcher subjectMatcher = subjectMatchers[i];
        Set<String> terms = subjectMatcher.getMatchTerms( );
        for (Iterator<String> termIt = terms.iterator(); termIt.hasNext(); )
        {
          String term = termIt.next();
          ArrayList<IIndexMatcher> matchLst = termMatcherMap.get( term );
          if (matchLst == null)
          {
            matchLst = new ArrayList<IIndexMatcher>( );
            termMatcherMap.put( term, matchLst );
          }
          matchLst.add( subjectMatcher );
        }
      }
    }
  }
    
  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException
  {
    if (subjectMatchers == null)
    {
      LOG.error( "No Subject Matcher List! ");
      return null;
    }
        
    // ===========================================================================
    // Find the "best" Thesaurus Subject category
    // Tag MatchStatistics with the SUBJECT_TAG
    // ===========================================================================
        
    // filter based on termMatcherMap
    // Get list of possible Node matchers based on Term match
    // Create a map of possible Composite matchers: Classification Path -> CompositeNode
        
    HashMap<String,CompositeNode> compositeMatcherMap = new HashMap<String,CompositeNode> ( );
    Set<String> indexTermSet = invIndex.getTermSet();
    for (Iterator<String> termIt = indexTermSet.iterator(); termIt.hasNext(); )
    {
      String term = termIt.next();
      ArrayList<IIndexMatcher> posMatchers = termMatcherMap.get( term );
      if (posMatchers != null)
      {
        // for each Node matcher
        //    Get a Score
        //    For each classificationPath Node above Node - Create or Get a CompositeNode and
        //      set or add the score from the Node matcher
        for (int i = 0; i < posMatchers.size(); i++)
        {
          IIndexMatcher posMatcher = posMatchers.get( i );
          MatchStatistics posStats = posMatcher.getMatchStatistics( invIndex );
          if (posStats.getScore() != null && posStats.getScore().getQuantity() > 0.0)
          {
            addCompositeNodes( posMatcher, posStats, compositeMatcherMap );
          }
        }
      }
    }
        
    CompositeNode bestNode = null;
    // Iterate through the possible CompositeNodes (compositeMatcherMap.values( ) )
    // if the score > bestNode || bestNode is null - take it
    // if the score == bestNode - determine which has the longer path and take that one
    // if path lengths are equal but different = may need to create array
    
    // Add the match Statistics for the Top Composite Matcher
    for (Iterator<CompositeNode> compIt = compositeMatcherMap.values().iterator(); compIt.hasNext(); )
    {
      CompositeNode compNode = compIt.next();
      if (bestNode == null || compNode.cumulativeScore.getQuantity() > bestNode.cumulativeScore.getQuantity() )
      {
        bestNode = compNode;
      }
      else if (bestNode != null && compNode.cumulativeScore.getQuantity() == bestNode.cumulativeScore.getQuantity() )
      {
        if ( compNode.getClassPathLength( ) > bestNode.getClassPathLength(  ))
        {
          bestNode = compNode;
        }
      }
    }
        
    if (bestNode != null)
    {
      MatchStatistics bestStats = bestNode.compositeStats;
      bestStats.setIndexMatcher( this );
      bestStats.setMatches( true );
        
      String subjectProp = SUBJECT_TAG + this.getName( );
      String subjectName = bestNode.name;
      bestStats.addProperty( new StringProperty( subjectProp, subjectName ));
        
      return bestStats;
    }
        
    return null;
  }
    
  private void addCompositeNodes( IIndexMatcher subMatcher, MatchStatistics matchStats, HashMap<String,CompositeNode> compositeNodeMap )
  {
    IProperty classPathProp = subMatcher.getProperty( "ClassificationPath" );
    if (classPathProp != null)
    {
      String[] classPaths = getClassificationPaths( classPathProp );
        
      for (int c = 0; c < classPaths.length; c++)
      {
        ArrayList<String> ancestorPaths = getAncestorPaths( classPaths[c] );
            
        // for all classificationPaths up to root children (were path size is > 1)
        // look up the CompositeNode for this path..
        if (ancestorPaths != null)
        {
          for (int i = 0; i < ancestorPaths.size( ); i++)
          {
            String ancestorPath = ancestorPaths.get( i );
            CompositeNode cn = compositeNodeMap.get( ancestorPath );
            if (cn == null)
            {
              cn = new CompositeNode( );
              cn.compositeStats = new MatchStatistics( );
              cn.compositeStats.addMatchStatistics( matchStats );
              cn.classificationPath = ancestorPath;
              cn.name = subMatcher.getName( );
              cn.cumulativeScore = matchStats.getScore( );
              compositeNodeMap.put( ancestorPath, cn );
            }
            else
            {
              try
              {
                // Accumulate scores on this node
                cn.cumulativeScore = cn.cumulativeScore.add( matchStats.getScore( ) );
                cn.compositeStats.addMatchStatistics( matchStats );
              }
              catch ( QuantityOperationException qoe )
              {
                LOG.error( "QuantityOperationException!" + cn.cumulativeScore );
              }
            }
          }
        }
      }
    }
  }
    
  private String[] getClassificationPaths( IProperty classPathProp )
  {
    if (classPathProp == null) return new String[0];
        
    if (classPathProp instanceof StringListProperty)
    {
      StringListProperty slProp = (StringListProperty)classPathProp;
      return slProp.getStringList( );
    }
    else if (classPathProp instanceof PropertyList)
    {
      PropertyList pl = (PropertyList)classPathProp;
      ArrayList<String> plStrings = new ArrayList<String>( );
      for (Iterator<IProperty> propIt = pl.getProperties(); propIt.hasNext(); )
      {
        IProperty subProp = propIt.next();
        plStrings.add( subProp.getValue() );
      }
      String[] strings = new String[ plStrings.size() ];
      plStrings.toArray( strings );
      return strings;
    }
        
    String[] strings = new String[1];
    strings[0] = classPathProp.getValue( );
    return strings;
  }
    
  private ArrayList<String> getAncestorPaths( String classificationPath )
  {
    String[] pathParts = classificationPath.split( "/" );
    if (pathParts == null || pathParts.length < 2) return null;
    
    ArrayList<String> ancestorPaths = new ArrayList<String>( );
    for (int i = 1; i < pathParts.length; i++)
    {
      StringBuffer strbuf = new StringBuffer( );
      for (int j = 0; j <= i; j++)
      {
        if (strbuf.length() > 0) strbuf.append( "/" );
        strbuf.append( pathParts[j] );
      }
            
      ancestorPaths.add( strbuf.toString() );
    }
    
    return ancestorPaths;
  }

  @Override
  public Set<String> getMatchTerms()
  {
    if (subjectMatchers == null)
    {
      LOG.error( "Cannot get Match Terms set: subjectMatchers list is NULL!" );
      return null;
    }
        
    HashSet<String> matchTermsSet = new HashSet<String>( );
    for (int i = 0; i < subjectMatchers.length; i++)
    {
      Set<String> matchTerms = subjectMatchers[i].getMatchTerms( );
      if (matchTerms != null)
      {
        matchTermsSet.addAll( matchTerms );
      }
    }
        
    return matchTermsSet;
  }

  @Override
  public Set<String> getMatchPhrases()
  {
    if (subjectMatchers == null)
    {
      LOG.error( "Cannot get Match Terms set: subjectMatchers list is NULL!" );
      return null;
    }
        
    HashSet<String> matchPhraseSet = new HashSet<String>( );
    for (int i = 0; i < subjectMatchers.length; i++)
    {
      Set<String> matchPhrases = subjectMatchers[i].getMatchPhrases( );
      if (matchPhrases != null)
      {
        matchPhraseSet.addAll( matchPhrases );
      }
    }
        
    return matchPhraseSet;
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

  @Override
  public String getSubject( MatchStatistics matchStats )
  {
    // Get the internal tag that was added to matchStats
    IProperty subjectProp = matchStats.getProperty( SUBJECT_TAG + this.getName( ) );
    return (subjectProp != null) ? subjectProp.getValue() : null;
  }

  class CompositeNode
  {
    String name;
    
    String classificationPath;
        
    // Temporary score used to compute the "best" subject for this matcher
    IQuantity cumulativeScore;
    
    MatchStatistics compositeStats;
    
    int getClassPathLength( )
    {
      // determine length by number of separators in path ...
      String[] subPaths = (classificationPath != null) ? classificationPath.split( "/" ) : new String[0];
      return subPaths.length;
    }
  }

}
