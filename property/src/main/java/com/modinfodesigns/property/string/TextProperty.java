package com.modinfodesigns.property.string;

/** 
 * Just like a StringProperty - but designed to handle larger text fields.
 * 
 * @author Ted Sullivan
 */
public class TextProperty extends StringProperty
{
  public TextProperty( ) {  }
    
  public TextProperty( String name, String value )
  {
    super(name, value );
  }
}
