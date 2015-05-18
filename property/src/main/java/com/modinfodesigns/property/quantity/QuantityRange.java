package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;

public class QuantityRange implements IRangeProperty, IPropertySet
{
    private IQuantity minimum;
    private IQuantity maximum;
    
    public void setMinimum( IQuantity minimum ) throws QuantityOperationException
    {
    	if (maximum != null && !maximum.getType().equals( minimum.getType() ))
    	{
    		throw new QuantityOperationException( "minimum must be of same quantity type as maximum" );
    	}
    	
    	this.minimum = minimum;
    }
    
    public void setMaximum( IQuantity maximum ) throws QuantityOperationException
    {
    	if (minimum != null && !minimum.getType().equals( maximum.getType() ))
    	{
    		throw new QuantityOperationException( "minimum must be of same quantity type as maximum" );
    	}
    	
    	this.maximum = maximum;
    }
    
	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public void setName(String name)
	{

	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
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
	public IProperty copy()
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
	public IPropertySet union(IPropertySet another)
			throws PropertyTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPropertySet intersection(IPropertySet another)
			throws PropertyTypeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean contains(IPropertySet another) throws PropertyTypeException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(IProperty another) throws PropertyTypeException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean intersects(IPropertySet another)
			throws PropertyTypeException {
		// TODO Auto-generated method stub
		return false;
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
