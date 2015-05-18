package com.modinfodesigns.entity;

/**
 * Base interface for classes that can extract text entities from a character string.
 * 
 * @author Ted Sullivan
 */

public interface IEntityExtractor
{
  /**
   * Extract entities from a String. Generated a EntityPositionMap which contains one
   * or more PhrasePositionMap(s) for each phrase that represents the 'entity'
   *
   * @param field
   * @param fromString
   * @return
   */
  public EntityPositionMap extractEntities( String field, String fromString );
    
  /**
   * Returns the Prefix String that can be used to markup Entities from this Extractor
   *
   * @return
   */
  public String getPrefixString( );
  
  /**
   * Returns the Postfix String that can be used to markup Entities from this Extractor
   *
   * @return
   */
  public String getPostfixString( );
    
}
