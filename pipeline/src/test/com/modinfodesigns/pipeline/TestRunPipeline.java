package com.modinfodesigns.test.pipeline;

import com.modinfodesigns.pipeline.RunPipeline;
import com.modinfodesigns.logging.LoggingManager;

public class TestRunPipeline
{
    private static String pipelineConfiguration = "G:/Projects/Prometheus/Testing/Pipelines/TestFlatFileDataSource/FlatFileDataSourceConfig.xml";
    private static String dataSourceName        = "FlatFileDataSource";
    
	public static void main(String[] args)
	{
		LoggingManager.addDebugClass( "com.modinfodesigns.app.ModInfoObjectFactory" );
		LoggingManager.addDebugClass( "com.modinfodesigns.app.ApplicationManager" );
		
		String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
		RunPipeline.main( pipelineArgs );
	}
}
