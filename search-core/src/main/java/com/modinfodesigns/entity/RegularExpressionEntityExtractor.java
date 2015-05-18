package com.modinfodesigns.entity;

import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.classify.InvertedIndex;

import com.modinfodesigns.utils.StringMethods;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Regular Expressions to extract entities from a String.
 * 
 * @author Ted Sullivan
 */

public class RegularExpressionEntityExtractor implements IEntityExtractor
{
  private transient static final Logger LOG = LoggerFactory.getLogger( RegularExpressionEntityExtractor.class );

  private ArrayList<String> inPatterns  = new ArrayList<String>( );
  private static HashMap<String,Pattern> patterns = new HashMap<String,Pattern>();

  private String startPattern;
  private String endPattern;

  private boolean includeStartPattern = false;
  private boolean includeEndPattern = false;
    
  private String prefixString;
  private String postfixString;
    
  public void setStartPattern( String startPattern )
  {
    this.startPattern = startPattern;
  }

  public void setEndPattern( String endPattern )
  {
    this.endPattern = endPattern;
  }

  public void setIncludeStartPattern( boolean includeStartPattern )
  {
    this.includeStartPattern = includeStartPattern;
  }

  public void setIncludeEndPattern( boolean includeEndPattern )
  {
    this.includeEndPattern = includeEndPattern;
  }
    
  public void addPattern( String pattern )
  {
    inPatterns.add( pattern );
  }

  @Override
  public EntityPositionMap extractEntities(String field, String fromString)
  {
    LOG.debug( "extractEntities" );
    EntityPositionMap entityPosMap = new EntityPositionMap( );
    if (prefixString != null && postfixString != null)
    {
      entityPosMap.setPrefixString( prefixString );
      entityPosMap.setPostfixString( postfixString );
    }
        
    InvertedIndex invIndex = new InvertedIndex( field, fromString );
    
    try
    {
      if (startPattern != null && endPattern != null)
      {
        Pattern startPat     = Pattern.compile( startPattern );
        Matcher startMatcher = startPat.matcher(fromString);

        Pattern endPat       = Pattern.compile( endPattern );
        Matcher endMatcher   = endPat.matcher(fromString);
                
        while ( startMatcher.find() && endMatcher.find() )
        {
          int startPos = (includeStartPattern) ? startMatcher.start() : startMatcher.end() + 1;
          int endPos   = (includeEndPattern) ? endMatcher.end() : endMatcher.start();

          if (startPos < endPos )
          {
            String outString = fromString.substring( startPos, endPos );
            PhrasePositionMap phraseMap = getPhrasePositionMap( field, outString, invIndex );
            entityPosMap.addPhrasePositionMap( phraseMap );
          }
        }
      }
      else if (inPatterns != null)
      {
        for (int p = 0; p < inPatterns.size( ); p++)
        {
          String inPattern = (String)inPatterns.get( p );
            
          LOG.debug( "Checking: '" + inPattern );

          Pattern pattern = getPattern( inPattern );
            
          Matcher matcher = pattern.matcher( fromString );
          while (matcher.find( ))
          {
            LOG.debug( "Found match " + inPattern + " groupCount = " + matcher.groupCount() );

            if (matcher.groupCount() > 0)
            {
              String[] groups = new String[ matcher.groupCount() - 1 ];
              if (groups.length > 1)
              {
                for (int i = 1; i <= groups.length; i++)
                {
                  groups[i-1] = matcher.group( i );
                  LOG.debug( " Group Adding[" + (i-1) + "]: '" + groups[i-1] + "'" );
                }
                  
                // build a word position list from each group term ...
                ArrayList<Integer> wordPositions = new ArrayList<Integer>( );
                for (int i = 0; i < groups.length; i++)
                {
                  List<Integer> wordPosList = invIndex.getPhrasePositions( field, groups[i], true );
                  wordPositions.addAll( wordPosList );
                }
                            
                Collections.sort( wordPositions );
                            
                List<Long> charPositions = invIndex.getCharacterPositions( field,  wordPositions );
                PhrasePositionMap phraseMap = getEntityMapper( StringMethods.getDelimitedString( groups, " " ), wordPositions, charPositions );
                entityPosMap.addPhrasePositionMap( phraseMap );
              }
              else
              {
                String group = matcher.group( );
                LOG.debug( "Adding single group: '" + group  + "'" );
                            
                // get Word Positions for the term...
                PhrasePositionMap phraseMap = getPhrasePositionMap( field, group, invIndex );
                if (phraseMap != null)
                {
                  entityPosMap.addPhrasePositionMap( phraseMap );
                }
              }
            }
          }
        }
      }

      return entityPosMap;
    }
    catch( Exception e )
    {
      LOG.error( "RegExprTermExtractor got EXCEPTION: " + e );
    }

    return null;
  }
    
  private PhrasePositionMap getPhrasePositionMap( String field, String output, InvertedIndex invIndex )
  {
    LOG.debug( "getEntityMapper " + output );
    List<Integer> wordPositions = invIndex.getPhrasePositions( field, output, true );
    if (wordPositions == null) return null;
    List<Long> charPositions = invIndex.getCharacterPositions( field, wordPositions );
        
    return getEntityMapper( output, wordPositions, charPositions );
  }
    
  private PhrasePositionMap getEntityMapper( String output, List<Integer> wordPositions, List<Long> charPositions )
  {
    LOG.debug( "getEntityMapper" + output + " " + wordPositions + " " + charPositions );
        
    if (wordPositions == null || wordPositions.size() == 0 || charPositions == null) return null;
        
    String[] components = StringTransform.getStringArray( output,  InvertedIndex.tokenDelimiter );
        
    ArrayList<IntegerRangeProperty> wordRanges = new ArrayList<IntegerRangeProperty>( );
    ArrayList<LongRangeProperty> charRanges = new ArrayList<LongRangeProperty>( );
        
    for (int i = 0; i < wordPositions.size(); i++)
    {
      Integer wordPos = wordPositions.get( i );
      IntegerRangeProperty pRange = new IntegerRangeProperty( output, wordPos.intValue(), wordPos.intValue() + components.length - 1 );
      wordRanges.add( pRange );
        
      Long charPos = charPositions.get( i );
      LongRangeProperty cRange = new LongRangeProperty( output, charPos.longValue(), charPos.longValue() + (long)output.length() );
      charRanges.add( cRange );
    }
        
    PhrasePositionMap eMapper = new PhrasePositionMap( output, wordRanges, charRanges );

    return eMapper;
  }

    
  private static Pattern getPattern( String pat )
  {
    Pattern re;
    synchronized (patterns)
    {
      re = (Pattern)patterns.get( pat );
      if (re == null)
      {
        try
        {
          LOG.debug( "getPattern() - Compiling RE pattern = " + pat );
                    
          re = Pattern.compile( pat );
          patterns.put( pat, re );
        }
        catch (Exception e)
        {
          LOG.error( "getPattern() - regular Expression " + pat + " failed to compile. " + e );
        }
      }
      else
      {
        LOG.debug( "getPattern() - obtained compiled pattern from pattern cache.");
      }
    }
        
    return re;
  }

  @Override
  public String getPrefixString()
  {
    return this.prefixString;
  }
    
  public void setPrefixString( String prefixString )
  {
    this.prefixString = prefixString;
  }

  @Override
  public String getPostfixString()
  {
    return this.postfixString;
  }
    
  public void setPostfixString( String postfixString )
  {
    this.postfixString = postfixString;
  }
}
