package com.modinfodesigns.property.quantity.function;

import java.util.ArrayList;
import java.util.Iterator;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;

public class SumProperty implements IFunctionProperty, IQuantity
{
  private IPropertyHolder propHolder;
  private String name;
  private String targetProperty;

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
    try
    {
      IQuantity quantity = computeSum( );
      return (quantity != null) ? quantity.getValue( ) : null;
    }
    catch ( QuantityOperationException qoe )
    {
    		
    }
    	
    return null;
  }

  @Override
  public String getValue(String format)
  {
    try
    {
      IQuantity quantity = computeSum( );
      return (quantity != null) ? quantity.getValue( format ) : null;
    }
    catch ( QuantityOperationException qoe )
    {
    		
    }
    	
    return null;
  }

  @Override
  public void setValue( String value, String format )
             throws PropertyValidationException
  {
    throw new PropertyValidationException( "Cannot set value on a FunctionProperty!" );
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    SumProperty copy = new SumProperty( );
    copy.name = this.name;
    copy.targetProperty = targetProperty;
    copy.propHolder = propHolder;
    
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    try
    {
      IQuantity sumQ = computeSum( );
      return (sumQ != null) ? new Double( sumQ.getQuantity() ) : null;
    }
    catch ( QuantityOperationException qoe )
    {
    		
    }
    	
    return null;
  }

  @Override
  public double getQuantity()
  {
    try
    {
      IQuantity sumQ = computeSum( );
      return (sumQ != null) ? sumQ.getQuantity() : 0.0;
    }
    catch ( QuantityOperationException qoe )
    {
    		
    }
    	
    return 0.0;
  }

  @Override
  public double getQuantity( String units )
  {
    try
    {
      IQuantity sumQ = computeSum( );
      return (sumQ != null) ? sumQ.getQuantity( units ) : 0.0;
    }
    catch ( QuantityOperationException qoe )
    {
    		
    }
    	
    return 0.0;
  }

  @Override
  public String[] getUnits()
  {
    try
    {
      IQuantity sumQ = computeSum( );
      return (sumQ != null) ? sumQ.getUnits( ) : null;
    }
    catch (QuantityOperationException qoe )
    {
            
    }
        
    return null;
  }

  @Override
  public IQuantity add(IQuantity another)
                    throws QuantityOperationException
  {
    IQuantity sumQ = computeSum( );
    if (sumQ != null)
    {
      return sumQ.add( another );
    }
    else
    {
      throw new QuantityOperationException( "Could not get Sum!" );
    }
  }

  @Override
  public IQuantity sub(IQuantity another) throws QuantityOperationException
  {
    IQuantity sumQ = computeSum( );
    if (sumQ != null)
    {
      return sumQ.sub( another );
    }
    else
    {
      throw new QuantityOperationException( "Could not get Sum!" );
    }
  }

  @Override
  public IQuantity multiply( IQuantity another )
  {
    try
    {
      IQuantity sumQ = computeSum( );
      if (sumQ != null)
      {
        return sumQ.multiply( another );
      }
    }
    catch ( QuantityOperationException qoe )
    {
            
    }
        
    return null;
  }

  @Override
  public IQuantity divide(IQuantity another)
  {
    try
    {
      IQuantity sumQ = computeSum( );
      if (sumQ != null)
      {
        return sumQ.divide( another );
      }
    }
    catch ( QuantityOperationException qoe )
    {
            
    }
    
    return null;
  }

  @Override
  public void setPropertyHolder(IPropertyHolder propHolder)
  {
    this.propHolder = propHolder;
  }

  @Override
  public void setFunction(String function)
  {
    this.targetProperty = function;
  }

  @Override
  public String getFunction()
  {
    return this.targetProperty;
  }

  @Override
  public IProperty execute()
  {
    try
    {
      return computeSum( );
    }
    catch ( QuantityOperationException qoe )
    {
            
    }
        
    return null;
  }

  private IQuantity computeSum( ) throws QuantityOperationException
  {
    IQuantity sum = null;
    ArrayList<IQuantity> quantities = getQuantities( );
    if (quantities != null)
    {
      for (int i = 0; i < quantities.size( ); i++)
      {
        if (i == 0)
        {
          sum = quantities.get( 0 );
        }
        else
        {
          sum = sum.add( quantities.get( i ) );
        }
      }
    }
    	
    return sum;
  }

  private ArrayList<IQuantity> getQuantities(  )
  {
    ArrayList<IQuantity> quantities = new ArrayList<IQuantity>( );
    if (propHolder == null) return quantities;
		
    if (propHolder instanceof IDataList)
    {
      IDataList propList = (IDataList)propHolder;
      Iterator<DataObject> dobIt = propList.getData( );
      while (dobIt != null && dobIt.hasNext() )
      {
        DataObject dob = dobIt.next( );
        IQuantity quan = (IQuantity)dob.getProperty( targetProperty );
        if (quan != null)
        {
          quantities.add( quan );
        }
      }
    }
    else
    {
      IQuantity quan = (IQuantity)propHolder.getProperty( targetProperty );
      if (quan != null)
      {
        quantities.add( quan );
      }
    }
		
    return quantities;
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
