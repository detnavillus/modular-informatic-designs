package com.modinfodesigns.network.http;

import com.modinfodesigns.app.INamedObject;

import java.io.IOException;

/**
 * Handles HTTP requests by creating a java.io.InputStream from which the source content can
 * be obtained.
 * 
 * @author Ted Sullivan
 */

public interface IHttpRequestHandler extends INamedObject
{	
  public HandlerResponse handleRequest( HttpRequestData httpRequest ) throws IOException;
}
