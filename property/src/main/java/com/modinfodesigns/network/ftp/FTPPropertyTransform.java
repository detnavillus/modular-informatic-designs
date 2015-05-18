package com.modinfodesigns.network.ftp;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.IPropertyTransformListener;
import com.modinfodesigns.property.transform.PropertyTransformException;

public class FTPPropertyTransform implements IPropertyHolderTransform
{

  @Override
  public IProperty transform( IProperty input )
                              throws PropertyTransformException
  {
    return null;
  }

  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }

  @Override
  public IPropertyHolder transformPropertyHolder(IPropertyHolder input)
          throws PropertyTransformException
  {
    return null;
  }
}
