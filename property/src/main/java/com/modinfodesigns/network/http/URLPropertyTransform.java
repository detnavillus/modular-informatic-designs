package com.modinfodesigns.network.http;


import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.PropertyValidationException;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.BasePropertyTransform;
import com.modinfodesigns.property.transform.IPropertyHolderTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.string.IStringTransform;
import com.modinfodesigns.property.transform.string.StringTransformException;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Converts a URL to the text 'behind' the URL.
 *  
 * @author Ted Sullivan
 *
 */

public class URLPropertyTransform extends BasePropertyTransform implements IPropertyHolderTransform, IStringTransform
{
  private transient static final Logger LOG = LoggerFactory.getLogger( URLPropertyTransform.class );

  private String urlProperty;
  private String contentProperty;
    
  private HashMap<String,String> headerPropertyMap;
  private HashMap<String,String> cookiePropertyMap;
    
  public URLPropertyTransform( )
  {
        
  }
    
  public void setURLProperty( String urlProperty )
  {
    // LOG.debug( "setURLProperty '" + urlProperty + "'" );
    this.urlProperty = urlProperty;
  }
    
  public void setContentProperty( String contentProperty )
  {
    this.contentProperty = contentProperty;
  }

  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    StringProperty strProp = new StringProperty( );
    strProp.setName( contentProperty );
        
    try
    {
      strProp.setValue( getContent( input.getValue( ), null ), null );
    }
    catch (PropertyValidationException pve )
    {
      throw new PropertyTransformException( pve.getMessage( ) );
    }
        
    return strProp;
  }

  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    IProperty urlProp = input.getProperty( urlProperty );
    LOG.debug( "transformPropertyHolder got URL Prop: '" + ((urlProp != null) ? urlProp.getValue( ) : "NULL!") + "'" );
        
    if (urlProp == null)
    {
      throw new PropertyTransformException( "URL Property is NULL!" );
    }
        
    StringProperty contentProp = new StringProperty( );
    contentProp.setName( contentProperty );
        
    Map<String,String> cookies = (cookiePropertyMap != null) ? new HashMap<String,String>( ) : null;
        
    HttpClientWrapper httpClient = getHttpClient( urlProp.getValue( ), cookies );
        
    try
    {
      contentProp.setValue( httpClient.getContent( ), "" );
    }
    catch ( PropertyValidationException pve )
    {
      throw new PropertyTransformException( pve.getMessage( ) );
    }
        
    input.setProperty( contentProp );
        
    // Check for Header properties etc...
    if (headerPropertyMap != null)
    {
      Map<String,String> headerMap = httpClient.getHeaderMap( );
        
      for (Iterator<String> headIt = headerMap.keySet().iterator(); headIt.hasNext(); )
      {
        String header = headIt.next();
        if (headerPropertyMap.keySet().contains( header ))
        {
          String headerField = headerPropertyMap.get( header );
          String headerValue = headerMap.get( header );
          input.addProperty( new StringProperty( headerField, headerValue ) );
        }
      }
    }
        
    if (cookiePropertyMap != null)
    {
      for (Iterator<String> cookIt = cookies.keySet().iterator(); cookIt.hasNext(); )
      {
        String cookie = cookIt.next( );
        String cookieField = cookiePropertyMap.get( cookie );
        String cookieValue = cookies.get( cookie );
        input.addProperty( new StringProperty( cookieField, cookieValue ) );
      }
    }
        
    return input;
  }
    
  @Override
  public String transformString( String inputString ) throws StringTransformException
  {
    return getContent( inputString, null );
  }

  @Override
  public String transformString( String sessionID, String inputString ) throws StringTransformException
  {
    return getContent( inputString, null );
  }
    
  private String getContent( String url, Map<String,String> cookies )
  {
    HttpClientWrapper httpClient = getHttpClient( url, cookies );
    return (httpClient != null) ? httpClient.getContent( true ) : null;
  }
    
  private HttpClientWrapper getHttpClient( String url, Map<String,String> cookies )
  {
    try
    {
      HttpClientWrapper hcw = new HttpClientWrapper( );
      hcw.doExecuteGet( url, cookies );
      return hcw;
    }
    catch ( Exception e )
    {
      LOG.debug( "getHttpClient got an Exception " + e );
      return null;
    }
  }

}
