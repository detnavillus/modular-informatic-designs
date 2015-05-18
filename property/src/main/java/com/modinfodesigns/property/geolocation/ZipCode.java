package com.modinfodesigns.property.geolocation;

import java.util.List;

import com.modinfodesigns.property.CompositeProperty;
import com.modinfodesigns.property.IProperty;

import com.modinfodesigns.property.string.RegularExpressionProperty;

import java.util.ArrayList;

/**
 * Represents a Zip or US Postal Code
 * 
 * An example of a structured String property. Consists of 2 RegularExpression properties
 * length 5 and length 4 separated by a '-'
 * 
 * @author Ted Sullivan
 *
 */

public class ZipCode extends CompositeProperty 
{

  ArrayList<IProperty> components = null;
	
  public ZipCode( )
  {
		
  }
	
  @Override
  public List<IProperty> getComponentProperties(  )
  {
    init( );
    return components;
  }
    
  @Override
  public List<String> getDelimiters( )
  {
    ArrayList<String> delims = new ArrayList<String>( );
    delims.add( "-" );
    return delims;
  }

    
  private void init( )
  {
    if (components != null) return;
    	
    components = new ArrayList<IProperty>( );
    	
    RegularExpressionProperty zipCode = new RegularExpressionProperty( );
    zipCode.setRegularExpression( "\\d\\d\\d\\d\\d" );
    zipCode.setMaximumLength( 5 );
    	
    RegularExpressionProperty extension = new RegularExpressionProperty( );
    extension.setRegularExpression( "\\d\\d\\d\\d" );
    extension.setMaximumLength( 4 );
    	
    components.add( zipCode );
    components.add( extension );
  }
}
