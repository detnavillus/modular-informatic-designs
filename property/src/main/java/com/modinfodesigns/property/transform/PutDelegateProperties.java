package com.modinfodesigns.property.transform;

import com.modinfodesigns.property.DataObject;
import com.modinfodesigns.property.DataObjectDelegate;
import com.modinfodesigns.property.IProperty;
import com.modinfodesigns.property.IPropertyHolder;
import com.modinfodesigns.property.string.StringProperty;

public class PutDelegateProperties implements IPropertyHolderTransform
{
  private String delegateField;
  private String sourceField;
  private String targetField;
    
  private boolean multiValue = true;
    
  public void setDelegateField( String delegateField )
  {
    this.delegateField = delegateField;
  }
    
  public void setSourceField( String sourceField )
  {
    this.sourceField = sourceField;
  }
    
  public void setTargetField( String targetField )
  {
    this.targetField = targetField;
  }
    
  public void setMultiValue( boolean multiValue )
  {
    this.multiValue = multiValue;
  }
    
  @Override
  public IProperty transform( IProperty input ) throws PropertyTransformException
  {
    return input;
  }
    
  @Override
  public IPropertyHolder transformPropertyHolder( IPropertyHolder input )
                                                  throws PropertyTransformException
  {
    IProperty targetProp = null;
      
    if (sourceField.equals( "name" ))
    {
      targetProp = new StringProperty( targetField, input.getName( ) );
    }
    else
    {
      IProperty inputProp = input.getProperty( sourceField );
      targetProp = inputProp.copy( );
      targetProp.setName( targetField );
    }
      
    IProperty delegateProp = input.getProperty( delegateField );
    if (!(delegateProp instanceof DataObject))
    {
      throw new PropertyTransformException( "Input property is not a DataObjectDelegate Instance!" );
    }
      
    DataObject delegate = (DataObject)delegateProp;
      
    if (multiValue)
    {
      delegate.addProperty( targetProp );
    }
    else
    {
      delegate.setProperty( targetProp );
    }
      
    return input;
  }
    
  @Override
  public void startTransform( IProperty input,
                              IPropertyTransformListener transformListener)
                              throws PropertyTransformException
  {
        
  }
    
}