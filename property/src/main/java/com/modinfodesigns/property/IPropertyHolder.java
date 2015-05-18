package com.modinfodesigns.property;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Interface for all objects that contain properties - also known as "Data Objects". IPropertyHolder objects can be 
 * associated or linked with other IPropertyHolder object via a named IAssociation link. This provides the basis for
 * creation of Ontologies.
 * 
 * Note: on getValue( IProperty.JSON_FORMAT ) - property holder objects must wrap the JSON string with "{  }" to indicate that
 * a Property Holder is a JSON Object. IProperty objects are property values, not data objects.
 * 
 * @author Ted Sullivan
 */

@SuppressWarnings("rawtypes")
public interface IPropertyHolder extends IProperty, Map
{
  /**
   * Returns the globally unique ID for this property
   */
  public String getID( );
    
  public void setID( String ID );
    
  public void setIsRootObject( boolean isRoot );
  public boolean isRootObject( );
    
  /**  
   * Returns the Property of a given name.  If the name is '/' delimited, gets a nested property
   */
  public IProperty getProperty( String name );


  /**
   * return list of Properties contained in the Property Holder.
   */
  public Iterator<IProperty> getProperties( );
    
    
  /**
   * @return iterator on all IProperty names.
   */
  public Iterator<String>getPropertyNames( );

  /**
   * Adds a property to the property holder - creates, appends multiple value properties
   */
  public void addProperty( IProperty property );


  /**
   * Sets a property to a singular value
   */
  public void setProperty( IProperty property );


  public void removeProperty( String propName );
    
  public void setProperties( Iterator<IProperty> properties );
    
  /**
   * Returns the data items in the list
   */
  public Map<String,IProperty> getPropertyMap( );
    
  /**
   * Adds an Association to another IPropertyHolder. The recipient of this call is the
   * Target IPropertyHolder. The caller inserts itself as the Source. The Target can then
   * make a reciprocal association by calling back on the source.
   */
  public void addAssociation( IAssociation association ) throws AssociationException;
    
  public void setAssociation( IAssociation association );
    
  public Iterator<String> getAssociationNames( );
    
  public List<IAssociation> getAssociations( String name );
    
  public IAssociation getAssociation( String name, String ID );
    
  public void removeAssociations( String name );
    
  public void removeAssociation( String name, String ID );
    
}
