package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.security.IUserCredentials;

/**
 * Base Interface for object that can create and inject DataObjects into a data 
 * processing pipeline.
 * 
 * @author Ted Sullivan
 *
 */
public interface IDataObjectSource extends Runnable
{
  /**
   * Starts the Data Object acquisition.
   */
  public void run( );
    
  /**
   * Starts the Data Object acquisition with security.
   */
  public void run( IUserCredentials withUser );
    
  /**
   * Adds a DataObject Processor that can work with the IDataList objects
   * created by this source.
   *
   * @param dataProcessor
   */
  public void addDataObjectProcessor( IDataObjectProcessor dataProcessor );
}
