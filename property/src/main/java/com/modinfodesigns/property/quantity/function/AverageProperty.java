package com.modinfodesigns.property.quantity.function;

import com.modinfodesigns.property.IFunctionProperty;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.quantity.ScalarQuantity;
import com.modinfodesigns.property.quantity.QuantityOperationException;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Computes the average of a set of IQuantity attributes of an IPropertyHolder.
 * 
 * @author Ted Sullivan
 */

public class AverageProperty implements IQuantity, IFunctionProperty 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( AverageProperty.class );

  private IPropertyHolder propHolder;
  private String name;
    
  private String targetProperty;
    
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
  public String getType( )
  {
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue( )
  {
    IQuantity average = (IQuantity)execute( );
    return (average != null) ? average.getValue( ) : null;
  }

  @Override
  public String getValue(String format)
  {
    IQuantity average = (IQuantity)execute( );
    return (average != null) ? average.getValue( format ) : null;
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
    IQuantity average = (IQuantity)execute( );
    return (average != null) ? average.getDefaultFormat( ) : null;
  }

  @Override
  public IProperty copy()
  {
    AverageProperty copy = new AverageProperty( );
    copy.name = name;
    copy.targetProperty = targetProperty;
    copy.propHolder = propHolder;
		
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    IQuantity average = (IQuantity)execute( );
    return (average != null) ? new Double( average.getQuantity( ) ) : null;
  }

  @Override
  public void setPropertyHolder( IPropertyHolder propHolder )
  {
    this.propHolder = propHolder;
  }


  @Override
  public void setFunction( String function )
  {
    targetProperty = function;
  }

  @Override
  public String getFunction()
  {
    return targetProperty;
  }

  @Override
  public IProperty execute()
  {
    IQuantity average = null;
    
    // get all of the IQuantities for the targetProperty
    // compute the sum
    // divide the average.
    // if the propHolder is an IDataList ...
    ArrayList<IQuantity> quantities = getQuantities( );
    if (quantities != null)
    {
      for (int i = 0; i < quantities.size(); i++)
      {
        if (i == 0)
        {
          average = quantities.get( i );
        }
        else
        {
          try
          {
            average = average.add( quantities.get( i ) );
          }
          catch (QuantityOperationException qoe )
          {
            // can't add this one ...
            LOG.error( "Cannot add property: " + qoe );
          }
        }
      }
    }
		
    if (quantities.size( ) > 1 )
    {
      average = average.divide( new ScalarQuantity( "count", (double)quantities.size( ) ) );
    }
		
    return average;
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
  public double getQuantity()
  {
    IQuantity averageQuan = (IQuantity)execute( );
    return (averageQuan != null) ? averageQuan.getQuantity( ) : 0.0;
  }

  @Override
  public double getQuantity( String units )
  {
    IQuantity averageQuan = (IQuantity)execute( );
    return (averageQuan != null) ? averageQuan.getQuantity( units ) : 0.0;
  }

  @Override
  public String[] getUnits()
  {
    IQuantity averageQuan = (IQuantity)execute( );
    return (averageQuan != null) ? averageQuan.getUnits() : null;
  }

  @Override
  public IQuantity add(IQuantity another) throws QuantityOperationException
  {
    IQuantity averageQuan = (IQuantity)execute( );
    return (averageQuan != null) ? averageQuan.add( another ) : null;
  }

  @Override
  public IQuantity sub(IQuantity another) throws QuantityOperationException
  {
    IQuantity averageQuan = (IQuantity)execute( );
    return (averageQuan != null) ? averageQuan.sub( another ) : null;
  }

  @Override
  public IQuantity multiply( IQuantity another )
  {
    IQuantity averageQuan = (IQuantity)execute( );
    return (averageQuan != null) ? averageQuan.multiply( another ) : null;
  }

  @Override
  public IQuantity divide( IQuantity another )
  {
    IQuantity averageQuan = (IQuantity)execute( );
    return (averageQuan != null) ? averageQuan.divide( another ) : null;
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
