package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.security.IUserCredentials;

/**
 * Parses a complex boolean Property Value expression.
 * 
 *  e.g. ['name']( format ) contains 'some value' || name2/dlist[3]( format ) startsWith 'other value'
 *  
 *  where ( format ) is optional (if left out, the default property format will be used).
 *  
 *  OR AND, EQ, LE, GT, GTE, LTE, NOT can be substituted for '&&', '||', '==', '<', '>', '>=', '<=',
 *   '!=' respectively
 *  
 * @author Ted Sullivan
 */

public class PropertyExpressionMatcher implements IPropertyHolderMatcher
{
  private String matchExpression;
    
    
  public PropertyExpressionMatcher( ) {   }
    
  public PropertyExpressionMatcher( String matchExpression )
  {
    this.matchExpression = matchExpression;
  }
    
  public String getMatchExpression( )
  {
    return this.matchExpression;
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    return false;
  }

  @Override
  public boolean equals( IUserCredentials user, IPropertyHolder propHolder )
  {
    return false;
  }

}
