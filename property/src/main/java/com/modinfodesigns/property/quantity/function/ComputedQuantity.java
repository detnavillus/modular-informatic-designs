package com.modinfodesigns.property.quantity.function;

import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;

/**
 * A computed quantity uses a calculator expression to derive a property from a 
 * set of IQuantity properties held in an IPropertyHolder object.
 * 
 * @author Ted Sullivan
 */

public class ComputedQuantity implements IFunctionProperty, IQuantity
{
  private IPropertyHolder propHolder;
  private String calculatorExpression;
  private String name;
    
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
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue()
  {
    IQuantity outputQuan = computeQuantity( );
    return (outputQuan != null) ? outputQuan.getValue() : null;
  }

  @Override
  public String getValue( String format )
  {
    if (format.equals( IFunctionProperty.FUNCTION ))
    {
      return calculatorExpression;
    }
		
    IQuantity outputQuan = computeQuantity( );
    return (outputQuan != null) ? outputQuan.getValue( format ) : null;
  }

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    throw new PropertyValidationException( "setValue cannot be called on computed quantity." );
  }

  @Override
  public String getDefaultFormat()
  {
    IQuantity outputQuan = computeQuantity( );
    return (outputQuan != null) ? outputQuan.getDefaultFormat( ) : null;
  }

  @Override
  public IProperty copy()
  {
    ComputedQuantity copy = new ComputedQuantity( );
    copy.name = this.name;
    copy.calculatorExpression = this.calculatorExpression;
    copy.propHolder = this.propHolder;
    
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    IQuantity outputQuan = computeQuantity( );
    return (outputQuan != null) ? new Double( outputQuan.getQuantity() ) : null;
  }

  @Override
  public void setPropertyHolder( IPropertyHolder propHolder )
  {
    this.propHolder = propHolder;
  }


  @Override
  public void setFunction( String function )
  {
    this.calculatorExpression = function;
  }

  @Override
  public String getFunction()
  {
    return this.calculatorExpression;
  }
	
  private IQuantity computeQuantity(  )
  {
    try
    {
      return ExpressionCalculator.calculate( calculatorExpression, this.propHolder );
    }
    catch ( QuantityOperationException pte )
    {
			
    }
		
    return null;
  }

  @Override
  public double getQuantity()
  {
    IQuantity computedQuan = computeQuantity( );
		
    return (computedQuan != null) ? computedQuan.getQuantity( ) : 0.0D;
  }

  @Override
  public double getQuantity(String units)
  {
    IQuantity computedQuan = computeQuantity( );
    return (computedQuan != null) ? computedQuan.getQuantity( units ) : 0.0D;
  }

  @Override
  public String[] getUnits()
  {
    IQuantity computedQuan = computeQuantity( );
    return (computedQuan != null) ? computedQuan.getUnits(  ) : null;
  }

  @Override
  public IQuantity add( IQuantity another ) throws QuantityOperationException
  {
    IQuantity computedQuan = computeQuantity( );
    return (computedQuan != null) ? computedQuan.add( another ) : another;
  }

  @Override
  public IQuantity sub( IQuantity another ) throws QuantityOperationException
  {
    IQuantity computedQuan = computeQuantity( );
    return (computedQuan != null) ? computedQuan.sub( another ) : null;
  }

  @Override
  public IQuantity multiply( IQuantity another )
  {
    IQuantity computedQuan = computeQuantity( );
    return (computedQuan != null) ? computedQuan.multiply( another ) : null;
  }

  @Override
  public IQuantity divide( IQuantity another )
  {
    IQuantity computedQuan = computeQuantity( );
    return (computedQuan != null) ? computedQuan.divide( another ) : null;
  }

  @Override
  public IProperty execute()
  {
    return computeQuantity( );
  }

  @Override
  public boolean isMultiValue()
  {
    IProperty computedQuan = execute( );
    return (computedQuan != null) ? computedQuan.isMultiValue(  ) : false;
  }

  @Override
  public String[] getValues(String format)
  {
    IProperty computedQuan = execute( );
    return (computedQuan != null) ? computedQuan.getValues( format ) : null;
  }

}
