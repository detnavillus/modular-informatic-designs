package com.modinfodesigns.property.quantity;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.transform.string.StringTransform;

/**
 * Represents an Integer Range, pairing a minimum and a maximum value.
 * 
 * @author Ted Sullivan
 */

public class IntegerRangeProperty implements IProperty, IRangeProperty, IPropertySet, IComputableProperties, Comparable<IntegerRangeProperty>
{
  private static ArrayList<String> intrinsics;
  private String name;
    
  private int minimum;
  private int maximum;
    
  private String delimiter = " - ";
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Minimum" );
    intrinsics.add( "Maximum" );
  }
    
  public IntegerRangeProperty(  )
  {
    	
  }
    
  public IntegerRangeProperty( String name, int minimum, int maximum )
  {
    this.name = name;
    this.minimum = (minimum <= maximum) ? minimum : maximum;
    this.maximum = (minimum <= maximum) ? maximum : minimum;
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
    return this.getClass().getCanonicalName( );
  }

  @Override
  public String getValue()
  {
    return getValue( "{MIN} - {MAX}" );
  }

  @Override
  // format can be a {MIN} {MAX} type of format string
  // can be a delimiter
  public String getValue( String format )
  {
    if (format != null && format.equals( IProperty.JSON_FORMAT) )
    {
      return new String( "\"" + getName() + "\":" + getValue( IProperty.JSON_VALUE ) );
    }
    else if (format != null && format.equals( IProperty.JSON_VALUE ))
    {
      return new String( "\"" + Integer.toString( minimum ) + delimiter + Integer.toString( maximum ) + "\"" );
    }
    else if (format != null && format.equals( IProperty.XML_FORMAT ))
    {
      return "<Property type=\"com.modinfodesigns.property.quantity.IntegerRangeProperty\"><Name>"
                + StringTransform.escapeXML( this.name ) + "</Name><Minimum>"
                + Integer.toString( minimum ) + "</Minimum><Maximum>"
                + Integer.toString( maximum ) + "</Maximum></Property>";
    }

    return new String( Integer.toString( minimum ) + delimiter + Integer.toString( maximum ) );
  }

  @Override
  public void setValue(String value, String format)
  {
    if (format == null)
    {
        
    }
    if (format.equals(IProperty.DELIMITED_FORMAT))
    {
        
    }
    else if (format.startsWith( "delimiter=" ))
    {
			
    }
  }

  @Override
  public IProperty copy()
  {
    return  new IntegerRangeProperty( this.name, this.minimum, this.maximum );
  }
	
  public void setMinimum( int minimum )
  {
    this.minimum = minimum;
  }
	
  public int getMinimum( )
  {
    return this.minimum;
  }
	
  public void setMaximum( int maximum )
  {
    this.maximum = maximum;
  }
	
  public int getMaximum(  )
  {
    return this.maximum;
  }
	
  public boolean overlaps( IntegerRangeProperty another )
  {
    if ( contains(another) || another.contains( this )) return true;
		
    return ((another.maximum >= this.minimum && another.maximum <= this.maximum) ||
            (another.minimum >= this.minimum && another.minimum <= this.minimum) ||
            (another.minimum == this.maximum) || (another.maximum == this.minimum));
  }
	
  public boolean overlaps( LongRangeProperty another )
  {
    if ( contains(another) || another.contains( this )) return true;
		
    return (((int)another.getMaximum() >= this.minimum && (int)another.getMaximum() <= this.maximum) ||
            ((int)another.getMinimum() >= this.minimum && (int)another.getMinimum() <= this.minimum) ||
            ((int)another.getMinimum() == this.maximum) || ((int)another.getMaximum() == this.minimum));
  }
	
  public boolean contains( IntegerRangeProperty another )
  {
    return (another.minimum >= this.minimum && another.maximum <= this.maximum);
  }
	
  public boolean contains( LongRangeProperty another )
  {
    return ((int)another.getMinimum( ) >= this.minimum && (int)another.getMaximum( ) <= this.maximum);
  }
	
  public boolean contains( int value )
  {
    return (value >= this.minimum && value <= this.maximum);
  }
	
  public IntegerRangeProperty intersection( IntegerRangeProperty another )
  {
    if (contains(another))
    {
      return (IntegerRangeProperty)another.copy( );
    }
    else if (another.contains( this ))
    {
      return (IntegerRangeProperty)copy( );
    }
    else if (overlaps( another ))
    {
      if (contains( another.minimum ))
      {
        return new IntegerRangeProperty( "intersect", another.minimum, this.maximum );
      }
      else
      {
        return new IntegerRangeProperty( "intersect", this.minimum, another.maximum );
      }
    }
		
    return null;
  }
	
  public IntegerRangeProperty union(IntegerRangeProperty another )
  {
    return new IntegerRangeProperty( "union", Math.min( this.minimum, another.minimum), Math.max( this.maximum, another.maximum ));
  }


  @Override
  public int compareTo(IntegerRangeProperty irp)
  {
    return minimum - irp.minimum;
  }
	
  public int distance( IntegerRangeProperty another )
  {
    int distance = -1;
		
    if (another.maximum == minimum || (maximum == another.minimum))
    {
      distance = 0;
    }
    else if (another.contains( this ) || this.contains( another ))
    {
      distance = 0;
    }
    else if (another.overlaps( this ))
    {
      distance = 0;
    }
    else if (another.minimum >= maximum)
    {
      distance = another.minimum - maximum;
    }
    else if (minimum >= another.maximum)
    {
      distance = minimum - another.maximum;
    }
		
    return distance;
  }

  @Override
  public Object getValueObject()
  {
    return this;
  }

  @Override
  public String getDefaultFormat()
  {
    return "delimiter= - ";
  }

  @Override
  public IPropertySet union( IPropertySet another )
                            throws PropertyTypeException
  {
    if (!(another instanceof IntegerRangeProperty) || another == null)
    {
      throw new PropertyTypeException( "Not an IntegerRangeProperty!" );
    }
		
    return union( (IntegerRangeProperty)another );
  }

  @Override
  public IPropertySet intersection( IPropertySet another )
                                    throws PropertyTypeException
  {
    if (!(another instanceof IntegerRangeProperty) || another == null)
    {
      throw new PropertyTypeException( "Not an IntegerRangeProperty!" );
    }
		
    return intersection( (IntegerRangeProperty)another );
  }

	
  @Override
  public boolean contains(IPropertySet another)
                        throws PropertyTypeException {
    if (!(another instanceof IntegerRangeProperty) || another == null)
    {
      throw new PropertyTypeException( "Not an IntegerRangeProperty!" );
    }
    
    return contains( (IntegerRangeProperty)another );
  }

	
  @Override
  public boolean contains( IProperty another )
                            throws PropertyTypeException
  {
    if ( (!(another instanceof IntegerRangeProperty) && !(another instanceof IntegerProperty))
        || another == null)
    {
      throw new PropertyTypeException( "Not an IntegerRangeProperty or IntegerProperty!" );
    }
		
    if (another instanceof IntegerRangeProperty)
    {
      return contains( (IntegerRangeProperty)another );
    }
    else
    {
      return contains( (IntegerProperty)another );
    }
  }

  @Override
  public boolean intersects( IPropertySet another )
                            throws PropertyTypeException
  {
    if (!(another instanceof IntegerRangeProperty) || another == null)
    {
      throw new PropertyTypeException( "Not an IntegerRangeProperty!" );
    }
		
    return overlaps( (IntegerRangeProperty)another );
  }

  @Override
  public List<String> getIntrinsicProperties()
  {
    return intrinsics;
  }

  @Override
  public IProperty getIntrinsicProperty(String name)
 {
    if (name.equals( "Minimum" ))
    {
        
    }
    else if (name.equals( "Maximum" ))
    {
    
    }
    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    return null;
  }
	
  @Override
  public IProperty getComputedProperty( String name, IProperty fromProp)
  {
    // can be things like shifted etc...
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
