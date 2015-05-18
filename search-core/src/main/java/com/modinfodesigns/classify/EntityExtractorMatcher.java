package com.modinfodesigns.classify;

import java.util.Iterator;

import java.util.Set;
import java.util.List;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.entity.IEntityExtractor;
import com.modinfodesigns.entity.EntityPositionMap;
import com.modinfodesigns.entity.PhrasePositionMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses an IEntityExtractor to implement the IIndexMatcher interface. Returns a positive
 * result if the IEntityExtractor is able to extract terms or phrases from an input
 * document.
 * 
 * @author Ted Sullivan
 */

public class EntityExtractorMatcher extends DataObject implements IIndexMatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( EntityExtractorMatcher.class );

  private IEntityExtractor entityExtractor;
    
  private String addMatchPhrasesMode;
  private String addMatchPhrasesProperty;
    
  public void setEntityExtractor( IEntityExtractor entityExtractor )
  {
    this.entityExtractor = entityExtractor;
  }
    
  public void addEntityExtractor( IEntityExtractor entityExtractor )
  {
    setEntityExtractor( entityExtractor );
  }
    
  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    return false;
  }

  @Override
  public String getType( )
  {
    return "com.modinfodesigns.classify.EntityExtractorMatcher";
  }
    

  @Override
  public MatchStatistics getMatchStatistics(InvertedIndex invIndex)
        throws IndexMatcherException
  {
    LOG.debug( "getMatchStatistics( ) " );
        
    MatchStatistics matchStats = new MatchStatistics( );
    // get the String from the invIndex.
    for (Iterator<String> sit = invIndex.getFields( ).iterator(); sit.hasNext(); )
    {
      String field = sit.next();
      String inputStr = invIndex.getFieldString( field );
        
      LOG.debug( "field: " + field );
      LOG.debug( "inputStr " + inputStr );

      EntityPositionMap ePositionMap = entityExtractor.extractEntities( field, inputStr );
      if (ePositionMap != null)
      {
        List<PhrasePositionMap> phraseMaps = ePositionMap.getPhrasePositionMaps( );
        for (int i = 0; i < phraseMaps.size(); i++)
        {
          PhrasePositionMap phraseMap = phraseMaps.get( i );
          String phrase = phraseMap.getPhrase( );
          LOG.debug( "Got phrase: '" + phrase + "'" );
                    
          List<IntegerRangeProperty> wordPositions = phraseMap.getWordPositions( );
          List<LongRangeProperty> charPositions = phraseMap.getCharacterPositions( );
                    
          for (int n = 0; n < wordPositions.size(); n++ )
          {
            IntegerRangeProperty wordPos = wordPositions.get( n );
            LongRangeProperty charPos = charPositions.get( n );
              
            LOG.debug( "wordPos " + wordPos.getValue() );
                        
            matchStats.addPhrase( field, phrase, wordPos, charPos );
            matchStats.setMatches( true );
          }
        }
      }
    }
        
    LOG.debug( "Returning " + matchStats );
    return matchStats;
  }

  @Override
  public Set<String> getMatchTerms()
  {
    // No terms - we are a non-term matcher
    return null;
  }

  @Override
  public Set<String> getMatchPhrases()
  {
    // No phrases - we are a non-term matcher
    return null;
  }

  @Override
  public void addMatchProperties(IPropertyHolder input, MatchStatistics matchStats)
  {
    MatchStatistics.addMatchProperties( input, matchStats, this );
  }

  @Override
  public void initialize(QueryTree qTree)
  {
    // Nothing to do here ...
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
