package com.modinfodesigns.entity.tagging;

import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.transform.string.IStringTransform;
import com.modinfodesigns.property.transform.string.StringTransformException;

import com.modinfodesigns.entity.IEntityExtractor;
import com.modinfodesigns.entity.EntityPositionMap;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Puts markup tags around terms extracted by one or more EntityExtractors.  EntityExtractors define their
 * own prefix and postfix markup.
 * 
 * @author Ted Sullivan
 */

public class EntityTaggerStringTransform implements IStringTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( EntityTaggerStringTransform.class );

  private ArrayList<IEntityExtractor> entityExtractors;
    
  private String prefixString  = "<b>";;
  private String postfixString  = "</b>";
    
  private boolean caseSensitive = true;
    
  public EntityTaggerStringTransform( ) {  }
    
  public EntityTaggerStringTransform( IEntityExtractor entityExtractor )
  {
    addEntityExtractor( entityExtractor );
  }
    
  public void addEntityExtractor( IEntityExtractor entityExtractor )
  {
    if (entityExtractor == null) return;
    	
    if (entityExtractors == null) entityExtractors = new ArrayList<IEntityExtractor>( );
    entityExtractors.add( entityExtractor );
  }
    
  public void addEntityExtractors( List<IEntityExtractor> entityExtractors )
  {
    if (this.entityExtractors == null) this.entityExtractors = new ArrayList<IEntityExtractor>( );
    this.entityExtractors.addAll( entityExtractors );
  }
    
  public List<IEntityExtractor> getEntityExtractors(  )
  {
    return this.entityExtractors;
  }
    
  public void setPrefix( String prefixString )
  {
    this.prefixString = prefixString;
  }
    
  public void setPostfix( String postfixString )
  {
    this.postfixString = postfixString;
  }

  public void setCaseSensitive( boolean caseSensitive )
  {
    this.caseSensitive = caseSensitive;
  }
    
  @Override
  public String transformString( String inputString ) throws StringTransformException
  {
    if ( entityExtractors == null )
    {
      throw new StringTransformException( "EntityExtractor is NULL!" );
    }
        
    return transformString( entityExtractors, inputString );
  }
    
  public String transformString( List<IEntityExtractor> entityExtractors, String inputString )
  {
    if (entityExtractors == null || inputString == null)
    {
      return null;
    }
        
    ArrayList<EntityPositionMap> eMapperList = new ArrayList<EntityPositionMap>( );
    for (int i = 0; i < entityExtractors.size(); i++)
    {
      IEntityExtractor entityExtractor = entityExtractors.get( i );
      EntityPositionMap eMapperSet = entityExtractor.extractEntities( "field", inputString );
      if (eMapperSet != null)
      {
        if (this.prefixString != null && this.postfixString != null)
        {
          // override
          eMapperSet.setPrefixString( prefixString );
          eMapperSet.setPostfixString( postfixString );
        }
        eMapperList.add( eMapperSet );
      }
    }
        
    return transformString( eMapperList, inputString, 0 );
  }
    
  public String transformString( EntityPositionMap eMapperSet, String inputString )
  {
    ArrayList<EntityPositionMap> eMapperList = new ArrayList<EntityPositionMap>( );
    eMapperList.add( eMapperSet );
    return transformString( eMapperList, inputString, 0, caseSensitive );
  }
    
    
  // capable of dealing with multiple EntityMapperSet(s) each with their own prefix/postfix strings.
  public static String transformString( List<EntityPositionMap> ePositionMapList, String inputString, long offset )
  {
    return transformString( ePositionMapList, inputString, offset, false );
  }

  public static String transformString( List<EntityPositionMap> ePositionMapList, String inputString,
                                        long offset, boolean caseSensitive )
  {
    if (ePositionMapList == null || inputString == null) return null;
    	
    TreeMap<Integer,String> sortedPhrases = new TreeMap<Integer,String>( );
        
    // =====================================================================================
    // Phase 1: Get all of the phrases in the Set of EntityMapperSets sort them by position
    // Each phrase needs to be tied back to the EntityMapper tags in the second phase
    // so create a temporary map of phrase --> EntityMapperSet
    // =====================================================================================
    HashMap<String,EntityPositionMap> ePositionMaps = new HashMap<String,EntityPositionMap>( );
    
    for (int i = 0; i < ePositionMapList.size(); i++)
    {
      EntityPositionMap ePositionMap = ePositionMapList.get( i );
        
      Set<String> phrases = ePositionMap.getMappedPhrases( );
      if ( phrases != null )
      {
        // ====================================================================
        // For each phrase, Get the string position, only add mark up IF the
        // start and end of the phrase is in the set of character positions
        // extracted by the Entity Extractor.  This is done so that only phrases
        // that are relevant or part of more complex entity extraction will
        // be highlighted.
        // ====================================================================
        for ( Iterator<String> phraseIt = phrases.iterator(); phraseIt.hasNext(); )
        {
          String phrase = phraseIt.next();
          LOG.debug( "Checking '" + phrase + "'" );
          ePositionMaps.put( phrase, ePositionMap );
                    
          int startingAt = 0;
          while ( startingAt >= 0 )
          {
            int startPos = (caseSensitive)
                         ? inputString.indexOf( phrase, startingAt )
                         : inputString.toLowerCase( ).indexOf( phrase.toLowerCase(), startingAt );
            if (startPos >= 0)
            {
              Integer startInt = new Integer( startPos );
              String oldPhrase = sortedPhrases.get( startInt );
              if (oldPhrase == null || oldPhrase.length() < phrase.length() )
              {
                LOG.debug( "Adding sorted: " + phrase );
                sortedPhrases.put( new Integer( startPos ), phrase );
              }
                        	
              startingAt = startPos + phrase.length() + 1;
            }
            else
            {
              startingAt = -1;
            }
          }
        }
      }
    }
        
    String outputString = inputString;
        
    int markupOffset = 0;
    for ( Iterator<Integer> intIt = sortedPhrases.keySet().iterator(); intIt.hasNext(); )
    {
      Integer startAt = intIt.next( );
      String phrase = sortedPhrases.get( startAt );
      LOG.debug( "Tagging '" + phrase + "'" );
      EntityPositionMap eMapperSet = ePositionMaps.get( phrase );
      String prefixString = eMapperSet.getPrefixString( );
      if (prefixString == null) prefixString = "<b>";
      String postfixString = eMapperSet.getPostfixString( );
      if (postfixString == null) postfixString = "</b>";
      int prefixLength = prefixString.length() + postfixString.length();
            
      List<LongRangeProperty> charPositions = removeInternalRanges( eMapperSet.getCharacterPositions( offset ) );
            
      LOG.debug( phrase + " starts at " + startAt.toString() );
    
      int endAt = startAt.intValue() + phrase.length( );
                            
      LongRangeProperty lrp = new LongRangeProperty( "", (long)startAt.intValue(), (long)endAt );
      if (isInside( lrp, charPositions ))
      {
        LOG.debug( "Is Inside! - marking up " );
        // mark up the phrase -
        // replace the string with substring 0, startPos + markedUP + substring endPos+1
        String outputPhrase = new String( outputString.substring( startAt.intValue() + markupOffset,
                                                                  endAt + markupOffset ));
                
        String firstPart = new String( outputString.substring( 0, startAt.intValue() + markupOffset ) );
        String markedUp = new String( prefixString + outputPhrase + postfixString );
        String lastPart = new String( outputString.substring( endAt + markupOffset ) );
                                
        outputString = new String( firstPart + markedUp + lastPart );
          
        LOG.debug( "outputString is now " + outputString );
        markupOffset += prefixLength;
      }
    }
                
    return outputString;
  }
    
  // return true if span matches one of the char range spans.
  private static boolean isInside( LongRangeProperty span, List<LongRangeProperty> charRanges )
  {
    if (charRanges == null || charRanges.size() == 0) return false;
        
    for (int i = 0; i < charRanges.size(); i++ )
    {
      LongRangeProperty aRange = charRanges.get( i );
      LOG.debug( "checking range " + span.getMinimum() + " to " + span.getMaximum() + " with " + aRange.getMinimum() + " to " + aRange.getMaximum( ) );
      if (aRange.equals( span )) return true;
    }
        
    LOG.debug( "not inside" );
    return false;
  }
    
  // =====================================================================
  // merge any overlapping ranges to larger ranges.
  // =====================================================================
  private static List<LongRangeProperty> removeInternalRanges( List<LongRangeProperty> charRanges )
  {
    HashSet<Integer> noOverlap = new HashSet<Integer>( );
    for (int i = 0; i < charRanges.size( ); i++)
    {
      LongRangeProperty lrp_i = charRanges.get( i );
      for (int j = 0; j < charRanges.size(); j++)
      {
        LongRangeProperty lrp_j = charRanges.get( j );
        if (i != j && lrp_i.contains( lrp_j ))
        {
          LOG.debug( lrp_i.getMinimum() + " to " + lrp_i.getMaximum( )
                    + " contains " + lrp_j.getMinimum() + " to " + lrp_j.getMaximum( ) );
          noOverlap.add( new Integer( j ) );
        }
      }
    }
    	
    ArrayList<LongRangeProperty> output = new ArrayList<LongRangeProperty>( );
    for (int i = 0; i < charRanges.size( ); i++)
    {
      if (!noOverlap.contains( new Integer( i )))
      {
        output.add( charRanges.get( i ) );
      }
    }
    	
    return output;
  }

  @Override
  public String transformString(String sessionID, String inputString) throws StringTransformException
  {
    return transformString( inputString );
  }

}
