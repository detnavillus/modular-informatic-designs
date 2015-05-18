package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.security.IUserCredentials;

public class DataObjectDelegateMatcher implements IPropertyHolderMatcher
{
  private IPropertyHolderMatcher delegateMatcher;
  private String delegateName;
    
  public void setDataMatcher( IPropertyHolderMatcher delegateMatcher )
  {
    this.delegateMatcher = delegateMatcher;
  }
    
  public void setDelegateName( String delegateName )
  {
    this.delegateName = delegateName;
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    if (property instanceof DataObjectDelegate )
    {
      DataObject delegateObj = ((DataObjectDelegate)property).getDelegate( );
      if (delegateObj != null)
      {
        return delegateMatcher.equals( user, delegateObj );
      }
    }
		
    return false;
  }

  @Override
  public boolean equals(IUserCredentials user, IPropertyHolder propHolder)
  {
    IProperty delegate = propHolder.getProperty( delegateName );
    return equals( user, delegate );
  }

}
