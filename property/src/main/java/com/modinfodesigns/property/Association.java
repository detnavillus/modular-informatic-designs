package com.modinfodesigns.property;

import java.util.Iterator;
import java.util.HashMap;

/**
 * Basic implementation of the IAssociation interface - describes an associative link between 
 * two Property Holder objects. The owner of the Association link is referred to as the association
 * 'Source'. The IPropertyHolder link contained by the Association is the association 'Target'.
 * 
 * The association can also contain one or more properties (IProperty) that describe the association.
 * 
 * @author Ted Sullivan
 */

public class Association implements IAssociation
{
  private String name;
  private String ID;
  private IPropertyHolder target;
    
  private boolean multiple = true;
    
  private HashMap<String,IProperty> properties;
    
  public Association( String name, String ID, IPropertyHolder target, boolean multiple )
  {
    this.name = name;
    this.ID = ID;
    this.target = target;
    this.multiple = multiple;
  }
    
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public String getID()
  {
    return this.ID;
  }

  @Override
  public IPropertyHolder getAssociationTarget()
  {
    return this.target;
  }
	
  @Override
  public boolean isMultiple( )
  {
    return this.multiple;
  }

  @Override
  public IAssociation copy()
  {
    Association copy = new Association( this.name, this.ID, this.target, this.multiple );
    if (properties != null)
    {
      for (Iterator<String> myProps = getPropertyNames( ); myProps.hasNext(); )
      {
        String propName = myProps.next();
        IProperty myProp = properties.get( propName );
        copy.setProperty( myProp.copy( ) );
      }
    }
		
    return copy;
  }


  @Override
  public IProperty getProperty(String name)
  {
    if (name == null) return null;
		
    return (properties != null) ? properties.get( name ) : null;
  }


  @Override
  public Iterator<IProperty> getProperties()
  {
    return (properties != null) ? properties.values().iterator() : null;
  }


  @Override
  public Iterator<String> getPropertyNames()
  {
    return (properties != null) ? properties.keySet().iterator() : null;
  }


  @Override
  public void addProperty(IProperty property)
  {
    if (property == null) return;
		
    String name = property.getName( );
    IProperty oldProp = properties.get( name );
    if (oldProp != null)
    {
      if (oldProp instanceof PropertyList)
      {
        PropertyList propList = (PropertyList)oldProp;
        propList.addProperty( property );
      }
      else
      {
        PropertyList propList = new PropertyList( );
        propList.setName( name );
				
        propList.addProperty( oldProp );
        propList.addProperty( property );
        properties.put( name, propList );
      }
    }
    else
    {
      properties.put( name, property );
    }
  }


  @Override
  public void setProperty(IProperty property)
  {
    if (property == null) return;
		
    if (properties == null) properties = new HashMap<String,IProperty>( );
    properties.put( property.getName(), property );
  }


  @Override
  public void removeProperty(String propName)
  {
    if (propName == null) return;
		
    if (properties != null)
    {
      properties.remove( propName );
    }
  }

}
