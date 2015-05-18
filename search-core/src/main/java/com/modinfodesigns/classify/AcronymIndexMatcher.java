package com.modinfodesigns.classify;

import com.modinfodesigns.search.QueryTree;

/**
 * Subclass of TermIndexMatcher forces case sensitivity to always be TRUE. Acronyms should
 * only use exact match (case sensitive matching) e.g. 'AIDS' does NOT = 'aids'
 * 
 * @author Ted Sullivan
 */
public class AcronymIndexMatcher extends TermIndexMatcher
{
  public AcronymIndexMatcher( )
  {
    this.caseSensitive = true;
  }
    
  public AcronymIndexMatcher( String term )
  {
    this.caseSensitive = true;
    setTerm( term );
  }
    
  public AcronymIndexMatcher( String term, boolean caseSensitive )
  {
    // Ignore caseSensitive -- must stay true
    this.caseSensitive = true;
    setTerm( term );
  }
    
  public AcronymIndexMatcher( QueryTree qTree )
  {
    super( qTree );
    this.caseSensitive = true;
  }
    
  @Override
  public void setCaseSensitive( boolean caseSensitive )
  {
    	// lock out changes to case sensitivity - by doing nothing!!
  }

}
