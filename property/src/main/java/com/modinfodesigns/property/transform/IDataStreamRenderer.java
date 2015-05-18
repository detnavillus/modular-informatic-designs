package com.modinfodesigns.property.transform;

import java.io.InputStream;

import com.modinfodesigns.property.DataObject;

/**
 * Renders DataObjects to an InputStream
 * 
 * @author Ted Sullivan
 */
public interface IDataStreamRenderer
{
  public InputStream renderData( DataObject dataObject );

  public String getContentType( );
}
