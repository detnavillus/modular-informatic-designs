package com.modinfodesigns.property.quantity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;

public class RatioQuantity implements IQuantity, IComputableProperties
{
	private static ArrayList<String> intrinsics;
	private String name;
	
	private IQuantity numerator;
	private IQuantity denominator;
	
	static
	{
		intrinsics = new ArrayList<String>( );
		intrinsics.add( "Numerator" );
		intrinsics.add( "Denominator" );
	}
	
	public RatioQuantity( ) {  }
	
	public RatioQuantity( String name, IQuantity numerator, IQuantity denominator )
	{
		this.name = name;
		this.numerator = numerator;
		this.denominator = denominator;
	}
	
	public RatioQuantity( IQuantity numerator, IQuantity denominator )
	{
		this.numerator = numerator;
		this.denominator = denominator;
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
		return Double.toString( getQuantity() );
	}

	@Override
	public String getValue(String format)
	{
		return null;
	}

	@Override
	public void setValue(String value, String format)
	{

	}

	@Override
	public IProperty copy() 
	{
        RatioQuantity rq = new RatioQuantity( );
        rq.name = this.name;
        rq.numerator = (numerator != null) ? (IQuantity)numerator.copy() : null;
        rq.denominator = (denominator != null) ? (IQuantity)denominator.copy() : null;
        
		return rq;
	}

	@Override
	public double getQuantity()
	{
		return 0;
	}

	@Override
	public double getQuantity(String units)
	{
		return 0;
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
		if (!(another instanceof RatioQuantity))
		{
			throw new QuantityOperationException( "Not a RatioQuantity!" );
		}
		
		
		// If so, create a new RatioQuantity where the numerator is the numerator sum
		// 
		return null;
	}

	@Override
	public IQuantity sub( IQuantity another )
			         throws QuantityOperationException 
	{
		return null;
	}

	@Override
	public IQuantity multiply(IQuantity another)
	{
		// if another's type is the same as my denominator type
		// return numerator type * Scalar of me / another.denominator
		
		return null;
	}

	@Override
	public IQuantity divide(IQuantity another)
	{
		return null;
	}

	@Override
	public Object getValueObject()
	{
		return this;
	}

	@Override
	public String getDefaultFormat()
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
		if (name.equals( "Numerator" ))
		{
			return numerator;
		}
		else if (name.equals( "Denominator" ))
		{
			return denominator;
		}
		return null;
	}

	@Override
	public Map<String, String> getComputableProperties()
	{
		return null;
	}
	

	@Override
	public IProperty getComputedProperty( String name, IProperty fromProp )
	{
		return null;
	}
	
	public IQuantity getNumerator( )
	{
		return numerator;
	}
	
	public IQuantity getDenominator( )
	{
		return denominator;
	}
	
	public static IQuantity createRatioQuantity( String fromUnits )
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
