package com.modinfodesigns.classify;

import java.util.Iterator;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.IObjectFactory;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.PropertyTransformException;

import com.modinfodesigns.utils.FileMethods;

import junit.framework.TestCase;

/**
 * Tests configuration of a classifier with an IIndexMatcherFactory.
 * 
 * @author Ted Sullivan
 */
public class TestConfiguredClassifier extends TestCase
{
  // Need to get these from PC:
    
	private static String configurationFile="G:/Projects/Prometheus/Testing/Pipelines/TestClassifierConfiguration/TestClassifierConfiguration.xml";
	private static String sampleTextFile="G:/Projects/Prometheus/Testing/Pipelines/TestClassifierConfiguration/Sample Text.txt";
    private static String modInfoClass = "com.modinfodesigns.app.ModInfoObjectFactory";

  public void testConfiguredClassifier( ) throws IndexMatcherException, PropertyTransformException
  {
    ApplicationManager appManager = ApplicationManager.getInstance( );
         
    String config = FileMethods.readFile( configurationFile );
    System.out.println( "Got Configuration: " + config );
        
    IObjectFactory objFactory = appManager.createObjectFactory( "MyObjectFactory", modInfoClass, config );

    Classifier classifier = (Classifier)objFactory.getApplicationObject( "Classifier", "DataTransform" );
        
    DataObject testObj = new DataObject( );
    testObj.addProperty( new StringProperty( "body", FileMethods.readFile( sampleTextFile ) ) );
    testObj.setUniquePropertiesOnly( true );
            
    classifier.addClassifyField( "body" );
        
    classifier.transformPropertyHolder( testObj );
        
    System.out.println( "\r\n Post classification properties: ");
    for (Iterator<IProperty> propIt = testObj.getProperties(); propIt.hasNext(); )
    {
      IProperty prop = propIt.next();
      System.out.println( prop.getName() + ":" + prop.getValue( ) );
    }
  }

}
