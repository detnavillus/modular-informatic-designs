package com.modinfodesigns.app.persistence.database;

import javax.sql.DataSource;

public interface IDataSourceFactory
{
  public DataSource getDataSource(  );
	
  // read CLOB
  // write CLOB method	
}
