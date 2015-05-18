package com.modinfodesigns.property;

import com.modinfodesigns.property.transform.string.StringTransform;

/**
 * IProperty implementation that wraps a Boolean value.
 * 
 * @author Ted Sullivan
 */
public class BooleanProperty implements IProperty
{
  public static final String TRUE = "true";
  public static final String FALSE = "false";
	
  private String name;
    
  private boolean value;
    
  private String metaType;
    
  public BooleanProperty( ) { }
    
  public BooleanProperty( String name, boolean value )
  {
    this.name = name;
    this.value = value;
  }
    
  public BooleanProperty( String name, Boolean value )
  {
    this.name = name;
    this.value = value.booleanValue( );
  }
    
  public BooleanProperty( String name, String value )
  {
    this.name = name;
    setValue( value, "" );
  }
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public String getType()
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue()
  {
    return (value) ? TRUE : FALSE;
  }
	
  public boolean getBooleanValue( )
  {
    return value;
  }

  @Override
  /**
   * Format can be any pair separated by a '|' character
   * e.g.  true|false, yes|no, 1|0, on|off etc.
   */
  public String getValue(String format)
  {
    if (format == null) return getValue( );
		
    else if (format.equals( IProperty.JSON_FORMAT) || format.equals( IProperty.JSON_VALUE))
    {
      return (value) ? "\"" + TRUE + "\"" : "\"" + FALSE + "\"";
    }
    else if (format.equals( IProperty.XML_FORMAT))
    {
      return "<Property type=\"com.modinfodesigns.property.BooleanProperty\"><Name>" + StringTransform.escapeXML( this.name )
           + "</Name><Value>" + Boolean.toString( value ) + "</Value></Property>";
    }
		
    else if (format.indexOf( "|" ) > 0)
    {
      String trueVal = new String( format.substring( 0, format.indexOf( "|" )));
      String falseVal = new String( format.substring( format.indexOf( "|" ) + 1 ));
      return (value) ? trueVal : falseVal;
    }
		
    return getValue( );
  }

  @Override
  public String getDefaultFormat( )
  {
    return "true|false";
  }
	
  @Override
  public void setValue(String value, String format)
  {
    if (value.equalsIgnoreCase( "true" ) || value.equals("1") || value.equalsIgnoreCase( "on" )
    || value.equalsIgnoreCase( "T" ) || value.equalsIgnoreCase( "yes" ))
    {
      this.value = true;
    }
  }

  @Override
  public IProperty copy()
  {
    BooleanProperty copy = new BooleanProperty( );
    copy.value = this.value;
    copy.name = this.name;
		
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return new Boolean( value );
  }

  @Override
  public boolean isMultiValue()
  {
    return false;
  }

  @Override
  public String[] getValues(String format)
  {
    String[] values = new String[1];
    values[0] = getValue( format );
    return values;
  }
	
}
