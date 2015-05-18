package com.modinfodesigns.test.ontology;

import com.modinfodesigns.app.ObjectFactoryCreator;

import com.modinfodesigns.logging.LoggingManager;
import com.modinfodesigns.ontology.transform.FacetCollectorTransform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.transform.PropertyTransformException;

public class TestFacetCollectorTransform
{
    private static String configFile = "C:/Projects/Prometheus/TestSearchApplications/ObjectFactoryConfig.xml";

    private static String taxoBuilderName = "SoftwareConceptsTaxonomyBuilder";
    
	public static void main( String[] args )
	{
		LoggingManager.addDebugClass( "com.modinfodesigns.ontology.FacetCollectorTransform" );
		
		ObjectFactoryCreator.initialize(  configFile );
		
		FacetCollectorTransform fct = new FacetCollectorTransform( );
		fct.setTaxonomyBuilderRef( taxoBuilderName );
		
		DataObject dobj = new DataObject( );
		dobj.addProperty( new StringProperty( "Path", "/Software Concepts/Computer Languages/Programming Languages/Java" ));
		dobj.addProperty( new StringProperty( "Path", "/Software Concepts/Object Oriented Programming/Anonymous Functions" ));
		dobj.addProperty( new StringProperty( "Path", "/Software Concepts/Anti Patterns/Lava Flow" ));
		dobj.addProperty( new StringProperty( "Path", "/Software Concepts/Functional Programming/Closure" ));
		
		try
		{
			IPropertyHolder output = fct.transformPropertyHolder( dobj );
			System.out.println( output.getValue( ) );
		}
		catch ( PropertyTransformException pte )
		{
			
		}
	}

}
