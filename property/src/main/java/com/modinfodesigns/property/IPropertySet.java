package com.modinfodesigns.property;

/**
 * Interface for Property objects that contain sets of other Property objects. Defines set
 * operations that can be performed on property sets.
 * 
 * @author Ted Sullivan
 */

public interface IPropertySet
{
  public IPropertySet union( IPropertySet another ) throws PropertyTypeException;
	
  public IPropertySet intersection( IPropertySet another ) throws PropertyTypeException;
	
  public boolean contains( IPropertySet another ) throws PropertyTypeException;
	
  public boolean contains( IProperty another ) throws PropertyTypeException;
	
  public boolean intersects( IPropertySet another ) throws PropertyTypeException;

}
