package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;

public class Velocity implements IQuantity
{
    private double velocity;  // meters per second 
    private String name;
    
    public Velocity( ) {  }
    
    public Velocity( String name, double velocity )
    {
    	this.name = name;
    	this.velocity = velocity;
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
		return Double.toString( velocity );
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
		Velocity vel = new Velocity( );
		vel.name = this.name;
		vel.velocity = this.velocity;
		
		return vel;
	}

	@Override
	public Object getValueObject()
	{
		return new Double( velocity );
	}

	@Override
	public double getQuantity()
	{
		return velocity;
	}

	@Override
	public double getQuantity( String units )
	{
		return 0;
	}

	@Override
	public String[] getUnits()
	{
		return null;
	}

	@Override
	public IQuantity add( IQuantity another )
			              throws QuantityOperationException
	{
		return null;
	}

	@Override
	public IQuantity sub( IQuantity another)
			              throws QuantityOperationException
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
