package com.modinfodesigns.property;

import java.util.List;
import java.util.Map;

/**
 * Interface for IProperty objects that have intrinsic or properties that can be computed relative
 * to other properties. For example, a List Property (such as PropertyList or StringListProperty or IntegerList)
 * can compute its count.  A list of IQuantity objects can compute an average and so on.
 * 
 * Computable properties work like IPropertyHolder sub properties in that they can be
 * retrieved via an XPath expression. For example, an GeoLocation property can compute a Distance
 * relative to another GeoLocation property.
 * 
 * @author Ted Sullivan
 */

public interface IComputableProperties
{
  /**
   * Get list of intrinsic properties that can be computed from an IProperty's native structure.
   * For example List properties can return their Count etc. a DateProperty can return an Age (Duration)
   *
   * @return
   */
  public List<String> getIntrinsicProperties( );
    
  /**
   * Returns an intrinsic property that is computable from the
   * property's internal data structure.
   *
   * @param name
   * @return
   */
  public IProperty getIntrinsicProperty( String name );
    
    
  /**
   * Returns a Map of property name, fromProperty type
   * e.g. "Distance", "GeoLocation"
   * @return
   */
  public Map<String,String> getComputableProperties( );
    
  /**
   * Returns a property that can be computed relative to another property.
   *
   * @param name
   * @param fromProp
   * @return
   */
  public IProperty getComputedProperty( String name, IProperty fromProp );
    
}
