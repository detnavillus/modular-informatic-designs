package com.modinfodesigns.property.transform.string;

import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;

public class BasicPropertyHolderRenderer implements IPropertyHolderRenderer
{
  private String format;
    
  public void setFormat( String format )
  {
    this.format = format;
  }
    
  @Override
  public String renderProperty(IProperty property)
  {
    if (property == null) return null;
		
    return (format != null) ? property.getValue( format ) : property.getValue( );
  }

  @Override
  public String renderPropertyHolder(IPropertyHolder propHolder)
  {
    if (propHolder == null) return null;
    return (format != null) ? propHolder.getValue( format ) : propHolder.getValue( );
  }

}
