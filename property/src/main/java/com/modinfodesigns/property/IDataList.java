package com.modinfodesigns.property;

import java.util.Iterator;

/**
 * Basic interface for Data List objects. IDataList extends IPropertyHolder so that
 * Data Lists themselves can be treated as Data Objects (i.e. can have
 * properties assigned to them).
 * 
 * @author Ted Sullivan
 */

public interface IDataList extends IPropertyHolder
{
  /**
   * Return the size of the data list
   */
  public int size( );

  /**
   * Returns the data items in the list
   */
  public Iterator<DataObject> getData( );
    
    
  /**
   * Returns an Item at the specified index.
   */
  public DataObject item( int index );
    
  public void addDataObject( DataObject dObj );
    
  public void removeDataObject( int index );
    
  public void clearDataList( );
    
  public void setChildProperty( IProperty childProperty );
    
}
