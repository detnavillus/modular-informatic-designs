package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TwoDimensionalVector implements IQuantity, IComputableProperties
{
	private static ArrayList<String> intrinsics;
	
	private String name;
	
	// NO: direction and magnitude are computable properties
	
    private IQuantity xCoordinate;
    private IQuantity yCoordinate;
    
    static
    {
    	intrinsics = new ArrayList<String>( );
    	intrinsics.add( "Direction" );
    	intrinsics.add( "Magnitude" );
    }
    
    public TwoDimensionalVector( ) {  }
    
    public TwoDimensionalVector( IQuantity xCoordinate, IQuantity yCoordinate )
    {
    	this.xCoordinate = xCoordinate;
    	this.yCoordinate = yCoordinate;
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
		return getClass().getCanonicalName();
	}

	@Override
	public String getValue()
	{
        if (xCoordinate == null || yCoordinate == null ) return null;
        return "(" + xCoordinate.getValue() + "," + yCoordinate.getValue( ) + " )";
	}

	// format should be as "(xCoordinate format, yCoordinate format )"
	@Override
	public String getValue( String format )
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
		if (xCoordinate == null || yCoordinate == null) return null;
		return "(" + xCoordinate.getDefaultFormat() + "," + yCoordinate.getDefaultFormat() + ")";
	}

	@Override
	public IProperty copy()
	{
		TwoDimensionalVector vc = new TwoDimensionalVector( );
		vc.xCoordinate = (this.xCoordinate != null) ? (IQuantity)this.xCoordinate.copy( ) : null;
		vc.yCoordinate = (this.yCoordinate != null) ? (IQuantity)this.yCoordinate.copy( ) : null;
		vc.name = this.name;
		
		return vc;
	}

	@Override
	public Object getValueObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getQuantity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getQuantity(String units) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IQuantity add(IQuantity another) throws QuantityOperationException
	{
        // can add another TwoDimensionalVector IF and Only IF, the direction and magnitude properties are
		// the same.
		
		
		return null;
	}

	@Override
	public IQuantity sub( IQuantity another ) throws QuantityOperationException
	{
		return null;
	}

	@Override
	public IQuantity multiply( IQuantity another )
	{
        // create a TwoDimensionalVector where 
		// xCoordinate = xCoordinate * another and yCoordinate = yCoordinate * another
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
		return true;
	}

	@Override
	public String[] getValues( String format )
	{
		String[] values = new String[2];
		values[0] = xCoordinate.getValue( format );   // ???
		values[1] = yCoordinate.getValue( format );
		return values;
	}

	@Override
	public List<String> getIntrinsicProperties()
	{
		return intrinsics;
	}

	@Override
	public IProperty getIntrinsicProperty( String name )
	{
        if ( name == null) return null;
        
        if (name.equals( "Direction" ))
        {
        	// compute the angle to the origin, 0,0
        }
        else if (name.equals( "Magnitude" ))
        {
        	// compute the distance to the origin, 0,0
        }
        
		return null;
	}

	// computable properties include distance to another Vector
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

}
