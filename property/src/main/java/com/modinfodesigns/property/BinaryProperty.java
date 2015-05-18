package com.modinfodesigns.property;

import com.modinfodesigns.property.schema.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

/** 
 * Represents a dual choice such as ON|OFF, LEFT|RIGHT, MALE|FEMALE, etc.
 * 
 * @author Ted Sullivan
 */

public class BinaryProperty implements IProperty, IExposeInternalProperties
{
  private String name;
    
  private String choice_1;
  private String choice_2;
    
  private String selected;  // will be one of choice_0 or choice_1
    
  public BinaryProperty( ) {  }
    
  public BinaryProperty( String firstChoice, String secondChoice )
  {
    this.choice_1 = firstChoice;
    this.choice_2 = secondChoice;
  }
    
  public void setFirstChoice( String firstChoice )
  {
    this.choice_1 = firstChoice;
  }
    
  public String getFirstChoice(  )
  {
    return this.choice_1;
  }
    
  public void setSecondChoice( String secondChoice )
  {
    this.choice_2 = secondChoice;
  }
    
  public String getSecondChoice( )
  {
    return this.choice_2;
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
    return getClass().getCanonicalName( );
  }

  @Override
  public String getValue( )
  {
    return selected;
  }

  @Override
  public String getValue(String format)
  {
    return selected;
  }

 @Override
  public void setValue(String value, String format)
              throws PropertyValidationException
  {
    if (value == null || value.trim().length() == 0) return;

    if (value.equals( choice_1 ))
    {
      selected = choice_1;
    }
    else if (value.equals( choice_2 ))
    {
      selected = choice_2;
    }
    else
    {
      throw new PropertyValidationException( "Value must be one of " + choice_1 + " OR " + choice_2 );
    }
  }

  @Override
  public String getDefaultFormat()
  {
    return choice_1;
  }

  @Override
  public IProperty copy()
  {
    BinaryProperty bp = new BinaryProperty( );
    bp.name = this.name;
    bp.choice_1 = this.choice_1;
    bp.choice_2 = this.choice_2;
    
    return bp;
  }

  @Override
  public Object getValueObject()
  {
    return selected;
  }

  @Override
  public boolean isMultiValue()
  {
    return false;
  }

  @Override
  public String[] getValues(String format)
  {
    return null;
  }
	
  @Override
  public List<PropertyDescriptor> getInternalProperties( )
  {
    ArrayList<PropertyDescriptor> internalProps = new ArrayList<PropertyDescriptor>( );
		
    PropertyDescriptor firstChoiceProp = new PropertyDescriptor( );
    firstChoiceProp.setName( "FirstChoice" );
    firstChoiceProp.setDisplayName( "First Choice" );
    firstChoiceProp.setPropertyType( "String" );
    internalProps.add( firstChoiceProp );
		
    PropertyDescriptor secondChoiceProp = new PropertyDescriptor( );
    secondChoiceProp.setName( "SecondChoice" );
    secondChoiceProp.setDisplayName( "Second Choice" );
    secondChoiceProp.setPropertyType( "String" );
    internalProps.add( secondChoiceProp );
    
    return internalProps;
  }

}
