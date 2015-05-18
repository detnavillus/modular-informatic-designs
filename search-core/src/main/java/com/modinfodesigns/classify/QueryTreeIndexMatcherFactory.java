package com.modinfodesigns.classify;

import com.modinfodesigns.search.IQueryTreeSetBuilder;

import com.modinfodesigns.search.QueryTreeSet;
import com.modinfodesigns.search.QueryTree;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * IndexMatcher Factory that uses a QueryTreeSetBuilder to create a set of IndexMatchers.
 * 
 * @author Ted Sullivan
 */

public class QueryTreeIndexMatcherFactory implements IIndexMatcherFactory
{
  private IQueryTreeSetBuilder queryTreeSetBuilder;
    
  public void setQueryTreeSetBuilder( IQueryTreeSetBuilder queryTreeSetBuilder )
  {
    this.queryTreeSetBuilder = queryTreeSetBuilder;
  }
    
  @Override
  public IIndexMatcher[] createIndexMatchers()
  {
    ArrayList<IIndexMatcher> indexMatcherList = new ArrayList<IIndexMatcher>( );
		
    QueryTreeSet qTreeSet = queryTreeSetBuilder.createQueryTreeSet();
    if (qTreeSet != null)
    {
      for (Iterator<QueryTree> qit = qTreeSet.getQueryTrees( ); qit.hasNext(); )
      {
        QueryTree qTree = qit.next();
        indexMatcherList.add( IndexMatcherFactory.createIndexMatcher( qTree ) );
      }
    }
		
    IIndexMatcher[] indexMatchers = new IIndexMatcher[ indexMatcherList.size() ];
    indexMatcherList.toArray( indexMatchers );
    return indexMatchers;
  }
}
