package com.modinfodesigns.network.http;

import com.modinfodesigns.security.IUserCredentials;

import org.apache.http.client.CookieStore;

import org.apache.http.impl.client.DefaultHttpClient;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.Header;

import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.BasicHttpContext;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;

import org.apache.http.client.params.AuthPolicy;
import org.apache.http.auth.params.AuthPNames;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses Apache HttpConnection library.
 * 
 * @author Ted Sullivan
 *
 */

public class HttpClientWrapper
{
  private transient static final Logger LOG = LoggerFactory.getLogger( HttpClientWrapper.class );
    
  DefaultHttpClient httpClient;
  HttpResponse httpResp;
  HttpRequest httpReq;
  InputStream responseStr;

  HttpContext httpContext;
    
  private Long contentOffset;
  private Integer contentLength;
  private String contentType;

  private static boolean appendLineFeeds = false;
    
  public static String executeGet( String url )
  {
    return executeGet( url, (Map<String,String>)null );
  }
    
  public static String executeGet( String url, Map<String,String> cookies )
  {
    String response = null;
    try
    {
      HttpClientWrapper hcw = new HttpClientWrapper( );
      hcw.doExecuteGet( url, cookies );
      response =  (hcw.isTextResponse( ))
               ? hcw.getContent( hcw.responseStr, appendLineFeeds )
               : null;
      hcw.closeConnection( );
    }
    catch ( Exception e )
    {
      LOG.error( "executeGet( ) got Exception: " + e.getMessage( ) );
    }

    return response;
  }
    

  public static String executeGetAuthorized( String theURL, Map<String,String> cookies,
                                             IUserCredentials user, String authMethods )
  {
    LOG.debug( "getAuthorizedURL( " + theURL + " ) authMethod: " + authMethods + " username: " + user.getUsername() );
    String getResp = null;
    try
    {
      HttpClientWrapper httpWrapper = new HttpClientWrapper( );
      httpWrapper.doExecuteGetAuthorized( theURL, cookies, user, authMethods );
      getResp =  (httpWrapper.isTextResponse( ))
              ? httpWrapper.getContent( httpWrapper.responseStr, appendLineFeeds )
              : null;
                    
      return getResp;
    }
    catch ( Exception e )
    {
      LOG.error( "executeGetAuthorized( ) got Exception: " + e.getMessage( ) );
    }

    return null;
  }
    
  public static String executePost( String url, String params )
  {
    return executePost( url, params, (Map<String,String>)null);
  }
    
  public static String executePost( String url, String params, String contentType )
  {
    return executePost( url, params, null, contentType );
  }
    
  public static String executePost( String url, String params, Map<String,String> cookies )
  {
      return executePost( url, params, cookies, null );
  }
    
  public static String executePost( String url, String params, Map<String,String> cookies, String contentType )
  {
    String response = null;
    try
    {
      HttpClientWrapper hcw = new HttpClientWrapper( );
      hcw.doExecutePost( url, params, cookies, contentType );
      response =  (hcw.isTextResponse( ))
               ? hcw.getContent( hcw.responseStr, appendLineFeeds )
               : null;
      hcw.closeConnection( );
    }
    catch ( Exception e )
    {
      LOG.error( "executePost( ) got Exception: " + e.getMessage( ) );
    }
        
    return response;
  }
    
  public void doExecuteGet( String url, Map<String,String> cookies )
  {
    try
    {
      httpClient = new DefaultHttpClient( );
      if (url.startsWith( "https" ))
      {
        httpClient = createSSLClient( httpClient );
      }

      HttpGet getMethod = new HttpGet( cleanupURL( url ) );
      httpReq = getMethod;

      httpContext = new BasicHttpContext( );

      addCookies( httpClient, cookies );

      httpResp = httpClient.execute( getMethod, httpContext );
      HttpEntity entity = httpResp.getEntity();
      responseStr = entity.getContent( );

      getCookies( httpClient, cookies );
    }
    catch ( Exception e )
    {
      LOG.error( "doExecuteGet( ) got Exception: " + e.getMessage( ) );
    }
  }
    
