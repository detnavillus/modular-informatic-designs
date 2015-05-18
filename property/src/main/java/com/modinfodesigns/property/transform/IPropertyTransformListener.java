package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;

public interface IPropertyTransformListener
{
  public void transformComplete( IProperty processedData, String status );
}
