package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.security.IUserCredentials;

public interface IPropertyHolderMatcher extends IPropertyMatcher
{
  public boolean equals( IUserCredentials user, IPropertyHolder propHolder );
}
