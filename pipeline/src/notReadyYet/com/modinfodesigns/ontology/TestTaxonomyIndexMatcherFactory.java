package com.modinfodesigns.ontology;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.ObjectFactoryCreator;

import com.modinfodesigns.ontology.TaxonomyIndexMatcherFactory;

import com.modinfodesigns.classify.Classifier;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.utils.FileMethods;

import junit.framework.TestCase;

// Needs to use ITaxonomyBuilder ---

public class TestTaxonomyIndexMatcherFactory extends TestCase
{
  private static String textFile   = "resources/TestTaxonomyClassification/TestData/TestFile.txt";
  private static String configFile = "resources/TestTaxonomyClassification/ObjectFactoryCreator.xml";
    
  public void testTaxonomyIndexMatcherFactory( ) throws PropertyTransformException
  {
    ObjectFactoryCreator.initialize( configFile );
        
    ApplicationManager appMan = ApplicationManager.getInstance( );
    TaxonomyIndexMatcherFactory timf = (TaxonomyIndexMatcherFactory)appMan.getApplicationObject( "SoftwareConceptsIndexMatcherFactory", "IndexMatcherFactory" );
        
    Classifier classifier = new Classifier( );
    classifier.addIndexMatcherFactory( timf );
    classifier.addClassifyField( "text" );
        
    // Create a DataObject and Classify it...
    DataObject dobj = new DataObject( );
    String fileText = FileMethods.readFile( textFile );
    dobj.addProperty( new StringProperty( "text", fileText ));
        
    classifier.transformPropertyHolder( dobj );
        
    System.out.println( dobj.getValue( IProperty.XML_FORMAT ));
  }
}
