package com.modinfodesigns.search;

/**
 * Interface for objects that can translate a query string into an IQuery object.
 * 
 * @author Ted Sullivan
 */

public interface IQueryParser
{
  public IQuery parse( String queryName, String queryString );
}
