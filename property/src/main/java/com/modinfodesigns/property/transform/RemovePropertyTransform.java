package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

import java.util.ArrayList;

/**
 * Removes one or more properties.
 *
 * @author Ted Sullivan
 */

public class RemovePropertyTransform extends BasePropertyTransform implements IPropertyHolderTransform
{
  private ArrayList<String> removeProperties;
	
  public void addRemoveProperty( String removeProperty )
  {
    if (removeProperties == null) removeProperties = new ArrayList<String>( );
    removeProperties.add( removeProperty );
  }
	
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    return input;
  }
    
    
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    if (removeProperties == null) return input;
      
    for (String removeProp : removeProperties )
    {
      input.removeProperty( removeProp );
    }
		
    return input;
  }
    
}
