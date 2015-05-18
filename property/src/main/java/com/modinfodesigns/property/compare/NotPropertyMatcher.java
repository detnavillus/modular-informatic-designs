package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.security.IUserCredentials;

/**
 * Negates the match decision made by its delegate.
 * 
 * @author Ted Sullivan
 *
 */
public class NotPropertyMatcher implements IPropertyMatcher
{
  IPropertyMatcher matcher;
    
  public void setPropertyMatcher( IPropertyMatcher propertyMatcher )
  {
    this.matcher = propertyMatcher;
  }
    
  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    if (matcher == null) return false;
    return !matcher.equals( user, property );
  }

}
