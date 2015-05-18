package com.modinfodesigns.property.persistence;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;

import com.modinfodesigns.property.persistence.SQLTemplatePersistenceManager;

import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.DataObjectSchemaManager;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.quantity.Currency;

import com.modinfodesigns.app.persistence.database.DerbyDataSourceFactory;

import junit.framework.TestCase;

public class TestDataListSQLTemplatePersistence extends TestCase
{
  public void testDataListSQLTemplatePersistence( )
  {
    DerbyDataSourceFactory dsf = new DerbyDataSourceFactory( "dataListSQLTemplateTest" );
    SQLTemplatePersistenceManager stpm = new SQLTemplatePersistenceManager( dsf );
    
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "TestSchema_2" );
        
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( "Company" );
    pd.setPropertyType( "StringProperty" );
    dos.addPropertyDescriptor( pd );
        
    pd = new PropertyDescriptor( );
    pd.setName( "Industry" );
    pd.setPropertyType( "StringProperty" );
    dos.addPropertyDescriptor( pd );
        
    pd = new PropertyDescriptor( );
    pd.setName( "Gross Revenue" );
    pd.setPropertyType( "com.modinfodesigns.property.quantity.Currency" );
    dos.addPropertyDescriptor( pd );
    
    DataObjectSchemaManager dosm = new DataObjectSchemaManager( cdsf );
    dosm.saveDataSchema( dos );

    DataList modelList = new DataList( );
    modelList.setName( "Toyota Motor Corp" );
    modelList.setDataObjectSchema( "TestSchema_2" );
    modelList.setProperty( new StringProperty( "Company", "Toyota" ));
    modelList.setProperty( new StringProperty( "Industry", "Automobile Manufacturing" ));
    modelList.setProperty( new Currency( "Gross Revenue", 15000000000.0) );
        
    DataObject chObj = new DataObject( );
    chObj.setName( "Camri" );
    modelList.addDataObject( chObj );
    
    chObj = new DataObject( );
    chObj.setName( "RAV-4" );
    modelList.addDataObject( chObj );
    
    chObj = new DataObject( );
    chObj.setName( "Celica" );
    modelList.addDataObject( chObj );
    
    stpm.save( modelList );

  }

}
