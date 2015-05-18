package com.modinfodesigns.property.geolocation;

import com.modinfodesigns.property.ICompositeProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyValidationException;

import java.util.List;
import java.util.Map;

/**
 * Represents a geopolitical address -
 * 
 * Components include street address, city, state, zip code which are themselves Property objects
 * (State is an EnumeratedProperty), etc.
 * 
 * Has intrinsic properties such as GeographicLocation.
 * 
 * @author Ted Sullivan
 *
 */

public class Address implements IProperty, ICompositeProperty
{

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
  public List<IProperty> getComponentProperties()
  {
    return null;
  }


  @Override
  public String getName()
  {
    return null;
  }

  @Override
  public void setName(String name)
  {
		
  }

  @Override
  public String getType()
  {
    return null;
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
  public void setValue(String value, String format)
                       throws PropertyValidationException
  {
		
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    return null;
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

  @Override
  public List<String> getDelimiters()
  {
    return null;
  }

}
