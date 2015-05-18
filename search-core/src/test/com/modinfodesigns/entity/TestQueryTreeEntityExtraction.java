package com.modinfodesigns.entity;

import com.modinfodesigns.search.Query;
import com.modinfodesigns.search.QueryTree;
import com.modinfodesigns.search.QueryField;
import com.modinfodesigns.search.QueryFieldOperator;

import com.modinfodesigns.property.quantity.IntegerProperty;

import com.modinfodesigns.classify.IIndexMatcher;
import com.modinfodesigns.entity.IndexMatcherEntityExtractor;
import com.modinfodesigns.entity.tagging.EntityTaggerStringTransform;

import com.modinfodesigns.property.transform.string.StringTransformException;

import junit.framework.TestCase;

/**
 * Unit test of Entity Extraction and Tagging using a User Query and QueryMode.  Query is converted
 * to a QueryTree using the Query.convertToQueryTree method. The QueryTree is then used to create
 * an IIndexMatcher and a IndexMatcherEntityExtractor for use in advanced query term highlighting.
 * 
 * @author Ted Sullivan
 */
public class TestQueryTreeEntityExtraction extends TestCase
{

  public void testQueryTreeEntityExtraction( ) throws StringTransformException
  {
    Query query = new Query( );
    QueryField defaultField = new QueryField( "query", "relevant content" );
    query.setDefaultQueryField( defaultField );
    QueryFieldOperator qOp = new QueryFieldOperator( "NEAR" );
    qOp.addModifier( new IntegerProperty( "DISTANCE", 5 ));
    defaultField.setQueryFieldOperator( qOp );
        
    QueryTree qTree = query.convertToQueryTree( );
    IIndexMatcher ndxMatcher = qTree.getIndexMatcher( );
    IndexMatcherEntityExtractor imee = new IndexMatcherEntityExtractor( ndxMatcher );
    EntityTaggerStringTransform etst = new EntityTaggerStringTransform( imee );
        
    String input = SEARCH_BLURB;
    String output = etst.transformString( input );
    System.out.println( output );
        
    query = new Query( );
    defaultField = new QueryField( "query", "search technology" );
    query.setDefaultQueryField( defaultField );
    qOp = new QueryFieldOperator( "NEAR" );
    qOp.addModifier( new IntegerProperty( "DISTANCE", 5 ));
    defaultField.setQueryFieldOperator( qOp );
        
    qTree = query.convertToQueryTree( );
    ndxMatcher = qTree.getIndexMatcher( );
    imee = new IndexMatcherEntityExtractor( ndxMatcher );
    etst = new EntityTaggerStringTransform( imee );
        
    input = SEARCH_BLURB_2;
    output = etst.transformString( input );
    System.out.println( output );
  }
	
  private static final String SEARCH_BLURB = "The second phase of the investigation seeks to understand the information "
                                           + "content that users are or will be searching. The goal is to determine both "
                                           + "what is the most relevant content and what if any are the salient features "
                                           + "of this content that make it relevant";
	
  private static final String SEARCH_BLURB_2 = "Put simply, this is one major reason for the general failure of "
                                             + "Search technology on the web - the search engine answers the wrong "
                                             + "question because it is answering the only question that it knows how to.";

}
