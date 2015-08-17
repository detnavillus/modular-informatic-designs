package com.modinfodesigns.entity;

import com.modinfodesigns.pipeline.RunPipeline;

import junit.framework.TestCase;

public class TestEntityExtractorPipeline extends TestCase
{
  private static String pipelineConfiguration = "TestEntityExtractorPipeline/EntityExtractorPipelineConfiguration.xml";
  private static String dataSourceName        = "XMLDataSource";
    
  public void testEntityExtractorPipeline( )
  {
    // clear files in resources/TestEntityExtractorPipeline/ClassifiedXMLFiles
      
    String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
    RunPipeline.main( pipelineArgs );
      
    // should be output files in resources/TestEntityExtractorPipeline/ClassifiedXMLFiles
    
  }
}
