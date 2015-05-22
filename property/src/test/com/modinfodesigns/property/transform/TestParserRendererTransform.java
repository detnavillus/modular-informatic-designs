package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.transform.xml.XMLParserTransform;
import com.modinfodesigns.property.transform.string.PropertyRendererTransform;
import com.modinfodesigns.property.string.StringProperty;

import junit.framework.TestCase;

/**
 * Tests Parser and Renderer Transforms - Parser transforms take a String and translate to
 * some type of IProperty or IPropertyHolder.
 * 
 * @author Ted Sullivan
 */
public class TestParserRendererTransform extends TestCase
{

  public void testParserRendererTransform( ) throws PropertyTransformException
  {
    // =================================================================================
    // Example 1: Transform an StringProperty containing an XML String
    // to a DataObject using a XMLParserTransform - transforms XML String to DataObject
    // PropertyRendererTransform - transforms the DataObject to a StringProperty
    // using some transform renderer or specified format.
    // =================================================================================
    StringProperty strProp = new StringProperty( "foo", "<Object type=\"bar\" ><Child type=\"kid\" ><Grandkid type=\"gkid\" /></Child><Kid type=\"goat\" /><Child bad=\"none\" /></Object>" );
		
    XMLParserTransform xmlParserXform = new XMLParserTransform( );
	IProperty dobj = xmlParserXform.transform( strProp );
			
    assertTrue( dobj instanceof DataObject );

    PropertyRendererTransform propRendererXform = new PropertyRendererTransform( );
    propRendererXform.setFormat( "JSON" );
    IProperty output = propRendererXform.transform( dobj );
    assertTrue( output instanceof StringProperty );

    //assertEquals( output.getValue( ), "{\"Kid\":{\"type\":\"goat\"},\"type\":\"bar\",\"Child\":\"[{\"Grandkid\":{\"type\":\"gkid\"},\"type\":\"kid\"},{\"bad\":\"none\"}]\"}" );
  }

}
