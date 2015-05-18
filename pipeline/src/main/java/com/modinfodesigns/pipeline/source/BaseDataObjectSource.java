package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.property.IDataList;
import com.modinfodesigns.property.IPropertyHolder;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseDataObjectSource implements IDataObjectSource 
{
  private transient static final Logger LOG = LoggerFactory.getLogger( BaseDataObjectSource.class );

  private String name;
	
  private ArrayList<IDataObjectProcessor> outputProcessors = new ArrayList<IDataObjectProcessor>( );
	
  public void setName( String name )
  {
    this.name = name;
  }
    
  public String getName( )
  {
    return this.name;
  }
    
  protected boolean doProcessDataList( IDataList data )
  {
    LOG.debug( "doProcessDataList( ) " );
    	
    for (int i = 0; i < outputProcessors.size( ); i++)
    {
      IDataObjectProcessor dataProc = outputProcessors.get( i );
      LOG.debug( "Processing with: " + dataProc );
      data = dataProc.processDataList( data );
      if (data == null) return false;
    }
    	
    return true;
  }
    
    
  protected void sendProcessComplete( IPropertyHolder result, boolean status )
  {
    for (int i = 0; i < outputProcessors.size( ); i++)
    {
      IDataObjectProcessor dataProc = outputProcessors.get( i );
      dataProc.processComplete( result, status );
    }
  }

  @Override
  public void addDataObjectProcessor(IDataObjectProcessor dataProcessor)
  {
    outputProcessors.add( dataProcessor );
  }

}
