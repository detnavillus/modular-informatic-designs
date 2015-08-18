package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.string.StringProperty;

import com.modinfodesigns.property.transform.GetDelegateProperties;
import com.modinfodesigns.property.transform.PropertyTransformException;

import junit.framework.TestCase;

public class TestGetDelegateProperties extends TestCase
{
  public void testGetDelegateName( )
  {
    DataObject holderObj = new DataObject( );
    holderObj.setName( "Here" );
      
    DataObject targetObj = new DataObject( );
    targetObj.setName( "There" );
    targetObj.addProperty( new StringProperty( "targetProp1", "hello from there" ) );
      
    DataObjectDelegate dobjd = new DataObjectDelegate( targetObj );
      
    dobjd.setName( "Conduit" );
    holderObj.addProperty( dobjd );
      
    assertEquals( holderObj.getValue( IProperty.JSON_FORMAT ), "{\"Conduit\":{\"targetProp1\":\"hello from there\"}}" );

    GetDelegateProperties gdp = new GetDelegateProperties( );
    gdp.setDelegateField( "Conduit" );
    gdp.setSourceField( "name" );
    gdp.setTargetField( "ThereNameIs" );
      
    try
    {
      gdp.transformPropertyHolder( holderObj );
    }
    catch ( PropertyTransformException pte )
    {
      assertTrue( false );
    }
      
    assertEquals( holderObj.getValue( IProperty.JSON_FORMAT ), "{\"Conduit\":{\"targetProp1\":\"hello from there\"},\"ThereNameIs\":\"There\"}" );
      
  }
    
  public void testGetDelegateProperties( )
  {
    DataObject holderObj = new DataObject( );
    holderObj.setName( "Here" );
    
    DataObject targetObj = new DataObject( );
    targetObj.setName( "There" );
    targetObj.addProperty( new StringProperty( "targetProp1", "hello from there" ) );
        
    DataObjectDelegate dobjd = new DataObjectDelegate( targetObj );
        
    dobjd.setName( "Conduit" );
    holderObj.addProperty( dobjd );
        
    assertEquals( holderObj.getValue( IProperty.JSON_FORMAT ), "{\"Conduit\":{\"targetProp1\":\"hello from there\"}}" );
        
    GetDelegateProperties gdp = new GetDelegateProperties( );
    gdp.setDelegateField( "Conduit" );
    gdp.setSourceField( "targetProp1" );
    gdp.setTargetField( "delegateProp" );
        
    try
    {
      gdp.transformPropertyHolder( holderObj );
    }
    catch ( PropertyTransformException pte )
    {
      assertTrue( false );
    }
        
    assertEquals( holderObj.getValue( IProperty.JSON_FORMAT ), "{\"Conduit\":{\"targetProp1\":\"hello from there\"},\"delegateProp\":\"hello from there\"}" );
  }
    
  public void testGetPropertyList( )
  {
    DataObject holder = new DataObject( );
    holder.setName( "US" );
      
    DataObject target = new DataObject( );
    target.setName( "THEM" );
    target.addProperty( new StringProperty( "stooges", "Larry" ));
    target.addProperty( new StringProperty( "stooges", "Moe" ));
    target.addProperty( new StringProperty( "stooges", "Curley" ));
    target.addProperty( new StringProperty( "stooges", "Shemp" ));
      
    DataObjectDelegate dobjd = new DataObjectDelegate( target );
    dobjd.setName( "Comics" );
    holder.addProperty( dobjd );
      
    assertEquals( holder.getValue( IProperty.JSON_FORMAT ), "{\"Comics\":{\"stooges\":\"[\"Larry\",\"Moe\",\"Curley\",\"Shemp\"]\"}}" );
    GetDelegateProperties gdp = new GetDelegateProperties( );
    gdp.setDelegateField( "Comics" );
    gdp.setSourceField( "stooges" );
    gdp.setTargetField( "the_stooges" );
    gdp.setMultiValue( true );
      
    try
    {
      gdp.transformPropertyHolder( holder );
    }
    catch ( PropertyTransformException pte )
    {
      assertTrue( false );
    }

    holder.removeProperty( "Comics" );
    
    assertEquals( holder.getValue( IProperty.JSON_FORMAT ), "{\"the_stooges\":\"[\"Larry\",\"Moe\",\"Curley\",\"Shemp\"]\"}");
  }
    
  public void testGetPropertyListWithExcludes( )
  {
    DataObject holder = new DataObject( );
    holder.setName( "US" );
      
    DataObject target = new DataObject( );
    target.setName( "THEM" );
    target.addProperty( new StringProperty( "stooges", "Larry" ));
    target.addProperty( new StringProperty( "stooges", "Moe" ));
    target.addProperty( new StringProperty( "stooges", "Curley" ));
    target.addProperty( new StringProperty( "stooges", "Shemp" ));
      
    //System.out.println( target.getValue( IProperty.JSON_FORMAT ));
      
    DataObjectDelegate dobjd = new DataObjectDelegate( target );
    dobjd.setName( "Comics" );
    holder.addProperty( dobjd );
    
    //System.out.println( holder.getValue( IProperty.JSON_FORMAT ));
      
    GetDelegateProperties gdp = new GetDelegateProperties( );
    gdp.setDelegateField( "Comics" );
    gdp.setSourceField( "stooges" );
    gdp.setTargetField( "the_stooges" );
    gdp.setMultiValue( true );
    gdp.addExcudedValue( "Shemp" );
      
    try
    {
      gdp.transformPropertyHolder( holder );
    }
    catch ( PropertyTransformException pte )
    {
      assertTrue( false );
    }
      
    holder.removeProperty( "Comics" );
    assertEquals( holder.getValue( IProperty.JSON_FORMAT ), "{\"the_stooges\":\"[\"Larry\",\"Moe\",\"Curley\"]\"}" );
  }
}