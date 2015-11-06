package com.modinfodesigns.property.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.transform.string.StringTransform;
import com.modinfodesigns.property.quantity.IntegerProperty;
import com.modinfodesigns.property.quantity.IRangeProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Implements a String List property. Maintains an map of Strings to counts. Deduplicates the strings.
 */

public class StringCountProperty extends StringListProperty implements IProperty, IPropertySet, IComputableProperties
{
  private HashMap<String,IntegerProperty> stringCounts = new HashMap<String,IntegerProperty>( );
    
  private String countDelimiter = ":";
    
  public StringCountProperty( ) {  }
    
  public StringCountProperty( String name )
  {
    setName( name );
  }
    
  public StringCountProperty( String name, Iterator<String> stringIt )
  {
    setName( name );
    addStrings( stringIt );
  }
    
  public StringCountProperty( String name, List<String> strings )
  {
    setName( name );
    for ( String string : strings )
    {
      addString( string );
    }
  }
    
  public StringCountProperty( String name, String[] stringLst )
  {
    setName( name );
    if (stringLst != null)
    {
      for (int i = 0; i < stringLst.length; i++)
      {
        addString( stringLst[i] );
      }
    }
  }

  @Override
  public String getType()
  {
    return getClass().getCanonicalName( );
  }
    
  @Override
  public String getValue()
  {
    return getValue( IProperty.DELIMITED_FORMAT );
  }
    
  @Override
  public String getValue(String format)
  {
    if (format == null || format.equals( IProperty.DELIMITED_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      for (Iterator<String> strIt = stringCounts.keySet().iterator(); strIt.hasNext( ); )
      {
        String str = strIt.next( );
        IntegerProperty count = stringCounts.get( str );
        sbr.append( str ).append( countDelimiter ).append( count.getValue( ) );
        if (strIt.hasNext( ) )
        {
          sbr.append( delimiter );
        }
      }
      return sbr.toString( );
    }
    else if (format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append("\"" ).append( getName( ) ).append( "\":" );
      sbr.append( getValue( IProperty.JSON_VALUE ));
      return sbr.toString();
    }
    else if (format.equals( IProperty.JSON_VALUE ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "[" );
      for (Iterator<String> strIt = stringCounts.keySet().iterator( ); strIt.hasNext( ); )
      {
        String string = strIt.next( );
        IntegerProperty count = stringCounts.get( string );
        sbr.append( "{\"" ).append( "\"value\":\"" ).append( string ).append( "\", \"count\":" )
           .append( count.getValue( ) ).append( "}" );
        if (strIt.hasNext( ) )
        {
          sbr.append( "," );
        }
      }
      sbr.append( "]");
      return sbr.toString( );
    }
    else if (format.equals(IProperty.XML_FORMAT) || format.equals( IProperty.XML_FORMAT_CDATA))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append("<Property type=\"com.modinfodesigns.property.string.StringCountProperty\"><Name>" );
      if (format.equals( IProperty.XML_FORMAT))
      {
        sbr.append( StringTransform.escapeXML( getName( ) )  );
      }
      else
      {
        sbr.append( "<![CDATA[" ).append( getName( ) ).append( "]]>" );
      }
      sbr.append( "</Name><Values>");
      for (Iterator<String> strIt = stringCounts.keySet().iterator( ); strIt.hasNext( ); )
      {
        String val = strIt.next( );
        IntegerProperty count = stringCounts.get( val );
          
        sbr.append( "<Value>" );
        if (format.equals( IProperty.XML_FORMAT))
        {
          sbr.append( StringTransform.escapeXML( val ) );
        }
        else
        {
          sbr.append( "<![CDATA[" ).append( val ).append( "]]>" );
        }
        sbr.append( "</Value><Count>" ).append( count.getValue( ) ).append( "</Count>" );
      }
			
      sbr.append( "</Values></Property>" );
      return sbr.toString( );
    }
    else if ( format.equals( IProperty.HTML_FORM_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "<select name=\"" ).append( getName( ) ).append( "\">" );
      for (Iterator<String> strIt = stringCounts.keySet().iterator( ); strIt.hasNext( ); )
      {
        String val = strIt.next( );
        IntegerProperty count = stringCounts.get( val );
        sbr.append( "<option value=\"" ).append( val ).append( "\">" )
           .append( val ).append( " (" ).append( count.getValue( ) ).append( ")</option>" );
      }
      sbr.append( "</select>" );
      return sbr.toString( );
    }
    else if ( format.equals( IProperty.HTML_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "<ul>" );
      for (Iterator<String> strIt = stringCounts.keySet().iterator( ); strIt.hasNext( ); )
      {
        String val = strIt.next( );
        IntegerProperty count = stringCounts.get( val );
        sbr.append( "<li>" ).append( val ).append( " (" ).append( count.getValue( ) ).append( ")" );
      }
      sbr.append( "</ul>" );
      return sbr.toString( );
    }
		
    return getValue( IProperty.DELIMITED_FORMAT );
  }
	
