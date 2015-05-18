package com.modinfodesigns.network.http;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.BooleanProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.string.StringListProperty;
import com.modinfodesigns.utils.StringMethods;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import java.io.BufferedReader;
import java.io.IOException;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestData extends DataObject
{
  private transient static final Logger LOG = LoggerFactory.getLogger( HttpRequestData.class );

  public static final String SESSION_ID_PROP  = "sessionID";
  public static final String REQUEST_URI_PROP = "requestURI";
  public static final String REQUEST_URL_PROP = "requestURL";
  public static final String PARAMETER_PROP   = "parameters";
  public static final String ATTRIBUTE_PROP   = "attributes";
  public static final String COOKIE_PROP      = "cookies";
  public static final String REQUEST_BODY     = "body";
    
  public static final String REQUEST_METHOD   = "requestMethod";
    
  public HttpRequestData( ) {  }

  public HttpRequestData( HttpServletRequest httpRequest )
  {
    LOG.debug( "HttpRequestData( ) " );
    	
    setProperty( new StringProperty( SESSION_ID_PROP, httpRequest.getSession().getId()));
    setProperty( new StringProperty( REQUEST_URI_PROP, httpRequest.getRequestURI( )));
    setProperty( new StringProperty( REQUEST_URL_PROP, httpRequest.getRequestURL( ).toString()));
        
    String body = getRequestBody( httpRequest );
    if (body != null && body.trim().length() > 0)
    {
      if (httpRequest.getMethod( ).equalsIgnoreCase( "POST" ))
      {
        setProperty( new StringProperty( REQUEST_METHOD, "POST" ));
        // if parameters are in request format: parse them and set request parameters
        // TO DO - body could be in JSON or XML format too ...
        // use httpRequest.getContentType( ) - if application/json or application/xml
        // use generic JSON,XML methods to construct a DataObject as the request params
          
        LOG.debug( "Body: " + body );
        String[] params = StringMethods.getStringArray( body, "&" );
        if (params != null)
        {
          DataObject requestParams = new DataObject( );
          requestParams.setName( PARAMETER_PROP );
          setProperty( requestParams );
            		
          for (int i = 0; i < params.length; i++)
          {
            LOG.debug( "param pair: " + params[i] );
            String param = new String( params[i].substring( 0, params[i].indexOf( "=" )));
            String value = new String( params[i].substring( params[i].indexOf( "=" ) + 1 ));
            if (value != null && value.trim().length() > 0)
            {
              try
              {
                value = java.net.URLDecoder.decode( value, "UTF-8" );
              }
              catch ( Exception e )
              {
                  
              }
            				
              LOG.debug( "Adding request param: " + param + " = " + value );
              requestParams.setProperty( new StringProperty( param, value ));
            }
          }
        }
      }
      else
      {
        setProperty( new StringProperty( REQUEST_BODY, body ));
      }
    }
        
    if ( httpRequest.getMethod( ).equalsIgnoreCase( "GET" ) )
    {
      setProperty( new StringProperty( REQUEST_METHOD, "GET" ));
        
      DataObject paramObj = new DataObject(  );
      paramObj.setName( PARAMETER_PROP );
      setProperty( paramObj );

      for (@SuppressWarnings("rawtypes")
           Enumeration enit = httpRequest.getParameterNames(); enit.hasMoreElements(); )
      {
        String paramName = (String)enit.nextElement();
        String[] paramValues = httpRequest.getParameterValues( paramName );
            
        if (paramValues.length == 1)
        {
          paramObj.setProperty( new StringProperty( paramName, paramValues[0] ) );
        }
        else if (paramValues.length > 1)
        {
          paramObj.setProperty( new StringListProperty( paramName, paramValues ) );
        }
      }
    }
        
    if (httpRequest.getCookies() != null)
    {
      DataObject cookieObj = new DataObject( );
      cookieObj.setName( COOKIE_PROP );
      setProperty( cookieObj );
    
      Cookie[] cookies = httpRequest.getCookies( );
      for (int i = 0; i < cookies.length; i++)
      {
        cookieObj.addProperty( new CookieWrapper( cookies[i] ) );
      }
    }
        
    if (httpRequest.getAttributeNames() != null)
    {
      DataObject attribObj = new DataObject(  );
      attribObj.setName( ATTRIBUTE_PROP );
      setProperty( attribObj );
        
      for (@SuppressWarnings("rawtypes")
           Enumeration anit = httpRequest.getAttributeNames(); anit.hasMoreElements(); )
      {
        String attribName = (String)anit.nextElement();
        Object attribValue = httpRequest.getAttribute( attribName );
                
        LOG.debug( "attribute: " + attribName + " = " + attribValue.toString() );

        if (attribValue instanceof Boolean)
        {
          attribObj.setProperty( new BooleanProperty( attribName, ((Boolean)attribValue).booleanValue()));
        }
        else
        {
          attribObj.setProperty( new StringProperty( attribName, attribValue.toString( ) ) );
        }
      }
    }
        
    // Other Request Properties ...
    setProperty( new StringProperty( "Protocol", httpRequest.getProtocol() ) );
    LOG.debug( "HttpRequestData constructor DONE." );
  }
    
  public HttpRequestData( String url )
  {
    try
    {
      setProperty( new StringProperty( REQUEST_URL_PROP, url ));
        
      URL theURL = new URL( url );
      String queryParams = theURL.getQuery( );
      if (queryParams != null)
      {
        DataObject requestParams = new DataObject( );
        HashMap<String,String> paramMap = StringMethods.unpackString( queryParams, "&", "=" );
        for (Iterator<String> keyIt = paramMap.keySet().iterator(); keyIt.hasNext(); )
        {
          String param = keyIt.next( );
          String value = paramMap.get( param );
          requestParams.setProperty( new StringProperty( param, value ));
        }
    	    	
        setRequestParameters( requestParams );
      }
    }
    catch ( Exception e )
    {
      LOG.debug( "HttpRequestData constructor got exception " + e );
    }
  }
    
  public void setRequestParameters( DataObject requestParameters )
  {
    requestParameters.setName( PARAMETER_PROP );
    setProperty( requestParameters );
  }
    
  public DataObject getRequestParameters(  )
  {
    return (DataObject)getProperty( PARAMETER_PROP );
  }
    
  public String getSessionID(  )
  {
    IProperty sessionIDProp = getProperty( SESSION_ID_PROP );
    return (sessionIDProp != null) ? sessionIDProp.getValue() : "";
  }
    
  public void setRequestURL( String requestURL )
  {
    setProperty( new StringProperty( REQUEST_URL_PROP, requestURL ));
  }

  public String getRequestURL( )
  {
    IProperty reqURLProp = getProperty( REQUEST_URL_PROP );
    return (reqURLProp != null ) ? reqURLProp.getValue( ) : null;
  }
    
  public String getRequestURI( )
  {
    IProperty reqURIProp = getProperty( REQUEST_URI_PROP );
    return (reqURIProp != null ) ? reqURIProp.getValue( ) : null;
  }
    
  public String getRequestBody( )
  {
    IProperty bodyProp = getProperty( REQUEST_BODY );
    return (bodyProp != null) ? bodyProp.getValue() : null;
  }
    
  // Getters for Cookies, Application Name, ... Host name, port, Etc ...

  private String getRequestBody( HttpServletRequest httpRequest )
  {
    try
    {
      BufferedReader br = httpRequest.getReader( );
      StringBuilder strbuilder = new StringBuilder( );
            
      String line = null;
      while ((line = br.readLine()) != null)
      {
        strbuilder.append( line );
        // should append new line?
      }
            
      return strbuilder.toString();
    }
    catch ( IOException ioe )
    {
      // Log it at least ... not much we can do though
    }
        
    return null;
  }

}
