package com.modinfodesigns.property;

import com.modinfodesigns.property.DataList;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.string.StringProperty;
import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.quantity.IntegerProperty;

import junit.framework.TestCase;

public class TestDataObjectPath extends TestCase
{
  public void testDataObjectPath( )
  {
    DataList dList = new DataList( );
    dList.setName( "root" );
		
    DataList rootObj = new DataList( );
    rootObj.setName( "children" );
    dList.addDataObject( rootObj );
		
    DataObject childObject = new DataObject( );
    childObject.setName( "child1" );
    rootObj.addDataObject( childObject );
		
    DataObject grandChild = new DataObject( );
    grandChild.setName( "grandkid1" );
    childObject.addProperty( grandChild );
		
    IntegerProperty ageProp = new IntegerProperty( "age", 45 );
    grandChild.addProperty( ageProp );
		
    IProperty pathProp = dList.getProperty( "[0]/children[0]/grandkid1/age" );
    assertEquals( pathProp.getValue( ), "45" );
		
    pathProp = dList.getProperty( "root[0]/children[0]/grandkid1/age" );
    assertEquals( pathProp.getValue( ), "45" );
    
    pathProp = dList.getProperty( "root[0]/[0]/grandkid1/age" );
    assertEquals( pathProp.getValue( ), "45" );
		
    pathProp = dList.getProperty( "root[0]/children[0]/grandkid1" );
    assertEquals( pathProp.getValue( ), "{\"age\":\"45\"}" );
		
    pathProp = dList.getProperty( "root[0]" );
    assertEquals( pathProp.getValue( ), "{\"objects\":[{\"grandkid1\":{\"age\":\"45\"}}]}" );
  }
    
  public void testSetByPathAlone( ) throws PropertyValidationException
  {
      DataObject dobj = new DataObject( );
      
      StringProperty pathtoSomeProp = new StringProperty( "pathto/some/prop", "some text" );
      dobj.setProperty( pathtoSomeProp );
      
      assertEquals( dobj.getValue( ), "{\"pathto/some/prop\":\"some text\"}" );
		
      IProperty getIt = dobj.getProperty( "pathto/some/prop" );
      assertEquals( getIt.getValue( ), "some text" );
      
      DataObject pathObj = new DataObject();
      DataObject pathToOb = new DataObject( "pathto" );
      pathObj.addProperty( pathToOb );
      DataObject someOb = new DataObject( "some" );
      pathToOb.addProperty( someOb );
      StringProperty propOb = new StringProperty( "prop", "" );
      someOb.addProperty( propOb );
      
      pathObj.setValue( "some text", "pathto/some/prop" );
      assertEquals( pathObj.getValue( ), "{\"pathto\":{\"some\":{\"prop\":\"some text\"}}}" );
      
      getIt = pathObj.getProperty( "pathto/some/prop" );
      assertEquals( getIt.getValue( ), "some text" );
    }

}