  @SuppressWarnings("unused")
  public void doExecuteGetAuthorized( String theURL, Map<String,String> cookies,
                                      IUserCredentials user, String authMethods )
  {
    LOG.debug( "doExecuteGetAuthorized( " + theURL + " ) authMethod: "
               + authMethods + " username: " + user.getUsername() + " password: " + user.getPassword() );
    try
    {
      httpClient = new DefaultHttpClient( );
      if (theURL.startsWith( "https" ))
      {
        httpClient = createSSLClient( httpClient );
      }

      HttpGet getMethod = new HttpGet( cleanupURL( theURL ) );
      httpReq = getMethod;

      httpContext = new BasicHttpContext( );
        
      List<String> authPrefs = new ArrayList<String>(3);

      if (authMethods != null)
      {
        String[] authMethodList = authMethods.split( "," );
        for (int i = 0; i < authMethodList.length; i++)
        {
          Credentials creds = null;

          if (authMethodList[i].equalsIgnoreCase( "BASIC" ))
          {
            authPrefs.add( AuthPolicy.BASIC );
            creds = new UsernamePasswordCredentials( user.getUsername(), user.getPassword() );
          }
          else if (authMethodList[i].startsWith( "NTLM:" ))
          {
            authPrefs.add(AuthPolicy.NTLM);

            String domain = authMethodList[i].substring( "NTLM:".length() );
            String username = user.getUsername();
            String useUser = username;
                        
            if (username.indexOf( "\\" ) > 0)
            {
              useUser = username.substring( username.indexOf( "\\" ) + 1 );
              domain = username.substring( 0, username.indexOf( "\\" ));
            }
                        
            creds = new NTCredentials( useUser, user.getPassword(), "", domain );
          }
          else if (authMethodList[i].equalsIgnoreCase( "DIGEST" ))
          {
            authPrefs.add(AuthPolicy.DIGEST);
            creds = new UsernamePasswordCredentials( user.getUsername(), user.getPassword() );
          }
            
          if (creds != null)
          {
            httpClient.getCredentialsProvider().setCredentials( AuthScope.ANY, creds );
          }
        }
      }
      else
      {
        // Default to BASIC authentication:
        authPrefs.add( AuthPolicy.BASIC );
        httpClient.getCredentialsProvider().setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( user.getUsername(), user.getPassword() ) );
      }

      httpClient.getParams().setParameter(AuthPNames.TARGET_AUTH_PREF, authPrefs);

      addCookies( httpClient, cookies );
                                                           
      httpResp = httpClient.execute( getMethod, httpContext );
      HttpEntity entity = httpResp.getEntity();
      responseStr = entity.getContent( );

      getCookies( httpClient, cookies );

      debugHeader( );
    }
    catch ( Exception e )
    {
      LOG.error( "doExecuteGetAuthorized( ) got Exception: " + e );
      e.printStackTrace( );
    }

