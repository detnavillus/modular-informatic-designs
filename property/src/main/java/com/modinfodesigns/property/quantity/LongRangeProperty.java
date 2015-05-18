package com.modinfodesigns.property.quantity;


import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;

public class LongRangeProperty implements IProperty, IRangeProperty, IPropertySet, Comparable<LongRangeProperty>
{
  private String name;
	
  private long minimum;
  private long maximum;
	
  public LongRangeProperty( ) { }
	
  public LongRangeProperty( String name, long minimum, long maximum )
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
    return this.getClass().getCanonicalName();
  }

  @Override
  public String getValue()
  {
    return getValue( "{MIN} - {MAX}" );
  }

  @Override
  public String getValue(String format)
  {
    return new String( Long.toString( minimum ) + " - " + Long.toString( maximum ) );
  }

  @Override
  public void setValue(String value, String format)
  {

  }

  @Override
  public IProperty copy()
  {
    return new LongRangeProperty( this.name, this.minimum, this.maximum );
  }

  public void setMinimum( long minimum )
  {
    this.minimum = minimum;
  }
	
  public long getMinimum( )
  {
    return this.minimum;
  }
	
  public void setMaximum( long maximum )
  {
    this.maximum = maximum;
  }
	
  public long getMaximum( )
  {
    return this.maximum;
  }
	
  public boolean overlaps( LongRangeProperty another )
  {
    if ( contains(another) || another.contains( this )) return true;
		
    return ((another.maximum >= this.minimum && another.maximum <= this.maximum) ||
            (another.minimum >= this.minimum && another.minimum <= this.minimum) ||
            (another.minimum == this.maximum) || (another.maximum == this.minimum) );
  }
	
  public boolean overlaps( IntegerRangeProperty another )
  {
    if ( contains(another) || another.contains( this )) return true;
    
    return ((another.getMaximum() >= this.minimum && another.getMaximum() <= this.maximum) ||
            (another.getMinimum() >= this.minimum && another.getMinimum() <= this.minimum) ||
            (another.getMinimum() == this.maximum) || (another.getMaximum() == this.minimum));
  }
	
  public boolean contains( LongRangeProperty another )
  {
    return (another.minimum >= this.minimum && another.maximum <= this.maximum);
  }
	
  public boolean contains( IntegerRangeProperty another )
  {
    return (another.getMinimum() >= this.minimum && another.getMaximum() <= this.maximum);
  }
	
  public boolean contains( long value )
  {
    return (value >= this.minimum && value <= this.maximum);
  }
	
  public LongRangeProperty intersection( LongRangeProperty another )
  {
    if (contains(another))
    {
      return (LongRangeProperty)another.copy( );
    }
    else if (another.contains( this ))
    {
      return (LongRangeProperty)copy( );
    }
    else if (overlaps( another ))
    {
      if (contains( another.minimum ))
      {
        return new LongRangeProperty( "intersect", another.minimum, this.maximum );
      }
      else
      {
        return new LongRangeProperty( "intersect", this.minimum, another.maximum );
      }
    }
		
    return null;
  }
    
  public LongRangeProperty union( LongRangeProperty another )
  {
    return new LongRangeProperty( "union", Math.min( this.minimum, another.minimum), Math.max( this.maximum, another.maximum ));
  }
	
  public void add( long offset )
  {
    this.minimum += offset;
    this.maximum += offset;
  }

  @Override
  public int compareTo(LongRangeProperty lrp)
  {
    return (int)(minimum - lrp.minimum);
  }
	
  @Override
  public boolean equals( Object another )
  {
    if (!(another instanceof LongRangeProperty)) return false;
		
    LongRangeProperty anotherLRP = (LongRangeProperty)another;
    return (anotherLRP.minimum == this.minimum && anotherLRP.maximum == this.maximum);
  }
	
  public long distance( LongRangeProperty another )
  {
    long distance = -1;
		
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
    return null;
  }

  @Override
  public IPropertySet union( IPropertySet another )
                             throws PropertyTypeException
  {
    return null;
  }

  @Override
  public IPropertySet intersection( IPropertySet another )
                                    throws PropertyTypeException
  {
    return null;
  }

  @Override
  public boolean contains( IPropertySet another )
                            throws PropertyTypeException
  {
    return false;
  }

  @Override
  public boolean contains( IProperty another )
                            throws PropertyTypeException
  {
    return false;
  }

  @Override
  public boolean intersects( IPropertySet another )
                            throws PropertyTypeException
  {
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
