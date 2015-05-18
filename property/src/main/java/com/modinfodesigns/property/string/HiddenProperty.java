package com.modinfodesigns.property.string;

/**
 * Special type of StringProperty - persisted but not displayed in HTML_FORM format
 * as editable text field.
 * 
 * @author Ted Sullivan
 */

public class HiddenProperty extends StringProperty
{
  public HiddenProperty( ) {  }
    
  public HiddenProperty( String name, String value )
  {
    super(name, value );
  }
}
