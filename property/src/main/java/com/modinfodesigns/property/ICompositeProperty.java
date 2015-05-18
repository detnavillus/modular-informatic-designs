package com.modinfodesigns.property;

import java.util.List;

/**
 * Interface for IProperty classes that are composed of or can be transformed into sub-components (for
 * example, DateProperty).  These are often Formatted Properties that contain a series of precisely formatted
 * sub-strings but they can also include heterogeneous sub-properties.
 * 
 * @author Ted Sullivan
 */

public interface ICompositeProperty extends IComputableProperties
{
  public List<IProperty> getComponentProperties( );
    
  public List<String> getDelimiters( );
}
