package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IPropertyHolder;

public interface IPropertyHolderTransform extends IPropertyTransform
{
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException;
}