    LOG.debug( "doExecuteGetAuthorized( ) DONE." );
  }

    
  public void doExecutePost( String url, String params, Map<String,String> cookies )
  {
      doExecutePost( url, params, cookies, null );
  }
    
  public void doExecutePost( String url, String params, Map<String,String> cookies, String contentType )
  {
    try
    {
      httpClient = new DefaultHttpClient( );
      if (url.startsWith( "https" ))
      {
        httpClient = createSSLClient( httpClient );
      }

      HttpPost postMethod = new HttpPost( cleanupURL( url ) );
      if (contentType != null)
      {
        postMethod.setHeader( "Content-type", contentType );
      }
      httpReq = postMethod;
            
      postMethod.setEntity( new StringEntity( params ) );

      httpContext = new BasicHttpContext( );

      addCookies( httpClient, cookies );

      httpResp = httpClient.execute( postMethod, httpContext );
      HttpEntity entity = httpResp.getEntity();
      responseStr = entity.getContent( );

      getCookies( httpClient, cookies );
    }
    catch ( Exception e )
    {
      LOG.error( "doExecutePost( ) got Exception: " + e );
    }
  }
    
  public int getStatus( )
  {
    return 0;
  }
    
  public String getContent(  )
  {
    return getContent( false );
  }

  public String getContent( boolean appendNewLines )
  {
    LOG.debug( "getContent( ) ..." );

    return (responseStr != null) ? getContent( responseStr, appendNewLines, contentOffset, contentLength ) : null;
  }


  private static String getContent( InputStream is, boolean appendNewlines, Long contentOffset, Integer contentLength )
  {
    LOG.debug( "HttpClientWrapper.getContent( ) ..." );

    try
    {
      if (contentOffset != null && contentLength != null)
      {
        is.skip( contentOffset.longValue( ) );

        byte[] contentBytes = new byte[ contentLength.intValue( ) ];
        is.read( contentBytes, 0, contentLength.intValue( ) );
        is.close( );

        return new String( contentBytes );
      }

      BufferedReader buffRead = new BufferedReader( new InputStreamReader( is ) );

      StringBuffer strbuf = new StringBuffer( 1000 );
      String line = null;
        
      while((line = buffRead.readLine()) != null)
      {
        LOG.debug( line );
        strbuf.append( line );
        if (appendNewlines)
        {
          strbuf.append( "\r\n" );
        }
        else if ( !line.endsWith( " " ))
        {
          strbuf.append( " " );
        }
      }
            
      buffRead.close( );
            
      return strbuf.toString( );
    }
    catch ( Exception e )
    {
      LOG.error( "getContent( ) got Exception: " + e );
      e.printStackTrace( );
    }

    return null;
  }
    
    
  public String getContent( InputStream is, boolean appendLineFeeds )
  {
    LOG.debug( "getContent( ) ..." );

    try
    {
      if (contentOffset != null && contentLength != null)
      {
        is.skip( contentOffset.longValue( ) );

        byte[] contentBytes = new byte[ contentLength.intValue() ];
        is.read( contentBytes, 0, contentLength.intValue( ) );
        is.close( );

        return new String( contentBytes );
      }

      BufferedReader buffRead = new BufferedReader( new InputStreamReader( is ) );

      StringBuffer strbuf = new StringBuffer( 1000 );
      String line = null;
            
      while((line = buffRead.readLine()) != null)
      {
        LOG.debug( line );
        strbuf.append( line );
        if (appendLineFeeds)
        {
          strbuf.append( "\r\n" );
        }
        else if (!line.endsWith( " " ))
        {
          strbuf.append( " " );
        }
      }
    
      buffRead.close( );
            
      return strbuf.toString( );
    }
    catch ( Exception e )
    {
      LOG.error( "getContent( ) got Exception: " + e );
      e.printStackTrace( );
    }

    return null;
  }
    
    
  public boolean isTextResponse( )
  {
    if (httpResp == null) return false;

    Header ctHeader = httpResp.getFirstHeader( "Content-Type" );
    if (ctHeader == null) ctHeader = httpResp.getFirstHeader( "content-type" );
    if (ctHeader == null) ctHeader = httpResp.getFirstHeader( "Content-type" );

    if (ctHeader != null)
    {
      if (ctHeader.getValue().startsWith( "application" )) return false;
      if (ctHeader.getValue().startsWith( "image" )) return false;
      if (ctHeader.getValue().startsWith( "video" )) return false;
      if (ctHeader.getValue().startsWith( "text" )) return true;
    }

    return true;
  }
    
  @SuppressWarnings("unused")
  public Map<String,String> getHeaderMap( )
  {
    if (httpResp == null) return null;

    LOG.debug( "Response: " + httpResp.getStatusLine().getStatusCode( ) +
                                               " " + httpResp.getStatusLine().getReasonPhrase( ) );

    HashMap<String,String> headerMap = new HashMap<String,String>( );

    Header[] headerList = httpResp.getAllHeaders();
    for (int i = 0; i < headerList.length; i++)
    {
      LOG.debug( "Got Header: " + headerList[i].getName( ).toLowerCase( ) + " = "
                                                                             + headerList[i].getValue( ) );
      headerMap.put( headerList[i].getName( ).toLowerCase( ), headerList[i].getValue( ) );
    }

    return headerMap;
  }
    
  public InputStream getResponseStream( )
  {
    return this.responseStr;
  }
    
 
  public String getContentType(  )
  {
    if (httpResp == null) return null;

    Header ctHeader = httpResp.getFirstHeader( "Content-Type" );
    if (ctHeader == null) ctHeader = httpResp.getFirstHeader( "content-type" );
    if (ctHeader == null) ctHeader = httpResp.getFirstHeader( "Content-type" );

    if (ctHeader != null) return ctHeader.getValue( );

    LOG.debug( "Did not get content-type header! - returning text/html" );
    return "text/html";
  }
    
  public void finalize( )
  {
    closeConnection( );
  }
    
  public void closeConnection( )
  {
    try
    {
      if (responseStr != null) responseStr.close( );

      if (httpClient != null) httpClient.getConnectionManager().shutdown();
    }
    catch ( Exception e )
    {
      LOG.error( "closeConnection got Exception: " + e );
    }
  }
    
  private void getCookies( DefaultHttpClient httpClient, Map<String,String> cookies )
  {
    if (cookies == null) return;
        
    CookieStore cookieStore = httpClient.getCookieStore( );
    List<Cookie> theCookies = cookieStore.getCookies();
    for( Cookie cookie: theCookies )
    {
      cookies.put( cookie.getName( ), cookie.getValue( ) );
    }
  }
    
  private void addCookies( DefaultHttpClient httpClient, Map<String,String> cookies )
  {
    if (cookies == null) return;
        
    CookieStore cookieStore = httpClient.getCookieStore( );
    for (java.util.Iterator<String> it = cookies.keySet().iterator(); it.hasNext( ); )
    {
      String name = it.next( );
      String value = cookies.get( name );
      cookieStore.addCookie( new BasicClientCookie( name, value ) );
    }
  }
    
  @SuppressWarnings("deprecation")
  private DefaultHttpClient createSSLClient( DefaultHttpClient httpClient )
  {
    try
    {
      SSLContext ctx = SSLContext.getInstance("TLS");
      X509TrustManager tm = new X509TrustManager()
      {
        public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException { }
        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException { }
        public X509Certificate[] getAcceptedIssuers()
        {
          return null;
        }
      };

      ctx.init(null, new TrustManager[]{tm}, null);
      SSLSocketFactory ssf = new SSLSocketFactory(ctx);
      ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      ClientConnectionManager ccm = httpClient.getConnectionManager();
      SchemeRegistry sr = ccm.getSchemeRegistry();
      sr.register(new Scheme("https", ssf, 443));
      return new DefaultHttpClient(ccm, httpClient.getParams());
    }
    catch (Exception ex)
    {
      return null;
    }
  }
    
  private String cleanupURL( String url )
  {
    return url.replace( " ", "%20" );
  }

  @SuppressWarnings("unused")
  private void debugHeader(  )
  {
    LOG.debug( "Status " + httpResp.getStatusLine( ).getStatusCode( ) );

    Header[] headerList = httpResp.getAllHeaders();
    for (int i = 0; i < headerList.length; i++)
    {
      LOG.debug( "Got Header: " + headerList[i].getName( ) + " = "
                                                                      + headerList[i].getValue( ) );
    }
  }
}
