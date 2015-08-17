package com.modinfodesigns.ontology.builder;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.pipeline.process.IDataObjectProcessor;
import com.modinfodesigns.pipeline.source.IDataObjectSource;
import com.modinfodesigns.security.IUserCredentials;

public class MockDataObjectSource implements IDataObjectSource
{
  IDataObjectProcessor myProcessor;
    
  public MockDataObjectSource( ) {  }
    
  @Override
  public void run()
  {
    DataList theData = new DataList( );
        
    // Create some DataObjects that simulate a Taxonomy with parentIDs
    DataObject rootObj = new DataObject( "root" );
    rootObj.setID( "0" );
    theData.addDataObject( rootObj );
        
    DataObject childObj = new DataObject( "child 1" );
    childObj.setID( "1" );
    childObj.setProperty( new StringProperty( "parentID", "0" ) );
    theData.addDataObject( childObj );
        
    childObj = new DataObject( "child 2" );
    childObj.setID( "2" );
    childObj.setProperty( new StringProperty( "parentID", "0" ) );
    theData.addDataObject( childObj );
        
    // add some grand kids to child 1
    DataObject grandChildObj = new DataObject( "grandChild 1.1" );
    grandChildObj.setID( "3" );
    grandChildObj.setProperty( new StringProperty( "parentID", "1" ) );
    theData.addDataObject( grandChildObj );
        
    grandChildObj = new DataObject( "grandChild 1.2" );
    grandChildObj.setID( "4" );
    grandChildObj.setProperty( new StringProperty( "parentID", "1" ) );
    theData.addDataObject( grandChildObj );
        
    myProcessor.processDataList( theData );
    myProcessor.processComplete( (IPropertyHolder)null, true );
  }
    
  @Override
  public void run(IUserCredentials withUser)
  {
    run( );
  }
    
  @Override
  public void addDataObjectProcessor(IDataObjectProcessor dataProcessor)
  {
    myProcessor = dataProcessor;
  }
    
}