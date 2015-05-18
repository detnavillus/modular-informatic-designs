package com.modinfodesigns.property.persistence;

import com.modinfodesigns.property.schema.DataObjectSchemaManager;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.schema.PropertyDescriptor;

import com.modinfodesigns.app.persistence.database.DerbyDataSourceFactory;

import java.util.List;

import junit.framework.TestCase;

public class TestDataObjectSchemaManager extends TestCase
{
  public void testDataObjectSchemaManager( )
  {
    DerbyDataSourceFactory dsf = new DerbyDataSourceFactory( "dataObjectSchemaTest");
       
    DataObjectSchemaManager dosMan = new DataObjectSchemaManager( dsf );
        
    // ========================================================
    // Test 1: Create a DataObjectSchema
    // ========================================================
    DataObjectSchema dos = new DataObjectSchema( );
    dos.setName( "TestSchema1" );
		
    PropertyDescriptor pd = new PropertyDescriptor( );
    pd.setName( "Title" );
    pd.setPropertyType( "String" );
    pd.setDefaultValue( "" );
    pd.setRequired( true );
    dos.addPropertyDescriptor( pd );
		
    pd = new PropertyDescriptor( );
    pd.setName( "URL" );
    pd.setPropertyType( "String" );
    pd.setDefaultValue( "" );
    pd.setRequired( true );
    dos.addPropertyDescriptor( pd );
		
    System.out.println( dos.getValue( ) );
   
    dosMan.saveDataSchema( dos );
        
    DataObjectSchema retDos = dosMan.getDataObjectSchema( "TestSchema1" );
        
    if (retDos != null)
    {
      System.out.println( "Retrieved: " + retDos.getValue( ) );
    }
        
    // ==============================================================
    // Test 2: Add a Property to TestSchema1 - this should create
    // TestSchema1 version 1
    // ==============================================================
    pd = new PropertyDescriptor( );
    pd.setName( "Year" );
    pd.setPropertyType( "Integer" );
    pd.setDefaultValue( "" );
    pd.setRequired( false );
    dos.addPropertyDescriptor( pd );
		
    dosMan.saveDataSchema( dos );
    
    DataObjectSchema retVer1 = dosMan.getDataObjectSchema( "TestSchema1" );
    if (retVer1 != null)
    {
      System.out.println( "Retrieved after add prop: " + retVer1.getValue( ) );
    }
        
    DataObjectSchema dos2 = new DataObjectSchema( );
    dos2.setName( "TestSchema2" );
    
    pd = new PropertyDescriptor( );
    pd.setName( "Trade Name" );
    pd.setPropertyType( "String" );
    dos2.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( "Active Ingredient" );
    pd.setPropertyType( "String" );
    dos2.addPropertyDescriptor( pd );
    
    pd = new PropertyDescriptor( );
    pd.setName( "Indication" );
    pd.setPropertyType( "String" );
    pd.setMultiValue( true );
    dos2.addPropertyDescriptor( pd );
    
    dosMan.saveDataSchema( dos2 );
    
    List<String> dataSchemas = dosMan.getDataObjectSchemas( true );
    if (dataSchemas != null)
    {
      for (int i = 0; i < dataSchemas.size( ); i++)
      {
        System.out.println( "Schema: " + dataSchemas.get( i ) );
      }
    }
        
    DataObjectSchema dos3 = null;
    dosMan.saveDataSchema( dos3 );
  }

}
