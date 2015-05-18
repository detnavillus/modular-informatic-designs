package com.modinfodesigns.test.ontology.transform;

import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.ontology.transform.TaxonomyPathTransform;

public class TestTaxonomyPathTransform 
{


	public static void main(String[] args) 
	{
		TaxonomyPathTransform tpt = new TaxonomyPathTransform( );
		tpt.setPathProperty( "paths" );
		
		DataObject dobj = new DataObject( );
		dobj.addProperty( new StringProperty( "paths", "/root/quips/groucho marx" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/henny youngman" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/groucho marx/love my wife" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/groucho marx/love my wife/love my cigar too" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/groucho marx/chico/join a club/beat you with it" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/henny youngman/take my wife" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/henny youngman/take my wife/please" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/ogden nash/the bronx" ));
		dobj.addProperty( new StringProperty( "paths", "/root/quips/ogden nash/the bronx/no thonx" ));
		
		try
		{
			IPropertyHolder output = tpt.transformPropertyHolder( dobj );
			System.out.println( "got output = " + output );
			System.out.println( output.getValue() );
			
			System.out.println( "\n --------------------- XML -------------------- " );
			System.out.println( output.getValue( IProperty.XML_FORMAT ) );
		}
		catch( PropertyTransformException pte )
		{
			System.out.println( "Got PropertyTransformException: " + pte );
		}
		
	}

}
