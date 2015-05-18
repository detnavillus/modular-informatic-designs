package com.modinfodesigns.property.schema;

import java.util.List;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.ObjectFactoryCreator;

import junit.framework.TestCase;

public class TestSchemaTemplate extends TestCase
{
  private static String configFile = "ObjectFactoryCreator.xml";

  public void testSchemaTemplate( )
  {
    /*ObjectFactoryCreator.initialize(  configFile );
		
    ApplicationManager appMan = ApplicationManager.getInstance( );
    DataSchemaTemplate dst = (DataSchemaTemplate)appMan.getApplicationObject( "TestSchemaTemplate", "DataSchemaTemplate" );
    if (dst != null)
    {
        System.out.println( dst.getValue( ) );
    }
		
    List<Object> dsts = appMan.getApplicationObjects( "DataSchemaTemplate" );
    if (dsts != null)
    {
      System.out.println( "Got " + dsts.size( ) + " schema templates!" );
      for (int i = 0; i < dsts.size( ); i++)
      {
        dst = (DataSchemaTemplate)dsts.get( i );
				
        System.out.println( "Adding template name: " + dst.getTemplateName( ) );
      }
    }*/
  }

}
