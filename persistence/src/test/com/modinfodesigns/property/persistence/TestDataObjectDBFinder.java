package com.modinfodesigns.property.persistence;

import com.modinfodesigns.app.persistence.database.DerbyDataSourceFactory;
import com.modinfodesigns.property.persistence.DataObjectDBFinder;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.search.Query;
import com.modinfodesigns.search.QueryField;
import com.modinfodesigns.search.IResultList;
import com.modinfodesigns.search.FinderException;

import junit.framework.TestCase;

public class TestDataObjectDBFinder extends TestCase
{
  public void testDataObjectDBFinder( ) throws FinderException
  {
    DerbyDataSourceFactory dsf = new DerbyDataSourceFactory( "dataObjectDBFinderTest" );
    DataObjectDBFinder objectFinder = new DataObjectDBFinder( dsf, "Person" );
        
    Query query = new Query( );
    QueryField qf = new QueryField( );
    qf.setFieldName( "object_name" );
    qf.setFieldValue( "Neil Armstrong" );
    query.addQueryField( qf );
        
    IResultList results = objectFinder.executeQuery( query );
    if (results != null && results.size() > 0)
    {
      DataObject dobj = results.getData( ).next( );
      System.out.println( dobj.getValue( ) );
        		
      IProperty prop = dobj.getProperty( "Age" );
      if (prop != null)
      {
        System.out.println( "Age = " + prop.getValue( "years days" ) );
      }
    }
    else
    {
      System.out.println( "No results!!!" );
    }
  }

  public void testDataObjectDBPerson( ) throws FinderException
  {
    DerbyDataSourceFactory dsf = new DerbyDataSourceFactory( "dataObjectPersonTest" );
    DataObjectDBFinder objectFinder = new DataObjectDBFinder( dsf, "Person" );

    Query query = new Query( );
    QueryField qf = new QueryField( );
    qf.setFieldName( "object_name" );
    qf.setFieldValue( "Fred Rated" );
    query.addQueryField( qf );

    IResultList results = objectFinder.executeQuery( query );
    if (results != null && results.size( ) > 0)
    {
      DataObject dobj = results.getData( ).next( );
      System.out.println( dobj.getValue( ) );

      IProperty prop = dobj.getProperty( "Age" );
      if (prop != null)
      {
        System.out.println( "Age = " + prop.getValue( "years days" ) );
      }

      IProperty empDur = dobj.getProperty( "EmploymentDuration" );
      if (empDur != null)
      {
        System.out.println( "Employment duration " + empDur.getValue( "years days" ));
      }
    }
    else
    {
      System.out.println( "No results!!!" );
    }
  }
}
