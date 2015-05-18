package com.modinfodesigns.network.http;


import com.modinfodesigns.property.IProperty;

import javax.servlet.http.Cookie;

public class CookieWrapper implements IProperty
{
  public static final String DOMAIN = "DOMAIN";
  private Cookie theCookie;
    
  public CookieWrapper( Cookie cookie )
  {
    this.theCookie = cookie;
  }

  @Override
  public String getName()
  {
    return (theCookie != null) ? theCookie.getName() : null;
  }


  @Override
  public String getType()
  {
    return "com.modinfodesigns.network.http.CookieWrapper";
  }

  @Override
  public String getValue()
  {
    return (theCookie != null) ? theCookie.getValue() : null;
  }

  @Override
  // To Do: use this to get other values of a Cookie
  public String getValue(String format)
  {
    if (format != null && format.equals( DOMAIN ))
    {
      return theCookie.getDomain( );
    }
        
    return getValue( );
  }

  @Override
  public void setValue(String value, String format)
  {
    if (theCookie != null) theCookie.setValue( value );
  }

  @Override
  public IProperty copy()
  {
    return null;
  }

  @Override
  public void setName(String name)
  {
    
  }

  @Override
  public String getDefaultFormat()
  {
    return null;
  }

  @Override
  public Object getValueObject()
  {
    return theCookie;
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
