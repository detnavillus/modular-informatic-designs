package com.modinfodesigns.entity;

import com.modinfodesigns.entity.tagging.SnippetingPropertyTransform;
import com.modinfodesigns.entity.TermListEntityExtractor;
import com.modinfodesigns.entity.IndexMatcherEntityExtractor;
import com.modinfodesigns.classify.TermIndexMatcher;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.pipeline.source.XMLDataSource;
import com.modinfodesigns.pipeline.process.IDataObjectProcessor;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Tests the SnippetingPropertyTransform using a input text file and a file containing a list
 * of terms to highlight. Uses a TermListEntityExtractor.
 * 
 * @author Ted Sullivan
 */

public class TestSnippetingPropertyTransform extends TestCase
{
  private static String XMLFiles = "C:/Projects/Prometheus/TestFiles";
  private static String TermsFile = "C:/Projects/Prometheus/TestFiles/TermList.txt";
	
  public void testSnippetingPropertyTransform( ) throws Exception
  {
    XMLDataSource xmlSource = new XMLDataSource( );
    xmlSource.setXmlFilePath( XMLFiles );
    xmlSource.setRecordTag( "quotes" );
    TestDataGatherer dataGatherer = new TestDataGatherer( TermsFile );
        
    xmlSource.addDataObjectProcessor( dataGatherer );
        
    xmlSource.run( );
        
    for (Iterator<DataObject> outIt = dataGatherer.outputList.getData(); outIt.hasNext(); )
    {
      DataObject outObj = outIt.next( );
      System.out.println( outObj.getValue( ) );
    }
  }
	
  static class TestDataGatherer implements IDataObjectProcessor
  {
    SnippetingPropertyTransform spt;
    IDataList outputList;
		
    TestDataGatherer( String termsFile )
    {
      spt = new SnippetingPropertyTransform( );
			
      TermListEntityExtractor tlee = new TermListEntityExtractor( );
      tlee.loadFromFile( termsFile );
      tlee.setPrefixString( "<em>" );
      tlee.setPostfixString( "</em>" );
      spt.addEntityExtractor( tlee );
			
      TermIndexMatcher tim = new TermIndexMatcher( "Estella" );
      IndexMatcherEntityExtractor imee = new IndexMatcherEntityExtractor( );
      imee.setIndexMatcher( tim );
      imee.setPrefixString( "<span class='foo'>" );
      imee.setPostfixString( "</span>" );
      spt.addEntityExtractor( imee );
			
      spt.setInputProperty( "quote" );
      spt.setOutputProperty( "snippets" );
      spt.setPadding( 80 );
    }

    @Override
    public IDataList processDataList( IDataList data )
    {
      outputList = data;
      for (Iterator<DataObject> dobjIt = data.getData(); dobjIt.hasNext(); )
      {
        DataObject dobj = dobjIt.next( );
        System.out.println( dobj.getValue( ) );
        try
        {
          spt.transformPropertyHolder( dobj );
        }
        catch ( PropertyTransformException pte )
        {
            		
        }
      }
            
      return data;
    }

    @Override
    public void processComplete( IPropertyHolder result, boolean status )
    {
			
    }
  }
}
