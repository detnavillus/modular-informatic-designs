package com.modinfodesigns.property.transform.string;

import java.util.StringTokenizer;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

public class StringTransform implements IStringTransform
{
  private String action = "";

  private String from;
  private String to;

  private String at;

  private String delimiters = " ,;:.";

  public static String BEFORE_FIRST = "BEFORE-FIRST";
  public static String AFTER_FIRST  = "AFTER-FIRST";
  public static String BEFORE_LAST  = "BEFORE-LAST";
  public static String AFTER_LAST   = "AFTER-LAST";
    
  public static String TRIM          = "TRIM";
  public static String REPLACE       = "REPLACE";
  public static String REPLACE_FIRST = "REPLACE-FIRST";
  public static String REPLACE_LAST  = "REPLACE-LAST";

  public static String TO_UPPER      = "TO-UPPER";
  public static String TO_LOWER      = "TO-LOWER";
    
  public static String INIT_CAPS     = "INIT-CAPS";
  public static String INIT_CAPS_ALL = "INIT-CAPS-ALL";
    
  @Override
  public String transformString(String inputString) throws StringTransformException
  {
    if (inputString == null)
    {
      throw new StringTransformException( "Input String is NULL" );
    }
    return doTransform( inputString );
  }

  @Override
  public String transformString(String sessionID, String inputString) throws StringTransformException
  {
    if (inputString == null)
    {
      throw new StringTransformException( "Input String is NULL" );
    }
    return doTransform( inputString );
  }
	
  private String doTransform( String inputString )
  {
    if (action.equals( BEFORE_FIRST ))
    {
      return getSubstring( inputString, 0, inputString.indexOf( at ) );
    }
    else if (action.equals( AFTER_FIRST ))
    {
      return getSubstring( inputString, inputString.indexOf( at ) + at.length(), inputString.length( ) );
    }
    else if (action.equals( BEFORE_LAST ))
    {
      return getSubstring( inputString, 0, inputString.lastIndexOf( at ) );
    }
    else if (action.equals( AFTER_LAST ))
    {
      return getSubstring( inputString, inputString.lastIndexOf( at ) + at.length(), inputString.length( ) );
    }
    else if (action.equals( REPLACE ))
    {
      return replaceSubstring( inputString, from, to );
    }
    else if (action.equals( REPLACE_FIRST ))
    {
      return assembleString( getSubstring( inputString, 0, inputString.indexOf( at ) ), to,
                             getSubstring( inputString, (inputString.indexOf( at ) + at.length( )), inputString.length( ) ) );
    }
    else if (action.equals( REPLACE_LAST ))
    {
      if (inputString.endsWith( at ))
      {
        return assembleString( getSubstring( inputString, 0, inputString.lastIndexOf( at ) ), to, "" );
      }
      else
      {
        return assembleString( getSubstring( inputString, 0, inputString.lastIndexOf( at ) ), to,
                               getSubstring( inputString, (inputString.lastIndexOf( at ) + at.length( )), inputString.length( ) ) );
      }
    }
    else if (action.equals( INIT_CAPS ))
    {
      return initializeCapitals( inputString );
    }
    else if (action.equals( INIT_CAPS_ALL ))
    {
      StringBuilder sbuilder = new StringBuilder( );
      StringTokenizer strtok = new StringTokenizer( inputString, delimiters, true );
      while( strtok.hasMoreTokens( ))
      {
        String word = strtok.nextToken( );
        sbuilder.append( initializeCapitals( word ) );
      }
        
      return sbuilder.toString( );
    }

    // Insert after
      
    // REMOVE_SECTION|EXTRACT_SECTION

    else if (action.equals( TRIM ))
    {
      if (at != null)
      {
        int trimTo = Integer.parseInt( at );
        trimTo = Math.min( trimTo, inputString.length() );
        return new String( inputString.substring( 0, trimTo ));
      }
      return inputString.trim( );
    }
    else if (action.equals( TO_UPPER ))
    {
      return inputString.toUpperCase( );
    }
    else if (action.equals( TO_LOWER ))
    {
      return inputString.toLowerCase( );
    }

    return inputString;
  }
	
  public void setAction( String action )
  {
    this.action = action;
  }

  public void setFrom( String from )
  {
    this.from = from;
  }

  public void setTo( String to )
  {
    this.to = to;
  }

  public void setAt( String at )
  {
    this.at = at;
  }
    
  // 'of' reads better for BEFORE-FIRST, AFTER-FIRST, BEFORE-LAST and AFTER-LAST
  public void setOf( String of )
  {
    this.at = of;
  }
	
  // -------------------------------------------------------------------------
  // Static Methods
  // -------------------------------------------------------------------------

  public static String getSubstring( String input, int start, int end )
  {
    if (start < 0 || start >= input.length() || end < 0 || end > input.length() || start > end )
    {
      return input;
    }

    return new String( input.substring( start, end ) );
  }

