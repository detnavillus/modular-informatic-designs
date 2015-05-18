package com.modinfodesigns.security;

import com.modinfodesigns.property.transform.string.IStringTransform;
import com.modinfodesigns.property.transform.string.StringTransformException;

import java.util.HashMap;
import java.util.Random;
import java.util.Date;

public class PasswordEncryptor implements IStringTransform
{
  static char[] inputCharArray = { 'S', 'E', 'O', 'u', 'e', 'B', '*', 'U', '9', 'L', 'F', 'l', 'H', 'X', 'o', 'N',
                                    'D', '6', 's', 'i', '4', 'v', '$', '0', 'b', 'z', 'A', 'Y', 'C', 'R', 'y', '3',
                                    '8', 'm', '^', 'z', 'K', 'k', 'M', 'p', '2', 'P', 'Q', 'q', 'a', '%', 'h', 'V',
                                    'W', '5', 'J', 'x', '1', 'c', 'j', '@', 'f', 'r', '7', 'G', 't', '~', '!', 'd',
                                    '#', 'w', 'T', 'I', 'n', 'g', '?' };

  static char[] outputCharArray = { '7', 'I', 'Z', 'V', 'j', '$', 'g', '?', '%', 'k', '4', 'm', 'n', 'o', 'E', 'q',
                                    'x', 'U', 't', 'u', 'B', 'N', '#', 'y', 'z', 'A', 'v', 'C', '~', 'p', 'F', '2',
                                    'H', 'P', 'J', '9', 'L', '0', 'w', 'O', 'b', 'Q', '5', 'S', 'T', 's', 'd', 'W',
                                    'X', 'Y', 'c', '1', 'G', '3', 'l', 'R', '6', 'a', '!', 'K', 'M', 'D', '8', '@',
                                    'r', 'f', 'i', '^', 'e', '*', 'h' };

  static int[] posArray = { 23, 11, 42, 66, 15, 37, 25,  3, 19, 48, 33, 56, 12, 18,  7, 26, 39, 52, 8, 31, 2, 38,
                            55, 13, 10, 29, 71, 44, 21, 40, 68, 54, 20, 77, 35,  1, 22, 53,  5, 60 };

  static char[] charSequence = null;

  static HashMap<Character,Character> encodeMap = new HashMap<Character,Character>( );
  static HashMap<Character,Character> decodeMap = new HashMap<Character,Character>( );

  static Random randGen = null;

  static
  {
    charSequence = new char[ 62 ];

    for (int i = 0; i < 10; i++)
    {
      int charBase = (int)('0');
      charSequence[i] = (char)(charBase + i );
    }
    for (int i = 0; i < 26; i++)
    {
      int charBase = (int)('a');
      charSequence[i+10] = (char)(charBase + i );
    }
    for (int i = 0; i < 26; i++)
    {
      int charBase = (int)('A');
      charSequence[i+36] = (char)(charBase + i );
    }


    for (int i = 0; i < inputCharArray.length; i++)
    {
      Character inCh = new Character( inputCharArray[i] );
      Character outCh = new Character( outputCharArray[i] );

      encodeMap.put( inCh, outCh );
      decodeMap.put( outCh, inCh );
    }

    Date now = new Date( );
    randGen = new Random( now.getTime() );
  }


  private static String getRandomString(  )
  {
    StringBuffer strbuf = new StringBuffer( );
    for (int i = 0; i < 80; i++)
    {
      int nextNdx = Math.abs( randGen.nextInt( ) ) % 62;
      strbuf.append( charSequence[ nextNdx ] );
    }

    return strbuf.toString( );
  }

  private String mode = "ENCRYPT";
  public PasswordEncryptor( ) {  }

  public PasswordEncryptor( String mode )
  {
    this.mode = mode;
  }

  public static String decryptString( String inputString )
  {
    return new PasswordEncryptor( "DECRYPT" ).doDecryptString( inputString );
  }

  public static String encryptString( String inputString )
  {
    return new PasswordEncryptor( "ENCRYPT" ).doEncryptString( inputString );
  }

  private String doEncryptString( String inputString )
  {
    if (inputString.length() > posArray.length )
    {
      return null;
    }

    String randString = getRandomString( );
      
    char lengthChar = charSequence[ inputString.length() ];

    char[] randArray = randString.toCharArray( );
    char[] outputArray = new char[ randArray.length + 1 ];
    outputArray[randArray.length] = lengthChar;
    System.arraycopy( randArray, 0, outputArray, 0, randArray.length );

    char[] inputArray = inputString.toCharArray( );

    for (int i = 0; i < inputString.length(); i++)
    {
      char inputChar = inputArray[ i ];
      Character outputChar = (Character)encodeMap.get( new Character( inputChar ) );

      outputArray[ posArray[i] ] = outputChar.charValue( );
    }

    return new String( outputArray );
  }

  private String doDecryptString( String inputString )
  {
    char[] inputArray = inputString.toCharArray( );
    char lengthChar = inputArray[ inputArray.length - 1 ];
      
    int length = 0;
    if (lengthChar >= '0' && lengthChar <= '9' )
    {
      length = (int)(lengthChar - '0');
    }
    else if (lengthChar >= 'a' && lengthChar <= 'z' )
    {
      length = 10 + ((int)(lengthChar - 'a'));
    }
    else if (lengthChar >= 'A' && lengthChar <= 'Z' )
    {
      length = 36 + ((int)(lengthChar - 'A'));
    }

    StringBuilder strbuilder = new StringBuilder( );
    for (int i = 0; i < length; i++)
    {
      char inputCh = inputArray[ posArray[i] ];
      Character outputCh = (Character)decodeMap.get( new Character( inputCh ) );
        
      strbuilder.append( outputCh.charValue( ) );
    }

    return strbuilder.toString( );
  }


  @Override
  public String transformString( String inputString )
                                 throws StringTransformException
  {
    if (mode.equals( "ENCRYPT" ))
    {
      return doEncryptString( inputString );
    }
    else
    {
      return doDecryptString( inputString );
    }
  }

  @Override
  public String transformString( String sessionID, String inputString )
                                  throws StringTransformException
  {
    return transformString( inputString );
  }

}
