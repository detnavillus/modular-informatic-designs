package com.modinfodesigns.search;

import com.modinfodesigns.property.IPropertyHolder;

import java.util.List;

/**
 * Base Interface for Search Navigators or Facets. Each Navigator can have one or more
 * Navigator Fields which in turn contain Facet values. 
 * 
 * @author Ted Sullivan
 */

public interface INavigator extends IPropertyHolder
{
  /**
   * Adds a Navigator Field to the Navigator.
   *
   * @param navField		The INavigatorField implementation containing the navigator
   * 						data.
  */
  public void addNavigatorField( INavigatorField navField );
	
  public INavigatorField getNavigatorField( String navFieldName );
    
  public List<String> getNavigatorFields( );
}
