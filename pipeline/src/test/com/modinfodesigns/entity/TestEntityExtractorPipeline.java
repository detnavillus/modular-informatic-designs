package com.modinfodesigns.test.entity;

import com.modinfodesigns.logging.LoggingManager;
import com.modinfodesigns.pipeline.RunPipeline;

public class TestEntityExtractorPipeline
{
    private static String pipelineConfiguration = "G:/Projects/Prometheus/Testing/Pipelines/TestEntityExtractorPipeline/EntityExtractorPipelineConfiguration.xml";
    private static String dataSourceName        = "XMLDataSource";
    
	public static void main(String[] args)
	{
		LoggingManager.addDebugClass( "com.modinfodesigns.app.ModInfoObjectFactory" );
		LoggingManager.addDebugClass( "com.modinfodesigns.app.ApplicationManager" );
		
		String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
		RunPipeline.main( pipelineArgs );
	}
}
