package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.TemplateFunctionProperty;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.string.StringProperty;

import junit.framework.TestCase;

/**
 * Tests the PropertyTemplateTransform and the associated IFunctionProperty TemplateFunctionProperty.
 * 
 * @author Ted Sullivan
 */

public class TestPropertyTemplateTransform extends TestCase
{
	
  public void testPropertyTemplateTransform( ) throws PropertyTransformException
  {
    PropertyTemplateTransform ptt = new PropertyTemplateTransform( );
    ptt.setTransformTemplate( theTemplate );
    ptt.setOutputProperty( "transformed" );
        
    DataObject dobj = new DataObject( );
    dobj.setProperty( new StringProperty( "system", "Cable News Network" ));
    dobj.setProperty( new StringProperty( "condition", "hysterical" ));
        
    ptt.transformPropertyHolder( dobj );
    System.out.println( dobj.getValue( ) );
        
    TemplateFunctionProperty tfp = new TemplateFunctionProperty( );
    tfp.setName( "formletter" );
    tfp.setFunction( theTemplate );
    dobj.setProperty( tfp );
      
    assertEquals( tfp.getValue( ), "This is a test of the Cable News Network. Shouldn't you be hysterical now?" );
        
    dobj.setProperty( new StringProperty( "condition", "laughing" ) );
    dobj.setProperty( new StringProperty( "system", "Daily Show" ));
    assertEquals( tfp.getValue( ), "This is a test of the Daily Show. Shouldn't you be laughing now?" );
  }
	
  private static String theTemplate = "This is a test of the {system}. Shouldn't you be {condition} now?";

}
