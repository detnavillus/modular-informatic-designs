package com.modinfodesigns.entity;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.classify.IIndexMatcherFactory;
import com.modinfodesigns.classify.InvertedIndex;
import com.modinfodesigns.classify.OrIndexMatcher;
import com.modinfodesigns.classify.MatchStatistics;
import com.modinfodesigns.classify.IndexMatcherException;

import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses an IIndexMatcher as an EntityExtractor. If instantiated with an
 * IIndexMatcherFactory - will create an OrIndexMatcher for entity extraction.
 * 
 * @author Ted Sullivan
 */

public class IndexMatcherEntityExtractor implements IEntityExtractor
{
  private transient static final Logger LOG = LoggerFactory.getLogger( IndexMatcherEntityExtractor.class );

  private IIndexMatcher indexMatcher;
    
  private IIndexMatcherFactory indexMatcherFactory;
    
  private String prefixString;
    
  private String postfixString;
    
  public IndexMatcherEntityExtractor(  ) {  }
    
  public IndexMatcherEntityExtractor( IIndexMatcher indexMatcher )
  {
    this.indexMatcher = indexMatcher;
  }
    
  @Override
  public EntityPositionMap extractEntities( String field, String fromString )
  {
    if (fromString == null) return null;
    	
    IIndexMatcher ndxMatcher = getIndexMatcher( );
    if (ndxMatcher == null) return null;
        
    String useField = (field != null) ? field : "field";
        
    try
    {
      InvertedIndex invIndex = new InvertedIndex( );
      invIndex.tokenize( useField, fromString );
      MatchStatistics matchStats = ndxMatcher.getMatchStatistics( invIndex );
      if (matchStats != null && matchStats.matches(  ))
      {
        HashMap<String,EntityPositionMap> entityPositionMaps = matchStats.getEntityPositionMaps( );
        if (prefixString != null && postfixString != null && entityPositionMaps != null)
        {
          for (Iterator<EntityPositionMap> mapIt = entityPositionMaps.values().iterator(); mapIt.hasNext(); )
          {
            EntityPositionMap ePositionMap = mapIt.next( );
            ePositionMap.setPrefixString( prefixString );
            ePositionMap.setPostfixString( postfixString );
          }
        }
                
        return (entityPositionMaps != null) ? entityPositionMaps.get( useField ) : null;
      }
    }
    catch (IndexMatcherException ime )
    {
      LOG.error( "IndexMatcherEntityExtractor got Exception: " + ime );
    }
        
    return null;
  }
    
  private IIndexMatcher getIndexMatcher(  )
  {
    if (indexMatcher != null)
    {
      return indexMatcher;
    }
        
    if (indexMatcherFactory != null)
    {
      IIndexMatcher[] indexMatchers = indexMatcherFactory.createIndexMatchers( );
      if (indexMatchers != null && indexMatchers.length > 0)
      {
        OrIndexMatcher orMatcher = new OrIndexMatcher( );
        for (int i = 0; i < indexMatchers.length; i++)
        {
          orMatcher.addIndexMatcher( indexMatchers[i] );
        }
                
        this.indexMatcher = orMatcher;
      }
    }
        
    return this.indexMatcher;
  }
    
  public void setIndexMatcher( IIndexMatcher matcher )
  {
    this.indexMatcher = matcher;
  }
    
  public void setIndexMatcherFactory( IIndexMatcherFactory indexMatcherFactory )
  {
    if (indexMatcher != null)
    {
      LOG.error( "Already have IndexMatcher - IndexMatcherFactory will not be used!" );
      return;
    }
        
    this.indexMatcherFactory = indexMatcherFactory;
  }

  @Override
  public String getPrefixString()
  {
    return this.prefixString;
  }
    
  public void setPrefixString( String prefixString )
  {
    this.prefixString = prefixString;
  }

  @Override
  public String getPostfixString()
  {
    return this.postfixString;
  }
    
  public void setPostfixString( String postfixString )
  {
    this.postfixString = postfixString;
  }

}