  public static String replaceSubstring( String input, String from, String to )
  {
    String ss = (from != null) ? from : "";
    String ns = (to != null)   ? to   : "";

    if (input == null) return "";

    if (input.indexOf( ss ) < 0) return input;

    StringBuilder sbuilder = new StringBuilder( );
    String inStr = input;
    while( inStr.indexOf( ss ) >= 0)
    {
      String first = new String( inStr.substring( 0, inStr.indexOf( ss )) );
      inStr = new String( inStr.substring( inStr.indexOf( ss ) + ss.length() ) );
      sbuilder.append( first );

      if (ns.equals( "\\n" ) || ns.equals( "\n" ))
      {
        sbuilder.append( '\n' );
      }
      else if (ns.equals( "\\r" ))
      {
        sbuilder.append( '\r' );
      }
      else if (ns.equals( "\\r\\n" ))
      {
        sbuilder.append( '\r' ).append( '\n' );
      }
      else
      {
        sbuilder.append( ns );
      }
    }

    sbuilder.append( inStr );

    return sbuilder.toString( );
  }

  public static String initializeCapitals( String input )
  {
    if (input == null || input.length() == 0 || (input.length() == 1 && Character.isLetter( input.charAt(0) ) == false))
    {
      return input;
    }

    return new String( Character.toUpperCase( input.charAt( 0 ) ) + new String( input.substring( 1 ).toLowerCase( ) ) );
  }

  public static String assembleString( String one, String two, String three )
  {
    StringBuilder sb = new StringBuilder( );
    sb.append( one ).append( two ).append( three );
    return sb.toString( );
  }
    
  public static String getDelimitedString( List<String> stringList, String delimiter )
  {
    StringBuilder sb = new StringBuilder( );
    Iterator<String> strings = stringList.iterator( );
    while (strings.hasNext( ))
    {
      sb.append( strings.next( ));
      if (strings.hasNext()) sb.append( delimiter );
    }
    return sb.toString( );
  }
    
  public static List<String> getStringList( String string, String delimiter )
  {
    if (string == null || delimiter == null)
    {
      return null;
    }
    	
    StringTokenizer strTok = new StringTokenizer( string, delimiter );
    ArrayList<String> strList = new ArrayList<String>( );
    while(strTok.hasMoreTokens( ))
    {
      strList.add( strTok.nextToken());
    }
    	
    return strList;
  }
    
  public static String[] getStringArray( String string, String delimiter )
  {
    List<String> strList = getStringList( string, delimiter );
    if (strList == null ) return new String[0];
    	
    String[] theStrings = new String[ strList.size( ) ];
    strList.toArray( theStrings );
    return theStrings;
  }
    
  public static String escapeXML( String string )
  {
    String output = replaceSubstring( string, "&", "&amp;");
    output = replaceSubstring( string, "<", "&lt;");
    output = replaceSubstring( string, ">", "&gt;");
    return output;
  }
    
  /**
   * Returns a string that contains a nested expression of (), {} or []
   * @param str
   * @param startChar
   * @param endChar
   * @return
   */
  public static String findNestedExpression( String str, String startChar, String endChar )
  {
    int startPos = str.indexOf( startChar );
    if (startPos < 0) return null;
    	
    return findNestedExpression( str, startChar, endChar, startPos );
  }
    
  public static String findNestedExpression( String str, String startChar, String endChar, int startPos )
  {
    int endPos = findEnd( str, startChar.charAt(0), endChar.charAt(0), startPos );
    if (endPos > startPos)
    {
      return new String( str.substring( startPos, endPos + 1 ) );
    }
    	
    return null;
  }
    
  private static int findEnd( String str, char startChar, char endChar, int startPos )
  {
    int i = startPos + 1;
    boolean escaped = false;
    for ( int nestCount = 1; nestCount > 0 && i < str.length(); ++i )
    {
      if      (escaped) escaped = false;
      else if (str.charAt( i ) == '\\' )      escaped = true;
      else if (str.charAt( i ) == startChar ) ++nestCount;
      else if (str.charAt( i ) == endChar )   --nestCount;
    }

    return i-1;
  }
    
  public static boolean isNumber( String s )
  {
    if ( s == null || s.trim( ).length( ) == 0 ) return false;

    char[] chs = new char[ s.length( ) ];
    s.getChars( 0, s.length( ), chs, 0 );

    for ( int i = 0; i < chs.length; i++ )
    {
      if ( !Character.isDigit( chs[i] ) && chs[i] != '.' ) return false;
    }
        
    return true;
  }

  public static boolean isInteger( String s )
  {
    if ( s == null || s.trim( ).length( ) == 0 ) return false;

    char[] chs = new char[ s.length( ) ];
    s.getChars( 0, s.length( ), chs, 0 );
      
    for ( int i = 0; i < chs.length; i++ )
    {
      if ( !Character.isDigit( chs[i] )) return false;
    }
        
    return true;
  }


}