  @Override
  public void addString( String str )
  {
    IntegerProperty strCount = stringCounts.get( str );
    if (strCount != null)
    {
      strCount.increment( );
    }
    else
    {
      strCount = new IntegerProperty( str, "1" );
      stringCounts.put( str, strCount );
    }
    
    System.out.println( "addString: '" + str + "' ( " + strCount.getValue( ) + " )" );
  }

	
  public String[] getStringList( )
  {
    String[] stringArray = new String[ stringCounts.size( ) ];
    stringCounts.keySet().toArray(stringArray);
    return stringArray;
  }
	
  @Override
  protected void clearList( )
  {
    stringCounts.clear( );
  }

	
  public Iterator<String> iterator( )
  {
    return stringCounts.keySet().iterator( );
  }
    
  public int getCount( String string )
  {
    IntegerProperty count = stringCounts.get( string );
    return (count != null) ? count.getIntegerValue( ) : 0;
  }
    
  public int getMaximumCount(  )
  {
    int max = 0;
    for (IntegerProperty count : stringCounts.values( ) )
    {
      int countVal = count.getIntegerValue( );
      if (countVal > max )
      {
        max = countVal;
      }
    }

    return max;
  }
    
  public double getMeanCount( )
  {
    int sum = 0;
    int nStr = 0;
    for (IntegerProperty count : stringCounts.values( ) )
    {
      sum += count.getIntegerValue( );
      ++nStr;
    }
    return (double)sum / (double)nStr;
  }
    
  public double getStandardDeviation(  )
  {
    double mean = getMeanCount( );
    double sum_squares = 0.0;
    for (IntegerProperty count : stringCounts.values( ) )
    {
      double delta = (double)count.getIntegerValue( ) - mean;
      sum_squares += delta * delta;
    }

    return Math.sqrt( sum_squares );
  }
    
  public String[] getStrings( int minCount, int maxCount )
  {
    ArrayList<String> strings = new ArrayList<String>( );
    for (String key : stringCounts.keySet() )
    {
      IntegerProperty count = stringCounts.get( key );
      if (count.getIntegerValue( ) >= minCount && count.getIntegerValue( ) <= maxCount)
      {
        strings.add( key );
      }
    }
    
    String[] keys = new String[ strings.size() ];
    strings.toArray( keys );
    return keys;
  }
	
  @Override
  public IProperty copy( )
  {
    StringCountProperty copy = new StringCountProperty( getName( ) );
        
    Iterator<String> sit = iterator( );
    while (sit.hasNext())
    {
      copy.addString( new String( sit.next()));
    }
		
    return copy;
  }
    
  @Override
  public Object getValueObject()
  {
    return stringCounts;
  }
    
    
  /**
   * returns "Count", "AverageLength"
   */
  @Override
  public List<String> getIntrinsicProperties()
  {
    return null;
  }
    
  @Override
  public IProperty getIntrinsicProperty(String name)
  {
    return null;
  }
    
  @Override
  public Map<String, String> getComputableProperties()
  {
    return null;
  }
	
  @Override
  public IProperty getComputedProperty( String name, IProperty fromProp )
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
    return getStringList( );
  }
    
}

