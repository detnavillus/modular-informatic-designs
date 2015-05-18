package com.modinfodesigns.app.persistence.database;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set of useful Utility methods for SQL Databases.
 * 
 * @author Ted Sullivan
 */
public class SQLMethods
{
  private transient static final Logger LOG = LoggerFactory.getLogger( SQLMethods.class );

  private DataSource dataSource;
    
  private Statement  statement;
  private Connection dbConn;
    
  // flags to choose Statement creation options

    
  public SQLMethods( DataSource dataSource )
  {
    this.dataSource = dataSource;
  }
	
  public SQLMethods( IDataSourceFactory dataSourceFactory )
  {
    this.dataSource = dataSourceFactory.getDataSource( );
  }
		
  /**
   * Executes a SQL Query against the Database.
   *
   * @param sqlQuery			The query to execute
   *
   * @param closeConnection	if true - close the DB connection after retrieving
   * 							the ResultSet
   *
   * @return					JDBC ResultSet
   */
  public ResultSet executeSQLQuery( String sqlQuery )
  {
    LOG.debug( "executeSQLQuery: '" + sqlQuery + "'" );
    	
    if (sqlQuery == null || sqlQuery.trim().length() == 0)
    {
      return null;
    }

    try
    {
      if (statement == null)
      {
        openDatabaseConnection();
          
        if (statement == null)
        {
          return null;
        }
      }

      return statement.executeQuery( sqlQuery );
    }
    catch( SQLException e )
    {
      closeDatabaseConnection();
      LOG.error( "Got SQLException: " + e );
      return null;
    }
  }
    
    
  /**
   * Executes a SQL update statement (Update or Insert)
   *
   * @param sqlQuery
   */
  public void executeSQLUpdate( String sqlUpdate )
  {
    executeSQLUpdate( sqlUpdate, true );
  }
    
  public void executeSQLUpdate( String sqlUpdate, boolean closeConnection )
  {
    LOG.debug( "executeSQLUpdate: " + sqlUpdate );
    	
    try
    {
      if (sqlUpdate != null && sqlUpdate.trim().length() > 0)
      {
        openDatabaseConnection();
        if (statement == null)
        {
          LOG.debug( "No connection! returning" );
          return;
        }

        statement.executeUpdate( sqlUpdate );
          
        if (closeConnection)
        {
          LOG.debug( "Committing and closing." );
          commit( );
          closeDatabaseConnection();
        }
      }
    }
    catch ( SQLException e )
    {
      LOG.error( "Got SQLException: " + e );
        	LOG.debug( "executeSQLUpdate got SQLException: " + e );
            closeDatabaseConnection();
        }
    }
    
    /**
     * Executes a set of SQL Update statements.
     * 
     * @param sqlUpdates
     */
    public void executeSQLUpdates( List<String> sqlUpdates )
    {
    	executeSQLUpdates( sqlUpdates, true );
    }
    
    public void executeSQLUpdates( List<String> sqlUpdates, boolean closeConnection )
    {
    	LOG.debug( "executeSQLUpdates( )..." );
    	
        try 
        {
            if (sqlUpdates != null && sqlUpdates.size( ) > 0)
            {
                openDatabaseConnection();
                for (int i = 0; i < sqlUpdates.size( ); i++)
                {
                    statement.executeUpdate( sqlUpdates.get( i ));
                }
                if (closeConnection)
                {
                    commit( );
                    closeDatabaseConnection();
                }
            }
        } 
        catch ( SQLException e ) 
        {
        	LOG.error( "Got SQLException: " + e );
            closeDatabaseConnection();
        }
    }
    
	private void openDatabaseConnection( )
	{
		LOG.debug( "openDatabaseConnection " );
		
		try
		{
		    this.dbConn = dataSource.getConnection( );
		    if (dbConn != null)
		    {
		    	LOG.debug( "Autocommit = " + dbConn.getAutoCommit( ) );
		        this.statement = dbConn.createStatement( );
		    }
		    else
		    {
		    	LOG.error( "Could not get DB Connection!" );
		    }
		}
		catch ( SQLException sqle )
		{
			LOG.error( "Got SQLException: " + sqle );
			sqle.printStackTrace( System.out );
			this.dbConn = null;
		}
	}
    
    public void closeDatabaseConnection(  )
    {
    	LOG.debug( "closeDatabaseConnection" );
    	
        try
        {
            if (statement != null)
            {
                statement.close( );
                statement = null;
            }

            if (this.dbConn != null)
            {
            	if (!this.dbConn.isClosed() )
            	{
                    commit( );
                    this.dbConn.close( );
            	}
            	this.dbConn = null;
            }
        }
        catch( Exception e )
        {
        	LOG.error( "Got Exception " + e );
        }
    }

    public void finalize() throws SQLException
    {
    	LOG.debug( "finalize" );
    	
        try
        {
            closeDatabaseConnection( );
        }
        catch (Exception e)
        {
            LOG.error( "Got Exception: " + e );
        }
    }
    
    private void commit( ) throws SQLException
    {
    	if (dbConn != null) dbConn.commit( );
    }
    
    // ===================================================================
    public String getCLOBColumnQuery( String columnName )
    {
    	return "";
    }

}
