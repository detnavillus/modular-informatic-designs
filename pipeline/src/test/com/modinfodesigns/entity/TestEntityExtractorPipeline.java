package com.modinfodesigns.entity;

import com.modinfodesigns.pipeline.RunPipeline;

import junit.framework.TestCase;

public class TestEntityExtractorPipeline extends TestCase
{
  private static String pipelineConfiguration = "G:/Projects/Prometheus/Testing/Pipelines/TestEntityExtractorPipeline/EntityExtractorPipelineConfiguration.xml";
  private static String dataSourceName        = "XMLDataSource";
    
  public void testEntityExtractorPipeline( )
  {
    String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
    RunPipeline.main( pipelineArgs );
  }
}
