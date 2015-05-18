package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;

public class Density implements IQuantity
{
  private static String[] VALID_UNITS = { };
  private String name;
  private double density;  // kilograms per cubic meter
    
  public Density( ) {  }
    
  public Density( String name, double density )
  {
    this.name = name;
    this.density = density;
  }
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName( String name )
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
    return Double.toString( density );
  }

  @Override
  public String getValue(String format)
  {
    return Double.toString( getQuantity( format ));
  }

  @Override
  public void setValue(String value, String format)
  {

  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    return new Density( this.name, this.density );
  }

  @Override
  public Object getValueObject()
  {
    return new Double( this.density );
  }

  @Override
  public double getQuantity()
  {
    return density;
  }

  @Override
  public double getQuantity( String units )
  {
    return 0;
  }

  @Override
  public String[] getUnits()
  {
    return VALID_UNITS;
  }

  @Override
  public IQuantity add( IQuantity another )
                        throws QuantityOperationException
  {
    return null;
  }

  @Override
  public IQuantity sub( IQuantity another )
                        throws QuantityOperationException
  {
    return null;
  }

  @Override
  public IQuantity multiply( IQuantity another )
  {
    return null;
  }

  @Override
  public IQuantity divide( IQuantity another )
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
