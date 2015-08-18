package com.modinfodesigns.property.time;

import com.modinfodesigns.property.IExposeInternalProperties;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.EnumerationProperty;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.quantity.IntegerRangeProperty;

import java.util.ArrayList;
import java.util.List;

// formats: 24 hour  AM/PM format

public class TimeOfDay implements IProperty, IExposeInternalProperties
{
  private String name;
	
  private IntegerRangeProperty hour;     // 0 - 23
  private IntegerRangeProperty minute;   // 0 - 60
  private IntegerRangeProperty second;   // 0 - 60
  private long milliseconds;
    
  private EnumerationProperty timeZone;
    
  @Override
  public List<PropertyDescriptor> getInternalProperties()
  {
    ArrayList<PropertyDescriptor> internalProps = new ArrayList<PropertyDescriptor>( );
		
    PropertyDescriptor timeZoneDesc = new PropertyDescriptor( );
    timeZoneDesc.setName( "TimeZone" );
    timeZoneDesc.setDisplayName( "Time Zone" );
    timeZoneDesc.setPropertyType( "EnumerationProperty" );
		
    // get time zones ...
    internalProps.add( timeZoneDesc );
    // EDT, CDT, MDT, PDF, EST, CST, MST, GMT
		
    return internalProps;
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
  public String getValue()
  {
    return getValue( "hh:mm:ss mmm AM/PM TZ" );
  }

  @Override
  public String getValue(String format)
  {
    return null;
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
      // set hour, minute, second, milliseconds
      
      

  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    TimeOfDay copy = new TimeOfDay( );
        
    return copy;
  }

  @Override
  public Object getValueObject()
  {
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
    return null;
  }
}
