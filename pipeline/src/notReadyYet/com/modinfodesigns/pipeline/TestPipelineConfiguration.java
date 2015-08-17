package com.modinfodesigns.objectFactory;

import java.util.List;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.IObjectFactory;

import com.modinfodesigns.utils.FileMethods;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;

import junit.framework.TestCase;

public class TestPipelineConfiguration extends TestCase
{
  private static String configurationFile = "C:/Projects/Prometheus/TestConfigurationFiles/TestPipelineConfiguration.xml";
  private static String modInfoClass = "com.modinfodesigns.app.ModInfoObjectFactory";

  public void testPipelineConfiguration( )
  {
    ApplicationManager appManager = ApplicationManager.getInstance( );
        
    String configXML = FileMethods.readFile( configurationFile );
    System.out.println( "Got Configuration: " + configXML );
        
    IObjectFactory objFactory = appManager.createObjectFactory( "MyObjectFactory", modInfoClass, configXML );

    List<Object> dataProcessors = objFactory.getApplicationObjects( "DataProcessor" );
    if ( dataProcessors != null)
    {
      System.out.println( "Got DataProcessors!" );
      for (int i = 0; i < dataProcessors.size(); i++)
      {
        IDataObjectProcessor dataProc = (IDataObjectProcessor)dataProcessors.get( i );
        System.out.println( "   " + dataProc );
      }
    }
    else
    {
      fail( "No DataProcessors in sight!" );
    }
  }

}
