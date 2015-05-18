package com.modinfodesigns.test.property.persistence;

import com.modinfodesigns.property.persistence.SQLTemplatePersistenceManager;
import com.modinfodesigns.app.persistence.database.DerbyDataSourceFactory;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchemaManager;
import com.modinfodesigns.property.schema.PropertyDescriptor;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.quantity.IntegerProperty;

import junit.framework.TestCase;

public class TestSQLTemplatePersistenceManager extends TestCase
{   

  public void testSQLTemplatePersistenceManager( )
  {
    SQLTemplatePersistenceManager stpm = new SQLTemplatePersistenceManager( );
    DerbyDataSourceFactory dsf = new DerbyDataSourceFactory( "SQLTemplatePersistenceManagerTest" );
    stpm.setDataSourceFactory( dsf );
    
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "TestSchema_1" );
        
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( "Title" );
    pd.setPropertyType( "StringProperty" );
    dos.addPropertyDescriptor( pd );
        
    pd = new PropertyDescriptor( );
    pd.setName( "URL" );
    pd.setPropertyType( "StringProperty" );
    dos.addPropertyDescriptor( pd );
        
    pd = new PropertyDescriptor( );
    pd.setName( "Year" );
    pd.setPropertyType( "Integer" );
    dos.addPropertyDescriptor( pd );
        
    DataObjectSchemaManager dosm = new DataObjectSchemaManager( cdsf );
    dosm.saveDataSchema( dos );

    DataObject dObj = new DataObject( );
    dObj.setDataObjectSchema( "TestSchema_1" );
        
    dObj.setName( "Great Title" );
    dObj.setProperty( new StringProperty( "Title", "Its a Wonderful Life" ) );
    dObj.setProperty( new StringProperty( "URL", "http://www.moviesAreUs.org" ) );
    dObj.setProperty( new IntegerProperty( "Year", 1975 ) );
    dObj.setProperty( new StringProperty( "adhocProp", "you never know" ));
        
    stpm.save( dObj );
        
    dObj.setProperty( new StringProperty( "Title", "Its a Freakin Wonderful Life" ) );
        
    stpm.save( dObj );
        
    DataObject dObj_copy = stpm.read( "Great Title", "TestSchema_1" );
        
    System.out.println( dObj_copy.getValue( IProperty.JSON_FORMAT ));
        		
  }

}
