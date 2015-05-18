package com.modinfodesigns.property.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertySet;
import com.modinfodesigns.property.PropertyTypeException;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.transform.string.StringTransform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Implements a String List property. Maintains an array of Strings.
 */

public class StringListProperty implements IProperty, IPropertySet, IComputableProperties
{
  private String name;
    
  private String delimiter = ";";
    
    
  private ArrayList<String> strings = new ArrayList<String>( );
    
  public StringListProperty( ) {  }

  public StringListProperty( String name )
  {
    this.name = name;
  }
    
  public StringListProperty( String name, Iterator<String> stringIt )
  {
    this.name = name;
    while (stringIt.hasNext() )
    {
      this.strings.add( stringIt.next() );
    }
  }
    
  public StringListProperty( String name, List<String> strings )
  {
    this.name = name;
    this.strings.addAll( strings );
  }
    
  public StringListProperty( String name, String[] stringLst )
  {
    this.name = name;
    if (stringLst != null)
    {
      for (int i = 0; i < stringLst.length; i++)
      {
        this.strings.add( stringLst[i] );
      }
    }
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
	
  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
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
    if (format == null || format.equals( IProperty.DELIMITED_FORMAT))
    {
      return StringTransform.getDelimitedString( strings, delimiter );
    }
    else if (format.startsWith( "delimiter=" ) || format.startsWith( "DELIMITER=" ))
    {
      String theDelimiter = new String( format.substring( "DELIMITER=".length() ));
      return StringTransform.getDelimitedString( strings, theDelimiter );
    }
    else if (format.equals( IProperty.JSON_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append("\"" ).append( this.name ).append( "\":" );
      sbr.append( getValue( IProperty.JSON_VALUE ));
      return sbr.toString();
    }
    else if (format.equals( IProperty.JSON_VALUE ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "[" );
      for (int i = 0; i < strings.size(); i++)
      {
        sbr.append( "\"" ).append( strings.get( i )).append( "\"" );
        if (i < strings.size( ) - 1) sbr.append( "," );
      }
      sbr.append( "]");
      return sbr.toString( );
    }
    else if (format.equals(IProperty.XML_FORMAT) || format.equals( IProperty.XML_FORMAT_CDATA))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append("<Property type=\"com.modinfodesigns.property.string.StringListProperty\"><Name>" );
      if (format.equals( IProperty.XML_FORMAT))
      {
        sbr.append( StringTransform.escapeXML(this.name)  );
      }
      else
      {
        sbr.append( "<![CDATA[" ).append( this.name ).append( "]]>" );
      }
      sbr.append( "</Name><Values>");
      for (int i = 0; i < strings.size(); i++ )
      {
        sbr.append( "<Value>" );
        if (format.equals( IProperty.XML_FORMAT))
        {
          sbr.append( StringTransform.escapeXML( strings.get( i ) ) );
        }
        else
        {
          sbr.append( "<![CDATA[" ).append( strings.get( i ) ).append( "]]>" );
        }
        sbr.append( "</Value>" );
      }
			
      sbr.append( "</Values></Property>" );
      return sbr.toString( );
    }
    else if ( format.equals( IProperty.HTML_FORM_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "<select name=\"" ).append( this.name ).append( "\">" );
      String[] values = getStringList( );
      for (int i = 0; i < values.length; i++)
      {
        sbr.append( "<option value=\"" ).append( values[i] ).append( "\">" )
           .append( values[i] ).append( "</option>" );
      }
      sbr.append( "</select>" );
      return sbr.toString( );
    }
    else if ( format.equals( IProperty.HTML_FORMAT ))
    {
      StringBuilder sbr = new StringBuilder( );
      sbr.append( "<ul>" );
      String[] values = getStringList( );
      for (int i = 0; i < values.length; i++)
      {
        sbr.append( "<li>" ).append( values[i] );
      }
      sbr.append( "</ul>" );
      return sbr.toString( );
    }
		
    return getValue( IProperty.DELIMITED_FORMAT );
  }
	
  public void addString( String str )
  {
    strings.add( str );
  }
	
  public void addStrings( Iterator<String> strings )
  {
    if (strings == null) return;
		
    while (strings.hasNext() )
    {
      addString( strings.next() );
    }
  }
	
  public String[] getStringList( )
  {
    String[] stringArray = new String[ strings.size( ) ];
    strings.toArray(stringArray);
    return stringArray;
  }
	
