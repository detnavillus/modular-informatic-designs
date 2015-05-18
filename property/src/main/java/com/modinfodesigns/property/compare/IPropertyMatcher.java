package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.security.IUserCredentials;

public interface IPropertyMatcher
{
  /**
   * Compares an IProperty for equality (matching)
   *
   * @param user     Session user credentials
   * @param property Any Property
   * @return true if Comparator matches the property and user
   */
  public boolean equals( IUserCredentials user, IProperty property );
}
