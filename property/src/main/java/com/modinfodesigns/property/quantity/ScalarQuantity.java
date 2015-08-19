package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;

/**
 * A Quantity with no units. Can be multiplied by any IQuantity to create another
 * IQuantity of the same type.
 * 
 * @author Ted Sullivan
 */
public class ScalarQuantity implements IQuantity
{
  protected double value = 0.0;
  private String name;
  private String origValue;
    
  public ScalarQuantity( ) {  }
    
  public ScalarQuantity( String name, double value )
  {
    this.name = name;
    this.value = value;
  }
    
  public ScalarQuantity( String name, String value )
  {
    this.name = name;
    this.origValue = value;
    this.value = Double.parseDouble( value );
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
    if (origValue != null) return origValue;
    return Double.toString( value );
  }

  @Override
  public String getValue(String format)
  {
    return Double.toString( value );
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
    try
    {
      this.value = Double.parseDouble( value );
      this.origValue = value;
    }
    catch( NumberFormatException nfe )
    {
      throw new PropertyValidationException( "Value must be a number!" );
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
    ScalarQuantity sq = new ScalarQuantity( );
    sq.name = this.name;
    sq.value = this.value;
    sq.origValue = this.origValue;
    return sq;
  }

  @Override
  public Object getValueObject()
  {
    return new Double( value );
  }

  @Override
  public double getQuantity()
  {
    return value;
  }

  @Override
  public double getQuantity(String units)
  {
    return value;
  }

  @Override
  public String[] getUnits()
  {
    return null;
  }

  @Override
  public IQuantity add( IQuantity another)
                    throws QuantityOperationException
  {
    if (!(another instanceof ScalarQuantity))
    {
      throw new QuantityOperationException( "Not a ScalarQuantity!" );
    }
		
    return new ScalarQuantity( "Sum", (this.value + another.getQuantity()) );
  }

  @Override
  public IQuantity sub( IQuantity another )
                    throws QuantityOperationException
  {
    if (!(another instanceof ScalarQuantity))
    {
      throw new QuantityOperationException( "Not a ScalarQuantity!" );
    }

    return new ScalarQuantity( "Difference", (this.value - another.getQuantity()) );
  }

  @Override
  public IQuantity multiply( IQuantity another )
  {
    IQuantity mult = (IQuantity)another.copy( );
		
    return mult.multiply( this );
  }

  @Override
  public IQuantity divide( IQuantity another )
  {
    // return RatioQuantity - this / another
    ScalarQuantity copy = (ScalarQuantity)copy( );
		
    return new RatioQuantity( copy, (IQuantity)another.copy( ) );
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
