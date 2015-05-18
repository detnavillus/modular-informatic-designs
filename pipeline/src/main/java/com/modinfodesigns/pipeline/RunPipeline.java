package com.modinfodesigns.pipeline;

import com.modinfodesigns.app.ApplicationManager;
import com.modinfodesigns.app.IObjectFactory;

import com.modinfodesigns.utils.FileMethods;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.pipeline.source.IDataObjectSource;

import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes a Data Processing Pipeline. 
 * Usage:
 * 
 * java com.modinfodesigns.application.RunPipeline <Pipeline Configuration File> <Data Source Name> <Object Factory Class name (optional)>
 * 
 * If Object Factory Class is not specified - com.modinfodesigns.app.ModInfoObjectFactory will be used.
 * 
 * @author Ted Sullivan
 */

public class RunPipeline
{
  private transient static final Logger LOG = LoggerFactory.getLogger( RunPipeline.class );

  public static void main( String[] args )
  {
    if (args.length == 0)
    {
      System.out.println( "Usage: java com.modinfodesigns.application.RunPipeline <Pipeline Configuration> <Data Source Name> <FactoryType (optional)>" );
      return;
    }
		
    ApplicationManager appMan = ApplicationManager.getInstance( );

    // create an IObjectFactory
    String factoryClass = "com.modinfodesigns.app.ModInfoObjectFactory";
    if (args.length == 3)
    {
      factoryClass = args[2];
    }
        
    String configDoc = getConfigurationDocument( args[0] );
    LOG.debug( "Got configDoc = " + configDoc );
        
    IObjectFactory objFac = appMan.createObjectFactory( "RunPipeline", factoryClass, configDoc );
        
    if (objFac != null)
    {
      runPipeline( args[1] );
    }
    else
    {
      LOG.error( "Could not create Object Factory!" );
    }
  }
	
  public static void runPipeline( String dataSourceName )
  {
    ApplicationManager appMan = ApplicationManager.getInstance( );
		
    IDataObjectSource dataSource = (IDataObjectSource)appMan.getApplicationObject( dataSourceName, "DataSource" );
    LOG.debug( "Got DataSource = " + dataSource );
    
    if (dataSource != null)
    {
      List<Object> dataProcessors = appMan.getApplicationObjects( "DataProcessor" );
      if (dataProcessors != null)
      {
        for (Iterator<Object> it = dataProcessors.iterator(); it.hasNext(); )
        {
          Object obj = it.next( );
          if (obj instanceof IDataObjectProcessor)
          {
            LOG.debug( "Adding DataProcessor " + obj );
            dataSource.addDataObjectProcessor( (IDataObjectProcessor)obj );
          }
        }
    	
        LOG.debug( "============ Running DataSource ============ " );
        dataSource.run( );
      }
    }
    else
    {
      LOG.error( "Could not create DataSource!" );
    }
  }
	
  private static String getConfigurationDocument( String filename )
  {
    return FileMethods.readFile( filename );
  }

}
