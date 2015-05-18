package com.modinfodesigns.classify;

import com.modinfodesigns.property.transform.PropertyTransformException;

public class IndexMatcherException extends PropertyTransformException
{
  public static final long serialVersionUID = 1L;
	
  public IndexMatcherException( String reason )
  {
    super( reason );
  }
}
