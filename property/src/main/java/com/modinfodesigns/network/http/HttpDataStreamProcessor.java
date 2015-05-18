package com.modinfodesigns.network.http;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.transform.IDataStreamRenderer;

import java.io.InputStream;
import java.io.BufferedOutputStream;

public class HttpDataStreamProcessor implements IHttpResponseProcessor
{
  private static final int BUFSIZE = 8192;
	
  private IDataStreamRenderer dataStreamRenderer;
    
  public void setDataStreamRenderer( IDataStreamRenderer dataStreamRenderer )
  {
    this.dataStreamRenderer = dataStreamRenderer;
  }
    
  @Override
  public void processResponse( DataObject responseData, HttpServletResponse httpResp )
  {
    if (dataStreamRenderer == null)
    {
      return;
    }
        
    httpResp.setContentType( dataStreamRenderer.getContentType() );
        
    try
    {
      InputStream is = dataStreamRenderer.renderData( responseData );

      ServletOutputStream out = httpResp.getOutputStream( );
      BufferedOutputStream bos = new BufferedOutputStream( out );

      byte[] b = new byte[ BUFSIZE ];
      int bytesread;

      while(( bytesread = is.read( b, 0, BUFSIZE )) > 0)
      {
        bos.write(b,0,bytesread);
      }

      bos.flush( );
      bos.close( );
      is.close( );
    }
    catch( Exception e )
    {

    }
  }
}
