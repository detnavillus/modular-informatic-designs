package com.modinfodesigns.network.http;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.compare.IPropertyHolderMatcher;
import com.modinfodesigns.security.IUserCredentials;

public class HttpRequestParamsMatcher implements IPropertyHolderMatcher
{
  private IPropertyHolderMatcher requestParamsMatcher;
    
  public void setDataMatcher( IPropertyHolderMatcher requestParamsMatcher )
  {
    this.requestParamsMatcher = requestParamsMatcher;
  }
    
  @Override
  public boolean equals(IUserCredentials user, IProperty property )
  {
    if (property instanceof HttpRequestData )
    {
      return matchesParams( user, ((HttpRequestData)property).getRequestParameters( ) );
    }
		
    return false;
  }

  @Override
  public boolean equals(IUserCredentials user, IPropertyHolder propHolder)
  {
    if (propHolder instanceof HttpRequestData )
    {
      return matchesParams( user, ((HttpRequestData)propHolder).getRequestParameters( ) );
    }
		
    return false;
  }
	
  private boolean matchesParams( IUserCredentials user, DataObject requestParams )
  {
    return (requestParams != null) ? requestParamsMatcher.equals( user, requestParams ) : null;
  }

}
