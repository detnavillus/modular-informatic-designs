package com.modinfodesigns.app;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.IObjectFactory;

import com.modinfodesigns.utils.FileMethods;

import com.modinfodesigns.property.transform.IPropertyHolderTransform;

import junit.framework.TestCase;

public class TestObjectFactory extends TestCase
{
  private static String configurationFile = "TestConfiguration.xml";
  private static String modInfoClass = "com.modinfodesigns.app.ModInfoObjectFactory";
    
  public void testObjectFactory( )
  {
    ApplicationManager appManager = ApplicationManager.getInstance( );
        
    String configXML = FileMethods.readFile( configurationFile );
    System.out.println( "Got Configuration: " + configXML );
        
    IObjectFactory objFactory = appManager.createObjectFactory( "MyObjectFactory", modInfoClass, configXML );
        
    IPropertyHolderTransform copyXform = (IPropertyHolderTransform)objFactory.getApplicationObject( "FooBarCopy", "Transform" );
        
    System.out.println( "Got Transform:" + copyXform );
  }

}
