package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.PropertyValidationException;

public class Distance implements IQuantity
{
  public static String[] VALID_UNITS = { "inches", "feet", "yards", "miles", "millimeters", "centimeters", "meters", "kilometers", "light-years"};
    
  private String name;
    
  private String defaultUnits = "meters";
    
  // Need unit conversions...
    
  double distance;   // distance in meters
    
  private double inches_per_meter = 39.3701;
    
  public Distance(  ) {  }
    
  public Distance( String name, double value )
  {
    this.name = name;
    this.distance = value;
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
    return this.getClass().getCanonicalName();
  }

  @Override
  public String getValue()
  {
    return getValue( defaultUnits );
  }

  /**
   * returns value in specified units. format must be one of the VALID_UNITS list
   */
  @Override
  public String getValue(String format)
  {
    if (format.equals( "meters" ))
    {
      return Double.toString( distance ) + " meters";
    }
    else if (format.equals( "inches" ))
    {
      return Double.toString( distance * inches_per_meter ) + " inches";
    }
    else if (format.equals( "feet" ))
    {
      return Double.toString( (distance * inches_per_meter ) / 12.0 ) + " feet";
    }
    else if (format.equals( "yards" ))
    {
      return Double.toString( (distance * inches_per_meter ) / 36.0 ) + " yards";
    }
    else if (format.equals( "centimeters" ))
    {
      return Double.toString( distance * 100.0 ) + " centimeters";
    }
    else if (format.equals( "millimeters" ))
    {
      return Double.toString( distance * 1000.0 ) + " millimeters";
    }
    else if (format.equals( "kilometers" ))
    {
      return Double.toString( distance / 1000.0 ) + " kilometers";
    }
    // etc...
    
    if (format.equals( IProperty.XML_FORMAT ))
    {
      StringBuilder strbuilder = new StringBuilder( );
      strbuilder.append( "<Property type=\"" ).append( getType( ) ).append( "\"> " );
        
      strbuilder.append( "</Property>" );
      return strbuilder.toString( );
    }
    return Double.toString( distance );
  }

  @Override
  public void setValue( String value, String format )  throws PropertyValidationException
  {
    try
    {
      double val = Double.parseDouble( value );
      if (format.equals( "meters" ))
      {
        this.distance = val;
      }
      else if (format.equals( "inches" ))
      {
        this.distance = val / inches_per_meter;
      }
      else if (format.equals( "feet" ))
      {
        this.distance = (val * 12.0) / inches_per_meter;
      }
      else if (format.equals( "yards" ))
      {
        this.distance = (val * 36.0) / inches_per_meter;
      }
      else if (format.equals( "centimeters" ))
      {
        this.distance = val / 100.0;
      }
      else if (format.equals( "millimeters" ))
      {
        this.distance = val / 1000.0;
      }
      else if (format.equals( "kilometers" ))
      {
        this.distance = val * 1000.0;
      }
      else
      {
        throw new PropertyValidationException( "Don't understand units: " + format );
      }
    }
    catch (NumberFormatException nfe )
    {
      throw new PropertyValidationException( "Value must be a number!" );
    }
  }

  @Override
  public IProperty copy()
  {
    Distance copy = new Distance( );
    copy.name = name;
    copy.distance = distance;
    
    return copy;
  }

  @Override
  public double getQuantity()
  {
    return distance;
  }

  @Override
  public double getQuantity(String units)
  {
    return 0;
  }

  @Override
  public String[] getUnits()
  {
    return VALID_UNITS;
  }

  @Override
  public IQuantity add( IQuantity another )  throws QuantityOperationException
  {
    IQuantity anotherQ = another;
		
    if (another instanceof IFunctionProperty )
    {
      IProperty funVal = ((IFunctionProperty)another).execute( );
      if (funVal instanceof IQuantity)
      {
        anotherQ = (IQuantity)funVal;
      }
      else
      {
        throw new QuantityOperationException( "Cannot resolve function value to a Quantity" );
      }
    }
		
    if (!(anotherQ instanceof Distance ))
    {
      throw new QuantityOperationException( "Not a Distance!" );
    }
		
    Distance sumDist = new Distance( );
    Distance anotherDist = (Distance)anotherQ;
    sumDist.name = this.name;
    sumDist.distance = this.distance + anotherDist.distance;
    
    return sumDist;
  }

  @Override
  public IQuantity sub( IQuantity another )  throws QuantityOperationException
  {
    IQuantity anotherQ = another;
		
    if (another instanceof IFunctionProperty )
    {
      IProperty funVal = ((IFunctionProperty)another).execute( );
      if (funVal instanceof IQuantity)
      {
        anotherQ = (IQuantity)funVal;
      }
      else
      {
        throw new QuantityOperationException( "Cannot resolve function value to a Quantity" );
      }
    }
		
		
    if (!(anotherQ instanceof Distance ))
    {
      throw new QuantityOperationException( "Not a Distance!" );
    }
		
    Distance difDist = new Distance( );
    Distance anotherDist = (Distance)anotherQ;
    difDist.name = this.name;
    difDist.distance = this.distance - anotherDist.distance;
		
    return difDist;
  }

  @Override
  public IQuantity multiply( IQuantity another )
  {
    // if quantity is a scalar - return distance
    // If quantity is another distance - return area
    // if quantity is area - return volume
    // else if quantity is RatioQuantity and denominator is Distance return numerator quantity
    // else return ProductQuantity
    
    if (another instanceof ScalarQuantity)
    {
      Distance output = new Distance( );
      output.name = this.name;
      output.distance = this.distance * another.getQuantity();
      return output;
    }
    else if (another instanceof Distance)
    {
      Area area = new Area( );
      // do the math
        
			
     return area;
    }
    else if (another instanceof Area)
    {
      Volume volume = new Volume( );
			
      return volume;
    }
    else if (another instanceof RatioQuantity)
    {
      // if denominator is distance, convert to Numerator * distance ratio
    }
		
    return new ProductQuantity( (IQuantity)this.copy( ), (IQuantity)another.copy( ));
  }

  @Override
  public IQuantity divide(IQuantity another)
  {
    // distance / distance = Scalar
    // distance / duration = Velocity
    // distance / Velocity = duration
    // else return RatioQuantity
    if (another instanceof Distance)
    {
      Distance anotherDist = (Distance)another;
      double result = this.distance / anotherDist.distance;
      return new ScalarQuantity( "Dividend", result );
    }
    else if (another instanceof Duration)
    {
      double result = this.distance / another.getQuantity( );
      return new Velocity( "Velocity", result );
    }
    else if (another instanceof Velocity)
    {
      double result = this.distance / another.getQuantity( );
      return new Duration( "Dividend", result );
    }
    else if (another instanceof ScalarQuantity)
    {
      double result = this.distance / another.getQuantity( );
      return new Distance( "Dividend", result );
    }
		
    // Create a RatioQuantity
    return new RatioQuantity( (IQuantity)this.copy( ), (IQuantity)another.copy( ));
  }

  @Override
  public Object getValueObject()
  {
    return this;
  }

  @Override
  public String getDefaultFormat()
  {
    return "meters";
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
