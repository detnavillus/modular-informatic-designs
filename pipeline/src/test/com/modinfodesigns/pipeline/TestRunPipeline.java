package com.modinfodesigns.test.pipeline;

import com.modinfodesigns.pipeline.RunPipeline;

import junit.framework.TestCase;

public class TestRunPipeline extends TestCase
{
  private static String pipelineConfiguration = "G:/Projects/Prometheus/Testing/Pipelines/TestFlatFileDataSource/FlatFileDataSourceConfig.xml";
  private static String dataSourceName        = "FlatFileDataSource";
    
  public void testRunPipeline( )
  {
    String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
    RunPipeline.main( pipelineArgs );
  }
}
