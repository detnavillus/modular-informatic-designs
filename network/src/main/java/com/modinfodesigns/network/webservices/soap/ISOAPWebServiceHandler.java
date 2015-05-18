package com.modinfodesigns.network.webservices.soap;

import org.w3c.dom.Element;

import java.io.Writer;
import java.io.IOException;

public interface ISOAPWebServiceHandler
{
  /**
   * Respond to a WebService call.
   * @param webServiceMessage - the child Element of the SOAPBody element of the WebService call
   * @param response 		    - StringBuilder in which the inner WebService response
   *                            message is to be written.
   *
   * @return                    must return true if this handler handles this WebService call, false otherwise
   * @throws WebServiceHandlerException
   */
  public boolean handleWebServiceCall( Element webServiceMessage, Writer response ) throws WebServiceHandlerException,IOException;

}
