package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;

public class LongProperty implements IProperty
{
  private String name;
  private long value;
    
  public LongProperty( ) {  }
    
  public LongProperty( String name, long value )
  {
    this.name = name;
    this.value = value;
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
    return getClass().getCanonicalName();
  }

  @Override
  public String getValue()
  {
    return Long.toString( value );
  }

  @Override
  public String getValue( String format )
  {
    return Long.toString( value );
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
    try
    {
      this.value = Long.parseLong( value );
    }
    catch ( NumberFormatException nfe )
    {
      throw new PropertyValidationException( "Value must be a Long Integer!" );
    }
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    return new LongProperty( this.name, this.value );
  }

  @Override
  public Object getValueObject()
  {
    return new Long( value );
  }

  public long getLongValue( )
  {
    return value;
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
