package com.modinfodesigns.network.http;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IComputableProperties;
import com.modinfodesigns.property.PropertyValidationException;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class URLProperty implements IProperty, IComputableProperties
{
  public static final String HOST  = "Host";
  public static final String PORT  = "Port";
  public static final String QUERY = "Query";
  public static final String PROTOCOL = "Protocol";
  public static final String PATH     = "Path";
  public static final String STATUS   = "Status";
  public static final String PUBLIC   = "Public";
  public static final String CONTENT     = "Content";
  public static final String CONTENT_TYPE = "ContentType";
  public static final String CONTENT_LENGTH = "ContentLength";
	
  private static ArrayList<String> intrinsics;
  private static HashMap<String,String> computables;
	
  private String name;
	
  private URL theURL;
    
  static
  {
    intrinsics = new ArrayList<String>( );
    intrinsics.add( HOST );
    intrinsics.add( PORT );
    intrinsics.add( QUERY );
    intrinsics.add( PROTOCOL );
    intrinsics.add( PATH );
    intrinsics.add( STATUS );
    intrinsics.add( PUBLIC );
    intrinsics.add( CONTENT );
    intrinsics.add( CONTENT_TYPE );
    intrinsics.add( CONTENT_LENGTH );
    	
    computables = new HashMap<String,String>( );
    	
    computables.put( "Accessible", "com.modinfodesigns.security.IUserCredentials" );
  }
    
    
  @Override
  public String getName()
  {
    return this.name;
  }

  @Override
  public void setName(String name)
  {
    this.name = name;
  }

  @Override
  public String getType()
  {
    return  getClass().getCanonicalName();
  }

  @Override
  public String getValue()
  {
    return (theURL != null) ? renderURL(  ) : null;
  }

  @Override
  public String getValue( String format )
  {
    if (theURL == null) return null;
		
    if (format.equals( PROTOCOL ))
    {
      return theURL.getProtocol( );
    }
    else if (format.equals( HOST ))
    {
      return theURL.getHost( );
    }
		
    return getValue( );
  }

  @Override
  public void setValue(String value, String format) throws PropertyValidationException
  {
    try
    {
      theURL = new URL( value );
    }
    catch( MalformedURLException mue )
    {
      throw new PropertyValidationException( "MalformedURLException: " + mue.getMessage( ) );
    }
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public IProperty copy()
  {
    URLProperty copy = new URLProperty( );
    copy.theURL = this.theURL;
    
    return copy;
  }

  @Override
  public Object getValueObject()
  {
    return theURL;
  }
	
  private String renderURL( )
  {
    return "";
  }

  @Override
  public List<String> getIntrinsicProperties()
  {
    return intrinsics;
  }

  @Override
  public IProperty getIntrinsicProperty( String name )
  {
    if (name.equals( HOST ))
    {
      return new StringProperty( HOST, theURL.getHost( ) );
    }
    else if (name.equals( PORT ))
    {
      return new IntegerProperty( PORT, theURL.getPort( ) );
    }
    else if ( name.equals( QUERY ))
    {
      return new StringProperty( QUERY, theURL.getQuery( ) );
    }
    else if ( name.equals( PROTOCOL ))
    {
      return new StringProperty( PROTOCOL, theURL.getProtocol( ) );
    }
    else if ( name.equals( PATH ))
    {
      return new StringProperty( PATH, theURL.getPath( ) );
    }
    else if ( name.equals( STATUS ))
    {
      // try to access the URL, can return
      // Cannot connect
      HttpClientWrapper httpWrapper = new HttpClientWrapper( );
      httpWrapper.doExecuteGet( renderURL( ), null );
    		
      int status = httpWrapper.getStatus( );
      String statusStr = "";
      if (status == 200)
      {
        statusStr = "OK";
      }
      else if (status == 403)
      {
        statusStr = "Access Denied.";
      }
      else if (status == 404)
      {
        statusStr = "Page Not Found";
      }
      else if (status == 500)
      {
        statusStr = "Server Error";
      }
    		
      return new StringProperty( STATUS, statusStr );
    }
    else if ( name.equals( PUBLIC ))
    {
      // access the URL if get 200 then return true,
      // if get 403 return false
      // if get connection refused - return unknown
    }
    else if ( name.equals( CONTENT ))
    {
    		
    }
    return null;
  }

  @Override
  public Map<String, String> getComputableProperties()
  {
    return computables;
  }

  @Override
  public IProperty getComputedProperty(String name, IProperty fromProp)
  {
    return null;
  }

  @Override
  public boolean isMultiValue()
  {
    return false;
  }

  @Override
  public String[] getValues(String format)
  {
    String[] values = new String[1];
    values[0] = getValue( format );
    return values;
  }

}
