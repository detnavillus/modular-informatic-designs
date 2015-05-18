package com.modinfodesigns.property;

import com.modinfodesigns.property.string.RegularExpressionProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Property class used to create String Properties that have a specific internal structure
 * such as Social Security Numbers, Bank routing numbers, phone numbers and so on.
 * 
 * Internal components should be FormattedProperty or EnumerationProperty
 * 
 * @author Ted Sullivan
 *
 */
public class CompositeProperty implements IProperty, ICompositeProperty
{
  private String name;
    
  private List<IProperty> components;
  private List<String> delimiters;     // length should be components.size( ) - 1
    
  private String delimiter = " ";
    
    
  public CompositeProperty(  ) {  }
    
  public CompositeProperty( List<IProperty> components, List<String> delimiters )
  {
    this.components = components;
    this.delimiters = delimiters;
  }
    
  public CompositeProperty( List<IProperty> components, String delimiter )
  {
    this.components = components;
    this.delimiter = delimiter;
  }
    
  public void addComponent( IProperty component )
  {
    addComponent( component );
  }
    
  public void addComponent( IProperty component, String delimiter )
  {
    if (components == null) components = new ArrayList<IProperty>( );
    components.add( component );
		
    if (delimiter != null)
    {
      if (delimiters == null) delimiters = new ArrayList<String>( );
      delimiters.add( delimiter );
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
    return getClass().getCanonicalName( );
  }

  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }
	
  @Override
  public String getValue( )
  {
    StringBuilder sbr = new StringBuilder( );
    for (int i = 0, isz = components.size( ); i < isz; i++)
    {
      IProperty component = components.get( i );
      sbr.append( component.getValue( ) );
      if ( i < (isz-1))
      {
        sbr.append( (delimiters != null && delimiters.size( ) > i ) ? delimiters.get( i ) : this.delimiter );
      }
    }
		
    return sbr.toString( );
  }

  @Override
  public String getValue( String format )
  {
    // Ignores format - format is fixed.
    return getValue( );
  }

  @Override
  public void setValue( String value, String format )
                        throws PropertyValidationException
  {
    if (components == null) return;
        
    String propValue = value;
    for (int i = 0, isz = components.size( ); i < isz; i++)
    {
      IProperty component = components.get( i );
      String delimiter = getDelimiter( i );
      if (i < (isz-1) && propValue.indexOf( delimiter) >= 0 && delimiter.length( ) > 0)
      {
        String curVal = new String( propValue.substring( 0, propValue.indexOf( delimiter )));
        String rest = new String( propValue.substring( propValue.indexOf( delimiter ) + delimiter.length() ));
        component.setValue( curVal, format );
        propValue = rest;
      }
      else
      {
        component.setValue( propValue, format );
      }
    }
  }
	
  private String getDelimiter( int compIndex )
  {
    if (delimiters == null) return delimiter;
    return delimiters.get( compIndex );
  }
	

  @Override
  public String getDefaultFormat()
  {
    if (components == null) return null;
		
    // create a '|' delimited string
    StringBuilder sbr = new StringBuilder( );
    for (int i = 0, isz = components.size( ); i < isz; i++)
    {
      IProperty component = components.get( i );
      String itsDef = component.getDefaultFormat( );
      if (itsDef != null) sbr.append( itsDef );
      if (i < (isz-1)) sbr.append( "|" );
    }
		
    return sbr.toString( );
  }
	

  @Override
  public IProperty copy()
  {
    CompositeProperty copy = new CompositeProperty( this.components, this.delimiters );
    copy.delimiter = this.delimiter;
    copy.name = this.name;
    
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    if (components == null) return null;
    
    Object[] values = new Object[ components.size( ) ];
    for (int i = 0, isz = components.size( ); i < isz; i++)
    {
      IProperty component = components.get( i );
      values[i] = component.getValueObject( );
    }
    return values;
  }

  @Override
  public List<String> getIntrinsicProperties()
  {
    if (this.components == null) return null;
		
    ArrayList<String> componentNames = new ArrayList<String>( );
    for (int i = 0; i < components.size( ); i++)
    {
      IProperty componentProp = components.get( i );
      if (componentProp instanceof RegularExpressionProperty)
      {
        componentNames.add( componentProp.getName( ) );
      }
    }
		
    return componentNames;
  }

  @Override
  public IProperty getIntrinsicProperty( String name )
  {
    if (this.components == null) return null;
		
    for (int i = 0; i < components.size( ); i++)
    {
      IProperty componentProp = components.get( i );
      if (componentProp.getName( ).equals( name ))
      {
        return componentProp;
      }
    }
		
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
  public List<IProperty> getComponentProperties(  )
  {
    return this.components;
  }
    
  @Override
  public List<String> getDelimiters( )
  {
    if (components != null && delimiters == null && this.delimiter != null)
    {
      ArrayList<String> delimList = new ArrayList<String>( );
      for (int i = 0; i < components.size( ) - 1; i++)
      {
        delimList.add( this.delimiter );
      }
    		
      return delimList;
    }
    
    return this.delimiters;
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
