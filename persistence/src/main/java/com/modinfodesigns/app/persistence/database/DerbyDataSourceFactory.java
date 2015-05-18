package com.modinfodesigns.app.persistence.database;

import org.apache.derby.jdbc.EmbeddedDataSource;

import javax.sql.DataSource;

public class DerbyDataSourceFactory implements IDataSourceFactory
{
  private String databaseName = "DerbyDB";
	
  public void setDatabaseName( String databaseName )
  {
    this.databaseName = databaseName;
  }
	
  public DerbyDataSourceFactory( ) {  }
	
  public DerbyDataSourceFactory( String databaseName )
  {
    this.databaseName = databaseName;
  }

  @Override
  public DataSource getDataSource()
  {
    EmbeddedDataSource eds = new EmbeddedDataSource( );
    eds.setDatabaseName( databaseName );
    eds.setCreateDatabase( "create" );
    return eds;
  }
}
