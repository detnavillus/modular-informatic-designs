package com.modinfodesigns.classify;

import com.modinfodesigns.property.PropertyList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.string.StringTransform;

import com.modinfodesigns.utils.FileMethods;
import com.modinfodesigns.utils.DOMMethods;

import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Probably should cache the IIndexMatcher array so that
// Subject Matchers can get a specific 
/**
 * Builds a Set of IIndexMatchers on a set of Thesaurus Nodes (defined in XML).
 * 
 * @author Ted Sullivan
 */
public class ThesaurusIndexMatcherFactory implements IIndexMatcherFactory
{
  private transient static final Logger LOG = LoggerFactory.getLogger( ThesaurusIndexMatcherFactory.class );
	
  private ArrayList<String> thesaurusDirectories;
  private String classificationPathAttribute  = "classificationPath";
  private String semanticTypeAttribute = "semanticType";

  private String termAttribute;
  private String synonymAttribute;
    
  private int numAncestors = 0;
    
  private ArrayList<IProperty> tagProperties;

  public void setThesaurusDirectories( ArrayList<String> thesaurusDirectories )
  {
    this.thesaurusDirectories = thesaurusDirectories;
  }
    
  public void addThesaurusDirectory( String thesaurusDirectory )
  {
    if (this.thesaurusDirectories == null) this.thesaurusDirectories = new ArrayList<String>( );
    thesaurusDirectories.add( thesaurusDirectory );
  }
    
  public void setTermAttribute( String termAttribute )
  {
    this.termAttribute = termAttribute;
  }
    
  public void setSynonymAttribute( String synonymAttribute )
  {
    this.synonymAttribute = synonymAttribute;
  }
    
  public void setNumberAncestors( String numAncestors )
  {
    this.numAncestors = Integer.parseInt( numAncestors );
  }
    
  public void setClassificationPathAttribute( String classificationPathAttribute )
  {
    this.classificationPathAttribute = classificationPathAttribute;
  }
    
  public void setSemanticTypeAttribute( String semanticTypeAttribute )
  {
    this.semanticTypeAttribute = semanticTypeAttribute;
  }
    
  public void addTagProperties( PropertyList tagPropertyList )
  {
    if (tagProperties == null) tagProperties = new ArrayList<IProperty>( );
    
    for (Iterator<IProperty> propIt = tagPropertyList.getProperties(); propIt.hasNext(); )
    {
      IProperty prop = propIt.next();
      tagProperties.add( prop.copy( ) );
    }
  }
    
  @Override
  public IIndexMatcher[] createIndexMatchers()
  {
    ArrayList<IIndexMatcher> matchList = new ArrayList<IIndexMatcher>( );
    for (int i = 0; i < thesaurusDirectories.size( ); i++)
    {
      String thesaurusDirectory = thesaurusDirectories.get( i );
      LOG.debug( "Loading: " + thesaurusDirectory );
      String[] files = FileMethods.getFileList( thesaurusDirectory, true );

      for (int f = 0; f < files.length; f++)
      {
        // LOG.debug( "createDocumentMatcher: " + files[f] );
        matchList.add( createIndexMatcher( files[f] ) );
      }
    }

    IIndexMatcher[] matchers = new IIndexMatcher[ matchList.size( ) ];
    matchList.toArray( matchers );

    LOG.debug( "Created " + matchers.length + " matchers." );

    return matchers;
  }
	
  private IIndexMatcher createIndexMatcher( String fileName )
  {
    Document doc = DOMMethods.getDocument( fileName );
    Element docEl = doc.getDocumentElement( );

    OrIndexMatcher compositeMatcher = new OrIndexMatcher(  );
    NodeList termList = docEl.getElementsByTagName( "Term" );
    if (termList != null && termList.getLength() > 0)
    {
      Element termEl = (Element)termList.item( 0 );
      String term = DOMMethods.getText( termEl );
      LOG.debug( "processing term '" + term + "'" );
      compositeMatcher.setName( term );
                
      if (term.indexOf( " " ) > 0)
      {
        compositeMatcher.addIndexMatcher( new PhraseIndexMatcher( term ) );
      }
      else
      {
        compositeMatcher.addIndexMatcher( new TermIndexMatcher( term ) );
      }

      if (termAttribute != null)
      {
        compositeMatcher.addProperty( new StringProperty( termAttribute, term ) );
      }

      if (synonymAttribute != null)
      {
        compositeMatcher.addProperty( new StringProperty( synonymAttribute, term ) );
      }
                
      if (tagProperties != null)
      {
        for (Iterator<IProperty> propIt = tagProperties.iterator(); propIt.hasNext(); )
        {
          compositeMatcher.addProperty( propIt.next() );
        }
      }
    }

    NodeList synonymList = docEl.getElementsByTagName( "Synonym" );
    if (synonymList != null && synonymList.getLength() > 0)
    {
      for (int i = 0; i < synonymList.getLength(); i++)
      {
        Element synEl = (Element)synonymList.item( i );
        String synonym = DOMMethods.getText( synEl );
        if (synonym.indexOf( " " ) > 0)
        {
          compositeMatcher.addIndexMatcher( new PhraseIndexMatcher( synonym ) );
        }
        else
        {
          compositeMatcher.addIndexMatcher( new TermIndexMatcher( synonym ) );
        }

        if (synonymAttribute != null)
        {
          compositeMatcher.addProperty( new StringProperty( synonymAttribute, synonym ) );
        }
      }
    }

    if ( classificationPathAttribute != null )
    {
      NodeList pathList = docEl.getElementsByTagName( "ClassificationPath" );
      if (pathList != null && pathList.getLength() > 0)
      {
        for (int i = 0; i < pathList.getLength(); i++)
        {
          Element classEl = (Element)pathList.item( i );
          String classificationPath = DOMMethods.getText( classEl );
            
          if (numAncestors > 0)
          {
            classificationPath = getTruncatedPath( classificationPath, numAncestors );
          }

          compositeMatcher.addProperty( new StringProperty( classificationPathAttribute, classificationPath ) );
        }
      }
    }

    if ( semanticTypeAttribute != null )
    {
      NodeList semanticLst = docEl.getElementsByTagName( "SemanticType" );
      if (semanticLst != null && semanticLst.getLength() > 0)
      {
        for (int i = 0; i < semanticLst.getLength(); i++)
        {
          Element semanticEl = (Element)semanticLst.item( i );
          String semanticType = DOMMethods.getText( semanticEl );
          compositeMatcher.addProperty( new StringProperty( semanticTypeAttribute, semanticType ) );
        }
      }
    }

    return compositeMatcher;
  }
	
  private String getTruncatedPath( String path, int numAncestors )
  {
    String[] pathComponents = StringTransform.getStringArray( path, "/" );
    if ((numAncestors + 1) >= pathComponents.length) return path;
      
    StringBuffer strbuf = new StringBuffer( );
    for (int i = (pathComponents.length - numAncestors - 1); i < pathComponents.length; i++)
    {
      strbuf.append( pathComponents[i] );
      if (i < (pathComponents.length - 1))
      {
        strbuf.append( "/" );
      }
    }

    return strbuf.toString( );
  }

}
