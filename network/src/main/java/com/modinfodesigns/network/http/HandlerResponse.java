package com.modinfodesigns.network.http;

import java.io.InputStream;

public class HandlerResponse
{
  private InputStream contentStream;
    
  private String contentType;
    
  public void setContentStream( InputStream contentStream )
  {
    this.contentStream = contentStream;
  }
    
  public InputStream getContentStream( )
  {
    return this.contentStream;
  }
    
  public void setContentType( String contentType )
  {
    this.contentType = contentType;
  }
    
  public String getContentType( )
  {
    return this.contentType;
  }
}
