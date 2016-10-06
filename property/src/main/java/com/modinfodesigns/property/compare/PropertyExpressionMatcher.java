package com.modinfodesigns.property.compare;

import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.security.IUserCredentials;

/**
 * Parses a complex boolean Property Value expression.
 * 
 *  e.g. name( format ) contains 'some value' || name2/dlist[3]( format ) startsWith 'other value'
 *  Cardinality - any | all |
 *  
 *  where ( format ) is optional (if left out, the default property format will be used).
 *  
 *  OR AND, EQ, LE, GT, GTE, LTE, NOT can be substituted for '&&', '||', '==', '<', '>', '>=', '<=',
 *   '!=' respectively
 *
 * Supports Regular Expressions
 *  path/to/object(format) matches [ regular expression ]
 *
 * any( path(format) operator value )
 *  
 * @author Ted Sullivan
 */

public class PropertyExpressionMatcher implements IPropertyHolderMatcher
{
  private String matchExpression;

  private String objectPath;
  private String format;
  private String operator;
  private String compValue;
  private boolean isAny;
    
  public PropertyExpressionMatcher( ) {   }
    
  public PropertyExpressionMatcher( String matchExpression )
  {
    this.matchExpression = matchExpression;
    parseExpression( matchExpression );
  }
    
  public String getMatchExpression( )
  {
    return this.matchExpression;
  }
    
  @Override
  public boolean equals( IUserCredentials user, IProperty property )
  {
    // check if IPropertyHolder ...
    if (property instanceof IPropertyHolder )
    {
      return equals( user, (IPropertyHolder)property );
    }
      
    return false;
  }

  @Override
  public boolean equals( IUserCredentials user, IPropertyHolder propHolder )
  {
    // -----------------------------------------------------------------
    // get the property from the path(format) expression
    // get the operator
    // get the value - do the comparison
    // -----------------------------------------------------------------
    IProperty prop = propHolder.getProperty( objectPath );
    
    return false;
  }
    
  // format object_path( format ) operator value
  // object_path operator value
  private void parseExpression( String matchExpression )
  {
      String trimmedExpr = matchExpression.trim( );
      
    if ( trimmedExpr.startsWith( "any" ) || trimmedExpr.startsWith( "ANY" )
      || trimmedExpr.startsWith( "all" ) || trimmedExpr.startsWith( "ALL" ))
    {
        
    }
      
    String opvalue = null;
    int parenNdx = matchExpression.indexOf( "(" );
    if (parenNdx > 0)
    {
      this.objectPath = matchExpression.substring( 0, parenNdx ).trim( );
      this.format = matchExpression.substring( parenNdx + 1, matchExpression.indexOf( ")" ) ).trim( );
      opvalue = matchExpression.substring( matchExpression.indexOf( ")" ) + 1 ).trim( );
    }
    else
    {
      this.objectPath = matchExpression.substring( 0, matchExpression.indexOf( " " )).trim( );
      opvalue = matchExpression.substring( matchExpression.indexOf( " " ) + 1 ).trim( );
    }
      
    this.operator = opvalue.substring( 0, opvalue.indexOf( " " )).trim( );
    this.compValue = opvalue.substring( opvalue.indexOf( " " ) + 1 ).trim( );
  }

}
