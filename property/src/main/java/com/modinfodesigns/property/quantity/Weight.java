package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;

public class Weight implements IQuantity
{
    public static String[] VALID_UNITS = { "kilograms", "grams", "milligrams", "micrograms",
    	                                   "pounds", "ounces" };
    
    private String name;
    private double weight;  // kilograms
    
    public Weight( ) {  }
    
    public Weight( String name, double weight )
    {
    	this.name = name;
    	this.weight = weight;
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
		return Double.toString( weight );
	}

	@Override
	public String getValue(String format)
	{
		return Double.toString( getQuantity( format ));
	}

	@Override
	public void setValue(String value, String format)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public String getDefaultFormat()
	{
		return "kilograms";
	}

	@Override
	public IProperty copy()
	{
		return new Weight( this.name, this.weight );
	}

	@Override
	public Object getValueObject()
	{
		return new Double( weight );
	}

	@Override
	public double getQuantity()
	{
		return weight;
	}

	@Override
	public double getQuantity(String units)
	{

		return weight;
	}

	@Override
	public String[] getUnits()
	{
		return VALID_UNITS;
	}

	@Override
	public IQuantity add( IQuantity another)
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
	public IQuantity multiply( IQuantity another )
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
