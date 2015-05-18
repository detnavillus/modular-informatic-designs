package com.modinfodesigns.property;

import com.modinfodesigns.app.INamedObject;

/**
 *  Base Interface for all data object property objects. A property has two key elements,
 *  a name and a value. The value for any property object can be expressed by a string. The
 *  property object can express its value using one of several common output formats (XML, JSON, DELIMITED).
 *  
 * @author Ted Sullivan
 */
public interface IProperty extends INamedObject
{
  public static final String XML_FORMAT = "XML";
  public static final String XML_FORMAT_CDATA = "XML_CDATA";
  public static final String JSON_FORMAT = "JSON";
  public static final String JSON_VALUE = "JSON_VALUE";
  public static final String DELIMITED_FORMAT = "DELIMITED";
  public static final String HTML_FORM_FORMAT = "HTML_FORM_FORMAT";
  public static final String HTML_FORMAT = "HTML_FORMAT";
    
  /**
   * Returns the name of the property.
   */
  public String getName( );
    
  /**
   * @param name  The new name of the property
   */
  public void setName( String name );

  /**
   * Returns the Type of the property
   */
  public String getType( );

  /**
   * Returns the string representation of the property in its default format.
   */
  public String getValue( );

  /**
   * returns a String representation of the property in the specified format
   */
  public String getValue( String format );
    
    
  /**
   * Sets the Properties value given a format - for initialization purposes
   * @param value
   * @param format
   */
  public void setValue( String value, String format ) throws PropertyValidationException;
    
    
  /**
   * Sets the property value using the default format.
   * @param value
   */
  public String getDefaultFormat( );
    
  /**
   * Creates a copy of the IProperty
   * @return
   */
  public IProperty copy( );
    
    
  /**
   * Returns the Value in Object form
   */
  public Object getValueObject( );
    
  public boolean isMultiValue( );
    
  public String[] getValues( String format );
    
}
