package com.modinfodesigns.network.http;

import com.modinfodesigns.network.http.URLPropertyTransform;

import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.transform.PropertyTransformException;

import junit.framework.TestCase;

public class TestURLPropertyTransform extends TestCase
{

  public void testURLPropertyTransform( ) throws PropertyTransformException
  {
    String url = "http://www.google.com";
		
    StringProperty urlProp = new StringProperty( "URL", url );
    URLPropertyTransform urlPropTransform = new URLPropertyTransform( );
		
    IProperty contentProp = urlPropTransform.transform( urlProp );
  }

}
