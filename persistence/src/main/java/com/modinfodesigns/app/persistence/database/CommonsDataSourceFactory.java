package com.modinfodesigns.app.persistence.database;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

public class CommonsDataSourceFactory implements IDataSourceFactory 
{
  private String driverClass;
  private String username;
  private String password;
  private String url;
    
  private int maxActive = 20;
  private int maxIdle = 5;
  private int initialSize = 10;
    
  private boolean autoCommit = false;
    
  public void setDriverClass( String driverClass )
  {
    this.driverClass = driverClass;
  }
    
  public void setUsername( String username )
  {
    this.username = username;
  }
    
  public void setPassword( String password )
  {
    this.password = password;
  }
    
  public void setUrl( String url )
  {
    this.url = url;
  }
    
  public void setAutoCommit( String autoCommit ) {
    this.autoCommit = Boolean.parseBoolean( autoCommit );
  }
    
    
  @Override
  public DataSource getDataSource( )
  {
    BasicDataSource dataSource = new BasicDataSource();

    dataSource.setDriverClassName( driverClass );
    dataSource.setUsername( username );
    dataSource.setPassword( password );
    dataSource.setUrl( url );
		
    dataSource.setMaxActive( maxActive );
    dataSource.setMaxIdle( maxIdle );
    dataSource.setInitialSize( initialSize );
      
    dataSource.setDefaultAutoCommit( autoCommit );
    
    return dataSource;
  }
}
