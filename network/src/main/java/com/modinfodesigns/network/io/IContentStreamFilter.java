package com.modinfodesigns.network.io;

import com.modinfodesigns.app.INamedObject;

import java.io.InputStream;

public interface IContentStreamFilter extends INamedObject
{
  public String getContentType( );
	
  public InputStream filterInputStream( InputStream source );
}
