package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.PropertyValidationException;


public class Area implements IQuantity
{
  public static String[] VALID_UNITS = { "square-inches", "square-feet", "square-yards",
                                         "square-miles", "square-millimeters",
                                         "square-centimeters", "square-meters",
                                         "square-kilometers", "acres" };

  private String name;
    
  private double area;   // square-meters
    
  public Area( ) {  }
    
  public Area( String name, double area )
  {
    this.name = name;
    this.area = area;
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
    return Double.toString( area );
  }

  @Override
  public String getValue( String format )
  {
    return Double.toString( getQuantity( format ));
  }

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {

  }

  @Override
  public String getDefaultFormat()
  {
    return "square-meters";
  }

  @Override
  public IProperty copy()
  {
    Area area = new Area( );
    area.area = this.area;
    return area;
  }

  @Override
  public Object getValueObject()
  {
    return new Double( area );
  }

  @Override
  public double getQuantity()
  {
    return area;
  }

  @Override
  public double getQuantity(String units)
  {
    if (units.equals( "square-meters" ))
    {
      return area;
    }
    else if (units.equals( "square-centimeters" ))
    {
      return area * 10000.0;
    }
    else if (units.equals( "square-millimeters" ))
    {
      return area * 1000000.0;
    }
    else if (units.equals( "square-miles" ))
    {
        
    }
    else if (units.equals( "square-inches" ))
    {
			
    }
    else if (units.equals( "square-feet" ))
    {
        
    }
    else if (units.equals( "square-yards" ))
    {
        
    }
		
    return area;
  }

  @Override
  public String[] getUnits()
  {
    return VALID_UNITS;
  }

  @Override
  public IQuantity add( IQuantity another ) throws QuantityOperationException
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
		
    if (!(anotherQ instanceof Area))
    {
      throw new QuantityOperationException( "Not an Area!" );
    }
		
    Area sumArea = new Area( );
    sumArea.area = this.area + ((Area)anotherQ).area;
		
    return sumArea;
  }

  @Override
  public IQuantity sub( IQuantity another ) throws QuantityOperationException
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
		
    
    if (!(anotherQ instanceof Area))
    {
      throw new QuantityOperationException( "Not an Area!" );
    }
		
    Area diffArea = new Area( );
    diffArea.area = this.area - ((Area)anotherQ).area;
    
    return diffArea;
  }

  @Override
  public IQuantity multiply(IQuantity another)
  {
    if (another instanceof Distance)
    {
      double result = this.area * another.getQuantity();
      return new Volume( "multiply", result );
    }
		
    return null;
  }

  @Override
  public IQuantity divide( IQuantity another )
  {
    if (another instanceof Area)
    {
      Area anotherArea = (Area)another;
      double result = this.area / anotherArea.area;
      return new ScalarQuantity( "Dividend", result );
    }
    else if (another instanceof Distance)
    {
      Distance anotherDist = (Distance)another;
      double result = this.area / anotherDist.getQuantity();
      return new Distance( "Dividend", result );
    }
    
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
