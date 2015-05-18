package com.modinfodesigns.property;

import java.util.Iterator;

/**
 * Base Interface for associative links between Data Objects (IPropertyHolder). Represents a
 * one-way named associative link. Bi-directional links must be created explicitly. 
 * 
 * Cardinality can be set to singular or multiple. For multiple associations,
 * each IAssociation should be assigned a unique ID so that specific associations can be removed
 * (See DataObject).
 * 
 * Associations can have properties (Strength / Ranking, Date Range, etc.) but they do not 
 * have nested associations.
 * 
 * @author Ted Sullivan
 */

public interface IAssociation 
{
  /**
   * @return The name of the Association - labels the relationship
   *         type.
   */
  public String getName( );

  public String getID( );
    
  public boolean isMultiple( );

  /**
   * @return The object that is Target of the association.
   */
  public IPropertyHolder getAssociationTarget(  );
    
    
  /**
   * @return Copy of the Association Object
   */
  public IAssociation copy( );
    
  /**
   * Returns the Property of a given name.  If the name is '/' delimited, gets a nested property
   */
  public IProperty getProperty( String name );


  /**
   * return list of Properties contained in the Association .
   */
  public Iterator<IProperty> getProperties( );
    
    
  /**
   * @return iterator on all IProperty names.
   */
  public Iterator<String>getPropertyNames( );

  /**
   * Adds a property to the Association - creates, appends multiple value properties
   */
  public void addProperty( IProperty property );


  /**
   * Sets a property to a singular value
   */
  public void setProperty( IProperty property );


  public void removeProperty( String propName );
}
