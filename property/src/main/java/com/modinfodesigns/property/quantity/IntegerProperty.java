package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.quantity.IQuantity;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.PropertyValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegerProperty implements IQuantity, Comparable<IntegerProperty>
{
  private transient static final Logger LOG = LoggerFactory.getLogger( IntegerProperty.class );

  private String name;
	
  protected int value;
	
  public IntegerProperty( ) { }
	
  public IntegerProperty( String name, Integer value )
  {
    this.name = name;
    this.value = value.intValue( );
  }
	
  public IntegerProperty( String name, int value )
  {
    this.name = name;
    this.value = value;
  }
	
  public IntegerProperty( String name, String value )
  {
    this.name = name;
    this.value = Integer.parseInt( value );
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
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue()
  {
    return Integer.toString( this.value );
  }
	
  public int getIntegerValue( )
  {
    return this.value;
  }
	
  public int compareTo( int compVal )
  {
    return compVal - this.value;
  }

  @Override
  public String getValue(String format)
  {
    if (format == null || format.equals( IProperty.JSON_VALUE ))
    {
      return Integer.toString( this.value );
    }
    else if (format.equals( IProperty.JSON_FORMAT ))
    {
      return "\"" + this.name + "\":" + Integer.toString( this.value );
    }
    else if (format.equals( IProperty.XML_FORMAT ) || format.equals( IProperty.XML_FORMAT_CDATA) )
    {
      return "<Property type=\"com.modinfodesigns.property.quantity.IntegerProperty\" ><Name>"
             + (format.equals(IProperty.XML_FORMAT_CDATA) ? ("<![CDATA[" + name) : StringTransform.escapeXML( name  ))
             + "</Name><Value>" + Integer.toString( value ) + "</Value></Property>";
    }
		
    return Integer.toString( this.value );
  }
	
  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    LOG.debug( "IntegerProperty: setValue '" + value + "'" );
		
    try
    {
      if (value.indexOf( "." ) > 0)
      {
        String intVal = new String( value.substring( 0, value.indexOf( "." )));
        this.value = Integer.parseInt( intVal );
      }
      else
      {
        this.value = Integer.parseInt( value );
      }
			
      LOG.debug( "Set Value to: " + this.value );
    }
    catch (NumberFormatException nfe )
    {
      LOG.debug( "Cannot set integer value from '" + value + "'" );
      throw new PropertyValidationException( "Value must be an Integer: " + value );
    }
		
  }
	
  public void setValue( int value )
  {
    this.value = value;
  }
	
  public void setValue( String value ) throws PropertyValidationException
  {
    try
    {
      this.value = Integer.parseInt( value );
    }
    catch (NumberFormatException nfe )
    {
      throw new PropertyValidationException( "Value must be an Integer: " + value );
    }
  }
	
  public void increment( )
  {
    this.value++;
  }
	
  public void add( int increment )
  {
    this.value += increment;
  }
	
  @Override
  public IProperty copy( )
  {
    IntegerProperty copy = new IntegerProperty( );
    copy.setName( name );
    copy.value = value;
    
    return copy;
  }
	
  public double getQuantity( )
  {
    return (double)value;
  }
	
	
  public double getQuantity( String units )
  {
    return (double)value;
  }
    
  /**
   * @return List of valid Units for this Quantity
   */
  public String[] getUnits( )
  {
    return new String[0];
  }

  public IQuantity add( IQuantity another ) throws QuantityOperationException
  {
    if (another instanceof IntegerProperty)
    {
      IntegerProperty anotherInt = (IntegerProperty)another;
      return new IntegerProperty( "sum", (this.value + anotherInt.value) );
    }
    // or other scalar quantities
    throw new QuantityOperationException( "Can't add Integer to " + another );
  }
    
  public IQuantity sub( IQuantity another ) throws QuantityOperationException
  {
    throw new QuantityOperationException( "Can't subtract Integer from " + another );

  }
    
  public IQuantity multiply( IQuantity another )
  {
    // multiplication of a scalar by any quantity is that type of quantity.
    return null;
  }
    
  public IQuantity divide( IQuantity another )
  {
    return null;
  }
 
  @Override
  public int compareTo(IntegerProperty intProp)
  {
    return intProp.getIntegerValue( ) - value;
  }

  @Override
  public Object getValueObject()
  {
    return new Integer( value );
  }

  @Override
  public String getDefaultFormat()
  {
    return "regexpr(\\d+)";
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
