package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.IFunctionProperty;

/**
 * Property Holder transform that uses an IFunctionProperty to do its work.
 * 
 * @author Tedm Sullivan
 */
public class FunctionPropertyTransform implements IPropertyHolderTransform
{
  private IFunctionProperty functionProperty;
	
  public void setFunctionProperty( IFunctionProperty functionProperty )
  {
    this.functionProperty = functionProperty;
  }
	

  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    return input;
  }

  @Override
  public void startTransform( IProperty input, IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {

  }

  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    functionProperty.setPropertyHolder( input );
    IProperty outputProp = functionProperty.execute( );
    if (outputProp != null)
    {
      input.addProperty( outputProp );
    }
        
    return input;
  }

}
