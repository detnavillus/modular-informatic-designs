package com.modinfodesigns.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringMethods
{
  private static final Pattern numberPat = Pattern.compile("[+-]?((\\d+(\\.\\d*)?)|(\\.\\d+))");
    
  public static String getDelimitedString( String[] strings, String delimiter )
  {
    return getDelimitedString( strings, delimiter, false );
  }

  public static String getDelimitedString( List<String> strings, String delimiter )
  {
    if (strings == null) return "";
    String[] stringArr = new String[ strings.size( ) ];
    strings.toArray( stringArr );
    return getDelimitedString( stringArr, delimiter );
  }


  public static String getDelimitedString( String[] strings, String delimiter, boolean dedupe )
  {
    if (strings == null || delimiter == null)
    {
      return "";
    }

    HashSet<String> uniqueSet = null;
    if ( dedupe )
    {
      uniqueSet = new HashSet<String>( );
    }

    StringBuffer strbuf = new StringBuffer( );
    int nVals = 0;
    for (int i = 0; i < strings.length; i++)
    {
      boolean isOK = true;
      if (dedupe && uniqueSet.contains( strings[i] ))
      {
        isOK = false;
      }
      if (isOK)
      {
        if (nVals > 0) strbuf.append( delimiter );
        strbuf.append( strings[i] );
        if ( dedupe ) uniqueSet.add( strings[i] );
        ++nVals;
      }
    }

    return strbuf.toString( );
  }
    
  public static String getDelimitedString( Set<String> stringSet, String delimiter )
  {
    if (stringSet == null) return null;
    	
    return getDelimitedString( stringSet.iterator(), delimiter );
  }
    
  public static String getDelimitedString( Iterator<String> strIt, String delimiter )
  {
    StringBuilder strbuilder = new StringBuilder( );
    while( strIt != null && strIt.hasNext() )
    {
      String string = strIt.next();
      if (string != null)
      {
        strbuilder.append( string );
      }
      if (strIt.hasNext()) strbuilder.append( delimiter );
    }
    
    return strbuilder.toString( );
  }
    
  public static String getDelimitedString( Map<String,String> strMap, String fieldDelim, String valueDelim )
  {
    StringBuilder strbuilder = new StringBuilder( );
    for (Iterator<String> strIt = strMap.keySet().iterator(); strIt.hasNext( ); )
    {
      String key = strIt.next( );
      String val = strMap.get( key );
        
      strbuilder.append( key ).append( valueDelim ).append( val );
      if (strIt.hasNext( ))
      {
        strbuilder.append( fieldDelim );
      }
    }
    	
    return strbuilder.toString( );
  }
    
  public static String[] getStringArray( String stringList, String delimiter )
  {
    return getStringArray( stringList, delimiter, false );
  }
    
  public static String[] getStringArray( String stringList, String delimiter, boolean includeDelim )
  {
    if (stringList == null || delimiter == null)
    {
      return new String[0];
    }

    StringTokenizer strtok = new StringTokenizer( stringList, delimiter, includeDelim );
    ArrayList<String> arrList = new ArrayList<String>( );
    boolean didSplit = false;
    boolean lastWasDelim = false;
      
    while (strtok.hasMoreTokens( ))
    {
      String tok = strtok.nextToken( );
      if (includeDelim && tok.equals( delimiter ))
      {
        if (lastWasDelim)
        {
          arrList.add( "" );
        }
        lastWasDelim = true;
      }
      else
      {
        arrList.add( tok );
        lastWasDelim = false;
      }
      didSplit = true;
    }
        
    if (didSplit)
    {
      String[] stList = new String[ arrList.size( ) ];
      arrList.toArray( stList );
      return stList;
    }
    else
    {
      String[] stList = new String[1];
      stList[0] = stringList;
      return stList;
    }
  }
    

  public static HashMap<String,String> unpackString( String input, String fieldDelimiter, String valueDelimiter )
  {
    HashMap<String,String> theMap = new HashMap<String,String>( );
    
    String[] pairs = getStringArray( input, fieldDelimiter );
    if (pairs != null)
    {
      for ( int i = 0; i < pairs.length; i++)
      {
        if (pairs[i].indexOf( valueDelimiter ) > 0)
        {
          String key = new String( pairs[i].substring( 0, pairs[i].indexOf( valueDelimiter )));
          String value = new String( pairs[i].substring( pairs[i].indexOf( valueDelimiter ) + valueDelimiter.length()));
          theMap.put( key, value );
        }
      }
    }
    		
    return theMap;
  }
    
  public static boolean isMultiTerm( String string, String delimiter )
  {
    StringTokenizer strtok = new StringTokenizer( string, delimiter, false );
    if (strtok.hasMoreTokens( ))
    {
      strtok.nextToken( );
      return strtok.hasMoreTokens( );
    }
    	
    return false;
  }

  /**
   * modifies the string to return a initial character uppercase version.
   *
   * @param source
   * @return
   */
  public static String initialCaps( String source )
  {
    if ( source == null || source.length( ) == 0 ) return source;
    char fc = source.charAt( 0 );
    return new String( Character.toUpperCase( fc ) + new String( source.substring( 1 ) ) );
  }
    
  /**
   * Checks a string to see if it has a mix of upper case and lower case letters.
   *
   * @param source   string to check
   * @return         true if a string contains both upper and lower case letters.
   */
  public static boolean isMixedCase( String source )
  {
    if (source == null || source.trim().length() == 0) return false;
    	
    boolean hasLowerCase = false;
    boolean hasUpperCase = false;
    	
    for (int i = 0; i < source.length(); i++)
    {
      char ic = source.charAt( i );
      if ((ic >= 'a' && ic <= 'z' ))
      {
        hasLowerCase = true;
      }
      if ((ic >= 'A' && ic <= 'Z' ))
      {
        hasUpperCase = true;
      }
    }
    	
    return (hasLowerCase && hasUpperCase);
  }
    
  /**
   * Checks a string to see if it has an initial capital letter
   *
   * @param source  String to check
   * @return        true if the source has an upper case letter in
   *                the first position but nowhere else.
   */
  public static boolean isInitialCaps( String source )
  {
    if (source == null || source.trim().length() == 0) return false;
    	
    char fc = source.charAt( 0 );
    if (fc < 'A' || fc > 'Z' ) return false;
    
    for ( int i = 1; i < source.length(); i++ )
    {
      char ic = source.charAt( i );
      if (ic >= 'A' && ic <= 'Z' ) return false;
    }
    	
    return true;
  }
    
  public static boolean isAllCaps( String source )
  {
    if (source == null || source.trim().length() == 0) return false;
    	
    for (int i = 0; i < source.length(); i++)
    {
      char ic = source.charAt( i );
      if ((ic >= 'a' && ic <= 'z' ))
      {
        return false;
      }
    }
    	
    return true;
  }
    
  public static boolean startsWithLetter( String source )
  {
    if (source == null || source.length() == 0) return false;
    	
    char fc = source.charAt( 0 );
    return ((fc >= 'A' && fc <= 'Z') || (fc >= 'a' && fc <= 'z'));
  }
    
  public static boolean isInteger( String string )
  {
    if (string == null || string.length() == 0) return false;
      
    for (int i = 0; i < string.length(); i++)
    {
      char fc = string.charAt( i );
      if (fc < '0' || fc > '9' )
      {
        return false;
      }
    }
    	
    return true;
  }
    
  public static boolean isNumber( String string )
  {
    if (string == null || string.length() == 0) return false;

    Matcher m = numberPat.matcher( string );
    return m.matches( );
  }
    
  public static List<String> getStrings( String str, char startCh, char endCh )
  {
    ArrayList<String> strings = new ArrayList<String>( );
    int startPos = str.indexOf( startCh );
    if (startPos >= 0)
    {
      while( startPos >= 0 && startPos < str.length() )
      {
        int endPos = findEnd( str, startCh, endCh, startPos );
        strings.add( new String( str.substring( startPos, endPos + 1 )) );
        startPos = str.indexOf( startCh, endPos + 1 );
      }
    }
    	
    return strings;
  }
    
  public static int findEnd( String str, char startCh, char endCh, int start )
  {
    int i = start + 1;
    for ( int nestCount = 1; nestCount > 0 && i < str.length(); ++i )
    {
      if      (str.charAt( i ) == startCh ) ++nestCount;
      else if (str.charAt( i ) == endCh )   --nestCount;
    }

    return i-1;
  }
    
  public static String stripEnclosingQuotes( String input )
  {
    if (input.trim().startsWith( "\"" ) && input.trim().endsWith( "\"" ))
    {
      String trimmed = input.trim( );
      return new String( trimmed.substring( 1, trimmed.length() - 1 ));
    }
    	
    return input;
  }
}