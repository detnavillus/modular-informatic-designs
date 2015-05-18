package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;

public class ProductQuantity implements IQuantity 
{
    private String name;
    
    private IQuantity operand1;
    private IQuantity operand2;
    
    public ProductQuantity( )
    {
    	
    }
    
    public ProductQuantity( IQuantity op1, IQuantity op2 )
    {
    	this.operand1 = op1;
    	this.operand2 = op2;
    }
    
    
    
	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
        this.name = name;
	}

	@Override
	public String getType()
	{
		return "com.modinfodesigns.property.quantity.ProductQuantity";
	}

	@Override
	public String getValue()
	{
		if (operand1 == null || operand2 == null) return null;
		
		return operand1.getValue() + " " + operand2.getValue( );
	}

	@Override
	public String getValue(String format)
	{
		if (operand1 == null || operand2 == null) return null;
		
		return operand1.getValue( format ) + " " + operand2.getValue( format );
	}

	@Override
	public void setValue(String value, String format)
	{
		// NOT IMPLEMENTED
	}

	@Override
	public IProperty copy()
	{
		return new ProductQuantity( (IQuantity)operand1.copy(), (IQuantity)operand2.copy() );
	}

	@Override
	public double getQuantity()
	{
		if (operand1 == null || operand2 == null) return 0;
		
		return operand1.getQuantity() * operand2.getQuantity( );
	}

	@Override
	public double getQuantity(String units)
	{
		if (operand1 == null || operand2 == null) return 0;
		
		return operand1.getQuantity( units ) * operand2.getQuantity( units );
	}

	@Override
	public String[] getUnits()
	{
		return null;
	}

	@Override
	public IQuantity add( IQuantity another ) throws QuantityOperationException
	{
		// another MUST be a ProductQuantity and must have the same two operand types.
		
		// IF one of the Operands type-pair have equal values, can create a 
		// new ProductQuantity with the other type-pair sum and the equal type-pair
		if (another instanceof ProductQuantity == false)
		{
			throw new QuantityOperationException( "Cannot add " + another );
		}
		
		ProductQuantity anotherProd = (ProductQuantity)another;
		if ( compareProductTypes( anotherProd ) )
		{
			IQuantity equalQuantity = null;
			IQuantity addQuantity = null;
			
			int equalQs = getEqualQuantity( anotherProd );
			if (equalQs > 0)
			{
			    if (equalQs == 11)
			    {
			    	equalQuantity = operand1;
			    	addQuantity = operand2.add( anotherProd.operand2 );
			    }
			    else if (equalQs == 12)
			    {
			    	equalQuantity = operand1;
			    	addQuantity = operand2.add( anotherProd.operand1 );
			    }
			    else if (equalQs == 21)
			    {
			    	equalQuantity = operand2;
			    	addQuantity = operand1.add( anotherProd.operand2 );
			    }
			    else if (equalQs == 22)
			    {
			    	equalQuantity = operand2;
			    	addQuantity = operand1.add( anotherProd.operand1 );
			    }
			}
			
			if (equalQuantity != null && addQuantity != null)
			{
				return new ProductQuantity( equalQuantity, addQuantity );
			}
		}
		
		throw new QuantityOperationException( "Cannot add " + another );
	}

	
	@Override
	public IQuantity sub( IQuantity another ) throws QuantityOperationException
	{
		if (another instanceof ProductQuantity == false)
		{
			throw new QuantityOperationException( "Cannot subtract " + another );
		}
		
		ProductQuantity anotherProd = (ProductQuantity)another;
		if ( compareProductTypes( anotherProd ) )
		{
			IQuantity equalQuantity = null;
			IQuantity addQuantity = null;
			
			int equalQs = getEqualQuantity( anotherProd );
			if (equalQs > 0)
			{
			    if (equalQs == 11)
			    {
			    	equalQuantity = operand1;
			    	addQuantity = operand2.sub( anotherProd.operand2 );
			    }
			    else if (equalQs == 12)
			    {
			    	equalQuantity = operand1;
			    	addQuantity = operand2.sub( anotherProd.operand1 );
			    }
			    else if (equalQs == 21)
			    {
			    	equalQuantity = operand2;
			    	addQuantity = operand1.sub( anotherProd.operand2 );
			    }
			    else if (equalQs == 22)
			    {
			    	equalQuantity = operand2;
			    	addQuantity = operand1.sub( anotherProd.operand1 );
			    }
			}
			
			if (equalQuantity != null && addQuantity != null)
			{
				return new ProductQuantity( equalQuantity, addQuantity );
			}
		}
		
		throw new QuantityOperationException( "Cannot subtract " + another );

	}

	@Override
	public IQuantity multiply( IQuantity another )
	{
		// if another is a ProductQuantity and has the same type-pair create a composite
		// where each 
		if (another instanceof RatioQuantity)
		{
			
		}
		else if (another instanceof ProductQuantity)
		{
			
		}
		
		return new ProductQuantity( (IQuantity)this.copy( ), (IQuantity)another.copy() );
	}

	@Override
	public IQuantity divide(IQuantity another)
	{
		return null;
	}
	
	private boolean compareProductTypes( ProductQuantity anotherProd )
	{
		if ((anotherProd.operand1.getType().equals( this.operand1.getType())) &&
			(anotherProd.operand2.getType().equals( this.operand2.getType())) )
		{
			return true;
		}

		if ((anotherProd.operand1.getType().equals( this.operand2.getType())) &&
			(anotherProd.operand2.getType().equals( this.operand1.getType())) )
		{
			return true;
		}
		
		return false;
	}
	
	private int getEqualQuantity( ProductQuantity anotherProd )
	{
		if ( operand1.getType().equals( anotherProd.operand1.getType() )
		  && operand1.getQuantity() == anotherProd.operand1.getQuantity() )
		{
			return 11;
		}
		
		if ( operand1.getType().equals( anotherProd.operand2.getType() )
	      && operand1.getQuantity() == anotherProd.operand2.getQuantity() )
		{
			return 12;
		}

		if ( operand2.getType().equals( anotherProd.operand2.getType() )
	      && operand2.getQuantity() == anotherProd.operand2.getQuantity() )
		{
			return 22;
		}

		if ( operand2.getType().equals( anotherProd.operand1.getType() )
		  && operand2.getQuantity( ) == anotherProd.operand1.getQuantity() )
		{
			return 21;
		}
		
		return 0;
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
	
	public static IQuantity createProductQuantity( String fromUnits )
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
