package com.modinfodesigns.property.quantity.function;

import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.QuantityOperationException;

import com.modinfodesigns.property.compare.PropertyExpressionMatcher;

import java.util.Iterator;

public class CountProperty extends IntegerProperty implements IFunctionProperty
{
  private String name;
  private IPropertyHolder propHolder;
    
  private PropertyExpressionMatcher propMatcher;
    
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
    IQuantity count = computeCount( );
    return (count != null) ? count.getValue( ) : null;
  }

  @Override
  public String getValue(String format)
  {
    IQuantity count = computeCount( );
    return (count != null) ? count.getValue( format ) : null;
  }

  @Override
  public void setValue(String value, String format)
            throws PropertyValidationException
  {
    throw ( new PropertyValidationException( "Cannot set value of function property!" ));
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    CountProperty copy = new CountProperty( );
    copy.name = this.name;
    copy.propHolder = this.propHolder;
    copy.propMatcher = this.propMatcher;
		
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return null;
  }

  @Override
  public void setPropertyHolder(IPropertyHolder propHolder)
  {
    this.propHolder = propHolder;
  }


  @Override
  public void setFunction( String function )
  {
    this.propMatcher = new PropertyExpressionMatcher( function );
  }

  @Override
  public String getFunction()
  {
    return (propMatcher != null) ? propMatcher.getMatchExpression( ) : null;
  }

  @Override
  public IProperty execute()
  {
    return computeCount( );
  }
	
  private IQuantity computeCount( )
  {
    IntegerProperty count = new IntegerProperty(  );
    if (propHolder != null && propMatcher != null)
    {
      Iterator<IProperty> propIt = propHolder.getProperties( );
      while( propIt != null && propIt.hasNext() )
      {
        IProperty prop = propIt.next( );
        if (propMatcher.equals( prop ))
        {
          count.increment( );
        }
      }
			
      if (propHolder instanceof IDataList)
      {
        Iterator<DataObject> dobjIt = ((IDataList)propHolder).getData( );
        while (dobjIt != null && dobjIt.hasNext() )
        {
          DataObject dob = dobjIt.next( );
          if (propMatcher.equals( dob ))
          {
            count.increment( );
          }
        }
      }
    }
		
    return count;
  }

  @Override
  public double getQuantity()
  {
    IQuantity count = computeCount( );
    return count.getQuantity( );
  }

  @Override
  public double getQuantity(String units)
  {
    IQuantity count = computeCount( );
    return count.getQuantity( units );
  }

  @Override
  public String[] getUnits()
  {
    return null;
  }

  @Override
  public IQuantity add( IQuantity another ) throws QuantityOperationException
  {
    IQuantity count = computeCount( );
    return (count != null) ? count.add( another ) : null;
  }

  @Override
  public IQuantity sub(IQuantity another) throws QuantityOperationException
  {
    IQuantity count = computeCount( );
    return (count != null) ? count.sub( another ) : null;
  }

  @Override
  public IQuantity multiply(IQuantity another)
  {
    IQuantity count = computeCount( );
    return (count != null) ? count.multiply( another ) : null;
  }

  @Override
  public IQuantity divide(IQuantity another)
  {
    IQuantity count = computeCount( );
    return (count != null) ? count.divide( another ) : null;
  }
}
