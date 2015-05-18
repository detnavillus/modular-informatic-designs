package com.modinfodesigns.pipeline.search;

import com.modinfodesigns.property.DataObjectBean;
import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.schema.DataObjectSchema;
import com.modinfodesigns.property.string.StringProperty;

public class FieldMapping extends DataObjectBean
{
  public static final String INPUT_FIELD = "InputField";
    
  private String outputField;
    
    
  public void setInputField( String inputField )
  {
    doSetProperty( new StringProperty( INPUT_FIELD, inputField ));
  }
    
  public String getInputField(  )
  {
    IProperty inputProp = getProperty( INPUT_FIELD );
    return (inputProp != null) ? inputProp.getValue() : null;
  }


  public void mapField( IPropertyHolder propHolder )
  {
    String inputField = getInputField( );
    String outputField = getOutputField( );
    	
    if (inputField != null && outputField != null)
    {
      IProperty prop = propHolder.getProperty( inputField );
      if (prop != null)
      {
        prop.setName( outputField );
      }
    }
    	
  }
    
  private String getOutputField( )
  {
    return null;
  }

  @Override
  public DataObjectSchema createDataObjectSchema( DataObject context )
  {
    return null;
  }
}
