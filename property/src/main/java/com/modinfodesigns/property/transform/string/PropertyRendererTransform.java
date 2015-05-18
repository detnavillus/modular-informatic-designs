package com.modinfodesigns.property.transform.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.transform.BasePropertyTransform;
import com.modinfodesigns.property.transform.IPropertyTransform;
import com.modinfodesigns.property.transform.PropertyTransformException;
import com.modinfodesigns.property.string.StringProperty;

/**
 * Uses an IPropertyRenderer or a format type to transform a property into its String representation
 * in the form of a StringProperty.
 * 
 * @author Ted Sullivan
 */

public class PropertyRendererTransform extends BasePropertyTransform implements IPropertyTransform
{
  private String format;
	
  private IPropertyRenderer propRenderer;
	
  public void setFormat( String format )
  {
    this.format = format;
  }
	
  public void setPropertyRenderer( IPropertyRenderer propRenderer )
  {
    this.propRenderer = propRenderer;
  }
	
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    if (input == null)
    {
      throw new PropertyTransformException( "Input Property is NULL!" );
    }
		
    String propValue = null;
		
    if (propRenderer != null)
    {
      propValue = propRenderer.renderProperty( input );
    }
    else
    {
      propValue = (format != null) ? input.getValue( format ) : input.getValue( );
    }
		
    return new StringProperty( input.getName(), propValue );
  }

}
