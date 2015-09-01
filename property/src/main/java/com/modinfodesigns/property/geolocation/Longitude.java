package com.modinfodesigns.property.geolocation;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * similar to an Angle quantity but can only go from -180 to +180
 */

public class Longitude implements IQuantity
{
  private static ArrayList<String> intrinsics;
	
  private String name;
  private double degrees;
    
  private char degSymbol = '°';
    
  private static double TwoPI = 2.0 * Math.PI;
    
  public static String[] VALID_UNITS = { "degrees", "radians" };
    
  private String defaultUnits = "degrees";
    
  public Longitude( ) {  }
    
  public Longitude( double degrees ) throws PropertyValidationException
  {
    this.degrees = degrees;
        
    if (this.degrees > 180.0 || this.degrees < -180.0 )
    {
      throw new PropertyValidationException( "Degrees must be between -180.0 and 180.0" );
    }
  }
    
  public Longitude( IQuantity degrees ) throws PropertyValidationException
  {
    this( degrees.getQuantity( ) );
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
    return Double.toString( degrees );
  }
    
  @Override
  public String getValue( String format )
  {
    if (format == null || format.equals( "radians" ))
    {
      return Double.toString( (degrees / 360.0) * TwoPI );
    }
    else if (format.equals( "E|W degrees" ) || format.equals( "EW degrees" ) || format.equals( "E/W degrees"))
    {
        StringBuilder strb = new StringBuilder( );
        strb.append( (degrees >= 0.0) ? "E " : "W " ).append( Double.toString( degrees ));
        return strb.toString( );
    }
    else if (format.equals( "degrees E|W" ) || format.equals( "degrees EW" ) || format.equals( "degrees E/W"))
    {
        StringBuilder strb = new StringBuilder( );
        strb.append( Double.toString( degrees )).append( (degrees >= 0.0) ? " E" : " W" );
        return strb.toString( );
    }
    else if (format.equals( "{E|W} d° m.m′" ))
    {
      // calculate minutes in decimal
      String dir = (degrees >= 0.0 ) ? "E" : "W";
      int degInt = (int)Math.abs( degrees );
      double fraction = degrees - (double)degInt;
        
      double minutes = fraction * 60.0;
      StringBuilder stb = new StringBuilder( );
      stb.append( dir ).append( " " ).append( Integer.toString( degInt )).append( degSymbol )
         .append( " " ).append( Double.toString( minutes ) ).append( "'" );
      return stb.toString( );
    }
    else if (format.equals( "d° m' s\" {E|W}"))
    {
      String dir = (degrees >= 0.0 ) ? "E" : "W";
      int degInt = (int)Math.abs( degrees );
      double fraction = degrees - (double)degInt;
        
      double minutes = fraction * 60.0;
      int minInt = (int)minutes;
      double minfrac = minutes - (double)minInt;
      double seconds = minfrac * 60.0;
      int secInt = (int)seconds;
        
      StringBuilder stb = new StringBuilder( );
      stb.append( Integer.toString( degInt )).append( degSymbol )
         .append( " " ).append( Integer.toString( secInt ) ).append( "' " )
         .append( Integer.toString( secInt )).append( "\" ").append( dir );
      return stb.toString( );
    }
    else
    {
      return Double.toString( degrees );
    }
  }
    
  @Override
  public void setValue( String value, String format)
              throws PropertyValidationException
  {
    try
    {
      if (format == null || format.equals( "radians" ))
      {
        double radians = Double.parseDouble( value );
        this.degrees = (radians / TwoPI ) * 360.0;
      }
      else if (format.equals( "degrees" ))
      {
        this.degrees = Double.parseDouble( value );
      }
      else if (format.equals( "E|W degrees" ) || format.equals( "EW degrees") || format.equals( "E/W degrees"))
      {
        String[] parts = format.split( " " );
        this.degrees = Double.parseDouble( parts[1] );
        if (parts[0].equalsIgnoreCase( "W" ))
        {
          this.degrees = -this.degrees;
        }
      }
      else if (format.equals( "degrees E|W" ) || format.equals( "degrees EW") || format.equals( "degrees E/W"))
      {
        String[] parts = format.split( " " );
        this.degrees = Double.parseDouble( parts[0] );
        if (parts[1].equalsIgnoreCase( "W" ))
        {
          this.degrees = -this.degrees;
        }
      }

      if (this.degrees > 180.0 || this.degrees < -180.0 )
      {
        throw new PropertyValidationException( "Degrees must be between -180.0 and 180.0" );
      }
    }
    catch ( NumberFormatException nfe )
    {
      throw new PropertyValidationException( "value must be a Number!" );
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
    Longitude copy = new Longitude( );
    copy.degrees = this.degrees;
    copy.name = this.name;
		
    return copy;
  }
    
  @Override
  public Object getValueObject()
  {
    return new Double( degrees );
  }
    
  @Override
  public boolean isMultiValue()
  {
    return false;
  }
    
  @Override
  public String[] getValues(String format)
  {
    return null;
  }
    
  @Override
  public double getQuantity()
  {
    return degrees;
  }

  @Override
  public double getQuantity(String units)
  {
    if (units == null || units.equals( "degrees" ))
    {
      return degrees;
    }
    else if (units.equals( "radians" ))
    {
      return (degrees / 360.0) * TwoPI;
    }
		
    return 0;
  }
    
  @Override
  public String[] getUnits()
  {
    return VALID_UNITS;
  }
    
  @Override
  public IQuantity add( IQuantity another ) throws QuantityOperationException
  {
    if (another instanceof Longitude )
    {
        // create a new angle
    }
        
    return null;
  }
    
  @Override
  public IQuantity sub(IQuantity another) throws QuantityOperationException
  {
    return null;
  }
    
  @Override
  public IQuantity multiply(IQuantity another)
  {
    return null;
  }
    
  @Override
  public IQuantity divide(IQuantity another)
  {
    return null;
  }
    
}


