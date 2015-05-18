package com.modinfodesigns.classify;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.TreeMap;

import java.util.StringTokenizer;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.utils.FileMethods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes a set of Pattern strings that it matches text against. The patterns can contain
 * tokens that are mapped to other IIndexMatchers. The mapped IIndexMatcher must match a token
 * in the index at the relative position within the Pattern.
 * 
 * The Pattern String can also contain spacer tokens to indicate nearness. For example: [3] 
 * indicates that there can be from 0 to 3 "noise" tokens between the pattern tokens. [1-*]
 * (can also use regular expression format for spacers.  Terms or phrases are implemented using
 * TermIndexMatcher and PhraseIndexMatcher respectively.
 * 
 * @author Ted Sullivan
 */

// ============================================================================
// Need to compute the relative positions of the Matchers in the Pattern
// Create Ordered NearIndexMatchers for the Pattern terms, tokens and phrases
// Pattern Parsing ends up with one NearIndexMatcher for each pattern that has
// nested NearIndexMatchers for pattern parts ...
// ============================================================================

// Get the Pattern name in the first line of the pattern file
// e.g. Combination Therapy
//
// after matching, extract the evidence from the nested match statistics.
//   which of the patterns matched?  what placeholder matcher matched and
//   with what matchPhrase?
//

public class PatternMatcher extends DataObject implements IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( PatternMatcher.class );
    
  private static final String TOKEN_TAG = "TOKEN";
  private static final String PATTERN_TAG = "PATTERN";
    
  // the pattern that we are matching
  // e.g. 'DRUG1:DRUG2
  private String matchPattern;
    
  private String patternProperty;
    
  // map of Token String --> IIndexMatcher for that token
  private HashMap<String,IIndexMatcher> patternMatcherMap;
    
  private ArrayList<String> patterns;
    
  // compile the Patterns into a list of Composite Near Matchers
  private OrIndexMatcher patternMatchers;
    
  private String patternMatchFile;
    
  private static String delimiter = " .,:;-(){}/\\";
    
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  // ==============================================================
  // Nested Object Properties
  // ==============================================================
  private String nestedTopicProperty;
  private String nestedTopicTitleProperty;
  private String nestedTopicPhraseProperty;
    
  public void addPattern( String pattern )
  {
    LOG.debug( "addPattern: " + pattern );
    if (patterns == null) patterns = new ArrayList<String>( );
    patterns.add( pattern );
  }
    
  public void setPatternMatchFile( String patternMatchFile )
  {
    this.patternMatchFile = patternMatchFile;
  }
    
  public void addPatternMap( PatternMap pMap )
  {
    LOG.debug( "addPatternMap: " + pMap.getName( ) );
        
    if (patternMatcherMap == null) patternMatcherMap = new HashMap<String,IIndexMatcher>( );
    patternMatcherMap.put( pMap.getName( ), pMap.getIndexMatcher( ) );
  }
    
  public void setMatchPattern( String matchPattern )
  {
    LOG.debug( "setMatchPattern: " + matchPattern );
    this.matchPattern = matchPattern;
  }
    
  public void setPatternProperty( String patternProperty )
  {
    this.patternProperty = patternProperty;
  }
    
  public void setNestedTopicProperty( String nestedTopicProperty )
  {
    this.nestedTopicProperty = nestedTopicProperty;
  }
    
  public void setNestedTopicTitleProperty( String nestedTopicTitleProperty )
  {
    this.nestedTopicTitleProperty = nestedTopicTitleProperty;
  }

  public void setNestedTopicPhraseProperty( String nestedTopicPhraseProperty )
  {
    this.nestedTopicPhraseProperty = nestedTopicPhraseProperty;
  }

  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    return false;
  }


  @Override
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex )  throws IndexMatcherException
  {
    LOG.debug( "getMatchStatistics( )" );
        
    IIndexMatcher patternMatchers = initPatternMatchers( );
    MatchStatistics matchStats = patternMatchers.getMatchStatistics( invIndex );
    matchStats.setIndexMatcher( this );
        
    return matchStats;
  }
    
  private IIndexMatcher initPatternMatchers( )
  {
    LOG.debug( "initPatternMatchers" );
        
    if (patternMatchers != null) return patternMatchers;
        
    LOG.debug( "Initializing ..." );
    patternMatchers = new OrIndexMatcher( );
        
    initPatterns( );
        
    // for each pattern, create a composite NearIndexMatcher
    for (int i = 0; i < patterns.size(); i++)
    {
      String pattern = patterns.get( i );
      IIndexMatcher compositeMatcher = createCompositeMatcher( pattern );
      if (compositeMatcher != null)
      {
        compositeMatcher.addProperty(new StringProperty( PATTERN_TAG, pattern ));
                
        patternMatchers.addIndexMatcher( compositeMatcher );
      }
    }
        
    return patternMatchers;
  }
    
    
  private void initPatterns(  )
  {
    if (patterns != null) return;
        
    patterns = new ArrayList<String>( );
        
    String[] fileLines = FileMethods.readFileLines( patternMatchFile );
    for (int i = 0; i < fileLines.length; i++)
    {
      patterns.add( fileLines[i] );
    }
  }
    
    
  private IIndexMatcher createCompositeMatcher( String pattern )
  {
    LOG.debug( "createCompositePattern " + pattern );
        
    IIndexMatcher leftMatcher = null;
    String currentPhrase = null;
    int currentDistance = 1;
    int lastDistance = -1;
        
    StringTokenizer strtok = new StringTokenizer( pattern, delimiter );
    while( strtok.hasMoreTokens() )
    {
      String token = strtok.nextToken( );
      IIndexMatcher tokenMatcher = patternMatcherMap.get( token );
      if ( tokenMatcher != null )
      {
        if ( leftMatcher == null )
        {
          if (currentPhrase != null)
          {
            NearIndexMatcher compositeMatcher = new NearIndexMatcher( true ); // ordered
            LOG.debug( "Adding phrase matcher: '" + currentPhrase + "'" );
            compositeMatcher.addIndexMatcher( getMatcher( currentPhrase ) );
                         
            LOG.debug( "Adding token matcher for: '" + token + "'" );
                         
            // wrap this in an AndIndexMatcher - set a property in the outside
            // index matcher with the token name ...
            compositeMatcher.addIndexMatcher( wrapTokenMatcher( tokenMatcher, token ) );
                         
            LOG.debug( "currentDistance = " + currentDistance );
            compositeMatcher.setMinimumDistance( currentDistance );
            leftMatcher = compositeMatcher;
            currentPhrase = null;
            lastDistance = currentDistance;
            currentDistance = 1;
          }
          else
          {
            LOG.debug( "Adding token matcher for: " + token );
            leftMatcher = wrapTokenMatcher( tokenMatcher, token );
          }
        }
        else
        {
          NearIndexMatcher compositeMatcher = new NearIndexMatcher( true ); // ordered
          LOG.debug( "shifting leftMatcher ..." );
          compositeMatcher.addIndexMatcher( leftMatcher );
          LOG.debug( "adding distance " + ((lastDistance > 0) ? lastDistance : currentDistance) );
          compositeMatcher.setMinimumDistance( (lastDistance > 0) ? lastDistance : currentDistance );
          if (currentPhrase != null)
          {
            LOG.debug( "Adding phrase matcher: '" + currentPhrase + "'" );
            compositeMatcher.addMatcher( getMatcher( currentPhrase ) );
          }
                    
          LOG.debug( "Adding token matcher for: '" + token + "'" );
          compositeMatcher.addIndexMatcher( wrapTokenMatcher( tokenMatcher, token ) );
                    
          LOG.debug( "adding distance = " + currentDistance );
          compositeMatcher.addMinimumDistance( currentDistance );

          leftMatcher = compositeMatcher;
          lastDistance = currentDistance;
          currentDistance = 1;
          currentPhrase = null;
        }
      }
      else if ( tokenIsSpacer( token ))
      {
        currentDistance =  getDistance( token );
        LOG.debug( "Got Spacer Token " + currentDistance );
        if (leftMatcher == null)
        {
          LOG.debug( "Adding phrase matcher: '" + currentPhrase + "'" );
          leftMatcher = getMatcher( currentPhrase );
          currentPhrase = null;
        }
        else if (leftMatcher != null && currentPhrase != null)
        {
          LOG.debug( "currentPhrase is broken up ..." );
          NearIndexMatcher compositeMatcher = new NearIndexMatcher( true ); // ordered
          LOG.debug( "shifting leftMatcher ..." );
          compositeMatcher.addIndexMatcher( leftMatcher );
                    
          LOG.debug( "adding distance " + ((lastDistance > 0) ? lastDistance : currentDistance) );
          compositeMatcher.setMinimumDistance( (lastDistance > 0) ? lastDistance : currentDistance );
                    
          LOG.debug( "Adding phrase matcher: '" + currentPhrase + "'" );
          compositeMatcher.addMatcher( getMatcher( currentPhrase ) );
                    
          LOG.debug( "adding distance = " + currentDistance );
          compositeMatcher.addMinimumDistance( currentDistance );

          leftMatcher = compositeMatcher;
          lastDistance = currentDistance;
          currentDistance = 1;
          currentPhrase = null;
        }
        if (lastDistance == -1)
        {
          lastDistance = currentDistance;
        }
      }
      else
      {
        if (currentPhrase == null)
        {
          currentPhrase = token;
        }
        else
        {
          currentPhrase = currentPhrase + " " + token;
        }
        LOG.debug( "currentPhrase = " + currentPhrase );
      }
    }

    if (currentPhrase != null && currentPhrase.length() > 0)
    {
      if (leftMatcher == null)
      {
        LOG.debug( "Adding phrase matcher: '" + currentPhrase + "'" );
        return getMatcher( currentPhrase );
      }
      else
      {
        NearIndexMatcher compositeMatcher = new NearIndexMatcher( true );
                
        LOG.debug( "shifting leftMatcher ..." );
        compositeMatcher.addIndexMatcher( leftMatcher );
        compositeMatcher.setMinimumDistance( (lastDistance > 0) ? lastDistance : currentDistance );
        LOG.debug( "Adding phrase matcher: '" + currentPhrase + "'" );
        compositeMatcher.addIndexMatcher( getMatcher( currentPhrase ) );
        compositeMatcher.addMinimumDistance( currentDistance );

        return compositeMatcher;
      }
    }

    LOG.debug( "returning leftMatcher " + leftMatcher );
    return leftMatcher;
  }
    
  // Wrap the token matcher so that we can attach unique properties (pattern token DRUG1 or DRUG2 for the
  // i.e. reused Matcher
  private IIndexMatcher wrapTokenMatcher( IIndexMatcher tokenMatcher, String token )
  {
    LOG.debug( "wrapTokenMatcher " + token );
    AndIndexMatcher andMatcher = new AndIndexMatcher( );
    andMatcher.addIndexMatcher( tokenMatcher );
    andMatcher.addProperty( new StringProperty( TOKEN_TAG, token ));
    return andMatcher;
  }
    
    
  private IIndexMatcher getMatcher( String currentPhrase )
  {
    if (currentPhrase.indexOf( " " ) > 0)
    {
      return new PhraseIndexMatcher( currentPhrase );
    }
    else
    {
      return new TermIndexMatcher( currentPhrase );
    }
  }
    
  private boolean tokenIsSpacer( String token )
  {
    return ( token.startsWith( "[" ) && token.endsWith( "]" ) );
  }
    

  private int getDistance( String token )
  {
    String intStr = token.substring( 1, token.indexOf( "]" ) );
    return Integer.parseInt( intStr );
  }

    
  @Override
  public Set<String> getMatchTerms()
  {
    IIndexMatcher theIndexMatcher = initPatternMatchers( );
    // Collect the terms in the Pattern(s) plus any terms in the Token mapped IIndexMatchers
    return theIndexMatcher.getMatchTerms( );
  }

  @Override
  public Set<String> getMatchPhrases()
  {
    // Collect the phrases in the Pattern(s) plus any phrases in the Token-Mapped IIndexMatchers
    IIndexMatcher theIndexMatcher = initPatternMatchers( );
    return theIndexMatcher.getMatchPhrases( );
  }


  @Override
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
    // =============================================================================================
    //  Find the patterns that matched
    //  add the pattern name with the matched phrases from the mapped matchers that matched
    //  use the TOKEN_TAG and PATTERN_TAG to determine the pattern and token map that matched
    //  plug these into the main pattern
    //  e.g. Combination Therapy: DRUG1, DRUG2 becomes 'Combination Therapy: Aspirin, Clopidogrel'
    // =============================================================================================
    LOG.debug( "addMatchProperties" );
    List<MatchStatistics> patternStatList = matchStats.getChildMatchStatistics( );
    if (patternStatList != null && patternStatList.size() > 0)
    {
      LOG.debug( "Got " + patternStatList.size( ) + " stats " );
      for (int i = 0; i < patternStatList.size(); i++)
      {
        MatchStatistics patternStats = patternStatList.get( i );
                
        Iterator<String> fieldIt = patternStats.getFields( );
        while (fieldIt != null && fieldIt.hasNext() )
        {
          String field = fieldIt.next( );

          ArrayList<String> matchPhrases = patternStats.getMatchPhrases( field );
          if (matchPhrases != null)
          {
            // ===============================================================================
            //  For Each Matched Phrase - get the components that created it and
            //  compose an output string where the placeholders are replaced with
            //  the phrases that comprised the match.
            // ===============================================================================
            for (int p = 0; p < matchPhrases.size(); p++)
            {
              LOG.debug( "Match Phrase: " + matchPhrases.get( p ) );
              String matchPhrase = new String( matchPhrases.get( p ) );
                            
              // ----------------------------------------------------------------------------
              // create an array of tokens and a sorted list of the values that matched them
              // by their position in the Matched Phrase
              // ----------------------------------------------------------------------------
              ArrayList<String> tokenList = new ArrayList<String>( );
              TreeMap<Integer,String> tokenValueList = new TreeMap<Integer,String>( );
              ArrayList<MatchStatistics> matchedStatList = new ArrayList<MatchStatistics>( );
                            
              List<MatchStatistics> childStatList = patternStats.getChildMatchStatistics( true );
              if (childStatList != null)
              {
                for (int c = 0; c < childStatList.size(); c++ )
                {
                  MatchStatistics childStats = childStatList.get( c );
                  IIndexMatcher childMatcher = childStats.getIndexMatcher( );
                  if (childMatcher != null && childMatcher.getProperty( TOKEN_TAG ) != null)
                  {
                    String tokenName = childMatcher.getProperty( TOKEN_TAG ).getValue( );
                    tokenList.add( tokenName );
                    LOG.debug( "Adding tokenName = " + tokenName );
                                        
                    ArrayList<String> childPhrases = childStats.getMatchPhrases( field, true );
                    if (childPhrases != null)
                    {
                      for (int cp = 0; cp < childPhrases.size(); cp++)
                      {
                        String childPhrase = childPhrases.get( cp );
                        LOG.debug( "Testing childPhrase " + childPhrase );
                        int childPos = matchPhrase.toLowerCase().indexOf( childPhrase.toLowerCase( ) );
                        if ( childPos >= 0 )
                        {
                          tokenValueList.put( new Integer( childPos ), childPhrase );
                          matchedStatList.add( childStats );
                          LOG.debug( "Adding " + childPhrase + " at " + childPos );
                          String mask = getMask( childPhrase.length( ) );
                          matchPhrase = StringTransform.replaceSubstring( matchPhrase, childPhrase, mask );
                          LOG.debug( "matchPhrase is now: " + matchPhrase );
                          break;
                        }
                      }
                    }
                    else
                    {
                      LOG.debug( "childMatcher returns null Match phrases " + childMatcher );
                    }
                  }
                }
                                
                // ========================================================================
                //  Now replace the tokens that matched in the output match phrase
                //  e.g. "Combination Therapy: Drug, Drug" becomes
                //  "Combination Therapy: Aspirin, Clopidogrel"
                // ========================================================================
                String matchTemplate = matchPattern;
                Iterator<Integer> posList = tokenValueList.keySet().iterator();
                  
                for (int t = 0; t < tokenList.size() && posList.hasNext(); t++)
                {
                  String tokenName = tokenList.get( t );
                  String tokenValue = tokenValueList.get( posList.next( ) );
                  LOG.debug( "Replacing " + tokenName + " with " + tokenValue );
                                    
                  matchTemplate = StringTransform.replaceSubstring( matchTemplate, tokenName, tokenValue );
                  LOG.debug( "matchTemplate is now: " + matchTemplate );
                }
                                
                LOG.debug( "matchTemplate = " + matchTemplate );
                if (matchTemplate != null && patternProperty != null)
                {
                  input.addProperty( new StringProperty( patternProperty, matchTemplate ) );
                }
                                
                // ================================================================================
                //  Create a nested object - these are the searchable Facts
                //  Title = matchTemplate
                //  Snippet = matchPhrases.get( p )
                // ================================================================================
                if (nestedTopicProperty != null)
                {
                  DataObject nestedTopic = new DataObject( );
                  nestedTopic.setName( nestedTopicProperty );
                  input.addProperty( nestedTopic );
                    
                  if (nestedTopicTitleProperty != null)
                  {
                    nestedTopic.addProperty( new StringProperty( nestedTopicTitleProperty, matchTemplate ) );
                  }
                                    
                  if (nestedTopicPhraseProperty != null )
                  {
                    nestedTopic.addProperty( new StringProperty( nestedTopicPhraseProperty, matchPhrases.get( p ) ) );
                  }
                }
              }
            }
          }
        }
      }
    }
  }
    
  private String getMask( int strlength )
  {
    return new String( MASK.substring( 0, strlength ) );
  }

  @Override
  public void initialize( QueryTree qTree )
  {
    // Not Relevant to the Matcher
  }
    

    
  private static final String MASK = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    
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
