package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;

public interface IQuantity extends IProperty
{
  public double getQuantity( );
  public double getQuantity( String units );
    
  /**
   * @return List of valid Units for this Quantity
   */
  public String[] getUnits( );

  public IQuantity add( IQuantity another ) throws QuantityOperationException;
  public IQuantity sub( IQuantity another ) throws QuantityOperationException;
  public IQuantity multiply( IQuantity another );
  public IQuantity divide( IQuantity another );
}
