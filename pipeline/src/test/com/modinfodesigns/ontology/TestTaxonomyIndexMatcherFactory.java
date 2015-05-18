package com.modinfodesigns.test.ontology;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.ObjectFactoryCreator;

import com.modinfodesigns.ontology.TaxonomyIndexMatcherFactory;

import com.modinfodesigns.classify.Classifier;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.utils.FileMethods;

import com.modinfodesigns.logging.LoggingManager;

// Needs to use ITaxonomyBuilder ---

public class TestTaxonomyIndexMatcherFactory
{
    private static String textFile   = "C:/Projects/Prometheus/TestTaxonomyClassification/TestData/TestFile.txt";
    private static String configFile = "C:/Projects/Prometheus/TestTaxonomyClassification/ObjectFactoryCreator.xml";
    
	public static void main( String[] args )
	{
		// LoggingManager.addDebugClass( "com.modinfodesigns.ontology.TaxonomyIndexMatcherFactory" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.ontology.DataListTaxonomyBuilder" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.pipeline.source.RaritanTreeSource" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.classify.NotIndexMatcher" );
		LoggingManager.addDebugClass( "com.modinfodesigns.classify.PhraseIndexMatcher" );
		LoggingManager.addDebugClass( "com.modinfodesigns.classify.TermIndexMatcher" );
		LoggingManager.addDebugClass( "com.modinfodesigns.classify.AcronymIndexMatcher" );	
		
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
        
        try
        {
        	classifier.transformPropertyHolder( dobj );
        }
        catch ( PropertyTransformException pte )
        {
        	LoggingManager.error( TestTaxonomyIndexMatcherFactory.class, "Got PropertyTransformException" + pte );
        }
        
        System.out.println( dobj.getValue( IProperty.XML_FORMAT ));
	}
}
