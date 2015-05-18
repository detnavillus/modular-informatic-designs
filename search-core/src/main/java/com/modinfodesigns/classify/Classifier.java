package com.modinfodesigns.classify;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;
import com.modinfodesigns.utils.StringMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classifies textual documents using one or more IIndexMatchers. On match, each matching IIndexMatcher
 * is given the opportunity to copy its property set to the matched data object.
 * 
 * Creates an inverted index of each indexed field.
 * 
 * @author Ted Sullivan
 */

// To do:  Add method to take an IndexMatcherFactoryRef - resolve the Reference
// just in time. 

// Also add IndexMatcherRef.

public class Classifier extends DataObject implements IPropertyHolderTransform, IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( Classifier.class );
    
  private ArrayList<String> classifyFields = new ArrayList<String>( );
    
  private ArrayList<String> attributeFields = new ArrayList<String>( );
    
  private HashMap<String,HashSet<IIndexMatcher>> termMatcherMap;
    
  private HashMap<String,IIndexMatcher> indexMatcherMap;
    
  private HashSet<String> matchPhrases = new HashSet<String>( );
    
  private boolean caseSensitive;
    
  private ArrayList<IIndexMatcher> nonTermMatchers;
    
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  private boolean debugMatchers = false;
    
  /**
   * Adds a field to be used for classification.
   *
   * @param classifyField
   */
  public void addClassifyField( String classifyField )
  {
    classifyFields.add( classifyField );
  }
    
  /**
   * Add fields that will be added to classified records.
   *
   * @param attributeField
   */
  public void addAttributeField( String attributeField )
  {
    LOG.debug( "addAttributeField: " + attributeField );
    attributeFields.add( attributeField );
  }
    
  public void addIndexMatcher( IIndexMatcher matcher )
  {
    LOG.debug( "addIndexMatcher: " + matcher );
        
    if ( matcher == null)
    {
      LOG.error( "addIndexMatcher called with NULL!" );
      return;
    }
        
    if (termMatcherMap == null) termMatcherMap = new HashMap<String,HashSet<IIndexMatcher>>( );
    
    if (matcher.getName() != null)
    {
      if (indexMatcherMap == null) indexMatcherMap = new HashMap<String,IIndexMatcher>( );
      indexMatcherMap.put( matcher.getName( ), matcher );
    }
        
    boolean addedMatcher = false;
        
    Set<String> matcherTerms = matcher.getMatchTerms( );
    if (matcherTerms != null)
    {
      for (Iterator<String> it = matcherTerms.iterator( ); it.hasNext( ); )
      {
        String term = it.next( );
        HashSet<IIndexMatcher> matchers = termMatcherMap.get( term );
        if (matchers == null)
        {
          matchers = new HashSet<IIndexMatcher>( );
          LOG.debug( "adding term " + term );
          termMatcherMap.put( term, matchers );
        }
                
        matchers.add( matcher );
        addedMatcher = true;
      }
    }
        
    Set<String> matcherPhrases = matcher.getMatchPhrases( );
    if (matcherPhrases != null && matchPhrases.size() > 0)
    {
      matchPhrases.addAll( matcherPhrases );
    }
        
    // else its a non-term matcher (wildcard, EntityExtractor, etc. )
    if (!addedMatcher)
    {
      if (nonTermMatchers == null) nonTermMatchers = new ArrayList<IIndexMatcher>( );
      nonTermMatchers.add( matcher );
    }
        
  }
    
  public void addIndexMatcherFactory( IIndexMatcherFactory matcherFactory )
  {
    LOG.debug( "addIndexMatcherFactory" + matcherFactory );
        
    if (matcherFactory == null)
    {
      return;
    }
        
    IIndexMatcher[] indexMatchers = matcherFactory.createIndexMatchers( );
    if (indexMatchers != null)
    {
      for (int i = 0; i < indexMatchers.length; i++)
      {
        addIndexMatcher( indexMatchers[i] );
      }
    }
    else
    {
      LOG.error( "No IndexMatchers created!" );
    }
  }
    
    
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    if (input == null)
    {
      throw new PropertyTransformException( "transformPropertyHolder called with NULL!" );
    }
    	
    LOG.debug( "transformPropertyHolder( )..." );
    // -------------------------------------------------------------------------
    // for each classify field, create an InvertedIndex from the IProperty
    // For each IndexMatcher - copy properties to input property holder
    // -------------------------------------------------------------------------
    InvertedIndex invertedNdx = new InvertedIndex( );
    invertedNdx.tokenize( input, classifyFields );
        
    try
    {
      HashSet<MatchStatistics> matchingMatchStats = evaluate( invertedNdx, true );
      for (Iterator<MatchStatistics> matchIt = matchingMatchStats.iterator(); matchIt.hasNext();)
      {
        MatchStatistics matchStats = matchIt.next();
        IIndexMatcher matcher = matchStats.getIndexMatcher();
        LOG.debug( "Matching stats: " + matchStats + " " + matcher );
                
        // Do the Tagging Thing ...
        for (int i = 0; i < attributeFields.size( ); i++)
        {
          String attributeField = attributeFields.get( i );
          IProperty taggerProp = matcher.getProperty( attributeField );
          if (taggerProp != null)
          {
            LOG.debug( "adding property: " + taggerProp.getValue() );
            input.addProperty( taggerProp.copy() );
          }
        }
            
        // set the score field if it is
        IProperty scoreProperty = matchStats.getScoreProperty( );
        if (scoreProperty != null)
        {
          input.addProperty( scoreProperty );
        }
            
        if (matcher != null)
        {
          matcher.addMatchProperties( input, matchStats );
        }
        else
        {
          LOG.debug( "no Matcher for this MatchProperties!" );
        }
      }
    }
    catch ( IndexMatcherException ime )
    {
      throw ime;
    }
        
    return input;
  }
    
    
  private HashSet<MatchStatistics> evaluate( InvertedIndex invertedNdx, boolean evaluateAll ) throws IndexMatcherException
  {
    LOG.debug( "evaluate" );
    HashSet<IIndexMatcher> possibleMatchers = new HashSet<IIndexMatcher>( );
    HashSet<MatchStatistics> matchingMatcherStats = new HashSet<MatchStatistics>( );
        
    if (termMatcherMap == null)
    {
      LOG.error( "Classifier not initialized!" );
      throw new IndexMatcherException( "Classifier is not initialized" );
    }
        
    if (nonTermMatchers != null)
    {
      possibleMatchers.addAll( nonTermMatchers );
    }
        
    Set<String> termSet = invertedNdx.getTermSet( );
    for (Iterator<String> it = termSet.iterator(); it.hasNext(); )
    {
      String term = it.next( );
      LOG.debug( "checking term: " + term );
      HashSet<IIndexMatcher> termMatchers = termMatcherMap.get( term );
      if (termMatchers != null && termMatchers.size() > 0)
      {
        if (debugMatchers)
        {
          for (Iterator<IIndexMatcher> matchIt = termMatchers.iterator(); matchIt.hasNext(); )
          {
            LOG.debug( "  has matcher: " + matchIt.next() );
          }
        }
        possibleMatchers.addAll( termMatchers );
      }
    }
        
    LOG.debug( "have " + possibleMatchers.size() + " matchers." );
    // Now Check each IMatchingMatcher
    if ( possibleMatchers.size( ) > 0)
    {
      for ( Iterator<IIndexMatcher> it = possibleMatchers.iterator(); it.hasNext( ); )
      {
        IIndexMatcher possibleMatcher = it.next( );
        MatchStatistics matchStats = possibleMatcher.getMatchStatistics( invertedNdx );
        LOG.debug( "got matchStats: " + matchStats );
        if ( matchStats.matches(  ))
        {
          LOG.debug( "Matches!" );
          matchingMatcherStats.add( matchStats );
          if (!evaluateAll) return matchingMatcherStats;
        }
        else
        {
          LOG.debug( "Doesn't match: " + possibleMatcher.getValue( ) );
        }
      }
    }
        
    return matchingMatcherStats;
  }
    
    
  /**
   * Create one
   */
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (input instanceof IPropertyHolder)
    {
      return transformPropertyHolder( (IPropertyHolder)input );
    }
        
    return input;
  }

  /**
   * Asynchronous Method
   */

  public void startTransform( IProperty input, IPropertyTransformListener transformListener ) throws PropertyTransformException
  {
        
  }
    
  // ===================================================================================
  // IIndexMatcher methods - works as a composite IIndexMatcher - if any contained IIndexMatchers match ...
  // ===================================================================================
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex )  throws IndexMatcherException
  {
    try
    {
      MatchStatistics compStats = new MatchStatistics( );
      HashSet<MatchStatistics> matchingMatchStats = evaluate( invIndex, false );
      // create a composite MatchStatistics from matching Matchers ...
      for (Iterator<MatchStatistics> matchIt = matchingMatchStats.iterator(); matchIt.hasNext(); )
      {
        compStats.addMatchStatistics( matchIt.next() );
      }
               
      if (matchingMatchStats.size() > 0)
      {
        compStats.setMatches( true );
        compStats.setIndexMatcher( this );
      }
      return compStats;
    }
    catch( IndexMatcherException ime )
    {
      // Oh My!
    }
        
    return null;
  }
    
    
  public Set<String> getMatchTerms(  )
  {
    return termMatcherMap.keySet( );
  }
    
  public Set<String> getMatchPhrases( )
  {
    return matchPhrases;
  }

    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats )
  {
    MatchStatistics.addMatchProperties( input, matchStats, this );
  }
    
  public boolean equals( IProperty property )
  {
    if (property instanceof IPropertyHolder )
    {
      InvertedIndex invertedNdx = new InvertedIndex( );
      invertedNdx.tokenize( (IPropertyHolder)property, classifyFields );
      try
      {
        MatchStatistics matchStats = getMatchStatistics( (InvertedIndex)property );
        return matchStats.matches( );
      }
      catch( IndexMatcherException ime )
      {
        return false;
      }
    }
        
    return false;
  }

  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    return false;
  }
    
  public void initialize( QueryTree qTree )
  {
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
    
  /**
   * Returns a list of IIndexMatchers that would match the given phrase.
   *
   * @param phrase
   * @return
   */
  public ArrayList<IIndexMatcher> getMatchersForPhrase( String phrase )
  {
    if (phrase == null || phrase.trim().length() == 0) return null;
    
    ArrayList<IIndexMatcher> matchers = new ArrayList<IIndexMatcher>( );
    String[] tokens = StringMethods.getStringArray( phrase, InvertedIndex.tokenDelimiter );
    	
    HashSet<IIndexMatcher> matcherSet = termMatcherMap.get( tokens[0] );
    if (matcherSet != null)
    {
      Iterator<IIndexMatcher> matchIt = matcherSet.iterator( );
      while ( matchIt != null && matchIt.hasNext() )
      {
        IIndexMatcher ndxMatcher = matchIt.next( );
        Set<String> matchPhrases = ndxMatcher.getMatchPhrases( );
        if (matchPhrases != null && matchPhrases.contains( phrase ))
        {
          matchers.add( ndxMatcher );
        }
      }
    }
    	
    return matchers;
  }
    
  public IIndexMatcher getIndexMatcher( String name )
  {
    return (indexMatcherMap != null) ? indexMatcherMap.get( name ) : null;
  }

}
