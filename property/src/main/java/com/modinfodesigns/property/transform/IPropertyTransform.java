package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;

public interface IPropertyTransform
{
  /**
   * Synchronous Method
   */
  public IProperty transform( IProperty input ) throws PropertyTransformException;

  /**
   * Asynchronous Method
   */

  public void startTransform( IProperty input, IPropertyTransformListener transformListener ) throws PropertyTransformException;

}
