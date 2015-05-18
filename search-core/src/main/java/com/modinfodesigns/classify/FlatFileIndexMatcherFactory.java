package com.modinfodesigns.classify;

import com.modinfodesigns.utils.FileMethods;
import com.modinfodesigns.utils.StringMethods;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.PropertyList;

import java.util.ArrayList;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a set of IIndexMatchers from a flat file. Each line in the file can be a term
 * or phrase from which an IIndexMatcher will be created.
 * 
 * @author Ted Sullivan
 */


public class FlatFileIndexMatcherFactory implements IIndexMatcherFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( FlatFileIndexMatcherFactory.class );

  private String fileName;
    
  private String termAttribute = "keyterm";
    
  private PropertyList globalProperties;
    
  private String delimiter = "|";
    
  private String name;
    
  public void setName( String name )
  {
    this.name = name;
  }
    
  public void setFileName( String fileName )
  {
    this.fileName = fileName;
  }
    
  public void setTermAttribute( String termAttribute )
  {
    this.termAttribute = termAttribute;
  }
    
  public void setGlobalProperties( PropertyList globalProperties )
  {
    this.globalProperties = globalProperties;
  }
    
  public void setDelimiter( String delimiter )
  {
    this.delimiter = delimiter;
  }

  @Override
  public IIndexMatcher[] createIndexMatchers()
  {
    LOG.debug( "createIndexMatchers( )..." );
    	
    ArrayList<IIndexMatcher> matchers = new ArrayList<IIndexMatcher>( );
        
    if (fileName != null)
    {
      String[] filelines = FileMethods.readFileLines( fileName );
      if (filelines != null)
      {
        LOG.debug( "got " + filelines.length + " lines." );
            	
        for( int i = 0; i < filelines.length; i++)
        {
          LOG.debug( "Parsing: " + filelines[i] );
                	
          if (filelines[i].indexOf( delimiter ) > 0 )
          {
            LOG.debug( "Is delimited" );
                		
            String[] terms = StringMethods.getStringArray( filelines[i], delimiter);
            OrIndexMatcher orMatcher = new OrIndexMatcher( );
            addGlobalProperties( orMatcher );
            for ( int t = 0; t < terms.length; t++ )
            {
              IIndexMatcher subMatcher = createMatcher( terms[t] );
              orMatcher.addIndexMatcher( subMatcher );
            }
            orMatcher.addProperty( new StringProperty( termAttribute, terms[0] ) );
            addGlobalProperties( orMatcher );
            matchers.add( orMatcher );
          }
          else
          {
            IIndexMatcher matcher  = createMatcher( filelines[i] );
            matcher.addProperty( new StringProperty( termAttribute, filelines[i] ) );
            addGlobalProperties( matcher );
            matchers.add( matcher );
          }
        }
      }
    }
        
    IIndexMatcher[] indexMatchers = new IIndexMatcher[ matchers.size( ) ];
    matchers.toArray( indexMatchers );
    return indexMatchers;
  }
    
  private IIndexMatcher createMatcher( String term )
  {
    LOG.debug( "createMatcher: '" + term + "'" );
    String[] tokens = StringMethods.getStringArray( term,  InvertedIndex.tokenDelimiter );
    	
    if (tokens.length > 1)
    {
      return new PhraseIndexMatcher( term );
    }
    else
    {
      return new TermIndexMatcher( term );
    }
  }
    
  private void addGlobalProperties( IIndexMatcher toMatcher )
  {
    if (globalProperties != null)
    {
      for (Iterator<IProperty> propIt = globalProperties.getProperties(); propIt.hasNext(); )
      {
        IProperty globalProp = propIt.next();
        toMatcher.addProperty( globalProp.copy( ));
      }
    }
  }
}
