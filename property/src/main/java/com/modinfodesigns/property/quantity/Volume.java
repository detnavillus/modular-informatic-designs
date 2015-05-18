package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;

public class Volume implements IQuantity
{
	private String name;
	
	private double volume;    // cubic meters
	
	public Volume( ) {  }
	
	public Volume( String name, double volume )
	{
		this.name = name;
		this.volume = volume;
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
		return null;
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
	public String getDefaultFormat()
	{
		return null;
	}

	@Override
	public IProperty copy()
	{
		return null;
	}

	@Override
	public Object getValueObject()
	{
		return new Double( volume );
	}

	@Override
	public double getQuantity()
	{
		return volume;
	}

	@Override
	public double getQuantity(String units)
	{
		return 0;
	}

	@Override
	public String[] getUnits()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQuantity add( IQuantity another)
			              throws QuantityOperationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQuantity sub( IQuantity another )
			              throws QuantityOperationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQuantity multiply(IQuantity another)
	{
		if (another instanceof ScalarQuantity)
		{
			
		}
		
		return null;
	}

	@Override
	public IQuantity divide(IQuantity another)
	{
		if (another instanceof Volume)
		{
			// return ScalarQuantity
			double result = this.volume / another.getQuantity();
			return new ScalarQuantity( "Dividend", result );
		}
		else if (another instanceof Area )
		{
			// return Distance
			double result = this.volume / another.getQuantity();
			return new Distance( "Dividend", result );
		}
		else if (another instanceof Distance)
		{
			double result = this.volume / another.getQuantity();
			return new Area( "Dividend", result );
		}
		
		
		return new RatioQuantity( "Dividend", (IQuantity)copy( ), (IQuantity)another.copy( ) );
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
