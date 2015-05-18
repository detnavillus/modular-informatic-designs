package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.security.IUserCredentials;

public class PropertyTypeMatcher implements IPropertyMatcher 
{
  private String propType;
    
  public PropertyTypeMatcher(  ) {  }
    
  public PropertyTypeMatcher( String type )
  {
    this.propType = type;
  }
    
  public void setType( String type )
  {
    this.propType = type;
  }
    
    
  @Override
  public boolean equals(IUserCredentials user, IProperty property)
  {
    return (property != null && property.getType().equals( propType ));
  }

}
