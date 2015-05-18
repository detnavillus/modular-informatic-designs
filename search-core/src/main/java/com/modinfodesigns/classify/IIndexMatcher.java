package com.modinfodesigns.classify;

import com.modinfodesigns.property.compare.IPropertyMatcher;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.search.QueryTree;

import java.util.Set;

/**
 * Base Interface for Inverted Index 'Matcher' objects.  The main method for this interface
 * evaluates an InvertedIndex and returns a MatchStatistics object with 'yea' or 'nay' as 
 * well as details of the match if successful - term token and character ranges.  
 * 
 * The IIndexMatcher interface also extends IPropertyHolder. This enables matchers to be
 * decorated with properties that can then be used to decorate the target property holder on 
 * a successful match. (See Classifier).
 * 
 * Also extends IPropertyMatcher so that IIndexMatchers can be used to filter property lists.
 * 
 * @author Ted Sullivan
 */

public interface IIndexMatcher extends IPropertyMatcher, IPropertyHolder
{   
  public static final String MATCHED = "MATCHED";
  public static final String MATCHABLE = "MATCHABLE";
    
  public MatchStatistics getMatchStatistics( InvertedIndex invIndex ) throws IndexMatcherException;
    
  public Set<String> getMatchTerms(  );
    
  public Set<String> getMatchPhrases( );
    
  public void addMatchProperties( IPropertyHolder input, MatchStatistics matchStats );

  public void initialize( QueryTree qTree );
    
  /**
   * Sets the mode for adding match terms to the input IPropertyHolder
   *    MATCHED = add only terms that matched
   *    MATCHABLE = add terms that could be matched (i.e. from getMatchTerms( )
   * @param addMatchTermsMode
   */
  public void setAddMatchPhrasesMode( String addMatchPhrasesMode );
  public String getAddMatchPhrasesMode( );
    
  /**
   * Sets the property that the match terms will be added to in the output IPropertyHolder
   * @param addMatchTermsProperty
   */
  public void setAddMatchPhrasesProperty( String addMatchPhrasesProperty );
  public String getAddMatchPhrasesProperty( );
}