  public void setStringList( String[] stringList )
  {
    strings.clear( );
    for (int i = 0; i < stringList.length; i++)
    {
      strings.add( stringList[i] );
    }
  }
	
  public Iterator<String> iterator( )
  {
    return strings.iterator( );
  }
	
  public boolean containsString( String str )
  {
    if (strings == null) return false;
    for (Iterator<String> strIt = iterator(); strIt.hasNext(); )
    {
      String myStr = strIt.next( );
      if (myStr.equals( str )) return true;
    }
		
    return false;
  }
	
  @Override
  public String getDefaultFormat( )
  {
    return "delimiter=;";
  }
	
  @Override
  public void setValue( String value, String format )
  {
    if (format == null || format.equals( IProperty.DELIMITED_FORMAT))
    {
      doSetValue( value, delimiter );
    }
    else if (format.startsWith( "delimiter=" ) || format.startsWith( "DELIMITER=" ))
    {
      String theDelimiter = new String( format.substring( "DELIMITER=".length()));
      doSetValue( value, theDelimiter );
    }
    else if (format != null)
    {
      // Assume that the format IS the delimiter wanted
      doSetValue( value, format );
    }
  }
	
  private void doSetValue( String value, String useDelimiter )
  {
    strings.clear( );
    String[] stringArray = StringTransform.getStringArray( value, useDelimiter );
    for (int i = 0; i < stringArray.length; i++)
    {
      strings.add( stringArray[i] );
    }
  }
	
  @Override
  public IProperty copy( )
  {
    StringListProperty copy = new StringListProperty( this.name );
    
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
    return strings;
  }

  @Override
  public IPropertySet union( IPropertySet another )
                            throws PropertyTypeException
  {
    if (!(another instanceof StringListProperty) || another == null)
    {
      throw new PropertyTypeException( "Not a StringListProperty!" );
    }
		
    StringListProperty union = (StringListProperty)copy( );
    StringListProperty anotherLst = (StringListProperty)another;
    String[] otherStrings = anotherLst.getStringList( );
    for (int i = 0; i < otherStrings.length; i++)
    {
      if (!containsString( otherStrings[i] ))
      {
        union.addString( otherStrings[i] );
      }
    }
    
    return union;
  }

  @Override
  public IPropertySet intersection( IPropertySet another )
                                    throws PropertyTypeException
  {
    if (!(another instanceof StringListProperty) || another == null)
    {
      throw new PropertyTypeException( "Not a StringListProperty!" );
    }
		
    StringListProperty intersection = new StringListProperty( );
    intersection.setName( this.name );
    StringListProperty anotherLst = (StringListProperty)another;
    String[] otherStrings = anotherLst.getStringList( );
    for (int i = 0; i < otherStrings.length; i++)
    {
      if (containsString( otherStrings[i] ))
      {
        intersection.addString( otherStrings[i] );
      }
    }
		
    return intersection;
  }

  @Override
  public boolean contains( IPropertySet another )
                            throws PropertyTypeException
  {
    if (!(another instanceof StringListProperty) || another == null)
    {
      throw new PropertyTypeException( "Not a StringListProperty!" );
    }
		
    StringListProperty anotherLst = (StringListProperty)another;
    String[] otherStrings = anotherLst.getStringList( );
    for (int i = 0; i < otherStrings.length; i++)
    {
      if (!containsString( otherStrings[i] ))
      {
        return false;
      }
    }
		
    return true;
  }

  @Override
  public boolean contains( IProperty another )
                            throws PropertyTypeException
  {
    if ( (!(another instanceof StringListProperty) && !(another instanceof StringProperty )) ||
            another == null)
    {
      throw new PropertyTypeException( "Not a StringProperty or StringListProperty" );
    }
		
    if (another instanceof StringProperty)
    {
      return containsString( ((StringProperty)another).getValue( ));
    }
		
    return contains( (IPropertySet)another );
  }

  @Override
  public boolean intersects( IPropertySet another )
                            throws PropertyTypeException
  {
    if (!(another instanceof StringListProperty) || another == null)
    {
      throw new PropertyTypeException( "Not a StringListProperty!" );
    }
		
    StringListProperty anotherLst = (StringListProperty)another;
    String[] otherStrings = anotherLst.getStringList( );
    for (int i = 0; i < otherStrings.length; i++)
    {
      if (containsString( otherStrings[i] ))
      {
        return true;
      }
    }
		
    return false;
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
    return getStringList( );
  }

}
