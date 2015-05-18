package com.modinfodesigns.property.transform.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Regular Expressions to transform a String. If a pattern is detected, an
 * output pattern is used to create the transformed string.
 * 
 * @author Ted Sullivan
 */
public class RegularExpressionStringTransform implements IStringTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( RegularExpressionStringTransform.class );

  private String regExprPattern;
  private String outputPattern;
	
  private String delimiter  = " ";
	
  private static HashMap<String,Pattern> patternMap = new HashMap<String,Pattern>( );

  @Override
  public String transformString( String inputString ) throws StringTransformException
  {
    LOG.debug( "transformString( )... " );
    Pattern compPat = compilePattern( this.regExprPattern );

    if (compPat != null )
    {
      LOG.debug( "Got compiled pattern for " + regExprPattern );
      Matcher matchedPattern = compPat.matcher( inputString );
      if (matchedPattern != null)
      {
        String output = null;

        if (outputPattern == null)
        {
          StringBuilder sBuilder = new StringBuilder( );
          while( matchedPattern.find( ) )
          {
            String groupPat = matchedPattern.group( );

            sBuilder.append( groupPat );
            sBuilder.append( delimiter );
          }

          output = sBuilder.toString();
        }
        else
        {
          LOG.debug( "replaceAll " + outputPattern );
          output = matchedPattern.replaceAll( outputPattern );
        }

        return output;
      }
      else
      {
        LOG.debug( "no matches for " + regExprPattern );
      }
    }
    else
    {
      LOG.error( "Could not compile " + regExprPattern );
    }
        
    return inputString;
  }
	
  private static Pattern compilePattern( String regExpression )
  {
    Pattern pattern;
    synchronized (patternMap)
    {
      pattern = patternMap.get( regExpression );
      if (pattern == null)
      {
        try
        {
          pattern = Pattern.compile( regExpression );
          patternMap.put( regExpression, pattern );
        }
        catch (Exception e)
        {
          LOG.error( "getCompiledPattern got Exception " + e );
        }
      }
    }
        
    return pattern;
  }

  @Override
  public String transformString(String sessionID, String inputString) throws StringTransformException
  {
    return transformString( inputString );
  }
	
  public void setRegularExpression( String regExprPattern )
  {
    this.regExprPattern = regExprPattern;
  }
	
  public void setOutputPattern( String outputPattern )
  {
    this.outputPattern = outputPattern;
  }

  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }
}
