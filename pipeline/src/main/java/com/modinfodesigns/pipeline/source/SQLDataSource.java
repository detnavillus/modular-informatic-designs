package com.modinfodesigns.pipeline.source;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.security.IUserCredentials;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;


public class SQLDataSource implements IDataObjectSource
{
  private String dataSourceFactoryName;
    
  private String sqlCommand;
    
  @Override
  public void run()
  {

  }

  @Override
  public void run( IUserCredentials withUser )
  {

  }

  @Override
  public void addDataObjectProcessor(IDataObjectProcessor dataProcessor)
  {

  }

}
