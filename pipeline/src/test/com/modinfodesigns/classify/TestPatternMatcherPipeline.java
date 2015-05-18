package com.modinfodesigns.classify;

import com.modinfodesigns.pipeline.RunPipeline;

import junit.framework.TestCase;

public class TestPatternMatcherPipeline extends TestCase
{
  private static String pipelineConfiguration = "PatternMatcherConfiguration.xml";
  private static String dataSourceName        = "XMLDataSource";
	   
  public void testPatternMatcherPipeline( )
  {
    String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
    RunPipeline.main( pipelineArgs );
  }
}

