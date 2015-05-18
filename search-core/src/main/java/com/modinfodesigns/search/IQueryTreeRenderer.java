package com.modinfodesigns.search;

public interface IQueryTreeRenderer extends IQueryRenderer
{
  /**
   * @return     The query language format with which the QueryTree will be rendered.
   */
  public String getFormat( );
	
  /**
   * Renders the query tree in some 'QL' format.
   *
   * @param queryTree  The QueryTree to be rendered
   * @return           A Query Language string that expresses the logic in the Query Tree
   */
  public String renderQueryTree( QueryTree queryTree );
}
