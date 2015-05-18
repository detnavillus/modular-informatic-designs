package com.modinfodesigns.search;

import com.modinfodesigns.property.quantity.ScalarQuantity;

/**
 * Contains the boost factor for a Query Term. The boost factor is set to the
 * super class ScalarQuantity double value. 
 * 
 * @author Ted Sullivan
 */

public class QueryTermBoost extends ScalarQuantity
{
  // The Query Term value to be boosted
  private String queryTerm;
	
  public QueryTermBoost( ) {  }
	
  public QueryTermBoost( String queryTerm, double boostFactor )
  {
    setName( queryTerm );
    this.queryTerm = queryTerm;
    this.value = boostFactor;
  }
	
  public QueryTermBoost( String name, String queryTerm, double boostFactor )
  {
    super( name, boostFactor );
    this.queryTerm = queryTerm;
  }

  @Override
  public String getValue( )
  {
    return (queryTerm != null) ? queryTerm : null;
  }
	
}
