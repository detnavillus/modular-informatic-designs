package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;

public class Currency implements IQuantity
{
  public static final String[] units = { "dollars", "euros", "yen", "yuan" };
	
  private String defaultUnits = "dollars";
  private double amount;
    
  private String name;
    
  public Currency( ) {  }
    
  public Currency( String name, double amount )
  {
    this.name = name;
    this.amount = amount;
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
    return getValue( "dollars" );
  }

  @Override
  public String getValue(String format)
  {
    if (format == null || format.equals( "dollars" ))
    {
      return Double.toString( this.amount );
    }
		
    return Double.toString( this.amount );
  }

  @Override
  public void setValue( String value, String format)
                        throws PropertyValidationException
  {
    try
    {
      this.amount = Double.parseDouble( value );
        
      if (format.equals( "dollars" ))
      {
				
      }
      else if (format.equals( "euros" ))
      {
				
      }
    }
    catch ( Exception e )
    {
      throw new PropertyValidationException( "Not a floating point number!" );
    }

  }

  @Override
  public String getDefaultFormat()
  {
    return defaultUnits;
  }

  @Override
  public IProperty copy()
  {
    Currency copy = new Currency( this.name, this.amount );
		
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return new Double( amount );
  }

  @Override
  public double getQuantity()
  {
    return amount;
  }

  @Override
  public double getQuantity(String units )
  {
    return 0;
  }

  @Override
  public String[] getUnits()
  {
    return units;
  }

  @Override
  public IQuantity add(IQuantity another) throws QuantityOperationException
  {
    return null;
  }

  @Override
  public IQuantity sub(IQuantity another) throws QuantityOperationException
  {
    return null;
  }

  @Override
  public IQuantity multiply( IQuantity another )
  {
    return null;
  }

  @Override
  public IQuantity divide(IQuantity another)
  {
    return null;
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
