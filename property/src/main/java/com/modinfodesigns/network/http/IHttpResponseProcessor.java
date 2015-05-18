package com.modinfodesigns.network.http;

import javax.servlet.http.HttpServletResponse;

import com.modinfodesigns.property.DataObject;

public interface IHttpResponseProcessor
{
  public void processResponse( DataObject responseData, HttpServletResponse httpResp );
}
