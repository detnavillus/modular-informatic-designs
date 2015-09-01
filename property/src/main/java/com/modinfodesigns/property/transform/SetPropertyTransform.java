package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

/**
 * Sets or Adds a Property to a PropertyHolder.
 * 
 * @author Ted Sullivan
 */

public class SetPropertyTransform extends BasePropertyTransform implements IPropertyHolderTransform
{
  private IProperty newProperty;
	
  private boolean isAdd = false;
	
  public void setProperty( IProperty property )
  {
    System.out.println( "setProperty " + property + " " + property.getValue( ) );
    this.newProperty = property;
  }
	
  public void setIsAdd( boolean isAdd )
  {
    this.isAdd = isAdd;
  }
	
  public void setIsAdd( String isAdd )
  {
    this.isAdd = Boolean.parseBoolean( isAdd );
  }
	

  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    return input;
  }


  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input ) throws PropertyTransformException
  {
    System.out.println( "SetPropertyTransform.transformPropertyHolder..." );
    if (newProperty != null)
    {
      if (isAdd)
      {
        input.addProperty( newProperty.copy() );
      }
      else
      {
        System.out.println( "setProperty: " + newProperty.getName( ) + " = " + newProperty.getValue( ) );
        input.setProperty( newProperty.copy() );
      }
    }
		
    return input;
  }

}
