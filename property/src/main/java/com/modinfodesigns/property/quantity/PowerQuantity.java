package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;

public class PowerQuantity implements IQuantity
{
    private IQuantity baseQuan;
    private int power;
    private String name;
    
    public PowerQuantity( ) {  }
    
    public PowerQuantity( String name, IQuantity baseQuantity, int power )
    {
    	this.name = name;
    	this.baseQuan = baseQuantity;
    	this.power = power;
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
		return getClass().getCanonicalName();
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
		return new PowerQuantity( this.name, (IQuantity)baseQuan.copy(), this.power );
	}

	@Override
	public Object getValueObject()
	{
		return null;
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
	public IQuantity add( IQuantity another )
			              throws QuantityOperationException
	{
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
