package com.modinfodesigns.network.webservices.soap;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a SOAP Web Services call. Delegates to an ISOAPWebServiceHandler
 * 
 * @author Ted Sullivan
 */

public class SOAPWebServiceDispatcher
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SOAPWebServiceDispatcher.class );

  private ArrayList<ISOAPWebServiceHandler> webServiceHandlers = new ArrayList<ISOAPWebServiceHandler>( );
    
  public void addWebServiceHandler( ISOAPWebServiceHandler webServiceHandler )
  {
    LOG.debug( "addWebServiceHandler: " + webServiceHandler );
    webServiceHandlers.add( webServiceHandler );
  }
    
  // gets the WebService Call element - returns an XML response ...
  public String handleWebServiceCall( Element webServiceEl )
  {
    LOG.debug( "handleWebServiceCall" );
    	
    NodeList soapBodyLst = webServiceEl.getElementsByTagNameNS( "*", "Body");
    if (soapBodyLst != null && soapBodyLst.getLength() > 0)
    {
      Element soapBodyEl = (Element)soapBodyLst.item( 0 );
      NodeList childNodes = soapBodyEl.getChildNodes( );
      if (childNodes != null && childNodes.getLength() > 0)
      {
        for ( int c = 0; c < childNodes.getLength(); c++)
        {
          Node n = childNodes.item( c );
          if (n instanceof Element)
          {
            return doHandleWebCall( (Element)n );
          }
        }
      }
    }
    	
    LOG.debug( "No Body Elements! returning No Body Elements" );
    return getFaultString( "No Body Elements" );
  }
    
  private String doHandleWebCall( Element webMessageEl )
  {
    LOG.debug( "doHandleWebCall ");
    	
    StringWriter strwriter = new StringWriter( );
    	
    for (int i = 0; i < webServiceHandlers.size(); i++)
    {
      ISOAPWebServiceHandler webServiceHandler = webServiceHandlers.get( i );
    			
      try
      {
        if ( webServiceHandler.handleWebServiceCall( webMessageEl, strwriter ))
        {
          String webResponse  = strwriter.toString();
          LOG.debug( "got web service response: " + webResponse );
    			   	
          // Add SOAPHeader and SOAPTrailer
          // cook up a DOM Document, return it.
          StringBuilder soapResponse = new StringBuilder( );
          soapResponse.append( SOAP_HEADER )
                      .append( webResponse )
                      .append( SOAP_TRAILER );
    			   	
          return soapResponse.toString();
        }
        else
        {
          LOG.debug( "WebServiceHandler returned false error: " + strwriter.toString() );
          return getFaultString( strwriter.toString() );
        }
      }
      catch (WebServiceHandlerException wshe )
      {
        LOG.debug( "doHandleWebCall got Exception: " + wshe );
        LOG.error( "doHandleWebCall got Exception: " + wshe );
        return getFaultString( wshe.getMessage( ) );
      }
      catch (IOException ioe )
      {
        return getFaultString( ioe.getMessage( ) );
      }
    }
    	
    LOG.debug( "Did not handle Response: returning HandleWebCall Error " );
    	
    return getFaultString( "HandleWebCall Error" );
  }
    
  private String getFaultString( String faultMessage )
  {
    return FAULT_HEADER + faultMessage + FAULT_TRAILER;
  }
    
  private static final String SOAP_HEADER = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\""
                                          + "               xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >"
                                          + "<soap:Header/><soapenv:Body>";
    
  private static final String SOAP_TRAILER = "</soap:Body></soap:Envelope>";
    
  // Need to get real Fault
  private static final String FAULT_HEADER = "<FAULT>";
    
  private static final String FAULT_TRAILER = "</FAULT>";
}
