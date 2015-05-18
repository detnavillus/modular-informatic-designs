package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.security.IUserCredentials;

import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;

/**
 * Matches Integer-based properties.  Will try to match a String-based property
 * if its value can be parsed by java.lang.Integer.
 * 
 * @author Ted Sullivan
 */
public class IntegerPropertyMatcher implements IPropertyMatcher
{
  private int compareTo;
	
  private String mode = "EQ";  // EQ | GT | LT | LE | GE

  @Override
  public boolean equals ( IUserCredentials user, IProperty property)
  {
    if (property == null) return false;
		
    if (property instanceof IntegerRangeProperty)
    {
      return doesEqual( (IntegerRangeProperty)property );
    }
    else if (property instanceof LongRangeProperty)
    {
      return doesEqual( (LongRangeProperty)property );
    }
		
    Integer integerVal = getIntegerValue( property );
    if (integerVal == null) return false;
		
    return doesEqual( integerVal.intValue( ) );
  }
	
  private boolean doesEqual( int intVal )
  {
    if (mode.equals( "EQ" ))
    {
      return intVal == compareTo;
    }
    else if (mode.equals( "GT" ))
    {
      return intVal > compareTo;
    }
    else if (mode.equals( "LT" ))
    {
      return intVal < compareTo;
    }
    else if (mode.equals( "GE" ))
    {
      return intVal >= compareTo;
    }
    else if (mode.equals( "LE" ))
    {
      return intVal <= compareTo;
    }
		
    return false;
  }

  private Integer getIntegerValue( IProperty property )
  {
    if (property instanceof IntegerProperty)
    {
      return new Integer( ((IntegerProperty)property).getIntegerValue( ) );
    }
    // if a quantity - get the double value
    else if (property instanceof IQuantity)
    {
      double quantity = ((IQuantity)property).getQuantity( );
      return new Integer( (int)quantity );
    }
    else
    {
      try
      {
        Integer intValue = new Integer( property.getValue( ) );
        return intValue;
      }
      catch (NumberFormatException nfe)
      {
        // log warning but this is not comparable ...
      }
    }
				
    return null;
  }
	
  private boolean doesEqual( IntegerRangeProperty irp )
  {
    if (mode.equals( "EQ" ))
    {
      return irp.contains( compareTo );
    }
    else if (mode.equals( "LT" ))
    {
      return irp.getMaximum() < compareTo;
    }
    else if (mode.equals( "GT" ))
    {
      return irp.getMinimum() > compareTo;
    }
    else if (mode.equals( "LE" ))  // not greater than
    {
      return irp.getMaximum() >= compareTo;
    }
    else if (mode.equals( "GE" ))
    {
      return irp.getMinimum( ) <= compareTo;
    }
    
    return false;
  }
	
  private boolean doesEqual( LongRangeProperty lrp )
  {
		
    if (mode.equals( "EQ" ))
    {
      return lrp.contains( (long)compareTo );
    }
    else if (mode.equals( "LT" ))
    {
      return lrp.getMaximum() < (long)compareTo;
    }
    else if (mode.equals( "GT" ))
    {
      return lrp.getMinimum() > (long)compareTo;
    }
    else if (mode.equals( "LE" ))  // not greater than
    {
      return lrp.getMaximum() >= (long)compareTo;
    }
    else if (mode.equals( "GE" ))
    {
      return lrp.getMinimum( ) <= (long)compareTo;
    }
    
    return false;
  }
}
