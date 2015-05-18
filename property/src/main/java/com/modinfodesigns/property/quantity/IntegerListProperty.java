package com.modinfodesigns.property.quantity;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.IComputableProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a list of integer values.
 * 
 * @author Ted Sullivan
 *
 */

public class IntegerListProperty implements IProperty, IPropertySet, IComputableProperties
{
  private static ArrayList<String> intrinsics;
  private String name;
    
  private ArrayList<Integer> integerList = new ArrayList<Integer>( );
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( "Count" );
    intrinsics.add( "Average" );
    intrinsics.add( "Range" );
    intrinsics.add( "Minimum" );
    intrinsics.add( "Maximum" );
  }
    
  public IntegerListProperty( ) { }
    
  public IntegerListProperty( String name, int[] integers )
  {
    this.name = name;
    for (int i = 0; i < integers.length; i++)
    {
      integerList.add( new Integer( integers[i] ));
    }
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
    return "java.util.ArrayList<Integer>";
  }

  @Override
  public String getValue()
  {
    return getValue( IProperty.DELIMITED_FORMAT );
  }

  @Override
  public String getValue(String format)
  {
    if (format.equals( IProperty.DELIMITED_FORMAT ))
    {
      StringBuffer strbuf = new StringBuffer( );
      for (int i = 0; i < integerList.size(); i++)
      {
        Integer intVal = integerList.get( i );
        strbuf.append( intVal.toString( ) );
        if (i < integerList.size() - 1)
        {
          strbuf.append( "," );
        }
      }
			
      return strbuf.toString( );
    }
    else if (format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuffer strbuf = new StringBuffer( );
      strbuf.append( "\"" ).append( getName() ).append( "\":" );
      strbuf.append( getValue( IProperty.JSON_VALUE ) );
    }
    else if (format.equals( IProperty.JSON_VALUE ))
    {
      StringBuffer strbuf = new StringBuffer( );
      strbuf.append( "[");
      strbuf.append( getValue( IProperty.DELIMITED_FORMAT ));
      strbuf.append( "]" );
      return strbuf.toString( );
    }
    else if (format.equals( IProperty.XML_FORMAT ))
    {
        
    }
		
    return null;
  }
	
  @Override
  public void setValue( String value, String format )
  {
    // IMPLEMENT ME!!
  }
	
  public void addInteger( int integer )
  {
    integerList.add( new Integer( integer ));
  }
	
  public boolean contains( int value )
  {
    return integerList.contains( new Integer( value ));
  }
	
  public boolean contains( IntegerListProperty another )
  {
    int[] itsInts = another.getIntegers( );
    for (int i = 0; i < itsInts.length; i++)
    {
      if (!contains( itsInts[i] )) return false;
    }
		
    return true;
  }
	
  public List<Integer> getIntegerList( )
  {
    return integerList;
  }
	
  public int size( )
  {
    return integerList.size( );
  }
	
  public IProperty copy( )
  {
    return null;
  }
	
  public int[] getIntegers( )
  {
    int[] intArray = new int[ integerList.size() ];
    for (int i = 0; i < integerList.size(); i++)
    {
      intArray[i] = integerList.get(i).intValue();
    }
		
    return intArray;
  }

  @Override
  public Object getValueObject()
  {
    return integerList;
  }

  @Override
  public String getDefaultFormat()
  {
    return IProperty.DELIMITED_FORMAT;
  }

  @Override
  public IPropertySet union( IPropertySet another ) throws PropertyTypeException
  {
    if (!(another instanceof IntegerListProperty))
    {
      throw new PropertyTypeException( "Not an IntegerListProperty!" );
    }
		
    IntegerListProperty union = (IntegerListProperty)copy( );
    IntegerListProperty anotherLst = (IntegerListProperty)another;
    int[] itsInts = anotherLst.getIntegers( );
    for (int i = 0; i < itsInts.length; i++)
    {
      if (!contains( itsInts[i]))
      {
        union.addInteger( itsInts[i] );
      }
    }
		
    return union;
  }

	
  @Override
  public IPropertySet intersection( IPropertySet another ) throws PropertyTypeException
  {
    if (!(another instanceof IntegerListProperty))
    {
      throw new PropertyTypeException( "Not an IntegerListProperty!" );
    }
		
    IntegerListProperty intersection = new IntegerListProperty( );
    intersection.setName( this.name );
    IntegerListProperty anotherLst = (IntegerListProperty)another;
    int[] itsInts = anotherLst.getIntegers( );
    for (int i = 0; i < itsInts.length; i++)
    {
      if (contains( itsInts[i] ))
      {
        intersection.addInteger( itsInts[i] );
      }
    }
		
    return intersection;
  }

  /**
   * Returns true if another is an IntegerListProperty and it
   * has all of the values that this IntegerListProperty has
   */
  @Override
  public boolean contains( IPropertySet another ) throws PropertyTypeException
  {
    if (!(another instanceof IntegerListProperty))
    {
      throw new PropertyTypeException( "Not an IntegerListProperty!" );
    }
		
    IntegerListProperty anotherLst = (IntegerListProperty)another;
    return contains( anotherLst );
  }
	
	
  @Override
  public boolean contains( IProperty another ) throws PropertyTypeException
  {
    if ((!(another instanceof IntegerListProperty) && !(another instanceof IntegerProperty))
        || another == null)
    {
      throw new PropertyTypeException( "Not an IntegerRangeProperty or IntegerProperty" );
    }
		
    if (another instanceof IntegerProperty)
    {
      IntegerProperty intProp = (IntegerProperty)another;
      return contains( intProp.getIntegerValue() );
    }
    else
    {
      return contains( (IntegerListProperty)another );
    }
  }

  @Override
  public boolean intersects( IPropertySet another ) throws PropertyTypeException
  {
    if (!(another instanceof IntegerListProperty))
    {
      throw new PropertyTypeException( "Not an IntegerListProperty!" );
    }
		
    IntegerListProperty anotherLst = (IntegerListProperty)another;
    int[] itsInts = anotherLst.getIntegers( );
    
    for ( int i = 0; i < itsInts.length; i++)
    {
      if (contains( itsInts[i] )) return true;
    }
		
    return false;
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
      int size = integerList.size( );
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
    int sum = 0;
    for (int i = 0; i < integerList.size(); i++)
    {
      Integer anInt = integerList.get( i );
      sum += anInt.intValue();
    }
		
    double avg = (double)sum / (double)integerList.size( );
    
    return new ScalarQuantity( "Average", avg );
  }
	
  public IntegerProperty getMinimum( )
  {
    int minimum = Integer.MAX_VALUE;
    for (int i = 0; i < integerList.size(); i++)
    {
      Integer anInt = integerList.get( i );
      if (anInt.intValue() < minimum)
      {
        minimum = anInt.intValue();
      }
    }
		
    return new IntegerProperty( "Minimum", minimum );
  }
	
  public IntegerProperty getMaximum( )
  {
    int maximum = Integer.MIN_VALUE;
    for (int i = 0; i < integerList.size(); i++)
    {
      Integer anInt = integerList.get( i );
      if (anInt.intValue() > maximum )
      {
        maximum = anInt.intValue();
      }
    }
		
    return new IntegerProperty( "Maximum", maximum );
  }
	
  public IntegerRangeProperty getRange(  )
  {
    IntegerProperty minProp = getMinimum( );
    IntegerProperty maxProp = getMaximum( );
		
    return new IntegerRangeProperty( "Range", minProp.getIntegerValue(), maxProp.getIntegerValue() );
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
    String[] values = new String[integerList.size( )];
    for (int i = 0, isz = integerList.size( ); i < isz; i++)
    {
      values[i] = Integer.toString( integerList.get( i ) );
    }
    return values;
  }

}
