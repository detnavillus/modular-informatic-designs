package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;
import com.modinfodesigns.property.quantity.LongRangeProperty;
import com.modinfodesigns.property.quantity.IQuantity;

import com.modinfodesigns.security.IUserCredentials;

public class IntegerRangePropertyMatcher implements IPropertyMatcher
{
  private IntegerRangeProperty compareTo;

  private String mode = "EQ";  // EQ | GT | LT | LE | GE | INTERSECTS | CONTAINS

  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    if (property == null || compareTo == null) return false;
    if (property instanceof IntegerRangeProperty)
    {
      return doesEqual( (IntegerRangeProperty)property );
    }
    else if (property instanceof LongRangeProperty)
    {
      return doesEqual( (LongRangeProperty)property );
    }
	    
    Integer intValue = getIntegerValue( property );
	    
    return (intValue != null) ? doesEqual( intValue.intValue() ) : false;
  }
	
  private boolean doesEqual( int value )
  {
    if (mode.equals( "EQ" ))
    {
      return compareTo.contains( value );
    }
    else if (mode.equals( "GT" ))
    {
      return value > compareTo.getMaximum( );
    }
    else if (mode.equals( "LT" ))
    {
      return value < compareTo.getMinimum();
    }
    else if (mode.equals( "GE" ))
    {
      return value >= compareTo.getMinimum( );
    }
    else if (mode.equals( "LE" ))
    {
      return value <= compareTo.getMaximum( );
    }
    
    return false;
  }
	
  private Integer getIntegerValue( IProperty property )
  {
    if (property instanceof IntegerProperty)
    {
      return new Integer (((IntegerProperty)property).getIntegerValue() );
    }
    else if (property instanceof IQuantity)
    {
      return new Integer( (int)((IQuantity)property).getQuantity( ) );
    }
    else
    {
      try
      {
        return new Integer( property.getValue( ) );
      }
      catch (NumberFormatException nfe )
      {
        // well - at least we tried!
      }
    }
        
    return null;
  }
	
  private boolean doesEqual( IntegerRangeProperty irp )
  {
    if (mode.equals( "EQ" ))
    {
      return ( irp.getMinimum() == compareTo.getMinimum()
            && irp.getMaximum() == compareTo.getMaximum());
    }
    else if (mode.equals( "GT" ))
    {
      return (irp.getMinimum() > compareTo.getMinimum());
    }
    else if (mode.equals( "LT" ))
    {
      return (irp.getMinimum() < compareTo.getMinimum());
    }
    else if (mode.equals( "LE" ))
    {
      return (irp.getMinimum() <= compareTo.getMinimum());
    }
    else if (mode.equals( "GE" ))
    {
      return (irp.getMinimum() >= compareTo.getMinimum());
    }
    else if (mode.equals( "INTERSECTS" ) || mode.equals( "OVERLAPS" ))
    {
      return irp.overlaps( compareTo );
    }
    else if (mode.equals( "CONTAINS" ))
    {
      return irp.contains( compareTo );
    }
    
    return false;
  }
	
  private boolean doesEqual( LongRangeProperty lrp )
  {
    if (mode.equals( "EQ" ))
    {
      return ( lrp.getMinimum() == compareTo.getMinimum()
            && lrp.getMaximum() == compareTo.getMaximum());
    }
    else if (mode.equals( "GT" ))
    {
      return (lrp.getMinimum() > compareTo.getMinimum());
    }
    else if (mode.equals( "LT" ))
    {
      return (lrp.getMinimum() < compareTo.getMinimum());
    }
    else if (mode.equals( "LE" ))
    {
      return (lrp.getMinimum() <= compareTo.getMinimum());
    }
    else if (mode.equals( "GE" ))
    {
      return (lrp.getMinimum() >= compareTo.getMinimum());
    }
    else if (mode.equals( "INTERSECTS" ) || mode.equals( "OVERLAPS" ))
    {
      return lrp.overlaps( compareTo );
    }
    else if (mode.equals( "CONTAINS" ))
    {
      return lrp.contains( compareTo );
    }
		
    return false;
  }

}
