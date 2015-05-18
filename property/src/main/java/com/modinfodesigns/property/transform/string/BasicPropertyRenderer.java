package com.modinfodesigns.property.transform.string;

import com.modinfodesigns.property.IProperty;

public class BasicPropertyRenderer implements IPropertyRenderer
{
  private String format;
    
  public void setFormat( String format )
  {
    this.format = format;
  }
    
  @Override
  public String renderProperty(IProperty property)
  {
    return (format != null) ? property.getValue( format ) : property.getValue( );
  }

}
