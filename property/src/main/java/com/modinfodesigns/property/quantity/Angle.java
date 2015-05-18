package com.modinfodesigns.property.quantity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;

// Has Intrinsic Property: Number of revolutions (IntegerProperty)
// should convert - angles to positive ?

public class Angle implements IQuantity, IComputableProperties
{
  private static ArrayList<String> intrinsics;
	
  private String name;
  private double radians;
    
  private static double TwoPI = 2.0 * Math.PI;
    
  public static String[] VALID_UNITS = { "degrees", "radians" };
    
  private String defaultUnits = "radians";

  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Sine" );
    intrinsics.add( "Cosine" );
    intrinsics.add( "Tangent" );
    intrinsics.add( "Revolutions" );  // number of 360 degree revolutions
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
    return null;
  }

  @Override
  public String getValue()
  {
    return Double.toString( radians );
  }

  @Override
  public String getValue( String format )
  {
    if (format == null || format.equals( "radians" ))
    {
      return Double.toString( radians );
    }
    else if (format.equals( "degrees" ))
    {
      double degrees = (radians / TwoPI) * 360.0;
      return Double.toString( degrees );
    }
		
    return null;
  }

  @Override
  public void setValue( String value, String format)
                        throws PropertyValidationException
  {
    if (format == null || format.equals( "radians" ))
    {
      this.radians = Double.parseDouble( value );
    }
    else if (format.equals( "degrees" ))
    {
      double degrees = Double.parseDouble( value );
      this.radians = (degrees / 360.0) * TwoPI;
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
    Angle copy = new Angle( );
    copy.radians = this.radians;
    copy.name = this.name;
		
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return new Double( radians );
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
    return radians;
  }

  @Override
  public double getQuantity(String units)
  {
    if (units == null || units.equals( "radians" ))
    {
      return radians;
    }
    else if (units.equals( "degrees" ))
    {
      return (radians / TwoPI) * 360.0;
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
    if (another instanceof Angle )
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

  @Override
  public List<String> getIntrinsicProperties()
  {
    return intrinsics;
  }

  @Override
  public IProperty getIntrinsicProperty(String name)
  {
    if (name == null) return null;
        
    if (name.equals( "Sine" ))
    {
      return new ScalarQuantity( "Sine", Math.sin( radians ) );
    }
    else if (name.equals( "Cosine" ))
    {
      return new ScalarQuantity( "Cosine", Math.cos( radians ) );
    }
    else if (name.equals( "Tangent" ))
    {
      return new ScalarQuantity( "Tangent", Math.tan( radians ) );
    }
        

    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    return null;
  }

  @Override
  public IProperty getComputedProperty(String name, IProperty fromProp)
  {
    return null;
  }

}
