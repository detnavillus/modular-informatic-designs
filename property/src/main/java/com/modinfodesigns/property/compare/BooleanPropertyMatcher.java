package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.security.IUserCredentials;

import java.util.ArrayList;

/**
 * Matches a Property IF ANY or ALL of its Sub-Matchers match 
 * (depending on the 'isAnd' flag).
 * 
 * @author Ted Sullivan
 */

public class BooleanPropertyMatcher implements IPropertyMatcher
{
  private ArrayList<IPropertyMatcher> matchers;
   
  private boolean isAnd = false;
   
  public void setIsAnd( String isAnd )
  {
    this.isAnd = (isAnd != null && isAnd.equalsIgnoreCase( "true" ));
  }
   
  public void setAnd( String isAnd )
  {
    setIsAnd( isAnd );
  }
   
  public void addPropertyMatcher( IPropertyMatcher propertyMatcher )
  {
    if (matchers == null) matchers = new ArrayList<IPropertyMatcher>( );
    matchers.add( propertyMatcher );
  }
   
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    if (matchers == null) return false;
		
    for (int i = 0; i < matchers.size(); i++)
    {
      IPropertyMatcher matcher = matchers.get( i );
      boolean matches = matcher.equals( user, property );
      if (matches != isAnd ) return !isAnd;
    }
		
    return isAnd;
  }
}
