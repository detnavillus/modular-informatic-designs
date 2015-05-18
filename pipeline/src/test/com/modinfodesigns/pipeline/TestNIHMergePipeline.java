package com.modinfodesigns.test.pipeline;

import com.modinfodesigns.logging.LoggingManager;
import com.modinfodesigns.pipeline.RunPipeline;

public class TestNIHMergePipeline
{
    private static String pipelineConfiguration = "G:/Projects/Prometheus/Testing/Pipelines/NIHPipelines/NIHMergeAuthPersonConfig.xml";
    private static String dataSourceName        = "FlatFileDataSource";

	public static void main(String[] args)
	{
		LoggingManager.setLogFile( "G:/Projects/Prometheus/Testing/Pipelines/NIHPipelines/Test Runs/test_20.txt" );
		LoggingManager.setErrorFile( "G:/Projects/Prometheus/Testing/Pipelines/NIHPipelines/Test Runs/errors_20.txt" );

		LoggingManager.addDebugClass( "com.modinfodesigns.pipeline.source.FlatFileDataSource" );
		
		LoggingManager.addDebugClass( "com.modinfodesigns.property.transform.LookupTransform" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.pipeline.process.DataListFilter" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.pipeline.process.FlatFileDataProcessor" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.entity.PersonNameMatcher" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.property.transform.ConditionalPropertyTransform" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.property.compare.PropertyValueMatcher" );
		// LoggingManager.addDebugClass( "com.modinfodesigns.property.compare.StringPropertyMatcher" );
		
		LoggingManager.addDebugClass( "com.modinfodesigns.search.DedupingFinder" );
		LoggingManager.addDebugClass( "com.modinfodesigns.search.DedupingFinder.ObjectCache" );
		LoggingManager.addDebugClass( "com.modinfodesigns.pipeline.process.NestedDataListExtractor" );
		
		String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
		RunPipeline.main( pipelineArgs );
	}

}
