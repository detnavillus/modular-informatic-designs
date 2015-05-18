package com.modinfodesigns.network.webservices;

import com.modinfodesigns.network.http.HttpClientWrapper;

import junit.framework.TestCase;

public class TestWebServiceClassifier extends TestCase
{
  public void testWebServiceClassifier( )
  {
    /*
    String url = "http://localhost:8090/WebServiceClassifier/classify";
        
    String response = HttpClientWrapper.executePost( url, SOAPRequest );
    System.out.println( response );
     */
  }
	
  private static String SOAPRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                                    + "                  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >"
                                    + "<soapenv:Body>"
                                    + " <ProcessData><![CDATA[<Results></Results>]]></ProcessData>"
                                    + "</soapenv:Body></soapenv:Envelope>";

}
