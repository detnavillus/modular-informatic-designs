package com.modinfodesigns.app.persistence.database;

import javax.sql.DataSource;

import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Creates a DataSource from a JNDI request.
 * 
 * @author Ted Sullivan
 */
public class JNDIDataSourceFactory implements IDataSourceFactory
{
  @Override
  public DataSource getDataSource()
  {
    return null;
  }

}
