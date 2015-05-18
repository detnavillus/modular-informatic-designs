package com.modinfodesigns.property.quantity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.IComputableProperties;

public class LongListProperty implements IProperty, IPropertySet, IComputableProperties
{
  private static ArrayList<String> intrinsics;
  private String name;
    
  private ArrayList<Long> longList = new ArrayList<Long>( );
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Count" );
    intrinsics.add( "Average" );
    intrinsics.add( "Range" );
    intrinsics.add( "Minimum" );
    intrinsics.add( "Maximum" );
  }
    
  @Override
  public String getName()
  {
    return this.name;
  }
	
  public void setName( String name )
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
    return null;
  }

  @Override
  public String getValue(String format)
  {
    return null;
  }
	
  @Override
  public void setValue( String value, String format )
  {
    // IMPLEMENT ME!!
  }

  @Override
  public IProperty copy( )
  {
    LongListProperty llp = new LongListProperty( );
    llp.name = this.name;
    llp.longList = this.longList;
		
    return llp;
  }
	
  public void addLong( long value )
  {
    longList.add( new Long( value ) );
  }
	
  public long[] getLongs( )
  {
    long[] longArray = new long[ longList.size() ];
    for (int i = 0; i < longList.size(); i++)
    {
      longArray[i] = longList.get(i).longValue();
    }
		
    return longArray;
  }
	
  public boolean contains( long longVal )
  {
    for (int i = 0; i < longList.size(); i++)
    {
      Long l = longList.get( i );
      if (l.longValue() == longVal ) return true;
    }
		
    return false;
  }
	
  public boolean contains( LongListProperty another )
  {
    long[] itsLongs = another.getLongs( );
    for (int i = 0; i < itsLongs.length; i++)
    {
      if (!contains( itsLongs[i] )) return false;
    }
		
    return true;
  }

  @Override
  public Object getValueObject()
  {
    return longList;
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IPropertySet union( IPropertySet another)
                            throws PropertyTypeException
  {
    if (!(another instanceof LongListProperty))
    {
      throw new PropertyTypeException( "Not a LongListProperty!" );
    }
		
    LongListProperty union = (LongListProperty)copy( );
    LongListProperty anotherLst = (LongListProperty)another;
    long[] itsLongs = anotherLst.getLongs( );
    for (int i = 0; i < itsLongs.length; i++)
    {
      if (!contains( itsLongs[i]))
      {
        union.addLong( itsLongs[i] );
      }
    }
		
    return union;
  }

  @Override
  public IPropertySet intersection( IPropertySet another )
                                    throws PropertyTypeException
  {
    if (!(another instanceof LongListProperty))
    {
      throw new PropertyTypeException( "Not a LongListProperty!" );
    }
		
    LongListProperty intersection = new LongListProperty( );
    intersection.setName( this.name );
    LongListProperty anotherLst = (LongListProperty)another;
    long[] itsLongs = anotherLst.getLongs( );
    for (int i = 0; i < itsLongs.length; i++)
    {
      if (contains( itsLongs[i] ))
      {
        intersection.addLong( itsLongs[i] );
      }
    }
		
    return intersection;
  }

  @Override
  public boolean contains( IPropertySet another )
                            throws PropertyTypeException
  {
    if (!(another instanceof LongListProperty))
    {
      throw new PropertyTypeException( "Not a LongListProperty!" );
    }
		
    LongListProperty anotherLst = (LongListProperty)another;
    return contains( anotherLst );
  }

  @Override
  public boolean contains( IProperty another )
                            throws PropertyTypeException
  {
    if ((!(another instanceof LongListProperty))
        || another == null)
    {
      throw new PropertyTypeException( "Not a LongRangeProperty" );
    }
		
    return contains( (LongListProperty)another );
  }

  @Override
  public boolean intersects( IPropertySet another )
                            throws PropertyTypeException
  {
    if ((!(another instanceof IntegerListProperty))
        || another == null)
    {
      throw new PropertyTypeException( "Not a LongRangeProperty" );
    }
		
    return contains( (LongListProperty)another );
  }

  @Override
  public List<String> getIntrinsicProperties()
  {
    return intrinsics;
  }

  @Override
  public IProperty getIntrinsicProperty( String name )
  {
    if (name.equals( "Count" ))
    {
      int size = longList.size( );
      return new IntegerProperty( name, size );
    }
    else if (name.equals( "Average" ))
    {
      return getAverage( );
    }
    else if (name.equals( "Range" ))
    {
      return getRange( );
    }
    else if (name.equals( "Minimum" ))
    {
      return getMinimum( );
    }
    else if (name.equals( "Maximum" ))
    {
      return getMaximum( );
    }
    return null;
  }

  public ScalarQuantity getAverage(  )
  {
    long sum = 0;
    for (int i = 0; i < longList.size(); i++)
    {
      Long aLong = longList.get( i );
      sum += aLong.longValue();
    }
		
    double avg = (double)sum / (double)longList.size( );
		
    return new ScalarQuantity( "Average", avg );
  }
	
  public LongProperty getMinimum( )
  {
    long minimum = Long.MAX_VALUE;
    for (int i = 0; i < longList.size(); i++)
    {
      Long aLong = longList.get( i );
      if (aLong.longValue() < minimum)
      {
        minimum = aLong.longValue();
      }
    }
		
    return new LongProperty( "Minimum", minimum );
  }
	
  public LongProperty getMaximum( )
  {
    long maximum = Long.MIN_VALUE;
    for (int i = 0; i < longList.size(); i++)
    {
      Long aLong = longList.get( i );
      if (aLong.longValue() > maximum )
      {
        maximum = aLong.intValue();
      }
    }
		
    return new LongProperty( "Maximum", maximum );
  }
	
  public LongRangeProperty getRange(  )
  {
    LongProperty minProp = getMinimum( );
    LongProperty maxProp = getMaximum( );
    
    return new LongRangeProperty( "Range", minProp.getLongValue(), maxProp.getLongValue() );
  }
	
	
  @Override
  public Map<String, String> getComputableProperties()
  {
    return null;
  }

  @Override
  public IProperty getComputedProperty(String name, IProperty fromProp)
  {
    return null;
  }

  @Override
  public boolean isMultiValue()
  {
    return true;
  }

  @Override
  public String[] getValues(String format)
  {
    String[] values = new String[longList.size( )];
    for (int i = 0, isz = longList.size( ); i < isz; i++)
    {
      values[i] = Long.toString( longList.get( i ) );
    }
    return values;
  }

}
