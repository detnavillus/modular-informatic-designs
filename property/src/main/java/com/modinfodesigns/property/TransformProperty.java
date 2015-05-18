package com.modinfodesigns.property;

import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Uses an IPropertyHolderTransform to generate a dynamic property.
 * 
 * On getValue( ) calls propertyTransform.transformPropertyHolder( this ) then
 * uses the returned value as a proxy - 
 * 
 * Own properties are used as input parameters to PropertyHolderTransform.
 * 
 * @author Ted Sullivan
 */

public class TransformProperty implements IPropertyHolder, IFunctionProperty
{
  private IPropertyHolder proxyPropertyHolder;
  private IPropertyHolder inputPropHolder;
	
  private IPropertyHolderTransform propertyTransform;
	
  private String name;
	

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
    return null;
  }

  @Override
  public String getValue()
  {
    doTransform( );
    return (proxyPropertyHolder != null) ? proxyPropertyHolder.getValue( ) : null;
  }

  @Override
  public String getValue(String format)
  {
    doTransform( );
    return (proxyPropertyHolder != null) ? proxyPropertyHolder.getValue( format ) : null;
  }
	
  private void doTransform( )
  {
    try
    {
      proxyPropertyHolder = propertyTransform.transformPropertyHolder( (inputPropHolder != null) ? inputPropHolder : this );
    }
    catch (PropertyTransformException pte )
    {
			
    }
  }
	
  /**
   * Set Value must be able to set the transform from JSON or XML ...
   *
   */

  @Override
  public void setValue( String value, String format ) throws PropertyValidationException
  {
    // Construct the IPropertyHolderTransform from value string ...
    // JSON - need class of the IPropertyHolderTransform as an attribute
    //
  }

  @Override
  public IProperty copy()
  {
    TransformProperty copy = new TransformProperty( );
    copy.setName( this.name );
      
    copy.proxyPropertyHolder = (proxyPropertyHolder != null) ? (IPropertyHolder)proxyPropertyHolder.copy( ) : null;
    return copy;
  }

	@Override
	public IProperty getProperty(String name)
	{
		doTransform( );
		return (proxyPropertyHolder != null) ? proxyPropertyHolder.getProperty( name ) : null;
	}

	@Override
	public Iterator<IProperty> getProperties() 
	{
		doTransform( );
		return (proxyPropertyHolder != null) ? proxyPropertyHolder.getProperties( ) : null;
	}

	@Override
	public Iterator<String> getPropertyNames()
	{
		doTransform( );
		return (proxyPropertyHolder != null) ? proxyPropertyHolder.getPropertyNames( ) : null;
	}

	@Override
	public void addProperty(IProperty property)
	{
		if (proxyPropertyHolder != null)
		{
			proxyPropertyHolder.addProperty( property );
		}
	}

	@Override
	public void setProperty(IProperty property)
	{
		if (proxyPropertyHolder != null)
		{
			proxyPropertyHolder.setProperty( property );
		}
	}

	@Override
	public void removeProperty( String propName )
	{
		if (proxyPropertyHolder != null)
		{
			proxyPropertyHolder.removeProperty( propName );
		}
	}

	@Override
	public void addAssociation( IAssociation association )
			throws AssociationException
	{

	}

	@Override
	public void setAssociation(IAssociation association)
	{

	}

	@Override
	public Iterator<String> getAssociationNames()
	{
		return null;
	}

	@Override
	public List<IAssociation> getAssociations(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAssociation getAssociation(String name, String ID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeAssociations(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAssociation(String name, String ID) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getValueObject()
	{

		return null;
	}

	@Override
	public String getDefaultFormat( )
	{
		// TO DO - get the proxy object's default format ...
		return null;
	}


	@Override
	public String getID()
	{
		return null;
	}

	@Override
	public void setID(String ID)
	{
		
	}

	@Override
	public void setProperties(Iterator<IProperty> properties)
	{
		
	}

	@Override
	public Map<String, IProperty> getPropertyMap() 
	{
		return null;
	}

	@Override
	public void setPropertyHolder( IPropertyHolder propHolder )
	{
		inputPropHolder = propHolder;
	}


	@Override
	public void setFunction(String function)
	{
		
	}

	@Override
	public String getFunction()
	{
		return null;
	}

	@Override
	public IProperty execute()
	{
		doTransform( );
		return proxyPropertyHolder;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set entrySet()
	{

		return null;
	}

	@Override
	public Object get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set keySet()
	{

		return null;
	}

	@Override
	public Object put(Object key, Object value)
	{
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void putAll(Map m)
	{
		
	}

	@Override
	public Object remove(Object key)
	{
		return null;
	}

	@Override
	public int size()
	{
		return 0;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMultiValue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getValues(String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIsRootObject(boolean isRoot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isRootObject() {
		// TODO Auto-generated method stub
		return false;
	}


}
