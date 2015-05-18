package com.modinfodesigns.classify;

import com.modinfodesigns.search.QueryTree;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexMatcherFactory implements IIndexMatcherFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( IndexMatcherFactory.class );

  private ArrayList<IIndexMatcherFactory> matcherFactoryList;
	
  private ArrayList<IIndexMatcher> prebuiltMatchers;
	
  private String mode = "LIST";  // LIST | OR | AND
	
  private String matcherName;
	
  public void addIndexMatcherFactory( IIndexMatcherFactory indexMatcherFactory )
  {
    LOG.debug( "addIndexMatcherFactory" );
    if (matcherFactoryList == null) matcherFactoryList = new ArrayList<IIndexMatcherFactory>( );
    matcherFactoryList.add( indexMatcherFactory );
  }
	
  public void addIndexMatcher( IIndexMatcher prebuiltMatcher )
  {
    LOG.debug( "addIndexMatcher" );
    if (prebuiltMatchers == null) prebuiltMatchers = new ArrayList<IIndexMatcher>( );
    prebuiltMatchers.add( prebuiltMatcher );
  }
	
  public void setMode( String mode )
  {
    LOG.debug( "setMode: " + mode );
    this.mode = mode;
  }
	
  public void setMatcherName( String matcherName )
  {
    this.matcherName = matcherName;
  }
	
	
  public IIndexMatcher[] createIndexMatchers(  )
  {
    LOG.debug( "createIndexMatchers( )..." );
    if (matcherFactoryList == null && prebuiltMatchers == null) return null;
    	
    if ( mode == null || mode.equals( "LIST" ) )
    {
      ArrayList<IIndexMatcher> matchers = new ArrayList<IIndexMatcher>( );
    		
      if (prebuiltMatchers != null)
      {
        matchers.addAll( prebuiltMatchers );
      }
    		
      if (matcherFactoryList != null)
      {
        for (int i = 0; i < matcherFactoryList.size(); i++)
        {
          IIndexMatcher[] itsMatchers = matcherFactoryList.get(i).createIndexMatchers( );
          if (itsMatchers != null)
          {
            for (int j = 0; j < itsMatchers.length; j++)
            {
              matchers.add( itsMatchers[j] );
            }
          }
        }
      }
    		
      IIndexMatcher[] outputList = new IIndexMatcher[ matchers.size( ) ];
      matchers.toArray( outputList );
      return outputList;
    }
    	
    if (mode.equals( "OR" ))
    {
      LOG.debug( "Creating OrIndexMatcher" );
      OrIndexMatcher orMatcher = new OrIndexMatcher( );
    		
      if (matcherName != null)
      {
        orMatcher.setName( matcherName );
      }
    		
      if (prebuiltMatchers != null)
      {
        for (int i = 0; i < prebuiltMatchers.size(); i++)
        {
          orMatcher.addIndexMatcher( prebuiltMatchers.get( i ) );
        }
      }
    		
      if (matcherFactoryList != null)
      {
        for (int i = 0; i < matcherFactoryList.size( ); i++)
        {
          IIndexMatcher[] childMatchers = matcherFactoryList.get(i).createIndexMatchers();
          if (childMatchers != null && childMatchers.length > 0)
          {
            for (int j = 0; j < childMatchers.length; j++)
            {
              orMatcher.addIndexMatcher( childMatchers[j] );
            }
          }
        }
      }
    		
      IIndexMatcher[] list = new IIndexMatcher[1];
      list[0] = orMatcher;
      return list;
    }
    else
    {
      AndIndexMatcher andMatcher = new AndIndexMatcher( );
        
      if (matcherName != null)
      {
        andMatcher.setName( matcherName );
      }
    		
      if (prebuiltMatchers != null)
      {
        for (int i = 0; i < prebuiltMatchers.size(); i++)
        {
          andMatcher.addIndexMatcher( prebuiltMatchers.get( i ) );
        }
      }
    		
      if (matcherFactoryList != null)
      {
        for (int i = 0; i < matcherFactoryList.size( ); i++)
        {
          IIndexMatcher[] childMatchers = matcherFactoryList.get( i ).createIndexMatchers();
          if (childMatchers != null && childMatchers.length > 0)
          {
            for (int j = 0; j < childMatchers.length; j++)
            {
              andMatcher.addIndexMatcher( childMatchers[j] );
            }
          }
        }
      }
    		
      IIndexMatcher[] list = new IIndexMatcher[1];
      list[0] = andMatcher;
      return list;
    }
  }
    

  public static IIndexMatcher createIndexMatcher( QueryTree qTree )
  {
    if (qTree == null || qTree.getOperator() == null)
    {
      LOG.error( "Cannot create IndexMatcher: qTree or Operator is NULL!" );
      return null;
    }
		
    String op = qTree.getOperator( );

    IIndexMatcher ndxMatcher = null;
    LOG.debug( "createIndexMatcher '" + op + "'" );
    if (op.equals( "WORD" ) || op.equals( QueryTree.TERM ))
    {
      String queryText = qTree.getQueryText( );
      if (queryText != null && queryText.indexOf( " " ) > 0)
      {
        ndxMatcher = new PhraseIndexMatcher( qTree );
      }
      else if (queryText != null)
      {
        LOG.debug( "Creating matcher for: " + queryText );
				
        TermIndexMatcher termIndexMatcher = new TermIndexMatcher( );
        termIndexMatcher.initialize( qTree );
        ndxMatcher = termIndexMatcher;
      }
    }
    else if (op.equalsIgnoreCase( "PHRASE" ))
    {
      ndxMatcher = new PhraseIndexMatcher( qTree );
    }
    else if (op.equalsIgnoreCase( "AND" ) || op.equalsIgnoreCase( "ALL" ))
    {
      ndxMatcher = new AndIndexMatcher( qTree );
    }
    else if (op.equalsIgnoreCase( "OR" ) || op.equalsIgnoreCase( "ANY" ))
    {
      ndxMatcher =  new OrIndexMatcher( qTree );
    }
    else if (op.equalsIgnoreCase( "NOT" ))
    {
      NotIndexMatcher notMatcher = new NotIndexMatcher(  );
      notMatcher.initialize( qTree );
      ndxMatcher = notMatcher;
    }
    else if (op.equalsIgnoreCase( "NEAR" ) || op.equalsIgnoreCase( "ONEAR" ))
    {
      ndxMatcher = new NearIndexMatcher( qTree );
    }
		
    if (ndxMatcher != null && qTree.getName() != null)
    {
      ndxMatcher.setName( qTree.getName( ) );
    }
		
    return ndxMatcher;
  }
}
