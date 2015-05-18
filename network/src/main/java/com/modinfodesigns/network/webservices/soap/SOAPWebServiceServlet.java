package com.modinfodesigns.network.webservices.soap;

import com.modinfodesigns.app.ApplicationManager;

import com.modinfodesigns.app.IObjectFactory;
import com.modinfodesigns.utils.DOMMethods;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.modinfodesigns.utils.FileMethods;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles SOAP Web Service calls by dispatching to a WebServiceDispatcher
 * WebServiceDispatcher returns an XML String that is sent back as a SOAP Response.
 * 
 * @author Ted Sullivan
 */
public class SOAPWebServiceServlet extends HttpServlet
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SOAPWebServiceServlet.class );

  private static final long serialVersionUID = 1L;
    
  private static final boolean DEBUG_ALL = false;
	
  private String webServiceConfigurationFile;
  private String objectFactoryClass = "com.modinfodesigns.app.ModInfoObjectFactory";
  private String webServiceFactory = "SOAPWebServiceFactory";
    
  private SOAPWebServiceDispatcher webServiceDispatcher = new SOAPWebServiceDispatcher( );
    
  @Override
  public void init()
  {

    LOG.debug( "WebServiceServlet.init..." );
        
    // ==============================================================================
    // get the web service configuration
    // using the ObjectFactory get the IWebServiceHandlers
    // ==============================================================================
    String tmp = getInitParameter( "WebServiceConfigurationFile" );
    if (tmp != null && tmp.trim().length() > 0)
    {
      this.webServiceConfigurationFile = tmp;
    }
        
    String config = FileMethods.readFile( webServiceConfigurationFile );
    if (config != null)
    {
      ApplicationManager appManager = ApplicationManager.getInstance( );
      IObjectFactory objFactory = appManager.createObjectFactory( webServiceFactory, objectFactoryClass, config );
            
      List<Object> webServiceHandlers = objFactory.getApplicationObjects( "SOAPWebServiceHandler" );
      if (webServiceHandlers != null)
      {
        for (int i = 0; i < webServiceHandlers.size(); i++)
        {
          Object obj = webServiceHandlers.get( i );
          if (obj instanceof ISOAPWebServiceHandler)
          {
            ISOAPWebServiceHandler webServiceHandler = (ISOAPWebServiceHandler)obj;
            webServiceDispatcher.addWebServiceHandler( webServiceHandler );
          }
          else
          {
            LOG.error( "Object " + obj + " is NOT an instance of ISOAPWebServiceHandler!" );
          }
        }
      }
      else
      {
        LOG.debug( "NO WebServiceHandlers found!" );
      }
    }
  }
    

  @Override
  public void service ( HttpServletRequest pReq, HttpServletResponse pRes)
              throws ServletException, IOException
  {
    LOG.debug( "service() " );
    	
    try
    {
      ServletInputStream servletInput = pReq.getInputStream( );

      StringBuffer strbuf = new StringBuffer( );
      BufferedReader bufRead = new BufferedReader( new InputStreamReader( servletInput ) );
      String line = null;
      while ((line = bufRead.readLine()) != null) strbuf.append( line );
      bufRead.close( );
            
      LOG.debug( "Got Input: " + strbuf.toString() );
            
      // -------------------------------------------------------------
      // Call createDocument with useNamespace flag set to true
      // -------------------------------------------------------------
      Document doc = DOMMethods.getDocument( new java.io.StringReader( strbuf.toString( ) ), "UTF-8", true );
      Element docElem = doc.getDocumentElement( );
            
      String response = webServiceDispatcher.handleWebServiceCall( docElem );
      LOG.debug( "Got response: " + response );
            
      pRes.setContentType( "text/xml" );
      ServletOutputStream out = pRes.getOutputStream( );
      BufferedOutputStream bos = new BufferedOutputStream( out );
        
      byte[] bytes = response.getBytes();
      bos.write( bytes, 0, bytes.length );
      bos.flush();
      bos.close();
    }
    catch ( Exception e )
    {
      LOG.debug( "Got Exception: " + e );
      LOG.error( "Reading ServletInputStream threw an Exception " + e );
      throw new ServletException( e.getMessage() );
    }
  }
}
