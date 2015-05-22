package com.modinfodesigns.pipeline;

import com.modinfodesigns.pipeline.RunPipeline;

import junit.framework.TestCase;

public class TestNIHMergePipeline extends TestCase
{
  private static String pipelineConfiguration = "G:/Projects/Prometheus/Testing/Pipelines/NIHPipelines/NIHMergeAuthPersonConfig.xml";
  private static String dataSourceName        = "FlatFileDataSource";

  public  void testNIHMergePipeline( )
  {
    String[] pipelineArgs = { pipelineConfiguration, dataSourceName };
    RunPipeline.main( pipelineArgs );
  }

}
