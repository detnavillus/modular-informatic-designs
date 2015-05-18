package com.modinfodesigns.property.transform.string;

public interface IStringTransform
{
  public String transformString( String inputString ) throws StringTransformException;

  public String transformString( String sessionID, String inputString ) throws StringTransformException;
}
