package com.modinfodesigns.search;

/**
 * Creates an QueryTree from a query String.
 * 
 * @author Ted Sullivan
 */
public interface IQueryTreeParser extends IQueryParser
{
  public QueryTree createQueryTree( String queryTreeName, String queryString );
}
